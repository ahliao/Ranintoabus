package com.ranintotree.ride.fragments;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.http.HttpResponse;

import com.ranintotree.ride.R;
import com.ranintotree.ride.util.HTTPSupport;
import com.ranintotree.ride.util.VehicleData;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
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

	//private ScheduledExecutorService scheduler;
	private Handler handler = new Handler();
	int i = 0;

	/*
	 * Create a new instance of StatusFragment
	 * initialized to show the route at 'route'
	 */
	public static StatusFragment newInstance(int route) {
		StatusFragment s = new StatusFragment();

		// Supply index input as an argument
		Bundle args = new Bundle();
		args.putInt("route_list_pos", route);
		s.setArguments(args);

		return s;
	}

	// Really only needed for like tablets I think
	public int getShownRoute() {
		return getArguments().getInt("route_list_pos", 0);
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

		// Initialize the scheduler and add the HTTPPOST function
		//new PostData().execute("");
		// Doesn't work on API 10 for some reason...
		/*scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.scheduleAtFixedRate(
				new Runnable() {
					@Override
					public void run() {
						i += 1;
						setData("BLAH" + i);
						
						//System.out.println(i);
						//Toast.makeText(getActivity(), R.string.toastMsg, Toast.LENGTH_LONG).show();
						//new PostData().execute("");
					}
				}, 0, 3, TimeUnit.SECONDS);*/
		
		handler.post(HTTPTask);
	}
	
	private Runnable HTTPTask = new Runnable() {
		@Override
		public void run() {
			//setData(i + "");
			//++i;
			if (HTTPSupport.isNetworkAvailable(getActivity())) {
				new PostData().execute("");	// TODO: do something about the AsyncTask string input?
			}
			handler.postDelayed(HTTPTask, 10000);
		}
	};
	
	@Override
	public void onPause() {
		super.onPause();
		handler.removeCallbacks(HTTPTask);
		//scheduler.shutdown(); // Shutdown the schedule 
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) { 
		View view = inflater.inflate(R.layout.fragment_status, container, false);

		return view;
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
			Resources res = getResources();
			String[] routeabb = res.getStringArray(R.array.routes_abb_array);
			HttpResponse response = HTTPSupport.postData(getString(R.string.postURI), 
					getString(R.string.ajaxControlID), getString(R.string.vehicleParams1) + routeabb[getShownRoute()] + 
					getString(R.string.vehicleParams2));
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

			// Should put the UI changes into another function/ the UI thread
			setData("");
			vehicles.clear();
			HTTPSupport.parseVehicleData(result, vehicles);
			// Display the data onto the textView (replace later)
			if (vehicles.size() == 0) {
				setData("No buses running on the selected: " + getShownRoute());
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
			text.setText(text.getText() + "Bus #" + v.getVehicleNum() + "\n" +
					"Route #" + v.getRouteAbb() + " " + v.getDirection() + "\n" +
					"Next Stop: " + v.getNextStop() + "\n" +
					"Arrival Time: " + v.getArrivalTime() + "\n" +
					"Status: " + v.getStatus() + "\n\n");
		}
	}
}
