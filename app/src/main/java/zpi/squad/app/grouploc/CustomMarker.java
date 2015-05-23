package zpi.squad.app.grouploc;

/**
 * Created by sanczo on 2015-05-20.
 */
public class CustomMarker {

    private String markerId;
    private String UserId;
    private double latitude;
    private double longitude;
    private String name;
    private boolean replace;
    private boolean saveOnServer;

    public CustomMarker(String markerId,String UserId,Double latitude,Double longitude,String name)
    {
        this.markerId=markerId;
        this.UserId=UserId;
        this.latitude=latitude;
        this.longitude=longitude;
        this.name=name;

    }
    public CustomMarker(String UserId,Double latitude,Double longitude)
    {
        this.UserId=UserId;
        this.latitude=latitude;
        this.longitude=longitude;
    }

    public void setMarkerId(String markerId) {
        this.markerId = markerId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setReplace(boolean replace) {
        this.replace = replace;
    }

    public void setSaveOnServer(boolean saveOnServer) {
        this.saveOnServer = saveOnServer;
    }

    public String getMarkerId() {
        return markerId;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public String getName() {
        return name;
    }

    public boolean isReplace() {
        return replace;
    }

    public boolean isSaveOnServer() {
        return saveOnServer;
    }



    public String getUserId() {
        return UserId;
    }




}
