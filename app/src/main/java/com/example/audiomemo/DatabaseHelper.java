package com.example.audiomemo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

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

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE "+TABLE_NAME +"("+
                COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+
                COLUMN_FILENAME+" TEXT, "+
                COLUMN_DESCRIPTION+" TEXT, "+
                COLUMN_TIMESTAMP+" DEFAULT CURRENT_TIMESTAMP)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(sqLiteDatabase);
    }

    public long insert (String filename , String description)
    {
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COLUMN_FILENAME, filename);
        contentValues.put(COLUMN_DESCRIPTION, description);
        long newRowId = sqLiteDatabase.insert(TABLE_NAME, null, contentValues);
        sqLiteDatabase.close();
        return newRowId;
    }

    public List<Recording> getRecordings ()
    {

        List<Recording> recordings = new ArrayList<>();

        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME;
                //+ " ORDER BY " + Note.COLUMN_TIMESTAMP + " DESC";

        SQLiteDatabase db = getReadableDatabase(); // create db for reading
        Cursor cursor = db.rawQuery(selectQuery, null); // execute query

        // loop through results from DB
        if (cursor.moveToFirst()) {
            do {
                Recording recording = new Recording(); // instantiate recording object to hold details
                recording.setID(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                recording.setFilename(cursor.getString(cursor.getColumnIndex(COLUMN_FILENAME)));
                recording.setDescription(cursor.getString(cursor.getColumnIndex(COLUMN_DESCRIPTION)));
                recording.setTimeStamp(cursor.getString(cursor.getColumnIndex(COLUMN_TIMESTAMP)));

                Log.e("SQL: ", recording.getFilename());

                //note.setId(cursor.getInt(cursor.getColumnIndex(Note.COLUMN_ID)));
                //note.setNote(cursor.getString(cursor.getColumnIndex(Note.COLUMN_NOTE)));
                //note.setTimestamp(cursor.getString(cursor.getColumnIndex(Note.COLUMN_TIMESTAMP)));

                recordings.add(recording);
            } while (cursor.moveToNext());
        }

        db.close(); // destroy db
        return recordings; // return list of recordings

        /*

        SQLiteDatabase db = getReadableDatabase();
        String[] columns = {COLUMN_ID};
        String selection = "*";
        db.query(TABLE_NAME, columns,
        //Cursor cursor = db.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null);

        /*
        String selection = COLUMN_USER + "= ? and " + COLUMN_PWD +"=?";
        String[] selectionArgs = {username, password};
        Cursor cursor = db.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null);
        int numberOfRows = cursor.getCount();
        cursor.close();
        db.close();

        if (numberOfRows > 0)
            return true;
        else
            return false;
        */


        //List<Recording> sourceList = new ArrayList<>(); // create list of news source objects
    }
}
