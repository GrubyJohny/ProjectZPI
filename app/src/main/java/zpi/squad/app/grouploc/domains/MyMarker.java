package zpi.squad.app.grouploc.domains;

import android.graphics.Bitmap;

import com.parse.ParseGeoPoint;
import com.parse.ParseUser;

/**
 * Created by gruby on 15.03.2016.
 */
public class MyMarker {

    private String objectId;
    private String name;
    private ParseUser owner;
    private ParseGeoPoint localization;
    private Bitmap icon;


    public MyMarker(String id, String n, ParseUser o, ParseGeoPoint l) {
        this.objectId = id;
        this.name = n;
        this.owner = o;
        this.localization = l;
    }

    public MyMarker(String id, String n, ParseUser o, ParseGeoPoint l, Bitmap ic) {
        this.objectId = id;
        this.name = n;
        this.owner = o;
        this.localization = l;
        this.icon = ic;
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

    public Bitmap getIcon() {
        return this.icon;
    }

    public void setIcon(Bitmap b) {
        this.icon = b;
    }


}
