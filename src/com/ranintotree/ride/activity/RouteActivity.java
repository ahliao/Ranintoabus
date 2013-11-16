package com.ranintotree.ride.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.ranintotree.ride.R;

public class RouteActivity extends FragmentActivity {
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_route);
		
		// Fragment
		//StatusFragment fragment = (StatusFragment) getSupportFragmentManager().findFragmentById(R.id.routeFragment);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
}
