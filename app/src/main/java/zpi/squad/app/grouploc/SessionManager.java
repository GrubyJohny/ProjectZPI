package zpi.squad.app.grouploc;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class SessionManager {

    private static final String PREF_NAME = "userInfo";
    private static SessionManager sessionManager;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    public static ArrayList<Friend> friends;
    private LatLng currentLocation;


    private SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();

    }

    public static SessionManager getInstance(Context context) {
        if (sessionManager == null)
            sessionManager = new SessionManager(context.getApplicationContext());

        return sessionManager;
    }

    public static SessionManager getInstance() {
        if (sessionManager != null)
            return sessionManager;

        throw new IllegalArgumentException("Should use getInstance(Context) at least once before using this method.");

    }


    private static String TAG = "SessionManager";

    private static int hintsLeft = 3;

    private static String userId = "id";
    private static String userName = "name";
    private static String userEmail = "email";
    private static String userPhoto = "photo";
    private static Boolean userIsLoggedIn = false;
    private static Boolean userIsLoggedByFacebook = false;


    public void setLoggedIn(boolean isLoggedIn) {
        userIsLoggedIn = isLoggedIn;
    }

    public void setUserId(String id) {
        userId = id;
    }

    public void setUserName(String name) {
        userName = name;
    }

    public void setUserEmail(String email) {
        userEmail = email;
    }

    public void setUserPhoto(String photo) {
        userPhoto = photo;
    }

    public void setUserIsLoggedByFacebook(boolean cond) { userIsLoggedByFacebook = cond; }

    public void setUserCurrentLocation(double lat, double lng) { currentLocation = new LatLng(lat, lng); }

    public void setUserCurrentLocation(LatLng locat) { currentLocation = new LatLng(locat.latitude, locat.longitude); }

    public boolean isLoggedByFacebook() { return userIsLoggedByFacebook; }

    public boolean isLoggedIn() { return userIsLoggedIn; }

    public String getUserId() {
        return userId;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserPhoto() {
        return userPhoto;
    }

    public LatLng getCurrentLocation() { return currentLocation; }

    public void logOut() {
        userIsLoggedIn = false;
        userIsLoggedByFacebook = false;
        ParseUser.logOut();
        friends = null;
    }

    public int getHintsLeft() {
        return hintsLeft;
    }

    public void setHintsLeft(int hLeft) {
        hintsLeft = hLeft;
    }

    public ArrayList<Friend> getFriendsList() {
        if (friends == null)
            friends = getFriendsFromParse();
        return friends;
    }

    private static ArrayList<Friend> getFriendsFromParse() {
        ArrayList<Friend> result = new ArrayList<>();

        ParseQuery checkIfFriends1 = new ParseQuery("Friendship");
        checkIfFriends1.whereEqualTo("friend1", ParseUser.getCurrentUser()).orderByAscending("name");
        ParseQuery checkIfFriends2 = new ParseQuery("Friendship");
        checkIfFriends2.whereEqualTo("friend2", ParseUser.getCurrentUser()).orderByAscending("name");

        Object[] friendsList = null, friendsList2 = null;
        ParseObject temp = null;

        try {
            friendsList = checkIfFriends1.find().toArray().clone();

            if (friendsList.length > 0) {
                for (int i = 0; i < friendsList.length; i++) {
                    //to jest typu Friendship
                    temp = ((ParseObject) friendsList[i]);

                    if (temp.get("accepted").toString().equals("true")) {
                        result.add(new Friend(
                                ((ParseUser) temp.get("friend2")).fetchIfNeeded().getObjectId(),
                                ((ParseUser) temp.get("friend2")).fetchIfNeeded().get("name").toString(),
                                ((ParseUser) temp.get("friend2")).fetchIfNeeded().getEmail(),
                                ((ParseUser) temp.get("friend2")).fetchIfNeeded().get("photo") != null ?
                                        ((ParseUser) temp.get("friend2")).fetchIfNeeded().get("photo").toString() : null));
                        Log.d("Friend added: ", ((ParseUser) temp.get("friend2")).fetchIfNeeded().get("name").toString());
                    }

                }

            }


            friendsList2 = checkIfFriends2.find().toArray().clone();

            if (friendsList2.length > 0) {
                for (int i = 0; i < friendsList2.length; i++) {
                    //to jest typu Friendship
                    temp = ((ParseObject) friendsList2[i]);

                    if (temp.get("accepted").toString().equals("true")) {
                        result.add(new Friend(
                                ((ParseUser) temp.get("friend1")).fetchIfNeeded().getObjectId(),
                                ((ParseUser) temp.get("friend1")).fetchIfNeeded().get("name").toString(),
                                ((ParseUser) temp.get("friend1")).fetchIfNeeded().getEmail(),
                                ((ParseUser) temp.get("friend1")).fetchIfNeeded().get("photo") != null ?
                                        ((ParseUser) temp.get("friend1")).fetchIfNeeded().get("photo").toString() : null));
                        Log.d("Friend added: ", ((ParseUser) temp.get("friend1")).fetchIfNeeded().get("name").toString());
                    }

                }
            }

        } catch (ParseException e) {
            Log.e("Parse: ", e.getLocalizedMessage());
            e.printStackTrace();
        } catch (Exception e) {
            Log.e("Exception: ", e.getLocalizedMessage());
            e.printStackTrace();
        }

        return result;
    }

    // method for bitmap to base64
    public String encodeBitmapTobase64(Bitmap image) {
        Bitmap immage = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immage.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);
        //Log.d("Image Log:", imageEncoded);
        return imageEncoded;
    }

    // method for base64 to bitmap
    public Bitmap decodeBase64ToBitmap(String input) {
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory
                .decodeByteArray(decodedByte, 0, decodedByte.length);
    }


}