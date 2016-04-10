package zpi.squad.app.grouploc.utils;

import java.util.List;

import zpi.squad.app.grouploc.domains.CustomMarker;

public class ToolsForMarkerList {


    public static CustomMarker getSpecificMarkerByLatitudeAndLongitude(List<CustomMarker> markers, double latitude, double longitude) {
        int i = 0;
        while (i < markers.size() && !(markers.get(i).getLatitude() == latitude && markers.get(i).getLongitude() == longitude))
            i++;
        if (i < markers.size())
            return markers.get(i);
        else
            return null;
    }

    public static CustomMarker getSpecificMarker(List<CustomMarker> markers, String id) {
        int i = 0;
        while (i < markers.size() && !(id.equals(markers.get(i).getMarkerIdSQLite())))
            i++;
        if (i < markers.size())
            return markers.get(i);
        else
            return null;
    }

}
