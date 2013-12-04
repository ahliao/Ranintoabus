package database;

import java.util.ArrayList;
import java.util.List;

import com.ranintotree.ride.util.RouteData;

import database.RouteNamesContract.RouteNameEntry;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHandler extends SQLiteOpenHelper {
	// All Static variables
	// Database Version
	private static final int DATABASE_VERSION = 1;

	// Database Name
	private static final String DATABASE_NAME = "routeManager";

	private static final String TEXT_TYPE = " TEXT";
	private static final String COMMA_SEP = ",";

	private static final String SQL_CREATE_ENTRIES =
			"CREATE TABLE " + RouteNameEntry.TABLE_NAME + " (" +
					RouteNameEntry._ID + " INTEGER PRIMARY KEY," + 
					RouteNameEntry.COLUMN_ABB + TEXT_TYPE + COMMA_SEP +
					RouteNameEntry.COLUMN_NAME + TEXT_TYPE + " )";

	private static final String SQL_DELETE_ENTRIES =
			"DROP TABLE IF EXISTS " + RouteNameEntry.TABLE_NAME;

	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(SQL_CREATE_ENTRIES);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Drop the older table if it exists
		db.execSQL(SQL_DELETE_ENTRIES);

		// Create tables again
		onCreate(db);
	}

	// The CRUD operators
	// ************************************************************** //

	// Adding a new route name
	// Input: String
	public void addRouteName(RouteData route) {
		SQLiteDatabase db = this.getWritableDatabase();	// get the database

		ContentValues values = new ContentValues();
		values.put(RouteNameEntry.COLUMN_ABB, route.getRouteAbb());
		values.put(RouteNameEntry.COLUMN_NAME, route.getName());

		// Insert the row
		db.insert(RouteNameEntry.TABLE_NAME, null, values);
		db.close();
	}

	// Getting a route name
	public String getRouteName(RouteData route) {
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(RouteNameEntry.TABLE_NAME, new String[] { 
				RouteNameEntry.COLUMN_NAME }, RouteNameEntry.COLUMN_ABB + "=?", 
				new String[] { route.getRouteAbb() }, 
				null, null, null, null);
		if (cursor != null) cursor.moveToFirst();
		else { 
			// Error
		} 
		

		String routename = cursor.getString(0);
		db.close();
		return routename;
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
		db.close();
	}
}
