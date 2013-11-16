// Holds a bunch of static functions to help with HTTP stuff

package com.ranintotree.ride.util;

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
}
