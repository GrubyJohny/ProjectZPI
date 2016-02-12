package zpi.squad.app.grouploc.domain;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

/**
 * Created by karol_000 on 2015-05-11.
 */
public class Friend {

    String uid;
    String name;
    String email;
    String photo;
    ParseGeoPoint location;
    private ParseUser parseUser;

    public Friend() {
    }

    public Friend(String uid, String name, String email, String photo, ParseGeoPoint location) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.photo = photo;
        this.location = location;
    }

    public Friend(String uid, String name, String email, String photo, double lat, double lon) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.photo = photo;
        this.location = new ParseGeoPoint(lat, lon);
    }

    public Friend(String uid, String name, String email, String photo, double lat, double lon, ParseUser pUser) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.photo = photo;
        this.location = new ParseGeoPoint(lat, lon);
        this.parseUser = pUser;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public ParseGeoPoint getLocation() {
        return location;
    }

    public void setLocation(ParseGeoPoint location) {
        this.location = location;
    }

    public ParseUser getParseUser() {
        return parseUser;
    }

    public void setParseUser(ParseUser parseUser) {
        this.parseUser = parseUser;
    }
}