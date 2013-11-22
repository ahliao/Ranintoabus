package com.ranintotree.ride.fragments;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;

import com.ranintotree.ride.R;
import com.ranintotree.ride.util.HTTPSupport;
import com.ranintotree.ride.util.VehicleData;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class StatusFragment extends Fragment {
	// VehicleData structures
	private ArrayList<VehicleData> vehicles; 
	
	// 
	private WeakReference<PostData> ayncTaskWeakRef;

	/*
	 * Create a new instance of StatusFragment
	 * initialized to show the route at 'route'
	 */
	public static StatusFragment newInstance(int route) {
		StatusFragment s = new StatusFragment();

		// Supply index input as an argument
		Bundle args = new Bundle();
		args.putInt("route", route);
		s.setArguments(args);

		return s;
	}

	// Really only need for like tablets I think
	public int getShownRoute() {
		return getArguments().getInt("route", 0);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// initialize the ArrayList
		vehicles = new ArrayList<VehicleData>();
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setRetainInstance(true);
		// Send the POST
		if (HTTPSupport.isNetworkAvailable(getActivity())) {
			startPOSTTask();
			//new PostData().execute("");
		} else {
			setData("No network connection.");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) { 
		View view = inflater.inflate(R.layout.fragment_status, container, false);
		
		return view;
	}
	
	private void startPOSTTask() {
		final Handler handler = new Handler();
		Timer timer = new Timer();
		TimerTask asyncPostTask = new TimerTask() {
			@Override
			public void run() {
				handler.post(new Runnable() {
					public void run() {
						try {
							if (HTTPSupport.isNetworkAvailable(getActivity())) {
								new PostData().execute();
							} else {
								setData("No network connection.");
							}
						} catch(Exception e) {
							
						}
					}
				});
			}
		};
		timer.schedule(asyncPostTask, 0, 10000);
		// Send the POST
		/*if (HTTPSupport.isNetworkAvailable(getActivity())) {
			PostData postDataTask = new PostData(this);
			this.ayncTaskWeakRef = new WeakReference<StatusFragment.PostData>(postDataTask);
			postDataTask.execute();
		} else {
			setData("No network connection.");
		}*/
	}

	// Update the data (IDK if this should go here or what)
	// Sends the POST stores the return and parses it
	private class PostData extends AsyncTask<String, Integer, StringBuilder> {
		/*private WeakReference<StatusFragment> fragmentWeakRef;
		
		private PostData(StatusFragment s) {
			this.fragmentWeakRef = new WeakReference<StatusFragment>(s);
		}*/
		
		@Override
		protected StringBuilder doInBackground(String... arg0) {
			HttpResponse response = HTTPSupport.postData(getString(R.string.postURI), 
					getString(R.string.ajaxControlID), getString(R.string.vehiclePontiac));
			StringBuilder str = null;
			try {
				str = HTTPSupport.inputStreamToString(response.getEntity().getContent());
			} catch (IOException e) {

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

			//if(this.fragmentWeakRef.get() != null) {
				Toast.makeText(getActivity(), R.string.toastMsg, Toast.LENGTH_LONG).show();

				//responseText.setText("");
				vehicles.clear();
				HTTPSupport.parseVehicleData(result, vehicles);
				// Display the data onto the textView (replace later)
				if (vehicles.size() == 0) {
					setData("No buses running");
				} else {
					for (Iterator<VehicleData> i = vehicles.iterator(); i.hasNext(); ) {
						VehicleData data = i.next();
						setData(data);
					}
				}
			//}
		}
	}

	public void setData(String s) {
		TextView text = (TextView) getView().findViewById(R.id.statusText);
		text.setText(s);
	}

	public void setData(VehicleData v) {
		TextView text = (TextView) getView().findViewById(R.id.statusText);
		if (v == null) {	// If v is null or there are no buses
			text.setText("No Buses! Sucks to be you.");
		} else {
			text.setText("Bus #" + v.getVehicleNum() + "\n" +
					"Route #" + v.getRouteAbb() + " " + v.getDirection() + "\n" +
					"Next Stop: " + v.getNextStop() + "\n" +
					"Arrival Time: " + v.getArrivalTime() + "\n" +
					"Status: " + v.getStatus());
		}
	}
}
