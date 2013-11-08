package com.ranintotree.ride.activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import com.ranintotree.ride.R;
import com.ranintotree.ride.util.VehicleData;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class StatusActivity extends Activity implements OnClickListener{
	private static final String TAG = "StatusActivity";
	
	// VehicleData structures
	private ArrayList<VehicleData> vehicles; 
	
	// UI elements
	private EditText responseText;
	private Button refreshBtn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.status);
		
		// Create the vehicle storage list
		vehicles = new ArrayList<VehicleData>();
		
		// Find the views
		responseText = (EditText) findViewById(R.id.responseText);
		refreshBtn = (Button) findViewById(R.id.refreshButton);
		refreshBtn.setOnClickListener(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	class PostData extends AsyncTask<String, Integer, StringBuilder> {
		@Override
		protected StringBuilder doInBackground(String... arg0) {
			HttpResponse response = postData();
			StringBuilder str = null;
			try {
				str = inputStreamToString(response.getEntity().getContent());
			} catch (IOException e) {
				Log.e(TAG, e.toString());
			}
			return str;
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}
		
		@Override
		protected void onPostExecute(StringBuilder result) {
			super.onPostExecute(result);
			Toast.makeText(getApplicationContext(), R.string.toastMsg, Toast.LENGTH_LONG).show();

			responseText.setText("");
			vehicles.clear();
			parseData(result);
			// Display the data onto the textView (replace later)
			for (Iterator<VehicleData> i = vehicles.iterator(); i.hasNext(); ) {
				VehicleData data = i.next();
				responseText.setText("Route: " + data.getRouteAbb() + 
						"\nBus Num: " + data.getVehicleNum() +
						"\nNext Stop: " + data.getNextStop() +
						"\nArrival Time: " + data.getArrivalTime() +
						"\nStatus: " + data.getStatus());
			}
		}
	}
	
	@Override
	public void onClick(View v) {
		new PostData().execute("");
	}
	
	// parse the response for the data
	public void parseData(StringBuilder input) {
		int routeAbb = input.indexOf("Rou");
		if (routeAbb >= 0)
		{
			String strDir 			= null;
	        String strNextStop 		= null;
	        String strArrival 		= null;
	        String strStatus 		= null;
	        String strRouteAbb 		= null;
            String strVehicleNum 	= null;
            String strBearing 		= null;
            String strLat 			= null;
            String strLog 			= null;
            
	        int index = input.indexOf("Direction");
	        String substr;
	        int offset = 0;
			int indexRouteAbb = 0;
	        while ((indexRouteAbb = input.indexOf("RouteA", indexRouteAbb + 30)) != -1) {
	        	// Read in the parts of the input that we want
	            strRouteAbb = input.substring(indexRouteAbb + 20, indexRouteAbb + 21);
	            strVehicleNum = input.substring(indexRouteAbb + 40, indexRouteAbb + 43);
	            strBearing = input.substring(indexRouteAbb + 55, indexRouteAbb + 56);
	            strLat = input.substring(indexRouteAbb + 63, input.indexOf(",", indexRouteAbb + 66));
	            strLog = input.substring(indexRouteAbb + 80, input.indexOf("}", indexRouteAbb + 81));
	            
	            // Write into the textbox
	            /*String in = "Route Abbr: " + strRouteAbb + "\nVehicle Num: " + strVehicleNum +
	            		"\nBearing: " + strBearing + "\nLat: " + strLat + "\nLog: " + strLog;
	            
	            responseText.setText(responseText.getText() + "\n" + in);*/
	            	            
	            // add in to the vehicle list
	        	VehicleData data = new VehicleData(Integer.parseInt(strRouteAbb), Integer.parseInt(strVehicleNum),
	        			Double.parseDouble(strBearing), Double.parseDouble(strLat), Double.parseDouble(strLog));
	        	vehicles.add(data);
	        }
	        index = 0;
	        for (Iterator<VehicleData> i = vehicles.iterator(); i.hasNext(); ) {
	        //while ((index = input.indexOf("Direction", index + 1140)) != -1) {
	        	if ((index = input.indexOf("Direction", index + 1140)) == -1) break;
	            substr = input.substring(index, input.indexOf("Status", index+10) + 70);
	            
	            // If the bus is in depart state, skip it
	            // if (substr.indexOf("Depart") != -1) continue;
	            
	            offset = 0;		// Reset the offset
	            // Read in the more detailed stuff
	            strDir = substr.substring(407, substr.indexOf("\\",410));
	            offset = strDir.length();
	            strNextStop = substr.substring(671+offset, substr.indexOf("\\u003",675+offset));
	            offset += strNextStop.length();
	            if (substr.indexOf("Depart", 685) != -1) offset += 2;
	            strArrival = substr.substring(1001+offset, substr.indexOf("\\",1005+offset));
	            offset += strArrival.length();
	            strStatus = substr.substring(1082+offset, substr.indexOf("\\",1086+offset));

	            strNextStop = strNextStop.replace("\\u0026", "&");	// Replace the unicode with the "&"
	            
	            // Add the data to the VehicleData structure
	            VehicleData data = i.next();
	            data.setDirection(strDir);
	            data.setNextStop(strNextStop);
	            data.setArrivalTime(strArrival);
	            data.setStatus(strStatus);

	            // Add into the textbox
	            //String in = "\nDirection: " + strDir + "\nNext Stop: " + strNextStop +
	            //    "\nEstimated Time: " + strArrival + "\nStatus: " + strStatus;
	            
	            //responseText.setText(responseText.getText() + in);
	        }
		}
	}
	
	// Send a POST request to theride.org
	public HttpResponse postData() {
		// Create a new HttpClient and Post Header
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(getString(R.string.postURI));
		
		try {
			// Add the data
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
			nameValuePairs.add(new BasicNameValuePair("__EVENTTARGET", "null"));
			nameValuePairs.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
			nameValuePairs.add(new BasicNameValuePair("__AJAXCONTROLID", getString(R.string.ajaxControlID)));
			nameValuePairs.add(new BasicNameValuePair("__AJAXPARAM", getString(R.string.vehiclePontiac)));
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			
			HttpResponse response = httpClient.execute(httpPost);
			return response;
			
		} catch (ClientProtocolException e) {
			Log.e(TAG, e.toString()); 
		} catch (IOException e) {
			Log.e(TAG, e.toString());
		}
		return null;
	}
	
	// Takes in an InputStream and returns a StringBuilder
	private StringBuilder inputStreamToString(InputStream is) {
	    String line = "";
	    StringBuilder total = new StringBuilder();
	    
	    // Wrap a BufferedReader around the InputStream
	    BufferedReader rd = new BufferedReader(new InputStreamReader(is));

	    // Read response until the end
	    try {
	    	line = rd.readLine();
	    	if (line != null) {
	    		total.append(line);
	    	}
	    } catch (IOException e) {
	    	Log.e(TAG, e.toString());
	    }
	    
	    // Return full string
	    return total;
	}
}
