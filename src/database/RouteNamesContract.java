package database;

import android.provider.BaseColumns;

public final class RouteNamesContract {
	// To prevent someone from accidentally instantiating the contract class,
    // give it an empty constructor.
    public RouteNamesContract() {}

    /* Inner class that defines the table contents */
    public static abstract class RouteNameEntry implements BaseColumns {
        public static final String TABLE_NAME = "routenames";
        public static final String COLUMN_ABB = "abb";
        public static final String COLUMN_NAME = "name";
    }
}
