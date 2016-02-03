package zpi.squad.app.grouploc;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseGeoPoint;

/**
 * Created by karol_000 on 2015-05-11.
 */
public class Friend {

    String uid;
    String name;
    String email;
    String photo;
    ParseGeoPoint location;

    public Friend(){

    }

    public Friend(String uid, String name, String email, String photo, ParseGeoPoint location){
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.photo = photo;
        this.location = location;
    }

    public Friend(String uid, String name, String email, String photo, double lat, double lon){
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.photo = photo;
        this.location = new ParseGeoPoint(lat, lon);
    }

    public String  getFriendID(){
        return uid;
    }
    public void setFriendID(String uid){
        this.uid = uid;
    }

    public String getFriendName(){
        return name;
    }
    public void setFriendName(String name){
        this.name = name;
    }

    public String getFriendEmail(){
        return email;
    }
    public void setFriendEmail(String email){
        this.email = email;
    }

    public String getFriendPhoto(){
        return photo;
    }
    public void setFriendPhoto(String email){
        this.photo = photo;
    }

    public LatLng getFriendLocationLatLng() {return new LatLng(location.getLatitude(), location.getLongitude()); }
    public ParseGeoPoint getFriendLocationParseGeoPoint() { return location;}
    public void setFriendLocation(LatLng loc) { this.location = new ParseGeoPoint(loc.latitude, loc.longitude); }
    public void setFriendLocation(ParseGeoPoint point) { this.location = point; }
    public void setFriendLocation(double lat, double lon) { this.location = new ParseGeoPoint(lat, lon); }



}