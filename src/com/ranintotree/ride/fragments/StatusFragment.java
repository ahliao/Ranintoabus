package com.ranintotree.ride.fragments;

import com.ranintotree.ride.R;
import com.ranintotree.ride.util.VehicleData;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class StatusFragment extends Fragment {
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) { 
		View view = inflater.inflate(R.layout.fragment_status, container, false);
		return view;
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
					"Status: " + v.getStatus());
		}
	}
}
