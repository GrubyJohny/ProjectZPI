package com.example.marcin.lokalizator;

import java.util.List;

/**
 * Created by sanczo on 2015-05-22.
 */
public class ToolsForMarkerList {


    public static CustomMarker getSpecificMarker(List<CustomMarker> markers,double latitude, double longitude)
    {
        int i=0;
        while(i<markers.size()&&!(markers.get(i).getLatitude()==latitude&&markers.get(i).getLongitude()==longitude))
            i++;
        if(i<markers.size())
            return markers.get(i);
        else
            return null;
    }
}
