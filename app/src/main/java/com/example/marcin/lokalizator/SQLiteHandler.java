package com.example.marcin.lokalizator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;

public class SQLiteHandler extends SQLiteOpenHelper {

    private static final String TAG = SQLiteHandler.class.getSimpleName();

    private static final int DATABASE_VERSION = 1;

    private static final String DATABASE_NAME = "groupLocDB";

    private static final String TABLE_FRIENDS = "friends";

    // Friends Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_UID = "uid";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";

    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_FRIENDS
                + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_UID + " TEXT,"
                + KEY_NAME + " TEXT,"
                + KEY_EMAIL + " TEXT UNIQUE"
                + ")";

        db.execSQL(CREATE_LOGIN_TABLE);

        Log.d(TAG, "Database Friends table created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIENDS);

        onCreate(db);
    }

    public void addFriend(String uid, String name, String email) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_UID, uid);
        values.put(KEY_NAME, name);
        values.put(KEY_EMAIL, email);

        long id = db.insert(TABLE_FRIENDS, null, values);
        db.close();

        Log.d(TAG, "New friend inserted into sqlite: " + id);


    }


    public HashMap<String, String> getFriendDetails() {
        HashMap<String, String> friend = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_FRIENDS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            friend.put("uid", cursor.getString(1));
            friend.put("name", cursor.getString(2));
            friend.put("email", cursor.getString(3));
        }
        cursor.close();
        db.close();

        Log.d(TAG, "Fetching friend from Sqlite: " + friend.toString());

        return friend;
    }

    public int getRowCount() {
        String countQuery = "SELECT  * FROM " + TABLE_FRIENDS;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);
        int rowCount = cursor.getCount();
        db.close();
        cursor.close();

        return rowCount;
    }

    public  String getId() {
                String countQuery = "SELECT "+ KEY_UID + " FROM " + TABLE_FRIENDS;
                SQLiteDatabase db = this.getReadableDatabase();
                Cursor cursor = db.rawQuery(countQuery, null);
                cursor.moveToFirst();
                String ID_U= cursor.getString(0);
                db.close();
                cursor.close();

            return ID_U;
    }




    public void deleteUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FRIENDS, null, null);
        db.close();

        Log.d(TAG, "Deleted all friends info from sqlite");
    }

    public ArrayList<Friend> getAllFriends() {
        ArrayList<Friend> friendsList = new ArrayList<Friend>();

        String selectQuery = "SELECT  * FROM " + TABLE_FRIENDS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Friend friend = new Friend();
                friend.setFriendID(Integer.parseInt(cursor.getString(0)));
                friend.setFriendName(cursor.getString(1));
                friend.setFriendEmail(cursor.getString(2));
                // Adding contact to list
                friendsList.add(friend);
            } while (cursor.moveToNext());
        }

        return friendsList;
    }


}