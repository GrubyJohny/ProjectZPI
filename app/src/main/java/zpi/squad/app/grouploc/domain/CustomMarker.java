package zpi.squad.app.grouploc.domain;

public class CustomMarker {

    private String markerIdMySQL;
    private String markerIdSQLite;
    private String UserId;
    private double latitude;
    private double longitude;
    private String name;
    private boolean replace;
    private boolean saveOnServer;

    public CustomMarker(String mySqlID, String UserId, Double latitude, Double longitude, String name) {
        this.markerIdMySQL = mySqlID;
        this.UserId = UserId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;

    }

    public CustomMarker(String markerIdMySQL, String sqlLiteID, String UserId, Double latitude, Double longitude, String name) {
        this.markerIdMySQL = markerIdMySQL;
        this.markerIdSQLite = sqlLiteID;
        this.UserId = UserId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;

    }

    public CustomMarker(String markerId, Double latitude, Double longitude) {
        this.markerIdMySQL = markerId;
        this.latitude = latitude;
        this.longitude = longitude;


    }

    public CustomMarker(Double latitude, Double longitude, String name) {

        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
    }

    public String getMarkerIdMySQL() {
        return markerIdMySQL;
    }

    public void setMarkerIdMySQL(String markerIdMySQL) {
        this.markerIdMySQL = markerIdMySQL;
    }

    public String getMarkerIdSQLite() {
        return markerIdSQLite;
    }

    public void setMarkerIdSQLite(String markerIdSQLite) {
        this.markerIdSQLite = markerIdSQLite;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isReplace() {
        return replace;
    }

    public void setReplace(boolean replace) {
        this.replace = replace;
    }

    public boolean isSaveOnServer() {
        return saveOnServer;
    }

    public void setSaveOnServer(boolean saveOnServer) {
        this.saveOnServer = saveOnServer;
    }
}
