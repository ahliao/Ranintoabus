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
	
	// parse the response for the route data aka the stops
	public static RouteData parseRouteData(String routeabb, StringBuilder input) {
		ArrayList<StopData> stops = new ArrayList<StopData>();
		String strRouteName;
		int index = input.indexOf("Name");
		strRouteName = input.substring(index + 7, input.indexOf("\"",index+8));
		Log.e(TAG, "Name: " + strRouteName);
		String strStopID;
		String strStopName;
		String strSeq;
		String strLat, strLog;

		// TODO: Get the ID
		index = input.indexOf("StopID", index + 20);
		while (index >= 0) {
			strStopID = input.substring(index + 9, input.indexOf("\"",index+11));
			index = input.indexOf("\"Name", index + 10);
			strStopName = input.substring(index + 8, input.indexOf("\"", index+9));
			index = input.indexOf("Seq\"", index + 20);
			strSeq = input.substring(index+5,input.indexOf(",",index+6));
			index += strSeq.length() + 7;
			strLat = input.substring(index+5,input.indexOf(",",index+6));
			
			index += strLat.length() + 7;
			strLog = input.substring(index+5,input.indexOf(",",index+6));
			index = input.indexOf("StopID", index + 20);
			stops.add(new StopData(strStopID,strStopName,strSeq,Double.parseDouble(strLat),Double.parseDouble(strLog)));
		}
		System.out.println("Size: " + stops.size());
		RouteData route = new RouteData(routeabb, strRouteName, stops);
		return route;
	}

	// NOTE: Maybe put into separate file
	// parse the response for the vehicle data
	public static void parseVehicleData(StringBuilder input, ArrayList<VehicleData> vehicles) {
		int routeAbb = input.indexOf("Rout");
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
				strRouteAbb = input.substring(indexRouteAbb + 20, input.indexOf("\"", indexRouteAbb + 21));
				offset = strRouteAbb.length() - 1;
				Log.i(TAG, "offset: " + offset);
				strVehicleNum = input.substring(indexRouteAbb + 40 + offset, indexRouteAbb + 43 + offset);
				strBearing = input.substring(indexRouteAbb + 55 + offset, indexRouteAbb + 56 + offset);
				strLat = input.substring(indexRouteAbb + 63 + offset, input.indexOf(",", indexRouteAbb + 66));
				strLog = input.substring(indexRouteAbb + 80 + offset, input.indexOf("}", indexRouteAbb + 81));
				Log.i(TAG, strRouteAbb);
				Log.i(TAG, "Num: " + strVehicleNum);
				Log.i(TAG, "Bearing: " + strBearing);
				Log.i(TAG, "Lat: " + strLat);
				Log.i(TAG, "Log: " + strLog);

				// add in to the vehicle list
				VehicleData data = new VehicleData(strRouteAbb, Integer.parseInt(strVehicleNum),
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

				offset = strRouteAbb.length() - 1;		// Reset the offset
				// Read in the more detailed stuff
				strDir = substr.substring(407 + offset, substr.indexOf("\\",410));
				Log.i(TAG,"Dir: " + strDir);
				offset += strDir.length();
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
