package com.ranintotree.ride.util;

import java.util.ArrayList;

public class RouteData {
	private int id;
	private String strRouteAbb;
	private String strName;
	private StopData[] stops;
	
	public RouteData(String strRoute, String name, ArrayList<StopData> st) {
		strRouteAbb = strRoute;
		strName = name;
		stops = st.toArray(new StopData[st.size()]);
	}
	
	public RouteData(int i, String strRoute, String name, ArrayList<StopData> st) {
		id = i;
		strRouteAbb = strRoute;
		strName = name;
		stops = st.toArray(new StopData[st.size()]);
	}
	
	public int getID() { return id; }
	public String getRouteAbb() { return strRouteAbb; }
	public String getName() { return strName; }
	public StopData[] getStops() { return stops; }
	public int getNumStops() { return stops.length; }
}
