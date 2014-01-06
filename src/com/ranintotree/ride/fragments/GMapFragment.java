package com.ranintotree.ride.fragments;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import org.apache.http.HttpResponse;

import android.app.Activity;
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
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ranintotree.ride.R;
import com.ranintotree.ride.database.DatabaseHandler;
import com.ranintotree.ride.util.HTTPSupport;
import com.ranintotree.ride.util.RouteData;
import com.ranintotree.ride.util.StopData;
import com.ranintotree.ride.util.VehicleData;


public class GMapFragment extends SupportMapFragment {
	// Ann Arbor's lat and long
	static final LatLng ANNARBOR = new LatLng(42.2814,83.7483);
	
	private static View view;
	private GoogleMap map;

	// Markers for the buses
	private ArrayList<VehicleData> vehicles;	// List of the buses
	private ArrayList<Marker> busMarkers;		// List of the bus markers on map
	private RouteData routeData;
	private ArrayList<Marker> stopMarkers;		// List of the stop markers

	//private ScheduledExecutorService scheduler;
	private Handler handler = new Handler();
	private PostBusData postBusTask = null;
	
	// Listener for when an infowindow is clicked
	private OnMapInfoWindowClickListener mListener;

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
		
		return view;
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		// initialize the ArrayList
		vehicles = new ArrayList<VehicleData>();
		busMarkers = new ArrayList<Marker>();
		stopMarkers = new ArrayList<Marker>();

		// get the map reference
		map = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.map))
				.getMap();
		map.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
	        @Override
	        public void onInfoWindowClick(Marker marker) {
	        	if (mListener != null) {
	        		int index = stopMarkers.indexOf(marker);
	        		// Using the index get the StopData from that route
	        		mListener.onMapInfoWindowClick(routeData.getRouteAbb(), routeData.getStopAt(index));
	        	}
	        }
	    });
		
		map.clear();

		// Zoom in to Ann Arbor
		map.moveCamera(CameraUpdateFactory.newLatLngZoom(ANNARBOR, 15));
		
		// If the route is already in the database, 
		// TODO: check if the timestamp is recent and how the network status is
		DatabaseHandler db = new DatabaseHandler(getActivity());
		Resources res = getResources();
		String[] routeabb = res.getStringArray(R.array.routes_abb_array);
		RouteData route = db.getRoute(routeabb[getShownRoute()]);
		db.close();
		if (route == null) {
			// If there is a network connection
			if (HTTPSupport.isNetworkAvailable(getActivity())) {
				// If the route isn't in the database, get it from online
				new PostRouteData().execute("");
			} else {
				// There is no network available
			}
		} else {
			// else just load it from the database
			// TODO: check the timestamp and determine if route should be updated
			loadRouteToMap(route);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		handler.removeCallbacks(HTTPTask);
		postBusTask.cancel(true); 
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mListener = (OnMapInfoWindowClickListener) activity;
		} catch (ClassCastException e) {
			throw new IllegalStateException("Activity must implement OnMapInfoWindowClickListener!");
		}
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	private Runnable HTTPTask = new Runnable() {
		@Override
		public void run() {
			if (HTTPSupport.isNetworkAvailable(getActivity())) {
				postBusTask = null;
				postBusTask = new PostBusData();	// TODO: do something about the AsyncTask string input?
				postBusTask.execute("");
			}
			handler.postDelayed(HTTPTask, 15000); // TODO: Check for the network
		}
	};

	// Load the route into the map
	private void loadRouteToMap(RouteData route) {
		this.routeData = route;
		StopData s1;
		LatLng lat;
		stopMarkers.clear();
		Marker stop;
		for (int i = 0; i < route.getNumStops(); ++i) {
			s1 = route.getStops()[i];

			lat = new LatLng(s1.getLat(),s1.getLog());
			
			stop = map.addMarker(new MarkerOptions().position(lat)
					.title(s1.getName())
					.icon(BitmapDescriptorFactory.fromResource(R.drawable.square)));
			stopMarkers.add(stop);
			if (i == 0) map.moveCamera(CameraUpdateFactory.newLatLngZoom(lat, 14));
		}
	}

	// Load the buses into the map
	private void loadBusesToMap() {
		// if some buses appeared/disappeared then just clear the busMarkers and restart
		// since we would have to change the listeners and text too
		if (vehicles.size() != busMarkers.size()) {
			busMarkers.clear();
			for (Iterator<VehicleData> i = vehicles.iterator(); i.hasNext(); ) {
				VehicleData data = i.next();
				Marker mark = map.addMarker(new MarkerOptions()
				.position(new LatLng(data.getLat(),data.getLog()))
				.title("Bus " + data.getVehicleNum())
				.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher)));
				busMarkers.add(mark);
			}
		}
		if (vehicles.size() == 0) {
			// No buses are running
		} else {
			// NOTE: we might run into a bug where a bus appears and another disappears
			// at the same time which would confuse the listener and text
			for (int i = 0; i < vehicles.size(); ++i) {
				VehicleData data = vehicles.get(i);
				busMarkers.get(i).setPosition(new LatLng(data.getLat(),data.getLog()));
				busMarkers.get(i).setTitle("Bus " + data.getVehicleNum());
			}
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

			// TODO: move this to a separate function
			vehicles.clear();
			HTTPSupport.parseVehicleData(result, vehicles);

			loadBusesToMap(); // Load that data into the map dawg
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

			Resources res = getResources();
			String[] routeabb = res.getStringArray(R.array.routes_abb_array);
			RouteData route = HTTPSupport.parseRouteData(routeabb[getShownRoute()], result);

			DatabaseHandler db = new DatabaseHandler(getActivity()); 
			db.addRouteName(route);	// Add route to the database
			db.close();

			loadRouteToMap(route);	// Put in the markers in the map
		}
	}
	
	public interface OnMapInfoWindowClickListener {
		public void onMapInfoWindowClick(String routeAbb, StopData stop);
	}
}