package com.ranintotree.ride.fragments;

import java.io.IOException;

import org.apache.http.HttpResponse;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ranintotree.ride.R;
import com.ranintotree.ride.util.HTTPSupport;
import com.ranintotree.ride.util.RouteData;
import com.ranintotree.ride.util.StopData;

public class GMapFragment extends SupportMapFragment {
	static final LatLng HAMBURG = new LatLng(42.2778682, -83.7465795);
	//static final LatLng KIEL = new LatLng(53.551, 9.993);
	private GoogleMap map;

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
	public View onCreateView(LayoutInflater inflater, ViewGroup container, 
			Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		return inflater.inflate(R.layout.fragment_map, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		map = ((SupportMapFragment) getFragmentManager().findFragmentById(R.id.map))
				.getMap();

		// Load the route and put it into the map
		

		//Marker hamburg = map.addMarker(new MarkerOptions().position(HAMBURG)
		//		.title("Hamburg"));
		/*Marker kiel = map.addMarker(new MarkerOptions()
		.position(KIEL)
		.title("Kiel")
		.snippet("Kiel is cool")
		.icon(BitmapDescriptorFactory
				.fromResource(R.drawable.ic_launcher)));*/

		// Move the camera instantly to hamburg with a zoom of 15.
		//map.moveCamera(CameraUpdateFactory.newLatLngZoom(HAMBURG, 2));

		// Zoom in, animating the camera.
		//map.animateCamera(CameraUpdateFactory.zoomTo(21), 2000, null);
		new PostData().execute("");
	}
	
	// Load the route into the map
	private void loadRouteToMap(RouteData route) {
		StopData s1;
		LatLng lat;
		Marker stop1;
		for (int i = 0; i < route.getNumStops(); ++i) {
			s1 = route.getStops()[i];
			lat = new LatLng(s1.getLat(),s1.getLog());
			stop1 = map.addMarker(new MarkerOptions().position(lat)
					.title(s1.getName()));
			if (i == route.getNumStops() - 1) map.moveCamera(CameraUpdateFactory.newLatLngZoom(lat, 12));
		}
		
	}

	// Update the data (IDK if this should go here or what)
	// Sends the POST stores the return and parses it
	private class PostData extends AsyncTask<String, Integer, StringBuilder> {
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
			RouteData route = HTTPSupport.parseRouteData("1", result);
			loadRouteToMap(route);
			//setData(route.getStops()[0]);
		}
	}
}