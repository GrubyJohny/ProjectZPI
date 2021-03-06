package zpi.squad.app.grouploc;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import zpi.squad.app.grouploc.domains.Friend;
import zpi.squad.app.grouploc.domains.MyMarker;
import zpi.squad.app.grouploc.domains.Notification;
import zpi.squad.app.grouploc.fragments.MapFragment;
import zpi.squad.app.grouploc.helpers.CommonMethods;

public class SessionManager {

    private Context context;
    private static final String PREF_NAME = "userInfo";
    private static SessionManager sessionManager;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private static ArrayList<Friend> friends;
    private static ArrayList<Notification> notifications;
    private static ArrayList<MyMarker> markers;
    private LatLng currentLocation;
    public boolean requestLocationUpdate = true;
    boolean isFriend;
    private int refreshFriendsPositionInterval = 10; //w sekundach
    private static HashMap<MarkerOptions, MyMarker> ownMarkers = new HashMap<>();       //własne markery
    private static HashMap<MarkerOptions, MyMarker> sharedMarkers = new HashMap<>();     //otrzymane od znajomych
    private static HashMap<MarkerOptions, Friend> friendsMarkers = new HashMap<>();     //znajomi


    private SessionManager(Context context) {
        pref = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = pref.edit();
        this.context = context;
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

    public void setUserIsLoggedByFacebook(boolean cond) {
        userIsLoggedByFacebook = cond;
    }

    public void setUserCurrentLocation(double lat, double lng) {
        currentLocation = new LatLng(lat, lng);
    }

    public void setUserCurrentLocation(LatLng locat) {
        currentLocation = new LatLng(locat.latitude, locat.longitude);
    }

    public boolean isLoggedByFacebook() {
        return userIsLoggedByFacebook;
    }

    public boolean isLoggedIn() {
        return userIsLoggedIn;
    }

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

    public LatLng getCurrentLocation() {
        return currentLocation;
    }

    public void logOut() {
        userIsLoggedIn = false;
        userIsLoggedByFacebook = false;
        ParseUser.logOut();
        friends = null;
        requestLocationUpdate = false;
        notifications = null;
        markers = null;
        try {
            MapFragment.getMap().clear();
        } catch (Exception e) {
        }
    }

    public ArrayList<Friend> getFriendsList() {
        if (friends == null)
            friends = getFriendsFromParse();
        return friends;
    }

    public ArrayList<Notification> getNotificationsList() {
        if (notifications == null)
            notifications = getNotificationsFromParse();
        return notifications;
    }

    private HashMap<MarkerOptions, Friend> getFriendsMarkersFromParse() {

        HashMap<MarkerOptions, Friend> result = new HashMap<>();
        ArrayList<Friend> friends = getFriendsFromParse();

        for (int i = 0; i < friends.size(); i++)
            result.put(new MarkerOptions()
                            .position(new LatLng(friends.get(i).getLocation().getLatitude(), friends.get(i).getLocation().getLongitude()))
                            .snippet("friends")
                            .title(friends.get(i).getName())
                            .icon(BitmapDescriptorFactory.fromBitmap(getMarkerBitmapFromView(CommonMethods.getInstance().decodeBase64ToBitmap(friends.get(i).getPhoto()))))
                            .visible(true)
                            .draggable(false),
                    friends.get(i));

        friendsMarkers = result;

        return result;
    }

    private Bitmap getMarkerBitmapFromView(Bitmap bitmap) {
        View customMarkerView = ((LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.view_custom_marker, null);
        ImageView markerImageView = (ImageView) customMarkerView.findViewById(R.id.marker_image);

        markerImageView.setImageBitmap(bitmap);
        customMarkerView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        customMarkerView.layout(0, 0, customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight());
        customMarkerView.buildDrawingCache();

        Bitmap returnedBitmap = Bitmap.createBitmap(customMarkerView.getMeasuredWidth(), customMarkerView.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(returnedBitmap);
        canvas.drawColor(Color.WHITE, PorterDuff.Mode.SRC_IN);

        Drawable drawable = customMarkerView.getBackground();
        if (drawable != null)
            drawable.draw(canvas);
        customMarkerView.draw(canvas);

        return returnedBitmap;
    }

    private void getOwnMarkersFromParse() {

        ArrayList<MyMarker> myMarkersResult = new ArrayList<>();
        ownMarkers.clear();

        ParseQuery markers = new ParseQuery("Marker");
        markers.whereEqualTo("owner", ParseUser.getCurrentUser());

        Object[] markersList = null;

        try {
            markersList = markers.find().toArray().clone();

            if (markersList.length > 0) {
                for (int i = 0; i < markersList.length; i++)
                    myMarkersResult.add(new MyMarker(((ParseObject) markersList[i]).getObjectId(),
                            ((ParseObject) markersList[i]).getString("name"),
                            (((ParseObject) markersList[i]).getParseUser("owner")),
                            (((ParseObject) markersList[i]).getParseGeoPoint("localization"))
                            //      ,((ParseObject) markersList[i]).getString("icon")==null? jakieafaultowyObrazek : CommonMethods.getInstance().decodeBase64ToBitmap(((ParseObject) markersList[i]).getString("icon"))
                    ));


                for (int i = 0; i < myMarkersResult.size(); i++) {
                    ownMarkers.put(new MarkerOptions()
                            .title(myMarkersResult.get(i).getName())
                            .position(new LatLng(myMarkersResult.get(i).getLocalization().getLatitude(), myMarkersResult.get(i).getLocalization().getLongitude()))
                            .snippet("own")
                            .visible(true)
                            .draggable(false), myMarkersResult.get(i));
                }


            } else
                Log.e("There are any ", "markers for current user.");
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    private void getSharedMarkersFromParse() {

        ArrayList<MyMarker> sharedMarkersResult = new ArrayList<>();
        sharedMarkers.clear();

        ParseQuery markers = new ParseQuery("SharedMarker");
        markers.whereEqualTo("sharedUser", ParseUser.getCurrentUser());

        Object[] markersList = null;

        try {
            markersList = markers.find().toArray().clone();

            if (markersList.length > 0) {
                for (int i = 0; i < markersList.length; i++)
                    sharedMarkersResult.add(new MyMarker(((ParseObject) markersList[i]).fetchIfNeeded().getParseObject("marker").fetchIfNeeded().getObjectId(),
                            ((ParseObject) markersList[i]).fetchIfNeeded().getParseObject("marker").fetchIfNeeded().getString("name"),
                            (((ParseObject) markersList[i]).fetchIfNeeded().getParseObject("marker").fetchIfNeeded().getParseUser("owner")),
                            (((ParseObject) markersList[i]).fetchIfNeeded().getParseObject("marker").fetchIfNeeded().getParseGeoPoint("localization"))
                            //      ,((ParseObject) markersList[i]).getString("icon")==null? jakieafaultowyObrazek : CommonMethods.getInstance().decodeBase64ToBitmap(((ParseObject) markersList[i]).getString("icon"))
                    ));


                for (int i = 0; i < sharedMarkersResult.size(); i++) {
                    sharedMarkers.put(new MarkerOptions()
                            .title(sharedMarkersResult.get(i).getName())
                            .position(new LatLng(sharedMarkersResult.get(i).getLocalization().getLatitude(), sharedMarkersResult.get(i).getLocalization().getLongitude()))
                            .snippet("from " + sharedMarkersResult.get(i).getOwner().fetchIfNeeded().get("name"))
                            .visible(true)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.mapmarkerhiblue))
                            .draggable(false), sharedMarkersResult.get(i));
                }


            } else
                Log.e("There are any ", "markers for current user.");
        } catch (ParseException e) {
            e.printStackTrace();
        }

    }

    public HashMap<MarkerOptions, MyMarker> getOwnMarkers() {
        //if (ownMarkers == null)
            getOwnMarkersFromParse();

        return ownMarkers;
    }

    public HashMap<MarkerOptions, MyMarker> getSharedMarkers() {
        //if(sharedMarkers == null)
        getSharedMarkersFromParse();

        return sharedMarkers;
    }

    public HashMap<MarkerOptions, Friend> getFriendsMarkers() {
        getFriendsMarkersFromParse();

        return friendsMarkers;
    }

    public void refreshOwnMarkers() {
        getOwnMarkersFromParse();
    }

    public void refreshSharedMarkers() {
        getOwnMarkersFromParse();
    }



    public ArrayList<MyMarker> getRefreshedFriendsMarkers() {
        ArrayList<MyMarker> result = new ArrayList<>();
        ArrayList<Friend> friends = getFriendsFromParse();

        for (int i = 0; i < friends.size(); i++)
            result.add(new MyMarker(friends.get(i).getUid(), friends.get(i).getName(), friends.get(i).getParseUser(), friends.get(i).getLocation(), CommonMethods.getInstance().decodeBase64ToBitmap(friends.get(i).getPhoto())));

        Log.e("FRIENDS MARKERS: ", "" + friends.size());

        return result;
    }


    private ArrayList<Notification> getNotificationsFromParse() {
        ArrayList<Notification> result = new ArrayList<>();

        ParseQuery notifications = new ParseQuery("Notification");
        notifications.whereEqualTo("receiverEmail", ParseUser.getCurrentUser().getEmail());
        notifications.orderByDescending("createdAt");

        Object[] notifList = null;

        try {
            notifList = notifications.find().toArray().clone();

            if (notifList.length > 0) {
                for (int i = 0; i < notifList.length; i++)
                    result.add(new Notification(((ParseObject) notifList[i]).getObjectId(),
                            ((ParseObject) notifList[i]).getString("senderName"),
                            ((ParseObject) notifList[i]).getString("senderEmail"),
                            (((ParseObject) notifList[i]).getInt("kindOfNotification")),
                            ((ParseObject) notifList[i]).getString("extra"),
                            ((ParseObject) notifList[i]).getCreatedAt().toLocaleString(),
                            ((ParseObject) notifList[i]).getBoolean("markedAsRead")));
            } else
                Log.e("There are any ", "notifications for current user.");
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return result;
    }

    private static ArrayList<Friend> getFriendsFromParse() {
        ArrayList<Friend> result = new ArrayList<>();

        ParseQuery checkIfFriends1 = new ParseQuery("Friendship");
        checkIfFriends1.whereEqualTo("friend1", ParseUser.getCurrentUser());
        ParseQuery checkIfFriends2 = new ParseQuery("Friendship");
        checkIfFriends2.whereEqualTo("friend2", ParseUser.getCurrentUser());

        Object[] friendsList = null, friendsList2 = null;
        ParseObject temp = null;

        try {
            friendsList = checkIfFriends1.find().toArray().clone();

            if (friendsList.length > 0) {
                for (int i = 0; i < friendsList.length; i++) {
                    //to jest typu Friendship
                    temp = ((ParseObject) friendsList[i]);

                    if (temp.get("accepted").toString().equals("true")) {

                        ParseUser actual = ((ParseUser) temp.get("friend2")).fetchIfNeeded();
                        ParseGeoPoint point = (ParseGeoPoint) actual.get("location");

                        result.add(new Friend(
                                actual.getObjectId(),
                                actual.get("name").toString(),
                                actual.getEmail(),
                                actual.get("photo") != null ? actual.get("photo").toString() : null,
                                point.getLatitude(), point.getLongitude(), actual, false));

                        Log.d("Friend added: ", "" + actual.get("name").toString() + " " + point.getLatitude() + ", " + point.getLongitude());
                    }
                }
            }
            friendsList2 = checkIfFriends2.find().toArray().clone();

            if (friendsList2.length > 0) {
                for (int i = 0; i < friendsList2.length; i++) {
                    //to jest typu Friendship
                    temp = ((ParseObject) friendsList2[i]);

                    if (temp.get("accepted").toString().equals("true")) {

                        ParseUser actual = ((ParseUser) temp.get("friend1")).fetchIfNeeded();
                        ParseGeoPoint point = (ParseGeoPoint) actual.get("location");

                        result.add(new Friend(
                                actual.getObjectId(),
                                actual.get("name").toString(),
                                actual.getEmail(),
                                actual.get("photo") != null ? actual.get("photo").toString() : null,
                                point.getLatitude(), point.getLongitude(), actual, false));
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

    public ArrayList<Friend> getAllUsersWithoutCurrentFromParse() {
        List<ParseUser> users = new ArrayList<>();
        ArrayList<Friend> result = new ArrayList<>();

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.addAscendingOrder("name");
        query.whereNotEqualTo("name_lowercase", ParseUser.getCurrentUser().get("name_lowercase"));
        //query.clearCachedResult();
        //query.setLimit(10);

        try {
            users = query.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        for (int i = 0; i < users.size(); i++) {
            result.add(new Friend(
                    users.get(i).getObjectId(),
                    users.get(i).get("name").toString(),
                    users.get(i).getEmail(),
                    users.get(i).get("photo") != null ? users.get(i).get("photo").toString() : null));
        }

        return result;
    }

    public ArrayList<Friend> getAllUsersFromParseWithoutCurrentAndFriends() {
        List<ParseUser> users = new ArrayList<>();
        ArrayList<Friend> result = new ArrayList<>();

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereNotEqualTo("email", ParseUser.getCurrentUser().getEmail());
        query.addAscendingOrder("name_lowercase");

        try {
            users = query.find();
        } catch (ParseException e) {
            e.printStackTrace();
        }

        ArrayList<Friend> fr = getFriendsList();
        isFriend = false;

        for (int i = 0; i < users.size(); i++) {
            isFriend = false;
            for (int j = 0; j < fr.size(); j++) {

                if (fr.get(j).getEmail().equals(users.get(i).getEmail()))
                    isFriend = true;
            }

            if (!isFriend)
                result.add(new Friend(
                        users.get(i).getObjectId(),
                        users.get(i).get("name").toString(),
                        users.get(i).getEmail(),
                        users.get(i).get("photo") != null ? users.get(i).get("photo").toString() : null));
        }

        return result;
    }

    public void refreshFriendsList() {
        friends = getFriendsFromParse();
    }

    public void refreshNotificationsList() {
        notifications = getNotificationsFromParse();
    }


    public static ArrayList<Friend> getNotAcceptedFriendsFromParse() {
        ArrayList<Friend> result = new ArrayList<>();

        ParseQuery checkIfFriends1 = new ParseQuery("Friendship");
        checkIfFriends1.whereEqualTo("friend1", ParseUser.getCurrentUser());
        ParseQuery checkIfFriends2 = new ParseQuery("Friendship");
        checkIfFriends2.whereEqualTo("friend2", ParseUser.getCurrentUser());

        Object[] friendsList = null, friendsList2 = null;
        ParseObject temp = null;

        try {
            friendsList = checkIfFriends1.find().toArray().clone();

            if (friendsList.length > 0) {
                for (int i = 0; i < friendsList.length; i++) {
                    //to jest typu Friendship
                    temp = ((ParseObject) friendsList[i]);

                    if (temp.get("accepted").toString().equals("false")) {

                        ParseUser actual = ((ParseUser) temp.get("friend2")).fetchIfNeeded();
                        ParseGeoPoint point = (ParseGeoPoint) actual.get("location");

                        result.add(new Friend(
                                actual.getObjectId(),
                                actual.get("name").toString(),
                                actual.getEmail(),
                                actual.get("photo") != null ? actual.get("photo").toString() : null,
                                point.getLatitude(), point.getLongitude(), actual, true));

                        Log.d("Friend added: ", "" + actual.get("name").toString() + " " + point.getLatitude() + ", " + point.getLongitude());
                    }
                }
            }
            friendsList2 = checkIfFriends2.find().toArray().clone();

            if (friendsList2.length > 0) {
                for (int i = 0; i < friendsList2.length; i++) {
                    //to jest typu Friendship
                    temp = ((ParseObject) friendsList2[i]);

                    if (temp.get("accepted").toString().equals("false")) {

                        ParseUser actual = ((ParseUser) temp.get("friend1")).fetchIfNeeded();
                        ParseGeoPoint point = (ParseGeoPoint) actual.get("location");

                        result.add(new Friend(
                                actual.getObjectId(),
                                actual.get("name").toString(),
                                actual.getEmail(),
                                actual.get("photo") != null ? actual.get("photo").toString() : null,
                                point.getLatitude(), point.getLongitude(), actual, true));
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

    public static ArrayList<Friend> getUsersWithoutCurrentFriendsAndWithGrayAlmostFriends() {
        ArrayList<Friend> searchFriendsList = new ArrayList<>();
        List<Friend> all = SessionManager.getInstance().getAllUsersFromParseWithoutCurrentAndFriends();
        List<Friend> notAccepted = SessionManager.getNotAcceptedFriendsFromParse();
        boolean notAcceptedAlready;
        for (int i = 0; i < all.size(); i++) {
            notAcceptedAlready = false;
            for (int j = 0; j < notAccepted.size(); j++) {
                if (all.get(i).getEmail().equals(notAccepted.get(j).getEmail())) {
                    all.get(i).alreadyInvited = true;
                }
            }

            searchFriendsList.add(all.get(i));

        }
        return searchFriendsList;
    }
}