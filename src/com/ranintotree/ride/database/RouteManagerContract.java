package com.ranintotree.ride.database;

import android.provider.BaseColumns;

public final class RouteManagerContract {
	// To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public RouteManagerContract() {}

    /* Inner class that defines the route table contents */
    public static abstract class RouteNameEntry implements BaseColumns {
        public static final String TABLE_NAME = "routenames";
        public static final String COLUMN_ABB = "abb";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_CREATED_AT = "created_at";
    }
    
    // The table will hold every single stop and be connected to the route name
    // by the id of the route
    public static abstract class RouteStopsEntry implements BaseColumns {
    	public static final String TABLE_NAME = "routestops";
    	public static final String COLUMN_ROUTE_ID = "routeid";
        public static final String COLUMN_ID = "id";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_SEQ = "seq";
        public static final String COLUMN_LAT = "lat";
        public static final String COLUMN_LOG = "log";
    }
}
