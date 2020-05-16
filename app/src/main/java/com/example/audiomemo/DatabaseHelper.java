package com.example.audiomemo;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "audiomemo.db";
    public static final String TABLE_NAME = "recordings";
    public static final String COLUMN_ID = "recording_id";
    public static final String COLUMN_FILENAME = "filename";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL("CREATE TABLE "+TABLE_NAME+"("+COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT, "+COLUMN_FILENAME+" TEXT, "+COLUMN_DESCRIPTION+" TEXT)");
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

    public boolean fetchUser (String username , String password)
    {
        SQLiteDatabase db = getReadableDatabase();
        String[] columns = {COLUMN_ID};
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
        return false;
    }
}
