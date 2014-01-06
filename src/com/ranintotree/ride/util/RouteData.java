package com.ranintotree.ride.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class RouteData {
	private int id;
	private String strRouteAbb;
	private String strName;
	private StopData[] stops;
	private ArrayList<StopData> stopsList;
	private class StopComparator implements Comparator<StopData>
	{            
		 @Override
		 public int compare(StopData lhs, StopData rhs) {
			 if (lhs.seq > rhs.seq) {
				 return 1;
			 } else if (lhs.seq == rhs.seq) {
				 return 0;
			 } else {
				 return -1;
			 }
		 }
     }
	
	public RouteData(String strRoute, String name, ArrayList<StopData> st) {
		strRouteAbb = strRoute;
		strName = name;
		stopsList = st;
		Collections.sort(stopsList, new StopComparator());
		stops = stopsList.toArray(new StopData[st.size()]);
	}
	
	public RouteData(int i, String strRoute, String name, ArrayList<StopData> st) {
		id = i;
		strRouteAbb = strRoute;
		strName = name;
		stopsList = st;
		Collections.sort(stopsList, new StopComparator());
		stops = stopsList.toArray(new StopData[st.size()]);
	}
	
	public int getID() { return id; }
	public void setID(int i) { id = i; }
	public String getRouteAbb() { return strRouteAbb; }
	public void setRouteAbb(String abb) { strRouteAbb = abb; }
	public String getName() { return strName; }
	public void setName(String name) { strName = name; }
	public StopData[] getStops() { return stops; }
	public int getNumStops() { return stops.length; }
	public StopData getStopAt(int i) { return stops[i]; }
	public ArrayList<StopData> getCleanStops() { return stopsList; }
	
	
}
