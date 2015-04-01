package com.mattcduff.travellog;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Created by Matt on 17/03/2015.
 */
public class DBAdapter {

    private static final String TAG = "DBAdapter"; //used for logging database version changes

    // Field Names:
    public static final String  COLUMN_ROWID = "_id";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_DIRECTION = "direction";
    public static final String COLUMN_LOCATION = "location";
    public static final String COLUMN_ARRIVED = "arrived";
    public static final String COLUMN_DEPARTED = "departed";
    public static final String COLUMN_FASTTRAIN = "fasttrain";
    public static final String COLUMN_COMMENTS = "comments";

    public static final String[] ALL_COLUMNS = new String[] {COLUMN_ROWID, COLUMN_DATE, COLUMN_DIRECTION,
            COLUMN_LOCATION, COLUMN_ARRIVED, COLUMN_DEPARTED, COLUMN_FASTTRAIN, COLUMN_COMMENTS};

    // Column Numbers for each Field Name:
    public static final int COL_ROWID = 0;
    public static final int COL_DATE = 1;
    public static final int COL_DIRECTION = 2;
    public static final int COL_LOCATION = 3;
    public static final int COL_ARRIVED = 4;
    public static final int COL_DEPARTED = 5;
    public static final int COL_FASTTRAIN = 6;
    public static final int COL_COMMENTS = 7;


    // DataBase info:
    public static final String DATABASE_NAME = "dbLogEntries";
    public static final String DATABASE_TABLE = "mainLogEntries";
    public static final int DATABASE_VERSION = 1; // The version number must be incremented each time a change to DB structure occurs.

    //SQL statement to create database
    private static final String DATABASE_CREATE_SQL =
            "CREATE TABLE " + DATABASE_TABLE
                    + " ("
                    + COLUMN_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + COLUMN_DATE + " TEXT,"
                    + COLUMN_DIRECTION + " TEXT,"
                    + COLUMN_LOCATION + " TEXT,"
                    + COLUMN_ARRIVED + " TEXT,"
                    + COLUMN_DEPARTED + " TEXT,"
                    + COLUMN_FASTTRAIN + " TEXT,"
                    + COLUMN_COMMENTS + " TEXT" + ")";

    private final Context context;
    private DatabaseHelper myDBHelper;
    private SQLiteDatabase db;


    public DBAdapter(Context ctx) {
        this.context = ctx;
        myDBHelper = new DatabaseHelper(context);
    }

    // Open the database connection.
    public DBAdapter open() {
        db = myDBHelper.getWritableDatabase();
        return this;
    }

    // Close the database connection.
    public void close() {
        myDBHelper.close();
    }



    //Add a new entry with an Arrived time
    public long addRowArrived(String date, String location, String direction, String time, String comments) {

        ContentValues initialValues = new ContentValues();
        initialValues.put(COLUMN_DATE, date);
        initialValues.put(COLUMN_LOCATION, location);
        initialValues.put(COLUMN_DIRECTION, direction);
        initialValues.put(COLUMN_ARRIVED, time);
        initialValues.put(COLUMN_COMMENTS, comments);

        Log.i(TAG,"Arrived Row Added");

        return db.insert(DATABASE_TABLE, null, initialValues);

    }

    //Add a new entry with a Departed time
    public long addRowDeparted(String date, String location, String direction, String time, String fasttrain, String comments) {

        ContentValues initialValues = new ContentValues();
        initialValues.put(COLUMN_DATE, date);
        initialValues.put(COLUMN_LOCATION, location);
        initialValues.put(COLUMN_DIRECTION, direction);
        initialValues.put(COLUMN_DEPARTED, time);
        initialValues.put(COLUMN_FASTTRAIN, fasttrain);
        initialValues.put(COLUMN_COMMENTS, comments);

        Log.i(TAG,"Departed Row Added");

        return db.insert(DATABASE_TABLE, null, initialValues);

    }

    //Check if the row already exists and if it does then return the row ID
    public long rowExists(String date, String location, String direction) {

        long rowID;
        String selection = COLUMN_DATE + "= ? AND " + COLUMN_LOCATION + " = ? AND " + COLUMN_DIRECTION + " = ? ";
        String [] selectionArgs ={ date, location, direction };
        Cursor c = db.query(DATABASE_TABLE, ALL_COLUMNS, selection, selectionArgs, null, null, null, null);
        c.moveToFirst();
        if (c.getCount()>0) {
            rowID = c.getLong(COL_ROWID);
            return rowID;
        } else {
            c.close();
            return -1;
        }
    }

    //Update a row with the Arrived time and any comments
    public boolean updateRowArrived(long rowID, String time, String comments) {

        String where = COLUMN_ROWID + "=" + rowID;
        ContentValues newValues = new ContentValues();
        newValues.put(COLUMN_ARRIVED, time);
        newValues.put(COLUMN_COMMENTS, comments);

        return db.update(DATABASE_TABLE, newValues, where, null) !=0;
    }

    //Update a row with the Departed time, whether a fast train was used and any comments
    public boolean updateRowDeparted(long rowID, String time, String fasttrain, String comments) {

        String where = COLUMN_ROWID + "=" + rowID;
        ContentValues newValues = new ContentValues();
        newValues.put(COLUMN_DEPARTED, time);
        newValues.put(COLUMN_FASTTRAIN, fasttrain);
        newValues.put(COLUMN_COMMENTS, comments);

        return db.update(DATABASE_TABLE, newValues, where, null) !=0;
    }
    
    //Update a row with all the fields that could have been edited
    public boolean updateEditedRow(long rowID, String location, String direction, String arrTime, String depTime, String fasttrain, String comments) {

        String where = COLUMN_ROWID + "=" + rowID;
        ContentValues newValues = new ContentValues();
        newValues.put(COLUMN_LOCATION, location);
        newValues.put(COLUMN_DIRECTION, direction);
        newValues.put(COLUMN_ARRIVED), arrTime
        newValues.put(COLUMN_DEPARTED, depTime);
        newValues.put(COLUMN_FASTTRAIN, fasttrain);
        newValues.put(COLUMN_COMMENTS, comments);

        return db.update(DATABASE_TABLE, newValues, where, null) !=0;
    }

    // Return all today's data in the database.
    public Cursor getAllTodaysRows(String date) {
        String where = COLUMN_DATE + "=" + date;
        String sortOrder = COLUMN_ROWID + " DESC";
        Cursor c = 	db.query(DATABASE_TABLE, ALL_COLUMNS, where, null, null, null, sortOrder, null);
        if (c.getCount()>0) {
            c.moveToFirst();
        }
        return c;
    }

    // Delete a row from the database, by rowId (primary key)
    public boolean deleteRow(long rowID) {
        String where = COLUMN_ROWID + "=" + rowID;
        return db.delete(DATABASE_TABLE, where, null) != 0;
    }

    private static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase _db) {

            _db.execSQL(DATABASE_CREATE_SQL);
            Log.i(TAG,"Database Created");
        }

        @Override
        public void onUpgrade(SQLiteDatabase _db, int oldVersion, int newVersion) {
            Log.i(TAG, "Upgrading application's database from version " + oldVersion
                    + " to " + newVersion + ", which will destroy all old data!");

            // Destroy old database:
            _db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);

            // Recreate new database:
            onCreate(_db);
        }
    }
}
