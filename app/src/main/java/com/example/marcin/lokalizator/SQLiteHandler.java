package com.example.marcin.lokalizator;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;

public class SQLiteHandler extends SQLiteOpenHelper {

    private static final String TAG = SQLiteHandler.class.getSimpleName();

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "groupLocDB";

    private static final String TABLE_FRIENDS = "friends";
    private static final String TABLE_NOTIFICATIONS = "notifications";

    // Friends Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_UID = "uid";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";

    // Notifications Table Columns names
    private static final String KEY_NOT_ID = "id";
    private static final String KEY_SENDER_ID = "senderid";
    private static final String KEY_SENDER_NAME = "senderName";
    private static final String KEY_SENDER_EMAIL = "senderEmail";
    private static final String KEY_RECEIVER_ID = "receiverid";
    private static final String KEY_TYPE = "type";
    private static final String KEY_MESSAGE_ID = "messageid";
    private static final String KEY_GROUP_ID = "groupid";
    private static final String KEY_CREATED_AT = "created_at";
    private static final String KEY_CHECKED = "checked";




    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_FRIENDS_TABLE = "CREATE TABLE " + TABLE_FRIENDS
                + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_UID + " TEXT,"
                + KEY_NAME + " TEXT,"
                + KEY_EMAIL + " TEXT UNIQUE"
                + ")";

        String CREATE_NOTIFICATIONS_TABLE = "CREATE TABLE " + TABLE_NOTIFICATIONS
                + "("
                + KEY_NOT_ID + " INTEGER PRIMARY KEY,"
                + KEY_SENDER_ID + " TEXT,"
                + KEY_SENDER_NAME + " TEXT,"
                + KEY_SENDER_EMAIL + " TEXT,"
                + KEY_RECEIVER_ID + " TEXT,"
                + KEY_TYPE + " TEXT,"
                + KEY_MESSAGE_ID + " TEXT,"
                + KEY_GROUP_ID + " TEXT,"
                + KEY_CREATED_AT + " TEXT,"
                + KEY_CHECKED + " INTEGER"
                + ")";

        db.execSQL(CREATE_FRIENDS_TABLE);
        db.execSQL(CREATE_NOTIFICATIONS_TABLE);

        Log.d(TAG, "Database Friends table created");
        Log.d(TAG, "Database Notifications table created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIENDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);

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


    public Friend getFriendDetails(int id) {
        Friend friend = new Friend();
        String selectQuery = "SELECT * FROM " + TABLE_FRIENDS + "WHERE uid = " + id;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        cursor.moveToFirst();

            friend.setFriendID(Integer.parseInt(cursor.getString(1)));
            friend.setFriendName(cursor.getString(2));
            friend.setFriendEmail(cursor.getString(3));

        cursor.close();
        db.close();

        return friend;
    }

    public int getRowCount(String table) {
        String countQuery = "SELECT  * FROM " + table;
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
                friend.setFriendID(Integer.parseInt(cursor.getString(1)));
                friend.setFriendName(cursor.getString(2));
                friend.setFriendEmail(cursor.getString(3));


                // Adding friend to list
                friendsList.add(friend);
            } while (cursor.moveToNext());
        }

        return friendsList;
    }

    public void addNotification(String senderid, String senderName, String senderEmail, String receiverid, String type, String messageid, String groupid, String createdAt, int checked) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_SENDER_ID, senderid);
        values.put(KEY_SENDER_NAME, senderName);
        values.put(KEY_SENDER_EMAIL, senderEmail);
        values.put(KEY_RECEIVER_ID, receiverid);
        values.put(KEY_TYPE, type);
        values.put(KEY_MESSAGE_ID, messageid);
        values.put(KEY_GROUP_ID, groupid);
        values.put(KEY_CREATED_AT, createdAt);
        values.put(KEY_CHECKED, checked);

        long id = db.insert(TABLE_NOTIFICATIONS, null, values);
        db.close();

        if(checked == 0) {
            Log.d(TAG, "New notification inserted into sqlite: " + id);
        }
        else{
            Log.d(TAG, "Old notification inserted into sqlite: " + id);
        }

    }


    public ArrayList<Notification> getAllNotifications() {
        ArrayList<Notification> notificationsList = new ArrayList<Notification>();

        String selectQuery = "SELECT  * FROM " + TABLE_NOTIFICATIONS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Notification notificaion = new Notification(cursor.getString(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7), cursor.getString(8),cursor.getInt(6));

                // Adding notification to list
                notificationsList.add(notificaion);
            } while (cursor.moveToNext());
        }

        return notificationsList;
    }

    public void deleteNotifications() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NOTIFICATIONS, null, null);
        db.close();

        Log.d(TAG, "Deleted all notifications info from sqlite");
    }

}