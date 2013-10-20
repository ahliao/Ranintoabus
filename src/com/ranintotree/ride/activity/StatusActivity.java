package com.ranintotree.ride.activity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
	
	EditText responseText;
	Button refreshBtn;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.status);
		
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
			// TODO: parse the data (COULD DO IN THE STRING BUILDER PART)

			//result = result.replaceAll("\\\\t", "");
			//int routeAbb = result.indexOf("Route");
			//String routeNum = result.substring(routeAbb + 20,routeAbb+22);
			parseData(result);
			responseText.setText(result);
		}
	}
	
	@Override
	public void onClick(View v) {
		new PostData().execute("");
	}
	
	// parse the response for the data
	public void parseData(StringBuilder s) {
		int routeAbb = s.indexOf("Rou");
		if (routeAbb >= 0)
		{
			//Log.d(TAG, Integer.toString(s.indexOf("Route"))); // Gives 176
			Log.d(TAG, s.substring(routeAbb + 20,routeAbb + 21));	// Always here it seems
			Log.d(TAG, "Vehicle # " + s.substring(215,219));	// Get the Vehicle Numbers
			Log.d(TAG, "Bearing " + s.substring(231,232)); 		// Get the bearing
			Log.d(TAG, "Lat " + s.substring(238,249));			// Get the lat
			Log.d(TAG, "Lon " + s.substring(255,266));			// Get the lon
			int nextStop = s.indexOf("Nex", 2000);
			int endStop = s.indexOf("\\", nextStop + 48);
			//Log.d(TAG, "Next Stop: " + nextStop);
			Log.d(TAG, "Next Stop: " + s.substring(nextStop + 48, endStop));
			int estimate = s.indexOf("Arrival", 2000);
			Log.d(TAG, "Estimated Arrival: " + estimate);
		}

		
		// search in the range 170 to 200
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
			
		} catch (IOException e) {
			
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
	    	//while ((line = rd.readLine()) != null) {
	    	// TODO: parse the data I need
	    	line = rd.readLine();
	    	if (line != null) {
	    		total.append(line);
	    		//total.append(line.indexOf("Route"));
	    	}
	    } catch (IOException e) {
	    	
	    }
	    
	    // Return full string
	    return total;
	}
}
