package com.ranintotree.ride.activity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.http.HttpResponse;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.ranintotree.ride.R;
import com.ranintotree.ride.fragments.StatusFragment;
import com.ranintotree.ride.util.HTTPSupport;
import com.ranintotree.ride.util.VehicleData;

public class MainActivity extends FragmentActivity {
	// ID for debugging with LogCat
	private static final String TAG = "StatusActivity";
	
	// VehicleData structures
	private ArrayList<VehicleData> vehicles; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Fragment
		StatusFragment fragment = (StatusFragment) getSupportFragmentManager().findFragmentById(R.id.statusFragment);
		
		// initialize the ArrayList
		vehicles = new ArrayList<VehicleData>();
		
		// Send the POST
		if (HTTPSupport.isNetworkAvailable(this)) {
			new PostData().execute("");
		} else {
			StatusFragment stfragment = 
					(StatusFragment) getSupportFragmentManager().findFragmentById(R.id.statusFragment);
			if (stfragment != null && stfragment.isInLayout()) {
				stfragment.setData("No network connection.");
			} 
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	// Sends the POST stores the return and parses it
	class PostData extends AsyncTask<String, Integer, StringBuilder> {
		@Override
		protected StringBuilder doInBackground(String... arg0) {
			HttpResponse response = HTTPSupport.postData(getString(R.string.postURI), 
					getString(R.string.ajaxControlID), getString(R.string.vehiclePontiac));
			StringBuilder str = null;
			try {
				str = HTTPSupport.inputStreamToString(response.getEntity().getContent());
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

			//responseText.setText("");
			vehicles.clear();
			parseData(result);
			// Display the data onto the textView (replace later)
			// TODO: display the selected data (the bus or stop)
			StatusFragment stfragment = 
				(StatusFragment) getSupportFragmentManager().findFragmentById(R.id.statusFragment);
			if (vehicles.size() == 0) {
				stfragment.setData("No buses running");
			} else {
				for (Iterator<VehicleData> i = vehicles.iterator(); i.hasNext(); ) {
					VehicleData data = i.next();

					if (stfragment != null && stfragment.isInLayout()) {
						stfragment.setData(data);
					}
				}
			}
		}
	}
	
	// NOTE: Maybe put into separate file
	// parse the response for the data
	public void parseData(StringBuilder input) {
		int routeAbb = input.indexOf("Rou");
		if (routeAbb >= 0)
		{
			// TODO: Clean this up
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
	            
	            // add in to the vehicle list
	        	//VehicleData data = new VehicleData(Integer.parseInt(strRouteAbb), Integer.parseInt(strVehicleNum),
	        	//		Double.parseDouble(strBearing), Double.parseDouble(strLat), Double.parseDouble(strLog));
	        	//vehicles.add(data);
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
	        }
		}
	} 
}
