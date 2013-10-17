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
import android.widget.EditText;
import android.widget.Toast;

public class StatusActivity extends Activity {
	
	EditText responseText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.status);
		
		// Find the views
		responseText = (EditText) findViewById(R.id.responseText);
		new PostData().execute("");
		
		//try {
			//HttpResponse response = postData();
			//responseText.setText(inputStreamToString(response.getEntity().getContent()));
		//} catch (IOException e) {}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	class PostData extends AsyncTask<String, Integer, String> {
		@Override
		protected String doInBackground(String... arg0) {
			HttpResponse response = postData();
			String str = null;
			try {
			str = inputStreamToString(response.getEntity().getContent()).toString();
			} catch (IOException e) {}
			return str;
		}
		
		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);
		}
		
		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			Toast.makeText(getApplicationContext(), R.string.toastMsg, Toast.LENGTH_LONG).show();
			responseText.setText(result);
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
			nameValuePairs.add(new BasicNameValuePair("__AJAXCONTROLID", "dnn%24ctr2257%24InteractiveMap%24ajaxControl0"));
			nameValuePairs.add(new BasicNameValuePair("__AJAXPARAM", "%7BMethodName%3A%22GetVehiclePositions_Internal%22%2CReturnHtml%3Atrue%2CAutoUpdateHtml%3Atrue%2CParameters%3A%5B%2216%22%2C0%2Cnull%5D%2CLoadOutOfBand%3Atrue%2CControlType%3A%22ASP.desktopmodules_artemis_ridertools_ucridetrak_ascx%2C%20App_Web_ucridetrak.ascx.ec37e418.bsepl5iy%2C%20Version%3D0.0.0.0%2C%20Culture%3Dneutral%2C%20PublicKeyToken%3Dnull%22%2CCallbackMethod%3A%22Invoke%22%2CProperties%3A%7BPortalID%3A0%2CTabID%3A62%2CModuleID%3A2257%2CControlName%3A%22%22%2CModulePath%3A%22%2F%22%2CID%3A%22dnn%24ctr2257%24InteractiveMap%24ajaxControl0%22%2CClientID%3A%22dnn_ctr2257_InteractiveMap_ajaxControl0%22%2CUniqueID%3A%22dnn%24ctr2257%24InteractiveMap%24ajaxControl0%22%2CDirection%3A%220%22%2CRefreshIntervalID%3A37%7D%2CPath%3A%22~%2FDefault.aspx%22%7D"));
			httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			
			HttpResponse response = httpClient.execute(httpPost);
			return response;
			
		} catch (ClientProtocolException e) {
			
		} catch (IOException e) {
			
		}
		return null;
	}
	
	private StringBuilder inputStreamToString(InputStream is) {
	    String line = "";
	    StringBuilder total = new StringBuilder();
	    
	    // Wrap a BufferedReader around the InputStream
	    BufferedReader rd = new BufferedReader(new InputStreamReader(is));

	    // Read response until the end
	    try {
	    	while ((line = rd.readLine()) != null) { 
	    		total.append(line); 
	    	}
	    } catch (IOException e) {
	    	
	    }
	    
	    // Return full string
	    return total;
	}
}
