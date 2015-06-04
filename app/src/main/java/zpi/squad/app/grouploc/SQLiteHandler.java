package zpi.squad.app.grouploc;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class SQLiteHandler extends SQLiteOpenHelper {

    private static final String TAG = SQLiteHandler.class.getSimpleName();

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "groupLocDB";

    private static final String TABLE_FRIENDS = "friends";
    private static final String TABLE_NOTIFICATIONS = "notifications";
    private static final String TABLE_MARKERS = "markers";

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

    //Markers Table Columns names
    private static final String KEY_MARK_ID_SQLITE="markerid_internal";
    private static final String KEY_MARK_ID_MYSQL="markerid_extrenal";
    private static final String KEY_USER_ID="uid";
    private static final String KEY_LATITUDE="latitude";
    private static final String KEY_LONGITUDE="longitude";
    private static final String KEY_MARKER_NAME="name";
    private static final String KEY_SAVE_ON_SERVER="saveOnServer";





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

        String CREATE_MARKERS_TABLE = "CREATE TABLE " + TABLE_MARKERS
                + "("
                + KEY_MARK_ID_SQLITE + " INTEGER PRIMARY KEY,"
                +KEY_MARK_ID_MYSQL+" INTEGER,"
                + KEY_USER_ID + " INTEGER,"
                + KEY_LATITUDE + " DOUBLE,"
                + KEY_LONGITUDE + " DOUBLE,"
                +KEY_MARKER_NAME+" TEXT,"
                +KEY_SAVE_ON_SERVER+" INTEGER DEFAULT 0"
                + ")";

        db.execSQL(CREATE_FRIENDS_TABLE);
        db.execSQL(CREATE_NOTIFICATIONS_TABLE);
        db.execSQL(CREATE_MARKERS_TABLE);

        Log.d(TAG, "Database Friends table created");
        Log.d(TAG, "Database Notifications table created");
        Log.d(TAG, "Database Markers table created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIENDS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MARKERS);

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

    public long addMarker(CustomMarker mark)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        String markerid=mark.getMarkerIdMySQL();

        values.put(KEY_MARK_ID_MYSQL,markerid);

        String uid=mark.getUserId();
        values.put(KEY_USER_ID, uid);

        double latitude=mark.getLatitude();
        values.put(KEY_LATITUDE,latitude);

        double longitude=mark.getLongitude();
        values.put(KEY_LONGITUDE,longitude);

        String name=mark.getName();
        values.put(KEY_MARKER_NAME, name);

        Boolean hardSave=mark.isSaveOnServer();
        values.put(KEY_SAVE_ON_SERVER,hardSave);

        long id=db.insert(TABLE_MARKERS,null,values);
        mark.setMarkerIdSQLite(Long.toString(id));
        db.close();

        Log.d(TAG, "New marker inserted into sqlite: " + id);
        return id;
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
                notificationsList.add(0, notificaion);
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
    public void deleteMarkers(){
        SQLiteDatabase db=this.getWritableDatabase();
        db.delete(TABLE_MARKERS,null,null);
        db.close();

        Log.d(TAG, "Deleted all markers from sqlite");
    }

    public List<CustomMarker> getAllMarkers() {
        List<CustomMarker> markersList = new ArrayList<CustomMarker>();

        String selectQuery = "SELECT  * FROM " + TABLE_MARKERS;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {

                String markeridInternal=cursor.getString(0);
                String marekerIdExternal=cursor.getString(1);
                String uid=cursor.getString(2);
                double latitude=cursor.getDouble(3);
                double longitude=cursor.getDouble(4);
                String name=cursor.getString(5);
                boolean hardSave=(cursor.getInt(6) == 1);
                CustomMarker customMarker = new CustomMarker(marekerIdExternal,markeridInternal,uid,latitude,longitude,name);
                customMarker.setSaveOnServer(hardSave);
                // Adding friend to list
                markersList.add(customMarker);
            } while (cursor.moveToNext());
        }

        return markersList;
    }
    public boolean removeMarker(String id)
    {
        Log.d("SQLite delete","Chce usunąć "+id);
        SQLiteDatabase db = this.getWritableDatabase();

        String selectQuery = "SELECT  * FROM " + TABLE_MARKERS;
        Cursor cursor = db.rawQuery(selectQuery, null);
        Log.d("SQLite delete","stan przed usuwaniem");
        if (cursor.moveToFirst()) {
            do {
             Log.d("SQLite delete","SQLite ID "+cursor.getString(0));
            } while (cursor.moveToNext());
        }

        boolean usun=db.delete(TABLE_MARKERS,KEY_MARK_ID_SQLITE+"="+id,null)>0;
        Log.d("SQLite delete","stan po domniemanym  usuwaniu");
        cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                Log.d("SQLite delete","SQLite ID "+cursor.getString(0));
            } while (cursor.moveToNext());
        }

        return usun;
    }
    public void setNotificationChecked(){


    }

}