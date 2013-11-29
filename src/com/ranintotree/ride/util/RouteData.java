package com.ranintotree.ride.util;

import java.util.ArrayList;

public class RouteData {
	private String strRouteAbb;
	private StopData[] stops;
	
	public RouteData(String strRoute, ArrayList<StopData> st) {
		strRouteAbb = strRoute;
		stops = st.toArray(new StopData[st.size()]);
	}
	
	public String getRouteAbb() { return strRouteAbb; }
	public StopData[] getStops() { return stops; }
	public int getNumStops() { return stops.length; }
}
