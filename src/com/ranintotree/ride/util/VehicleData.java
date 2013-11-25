package com.ranintotree.ride.util;

// Holds the data (ex. log, lat, time) of a bus
public class VehicleData {
	// The variables
	private String strRouteAbb;
	private int intVehicleNum;
	private double dbBearing;
	private double dbLat;
	private double dbLog;
    
	private String strDir;
	private String strNextStop;
	private String strArrival;
	private String strStatus;
	
	public VehicleData(String routeAbb, int vehicleNum, double bear, double lat, double log) {
		
		strRouteAbb = routeAbb;
		intVehicleNum = vehicleNum;
		dbBearing = bear;
		dbLat = lat;
		dbLog = log;
	}
	
	// Accessors
	// These only have getters as the vars shouldn't be changed by client
	public String getRouteAbb() { return strRouteAbb; }
	public int getVehicleNum() { return intVehicleNum; }
	public double getBearing() { return dbBearing; }
	public double getLat() { return dbLat; }
	public double getLog() { return dbLog; }
	
	public String getDirection() { return strDir; }
	public void setDirection(String d) { strDir = d; }
	public String getNextStop() { return strNextStop; }
	public void setNextStop(String ns) { strNextStop = ns; }
	public String getArrivalTime() { return strArrival; }
	public void setArrivalTime(String at) { strArrival = at; } 
	public String getStatus() { return strStatus; }
	public void setStatus(String s) { strStatus = s; }
}
