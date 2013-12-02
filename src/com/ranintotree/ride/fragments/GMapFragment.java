package com.ranintotree.ride.fragments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.http.HttpResponse;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ranintotree.ride.R;
import com.ranintotree.ride.util.HTTPSupport;
import com.ranintotree.ride.util.RouteData;
import com.ranintotree.ride.util.StopData;
import com.ranintotree.ride.util.VehicleData;

public class GMapFragment extends SupportMapFragment {
	static final LatLng HAMBURG = new LatLng(42.2778682, -83.7465795);
	//static final LatLng KIEL = new LatLng(53.551, 9.993);
	private static View view;
	private GoogleMap map;

	// Markers for the buses
	private ArrayList<VehicleData> vehicles;	// List of the buses
	private Marker mBus1; // This is just a test marker for now
	
	//private ScheduledExecutorService scheduler;
	private Handler handler = new Handler();
	private PostBusData postBusTask = null;

	/*
	 * Create a new instance of GMapFragment
	 * initialized to show the route at 'route'
	 * by loading the data from the database
	 */
	public static GMapFragment newInstance(int route) {
		GMapFragment g = new GMapFragment();

		// Supply index input as an argument
		Bundle args = new Bundle();
		args.putInt("route_list_pos", route);
		g.setArguments(args);

		return g;
	}

	// Really only needed for like tablets I think
	public int getShownRoute() {
		return getArguments().getInt("route_list_pos", 0);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setRetainInstance(true);

		// Initialize the scheduler and add the HTTPPOST function
		handler.post(HTTPTask);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		if (view != null) {
			ViewGroup parent = (ViewGroup) view.getParent();
			if (parent != null) 
				parent.removeView(view);
		}
		try {
			view = inflater.inflate(R.layout.fragment_map, container, false);
		} catch (InflateException e) {

		}

		//view = inflater.inflate(R.layout.fragment_map, container, false);
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// initialize the ArrayList
		vehicles = new ArrayList<VehicleData>();
		// get the map reference
		map = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.map))
				.getMap();
		map.clear();

		// Zoom in, animating the camera.
		//map.animateCamera(CameraUpdateFactory.zoomTo(21), 2000, null);
		new PostRouteData().execute("");
	}
	
	@Override
	public void onPause() {
		super.onPause();
		handler.removeCallbacks(HTTPTask);
		postBusTask.cancel(true); 
	}
	
	private Runnable HTTPTask = new Runnable() {
		@Override
		public void run() {
			//setData(i + "");
			//++i;
			if (HTTPSupport.isNetworkAvailable(getActivity())) {
				postBusTask = null;
				postBusTask = new PostBusData();	// TODO: do something about the AsyncTask string input?
				postBusTask.execute("");
			}
			handler.postDelayed(HTTPTask, 10000);
		}
	};

	// Load the route into the map
	private void loadRouteToMap(RouteData route) {
		StopData s1;
		LatLng lat;
		//Marker stop1;
		for (int i = 0; i < route.getNumStops(); ++i) {
			s1 = route.getStops()[i];

			lat = new LatLng(s1.getLat(),s1.getLog());
			/*Circle circle = map.addCircle(new CircleOptions()
				.center(lat)
				.radius(5)
				.strokeColor(Color.RED));*/
			map.addMarker(new MarkerOptions().position(lat)
					.title(s1.getName())
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.square)));
			if (i == 0) map.moveCamera(CameraUpdateFactory.newLatLngZoom(lat, 14));
		}

	}

	private class PostBusData extends AsyncTask<String, Integer, StringBuilder> {
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

			Toast.makeText(getActivity(), R.string.toastMsg, Toast.LENGTH_LONG).show();

			// Should put the UI changes into another function/ the UI thread
			vehicles.clear();
			HTTPSupport.parseVehicleData(result, vehicles);
			// Display the data onto the textView (replace later)
			if (vehicles.size() == 0) {
				//setData("No buses running on the selected: " + getShownRoute());
			} else {
				/*for (Iterator<VehicleData> i = vehicles.iterator(); i.hasNext(); ) {
					VehicleData data = i.next();
					//setData(data);
				}*/
				if (mBus1 != null) mBus1.remove();
				mBus1 = map.addMarker(new MarkerOptions()
						.position(new LatLng(vehicles.get(0).getLat(),vehicles.get(0).getLog()))
						.title("Bus" + vehicles.get(0).getVehicleNum())
						.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher)));
			}
		}
	}

	// Sends the POST for the route data and stores the return and parses it
	private class PostRouteData extends AsyncTask<String, Integer, StringBuilder> {
		@Override
		protected StringBuilder doInBackground(String... arg0) {
			Resources res = getResources();
			String[] routeabb = res.getStringArray(R.array.routes_abb_array);
			HttpResponse response = HTTPSupport.postData(getString(R.string.postURI), 
					getString(R.string.ajaxControlID), getString(R.string.routeParams1) + routeabb[getShownRoute()] + 
					getString(R.string.routeParams2));
			StringBuilder str = null;
			try {
				str = HTTPSupport.inputStreamToString(response.getEntity().getContent());
			} catch (IOException e) {
				Log.e("GMap", e.getMessage());
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

			// TODO: Store this data into a database and make it so that it
			// only need to update like once a week/when not using data
			RouteData route = HTTPSupport.parseRouteData("" + getShownRoute(), result);
			loadRouteToMap(route);	// Put in the markers in the map
			//setData(route.getStops()[0]);
		}
	}
}