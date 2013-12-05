package com.ranintotree.ride.util;

public class StopData {
	// The variables
	String id;	// Could change to a int maybe
	String name;
	String seq;
	double lat, log;
	
	public StopData(String i, String n, String s, double la, double lo) {
		id = i;
		name = n;
		seq = s;
		lat = la;
		log = lo;
	}
	
	public String getID() { return id; }
	public String getName() { return name; }
	public String getSeqNum() { return seq; }
	public double getLat() { return lat; }
	public double getLog() { return log; }
}
