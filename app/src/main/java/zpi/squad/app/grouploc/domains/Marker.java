package zpi.squad.app.grouploc.domains;

import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

/**
 * Created by gruby on 15.03.2016.
 */
public class Marker {

    private String objectId;
    private String name;
    private ParseUser owner;
    private ParseGeoPoint localization;

    public Marker(String id, String n, ParseUser o, ParseGeoPoint l) {
        this.objectId = id;
        this.name = n;
        this.owner = o;
        this.localization = l;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String n) {
        this.name = n;
    }

    public ParseUser getOwner() {
        return this.owner;
    }

    public void setOwner(ParseUser o) {
        this.owner = o;
    }

    public ParseGeoPoint getLocalization() {
        return this.localization;
    }

    public void setLocalization(ParseGeoPoint l) {
        this.localization = l;
    }

    public String getObjectId() {
        return this.objectId;
    }

}
