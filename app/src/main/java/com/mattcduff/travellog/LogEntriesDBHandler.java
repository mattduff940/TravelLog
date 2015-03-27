package com.mattcduff.travellog;

/**
 * Created by Matt on 09/03/2015.
 */

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.util.Log;

public class LogEntriesDBHandler extends SQLiteOpenHelper implements BaseColumns {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "logentriesDB.db";

    public static final String TABLE_LOGENTRIES = "logentries";
    //public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_LOCATION = "location";
    public static final String COLUMN_ARRIVED = "arrived";
    public static final String COLUMN_DEPARTED = "departed";
    public static final String COLUMN_FASTTRAIN = "fasttrain";
    public static final String COLUMN_COMMENTS = "comments";

    public LogEntriesDBHandler(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
        Log.d("Log Entries DB Handler", "Database created");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOGENTRIES_TABLE = "CREATE TABLE " +
                TABLE_LOGENTRIES + "(" +
                COLUMN_DATE + " TEXT," +
                COLUMN_LOCATION + " TEXT," +
                COLUMN_ARRIVED + " TEXT," +
                COLUMN_DEPARTED + " TEXT," +
                COLUMN_FASTTRAIN + " TEXT," +
                COLUMN_COMMENTS + " TEXT" + ")";
        db.execSQL(CREATE_LOGENTRIES_TABLE);
        Log.d("Log Entries DB Handler", "Table created");

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_LOGENTRIES);
        onCreate(db);

    }

    public void  addLogEntries (LogEntries logentry) {
        ContentValues values = new ContentValues();

        values.put(COLUMN_DATE, logentry.getDate());
        values.put(COLUMN_LOCATION, logentry.getLocation());
        values.put(COLUMN_ARRIVED, logentry.getArrived());
        values.put(COLUMN_DEPARTED, logentry.getDeparted());
        values.put(COLUMN_FASTTRAIN, logentry.getFastTrain());
        values.put(COLUMN_COMMENTS, logentry.getComments());

        SQLiteDatabase db = this.getWritableDatabase();
        db.insert(TABLE_LOGENTRIES, null, values);
        Log.d("Log Entries DB Handler", "One row inserted");
        db.close();

    }

    public LogEntries findLogEntries (String date) {
        String query = "SELECT * FROM " + TABLE_LOGENTRIES +
                " WHERE " + COLUMN_DATE + " = \"" + date + "\"";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(query,null);
        LogEntries logentries = new LogEntries();

        if (cursor.moveToFirst()) {
            //code in here that can hold multiple entries
        } else {
            logentries = null;
        }
        db.close();
        return logentries;

    }

    //I need to create one or more methods for deleting entries in the database but I'm not sure
    //yet what shape that is going to take

}
