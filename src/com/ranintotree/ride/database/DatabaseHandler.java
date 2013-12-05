package com.ranintotree.ride.database;

import java.util.ArrayList;
import java.util.List;

import com.ranintotree.ride.database.RouteManagerContract.RouteNameEntry;
import com.ranintotree.ride.database.RouteManagerContract.RouteStopsEntry;
import com.ranintotree.ride.util.RouteData;
import com.ranintotree.ride.util.StopData;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHandler extends SQLiteOpenHelper {
	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 1;

	// Database Name
	private static final String DATABASE_NAME = "routeManager";

	private static final String TEXT_TYPE = " TEXT";
	private static final String REAL_TYPE = " REAL";
	private static final String INT_TYPE = " INTEGER";
	private static final String COMMA_SEP = ",";

	private static final String CREATE_ROUTE_NAMES =
			"CREATE TABLE IF NOT EXISTS " + RouteNameEntry.TABLE_NAME + " (" +
					RouteNameEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," + 
					RouteNameEntry.COLUMN_ABB + TEXT_TYPE + COMMA_SEP +
					RouteNameEntry.COLUMN_NAME + TEXT_TYPE +  COMMA_SEP + 
					RouteNameEntry.COLUMN_CREATED_AT + INT_TYPE + " )";

	private static final String DELETE_ROUTE_NAMES =
			"DROP TABLE IF EXISTS " + RouteNameEntry.TABLE_NAME;

	private static final String CREATE_ROUTE_STOPS =
			"CREATE TABLE IF NOT EXISTS " + RouteStopsEntry.TABLE_NAME + " (" +
					RouteStopsEntry._ID + " INTEGER PRIMARY KEY," +
					RouteStopsEntry.COLUMN_ROUTE_ID + INT_TYPE + COMMA_SEP +
					RouteStopsEntry.COLUMN_ID + TEXT_TYPE + COMMA_SEP +
					RouteStopsEntry.COLUMN_NAME + TEXT_TYPE + COMMA_SEP +
					RouteStopsEntry.COLUMN_SEQ + TEXT_TYPE + COMMA_SEP +
					RouteStopsEntry.COLUMN_LAT + REAL_TYPE + COMMA_SEP +
					RouteStopsEntry.COLUMN_LOG + REAL_TYPE + " )";

	private static final String DELETE_ROUTE_STOPS =
			"DROP TABLE IF EXISTS " + RouteStopsEntry.TABLE_NAME;

	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_ROUTE_NAMES);
		db.execSQL(CREATE_ROUTE_STOPS);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop the older table if it exists
		db.execSQL(DELETE_ROUTE_NAMES);
		db.execSQL(DELETE_ROUTE_STOPS);

		// Create tables again
		onCreate(db);
	}

	// The CRUD operators
	// ************************************************************** //

	// This is the CRUD operators for the route names
	// aka the strings used to match to the route's stop table

	// Adding a new route name
	// Input: String
	public void addRouteName(RouteData route) {
		SQLiteDatabase db = this.getWritableDatabase();	// get the database

		ContentValues values = new ContentValues();
		values.put(RouteNameEntry.COLUMN_ABB, route.getRouteAbb());
		Log.d("GMap", "Added Abb: " + route.getRouteAbb());
		values.put(RouteNameEntry.COLUMN_NAME, route.getName());
		values.put(RouteNameEntry.COLUMN_CREATED_AT, System.currentTimeMillis());

		// Insert the row
		db.insert(RouteNameEntry.TABLE_NAME, null, values);
		
		Log.e("GMap", route.getRouteAbb());
		route.setID(getRouteID(route));

		// TODO: Put the stops into the stop table
		StopData stop = null;
		for (int i = 0; i < route.getNumStops(); ++i) {
			stop = route.getStopAt(i);
			values.clear();	// clear the values for the next operation
			values.put(RouteStopsEntry.COLUMN_ROUTE_ID, route.getID());
			values.put(RouteStopsEntry.COLUMN_ID, stop.getID());
			values.put(RouteStopsEntry.COLUMN_NAME, stop.getName());
			values.put(RouteStopsEntry.COLUMN_SEQ, stop.getSeqNum());
			values.put(RouteStopsEntry.COLUMN_LAT, stop.getLat());
			values.put(RouteStopsEntry.COLUMN_LOG, stop.getLog());
			
			// Insert the stop row
			db.insert(RouteStopsEntry.TABLE_NAME, null, values);
		}

		db.close();
	}
	
	public int getRouteID(RouteData route) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(RouteNameEntry.TABLE_NAME, new String[] { 
				RouteNameEntry._ID }, RouteNameEntry.COLUMN_ABB + "=?", 
				new String[] { route.getRouteAbb() }, 
				null, null, null, null);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				return cursor.getInt(0);
			}
		}
		return -1;
	}
	
	// returns the system currentmillis value stored
	public long getRouteTimeCreated(RouteData route) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(RouteNameEntry.TABLE_NAME, new String[] { 
				RouteNameEntry.COLUMN_CREATED_AT }, RouteNameEntry.COLUMN_ABB + "=?", 
				new String[] { route.getRouteAbb() }, 
				null, null, null, null);
		
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				return cursor.getLong(0);
			}
		}
		return 0;
	}

	// Getting a route name and returns it as a RouteData object
	public RouteData getRoute(String routeabb) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(RouteNameEntry.TABLE_NAME, new String[] { 
				RouteNameEntry._ID, RouteNameEntry.COLUMN_NAME }, 
				RouteNameEntry.COLUMN_ABB + "= ?", 
				new String[] { routeabb }, 
				null, null, null, null);
		Log.e("GMap", "ERROR: " + cursor.getCount());
		int routeid;
		if (cursor != null && cursor.moveToFirst())
			routeid = cursor.getInt(0);
		else return null;
		
		RouteData route = null;
		ArrayList<StopData> stops = new ArrayList<StopData>();
		if (cursor != null)  {
			if (cursor.moveToFirst()) {
				// Get the stops from the stop table
				Cursor stopcursor = db.query(RouteStopsEntry.TABLE_NAME,
						new String[] { RouteStopsEntry.COLUMN_ID, RouteStopsEntry.COLUMN_NAME,
						RouteStopsEntry.COLUMN_SEQ, RouteStopsEntry.COLUMN_LAT,
						RouteStopsEntry.COLUMN_LOG }, RouteStopsEntry.COLUMN_ROUTE_ID + "=?",
						new String[] { ""+routeid },
						null, null, null, null);
				
				if (stopcursor != null && stopcursor.moveToFirst()) {
					do {
						StopData s = new StopData(stopcursor.getString(0),
								stopcursor.getString(1), stopcursor.getString(2),
								stopcursor.getDouble(3), stopcursor.getDouble(4));
						stops.add(s);
					} while (stopcursor.moveToNext());
				}
					 
				route = new RouteData(routeabb, cursor.getString(1), stops);
			}
			else {
				// Record doesn't exist
				route = null;
			}
		}
		db.close();
		return route;
	}

	// Get all of the route names
	public List<String> getAllRouteNames() {
		List<String> routeNamesList = new ArrayList<String>();
		// Select All Query
		String selectQuery = "SELECT  * FROM " + RouteNameEntry.TABLE_NAME;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				// Adding route name to list
				routeNamesList.add(cursor.getString(2));
			} while (cursor.moveToNext());
		}
		db.close();

		// return contact list
		return routeNamesList;
	}

	// get the number of route names
	public int getRouteNamesCount() {
		String countQuery = "SELECT  * FROM " + RouteNameEntry.TABLE_NAME;
		SQLiteDatabase db = this.getReadableDatabase();
		Cursor cursor = db.rawQuery(countQuery, null);
		cursor.close();
		db.close();

		// return count
		return cursor.getCount();
	}

	// update a single route name 
	// Input is a route
	public int updateRouteName(RouteData route) {
		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(RouteNameEntry.COLUMN_NAME, route.getName());

		// updating row
		return db.update(RouteNameEntry.TABLE_NAME, values, RouteNameEntry._ID + " = ?",
				new String[] { null });
	}

	// delete a single route name
	public void deleteRouteName(RouteData route) {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(RouteNameEntry.TABLE_NAME, 
				RouteNameEntry.COLUMN_ABB + " =?",
				new String[] { route.getRouteAbb() });
		db.close();
	}

	public void delete() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(RouteNameEntry.TABLE_NAME, null, null);
		//db.delete(RouteStopsEntry.TABLE_NAME, null, null);
		db.close();
	}
}
