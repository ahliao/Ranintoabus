// Holds a bunch of static functions to help with HTTP stuff

package com.ranintotree.ride.util;

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

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class HTTPSupport {
	// ID for debugging in LogCat
	public static final String TAG = "HTTPSupport";

	// Send a POST request to theride.org
	public static HttpResponse postData(String URI, String controlID, String params) {
		// Create a new HttpClient and Post Header
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost httpPost = new HttpPost(URI);

		try {
			// Add the data
			// TODO: make more flexible
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
			nameValuePairs.add(new BasicNameValuePair("__EVENTTARGET", "null"));
			nameValuePairs.add(new BasicNameValuePair("__EVENTARGUMENT", ""));
			nameValuePairs.add(new BasicNameValuePair("__AJAXCONTROLID", controlID));
			nameValuePairs.add(new BasicNameValuePair("__AJAXPARAM", params));
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

			HttpResponse response = httpClient.execute(httpPost);
			return response;

		} catch (ClientProtocolException e) {
			Log.e(TAG, e.getMessage());
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
		}
		return null;
	}

	// NOTE: A StringBuilder isn't really needed
	// Takes in an InputStream and returns a StringBuilder
	public static StringBuilder inputStreamToString(InputStream is) {
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
			Log.e(TAG, e.getMessage());
		}   
		// Return full string
		return total;
	}

	// Check if there is internet
	public static boolean isNetworkAvailable(Activity a) {
		ConnectivityManager connectivity = (ConnectivityManager) a.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null) 
				for (int i = 0; i < info.length; i++) 
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}
		}
		return false;
	} 

	// NOTE: Maybe put into separate file
	// parse the response for the data
	public static void parseVehicleData(StringBuilder input, ArrayList<VehicleData> vehicles) {
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
			}
		}
	} 
}
