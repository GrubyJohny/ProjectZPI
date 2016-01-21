package zpi.squad.app.grouploc;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;

public class SessionManager {

    private static final String PREF_NAME = "userInfo";
    private static SessionManager sessionManager;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    public static ArrayList<Friend> friends;


    private SessionManager(Context context){
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();

    }

    public static SessionManager getInstance(Context context)
    {
        if(sessionManager == null)
            sessionManager = new SessionManager(context.getApplicationContext());

        return sessionManager;
    }

    public static SessionManager getInstance()
    {
        if(sessionManager != null)
            return sessionManager;

        throw new IllegalArgumentException("Should use getInstance(Context) at least once before using this method.");

    }


    private static String TAG = "SessionManager";

    //to jest już w sumie nieużywane, ale na razie zostaje
    private SQLiteHandler db;

    private static int hintsLeft = 3;



    private static final String KEY_IS_LOGGEDIN = "isLoggedIn";

    private static final String KEY_UID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_PHOTO = "photo";

    private static final String KEY_HINTS = "hints";

    private static final String KEY_GROUP_ID = "gId";
    private static final String KEY_GROUP_NAME = "gName";
    private static final String KEY_GROUP_ADMIN_ID = "gAdminId";
    private static final String KEY_GROUP_ADMIN_NAME = "gAdminName";


    public void setLogin(boolean isLoggedIn) {

        editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);

        editor.commit();

        Log.d(TAG, "User login session modified!");
    }

    public void setKeyUid(String id){
        editor.putString(KEY_UID, id);
        editor.commit();
        Log.d(TAG, "User Id putted into SharedPreferences!" + " UserId: " + id);
    }

    public void setKeyGroupId(String gid){
        editor.putString(KEY_GROUP_ID, gid);
        editor.commit();
        Log.d(TAG, "Group Id putted into SharedPreferences!");
    }

    public void setKeyGroupName(String name){
        editor.putString(KEY_GROUP_NAME, name);
        editor.commit();
        Log.d(TAG, "Group name putted into SharedPreferences!");
    }

    public void setKeyName(String name){
        editor.putString(KEY_NAME, name);
        editor.commit();
        Log.d(TAG, "User name putted into SharedPreferences!" + " UserName: " + name);
    }

    public void setKeyEmail(String email){
        editor.putString(KEY_EMAIL, email);
        editor.commit();
        Log.d(TAG, "User email putted into SharedPreferences!" + " UserEmail: " + email);
    }

    public void setKeyPhoto(String photo){
        editor.putString(KEY_PHOTO, photo);
        editor.commit();
        Log.d(TAG, "User photo putted into SharedPreferences!");
    }

    public boolean isLoggedIn(){

        return pref.getBoolean(KEY_IS_LOGGEDIN, false);
    }

    public String getUserId(){

        return pref.getString(KEY_UID, "Error: There's no User ID in Shared Preferences");
    }

    public String getUserName(){

        return pref.getString(KEY_NAME, "Error: There's no User name in Shared Preferences");
    }

    public String getUserEmail(){

        return pref.getString(KEY_EMAIL, "Error: There's no User email in Shared Preferences");
    }

    public String getUserPhoto(){

        return pref.getString(KEY_PHOTO, "Error: There's no User email in Shared Preferences");
    }

    public void logOut(){
        editor.commit();
        editor.clear();
        editor.commit();
        Log.d(TAG, "User info removed from SharedPreferences!");

        ParseUser.logOut();
        friends = null;

        //db.deleteUsers();
        //db.deleteNotifications();
        //db.deleteMarkers();


    }

    public int getHintsLeft() {
        return pref.getInt(KEY_HINTS, hintsLeft);
    }

    public void setHintsLeft(int hintsLeft) {
        editor.putInt(KEY_HINTS, hintsLeft);
        editor.commit();
        Log.d(TAG, "Hints left put into Shared Preferences");
    }

    public ArrayList<Friend> getFriendsList()
    {
        if(friends == null)
            friends = getFriendsFromParse();
        return friends;
    }
    private static ArrayList<Friend> getFriendsFromParse() {
        ArrayList<Friend> result = new ArrayList<>();

        ParseQuery ckechIfFriends1 = new ParseQuery("Friendship");
        ckechIfFriends1.whereEqualTo("friend1", ParseUser.getCurrentUser());
        ParseQuery checkIfFriends2 = new ParseQuery("Friendship");
        checkIfFriends2.whereEqualTo("friend2", ParseUser.getCurrentUser());

        Object[] friendsList = null, friendsList2 = null;
        ParseObject temp = null;

        try
        {
            friendsList = ckechIfFriends1.find().toArray().clone();

            if(friendsList.length>0)
            {
                for(int i=0; i<friendsList.length; i++)
                {
                    //to jest typu Friendship
                    temp = ((ParseObject) friendsList[i]);

                    if(temp.get("accepted").toString().equals("true"))
                    {
                        result.add(new Friend(
                                ((ParseUser) temp.get("friend2")).fetchIfNeeded().getObjectId(),
                                ((ParseUser) temp.get("friend2")).fetchIfNeeded().get("name").toString(),
                                ((ParseUser) temp.get("friend2")).fetchIfNeeded().getEmail(),
                                ((ParseUser) temp.get("friend2")).fetchIfNeeded().get("photo") != null ?
                                        ((ParseUser) temp.get("friend2")).fetchIfNeeded().get("photo").toString():null));
                        Log.e("Friend added", "probably successfully");
                    }

                }

            }


            friendsList2 = checkIfFriends2.find().toArray().clone();

            if(friendsList2.length>0)
            {
                for(int i=0; i<friendsList2.length; i++)
                {
                    //to jest typu Friendship
                    temp = ((ParseObject) friendsList2[i]);

                    if(temp.get("accepted").toString().equals("true"))
                    {
                        result.add(new Friend(
                                ((ParseUser) temp.get("friend1")).fetchIfNeeded().getObjectId(),
                                ((ParseUser) temp.get("friend1")).fetchIfNeeded().get("name").toString(),
                                ((ParseUser) temp.get("friend1")).fetchIfNeeded().getEmail(),
                                ((ParseUser) temp.get("friend1")).fetchIfNeeded().get("photo") != null ?
                                        ((ParseUser) temp.get("friend1")).fetchIfNeeded().get("photo").toString():null));
                        Log.e("Friend added", "probably successfully");
                    }

                }
            }

        }
        catch (ParseException e)
        {
            Log.e("Parse: ", e.getLocalizedMessage());
            e.printStackTrace();
        }
        catch (Exception e)
        {
            Log.e("Exception: ", e.getLocalizedMessage());
            e.printStackTrace();
        }

        return result;
    }


}