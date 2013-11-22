// This layout only needs a list with just the route names so
// it can just use the ListFragment for simplicity for now.

package com.ranintotree.ride.fragments;

import com.ranintotree.ride.R;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class RouteFragment extends ListFragment {
	
	// private listener for the list that gets called to inform the activity
	private OnRouteListClickListener mListener;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		String[] values = getResources().getStringArray(R.array.routes_array);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, values);
		setListAdapter(adapter);
	}
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Setup the reference to the listener and activity
		try {
			mListener = (OnRouteListClickListener) activity;
		} catch (ClassCastException e) {
			throw new IllegalStateException("Activity must implement OnRouteListClickListener!");
		}
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		//String str = (String) l.getItemAtPosition(position);
		//Toast.makeText(getActivity(), str, Toast.LENGTH_LONG).show();
		// Use if all the event handling is being done in the activity
		if (mListener != null) {
			mListener.onRouteListClick(l, v, position, id);
		}
	}
	
	/**
	 * Interface for listening for the list to be clicked.
	 * Classes that implement this interface will be notified when the event occurs.
	 */
	public interface OnRouteListClickListener {
		
		/**
		 * Callback for when the welcome button is clicked.
		 * Since we are intercepting this action in this Fragment, we must notify all listeners
		 * for the click when the event actually takes place. The system lets the Fragment know
		 * in onClick, since it implements the OnClickListener interface.
		 */
		public void onRouteListClick(ListView l, View v, int position, long id);
	}
}
