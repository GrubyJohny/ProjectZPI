package zpi.squad.app.grouploc;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.facebook.login.LoginManager;

public class SessionManager {

    private static String TAG = SessionManager.class.getSimpleName();

    SharedPreferences pref;
    SQLiteHandler db;
    Editor editor;
    Context _context;

    int PRIVATE_MODE = 0;

    private static int hintsLeft = 3;

    private static final String PREF_NAME = "userInfo";

    private static final String KEY_IS_LOGGEDIN = "isLoggedIn";
    private static final String KEY_UID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_HINTS = "hints";

    private static final String KEY_GROUP_ID = "gid";
    private static final String KEY_GROUP_NAME = "gname";


    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
        db = new SQLiteHandler(context);

    }

    public void setLogin(boolean isLoggedIn) {

        editor.putBoolean(KEY_IS_LOGGEDIN, isLoggedIn);

        editor.commit();

        Log.d(TAG, "User login session modified!");
    }

    public void setKeyUid(String id){
        editor.putString(KEY_UID, id);
        editor.commit();
        Log.d(TAG, "User Id putted into SharedPreferences!");
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
        Log.d(TAG, "User name putted into SharedPreferences!");
    }
    public void setKeyEmail(String email){
        editor.putString(KEY_EMAIL, email);
        editor.commit();
        Log.d(TAG, "User email putted into SharedPreferences!");
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

    public void logOut(){
        editor.putBoolean(KEY_IS_LOGGEDIN, false);
        editor.putString(KEY_UID, "");
        editor.putString(KEY_NAME, "");
        editor.putString(KEY_EMAIL, "");
        editor.commit();
        db.deleteUsers();
        db.deleteNotifications();
        db.deleteMarkers();
        LoginManager.getInstance().logOut();
        Log.d(TAG, "User info removed from SharedPreferences!");

    }

    public int getHintsLeft() {
        return pref.getInt(KEY_HINTS, hintsLeft);
    }

    public void setHintsLeft(int hintsLeft) {
        editor.putInt(KEY_HINTS, hintsLeft);
        editor.commit();
        Log.d(TAG, "Hints left put into Shared Preferences");
    }
}