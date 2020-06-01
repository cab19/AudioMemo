package com.example.audiomemo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    // DATABASE DETAILS
    public static final String DATABASE_NAME = "audiomemo.db";
    public static final String TABLE_NAME = "recordings";
    public static final String COLUMN_ID = "recording_id";
    public static final String COLUMN_FILENAME = "filename";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_TIMESTAMP = "timestamp";
    public static final int DATABASE_VERSION = 3;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // create table
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE "+TABLE_NAME +"("+
                COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                COLUMN_FILENAME+" TEXT, "+
                COLUMN_DESCRIPTION+" TEXT, "+
                COLUMN_TIMESTAMP+" DEFAULT CURRENT_TIMESTAMP)");
    }

    // when DB version changes drop and recreate table
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    // insert recording into db
    public long insertRecording(String filename , String description)
    {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_FILENAME, filename);
        contentValues.put(COLUMN_DESCRIPTION, description);
        long newRowId = sqLiteDatabase.insert(TABLE_NAME, null, contentValues);
        sqLiteDatabase.close();
        return newRowId;
    }

    // return a single recording from db
    public Recording getRecording(long id)
    {
        SQLiteDatabase db = getReadableDatabase(); // create db for reading
        Recording recording = new Recording();

        Cursor cursor = db.query(TABLE_NAME,
                new String[]{COLUMN_ID, COLUMN_FILENAME, COLUMN_DESCRIPTION, COLUMN_TIMESTAMP},
                COLUMN_ID + "=?",
                new String[]{String.valueOf(id)}, null, null, null, null);

        // if data? set recording, otherwise print error
        if (cursor.moveToFirst()) {
            recording.setID(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
            recording.setFilename(cursor.getString(cursor.getColumnIndex(COLUMN_FILENAME)));
            recording.setDescription(cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION)));
            recording.setTimeStamp(cursor.getString(cursor.getColumnIndex(COLUMN_TIMESTAMP)));
        }
        else
            Log.e("SQL","ERROR NO RESULTS");

        db.close(); // destroy db
        return recording; // return recording
    }

    // return a list of all recordings from db
    public List<Recording> getAllRecordings ()
    {
        List<Recording> recordings = new ArrayList<>(); // list to hold recordings
        SQLiteDatabase db = getReadableDatabase(); // create db for reading
        Cursor cursor = db.query(TABLE_NAME,
                new String[]{COLUMN_ID, COLUMN_FILENAME, COLUMN_DESCRIPTION, COLUMN_TIMESTAMP},
                null,
                null, null, null, COLUMN_ID+" DESC", null);

        // loop through results from DB
        if (cursor.moveToFirst()) {
            do {
                Recording recording = new Recording(
                        cursor.getInt(cursor.getColumnIndex(COLUMN_ID)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_FILENAME)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION)),
                        cursor.getString(cursor.getColumnIndex(COLUMN_TIMESTAMP))
                ); // instantiate recording object

                recordings.add(recording);
            } while (cursor.moveToNext());
        }

        db.close(); // destroy db
        return recordings; // return list of recordings
    }

    // update existing recording in db
    public long updateRecording(Recording recording) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_DESCRIPTION, recording.getDescription());
        long response = db.update(TABLE_NAME, contentValues, COLUMN_ID + " = ?",
                new String[]{String.valueOf(recording.getID())});
        db.close();
        return response;
    }

    // delete recording from db
    public void deleteRecording(Recording recording) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, COLUMN_ID + " = ?",
                new String[]{String.valueOf(recording.getID())});
        db.close();
    }
}
