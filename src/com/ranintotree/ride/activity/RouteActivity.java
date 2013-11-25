package com.ranintotree.ride.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.ListView;
import com.ranintotree.ride.R;
import com.ranintotree.ride.fragments.GMapFragment;
import com.ranintotree.ride.fragments.RouteFragment;
import com.ranintotree.ride.fragments.StatusFragment;
import com.ranintotree.ride.fragments.RouteFragment.OnRouteListClickListener;

public class RouteActivity extends FragmentActivity implements OnRouteListClickListener {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_route);
		
		// Add the route fragment
		// Check if the activity is using the layout version with the fragment
		if (findViewById(R.id.route_fragment_container) != null) {
			if (savedInstanceState != null) {
				return;
			}
			
			// Creates a new fragment to put into the container
			RouteFragment routeFragment = new RouteFragment();
			
			//routeFragment.setArguments(getIntent().getExtras());
			
			// Add the fragment to the fragment_container FrameLayout
			getSupportFragmentManager().beginTransaction().add(R.id.route_fragment_container, routeFragment).commit();
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onRouteListClick(ListView l, View v, int position, long id) {
		// Replace the fragment in the container to the selected route status
		StatusFragment status = StatusFragment.newInstance(position);
		//GMapFragment map = new GMapFragment();
		
		// Execute the transaction and replace the route fragment
		FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.route_fragment_container, status);
		//ft.replace(R.id.route_fragment_container, map);
		ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
		ft.addToBackStack(null);
		ft.commit();
	}
}
