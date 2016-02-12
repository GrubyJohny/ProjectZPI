package zpi.squad.app.grouploc.utils;

import android.app.Activity;
import android.util.Log;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import zpi.squad.app.grouploc.R;


/**
 * Created by Marcin on 2015-04-28.
 */
public class PoiJSONParser extends Activity{

    private String[] nameOfPoi = {"kfc", "mcdonalds"};
    private String[] typeOfPoi = {"food", "restaurant", "bar", "cafe", "night_club", "park",
            "shopping_mall", "store","grocery_or_supermarket" };


    public ArrayList<MarkerOptions> getJsonWithSelectedData(int type, int name, LatLng geo, String imageName) throws IOException {
        ArrayList<MarkerOptions> result = new ArrayList<>();
        ArrayList<Double> lat = new ArrayList<>();
        ArrayList<Double> lng = new ArrayList<>();

        String selectedType = typeOfPoi[type];


        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=";
        url += geo.latitude+","+geo.longitude+"&";
        url += "type="+selectedType+"&";
        if(name!=-1) {
            String selectedName = nameOfPoi[name];
            url += "name=" + selectedName + "&";   //name jest opcjonalne, wystarczy podać typ
        }
        url += "radius=15000&";
        //url += "sensor=false&";
        //url += "key=AIzaSyDdi-iJtQoHbWf5qp-zknZSWKHT4QMANO0";
        url += "key=AIzaSyCM8Pn_F9kmL2QH6hyWBAXnDG7u1hj6tYE";

        //Log.i(tag, url);

        InputStream isStream=null;
        HttpURLConnection urlConnection=null;
        try{
            URL address = new URL(url);
            urlConnection=(HttpURLConnection)address.openConnection();
            urlConnection.connect();

            isStream=urlConnection.getInputStream();
            BufferedReader br=new BufferedReader(new InputStreamReader(isStream));



            String line="";
            while ((line=br.readLine())!=null)
            {
                if(line.contains("lat"))
                {
                   lat.add(Double.parseDouble(line.split(":")[1].replace(",", "").toString()));
                }
                else if(line.contains("lng"))
                {
                    lng.add(Double.parseDouble( line.split(":")[1].replace(",","").toString()));
                }

            }

            br.close();
        }catch (Exception e) {
            Log.d("Exception url", e.toString());
        }
        finally {
            isStream.close();
            urlConnection.disconnect();
        }

        for(int i=0; i<lat.size(); i++)
        {
            switch(imageName)
            {
                case "kfclogo" :
                                {
                                    result.add(new MarkerOptions().position(new LatLng(lat.get(i), lng.get(i))).title(typeOfPoi[type].toString()).icon(BitmapDescriptorFactory.fromResource(R.drawable.kfclogo)));
                                    break;
                                }
                case "mcdonaldslogo" :
                                {
                                    result.add(new MarkerOptions().position(new LatLng(lat.get(i), lng.get(i))).title(typeOfPoi[type].toString()).icon(BitmapDescriptorFactory.fromResource(R.drawable.mcdonaldslogo)));
                                    break;
                                }
                case "bar" :
                                {
                                    result.add(new MarkerOptions().position(new LatLng(lat.get(i), lng.get(i))).title(typeOfPoi[type].toString()).icon(BitmapDescriptorFactory.fromResource(R.drawable.bar)));
                                    break;
                                }
                case "coffee" :
                                {
                                    result.add(new MarkerOptions().position(new LatLng(lat.get(i), lng.get(i))).title(typeOfPoi[type].toString()).icon(BitmapDescriptorFactory.fromResource(R.drawable.coffee)));
                                    break;
                                }
               /* case "food" :
                                {
                                    result.add(new MarkerOptions().position(new LatLng(lat.get(i), lng.get(i))).title(typeOfPoi[type].toString()).icon(BitmapDescriptorFactory.fromResource(R.drawable.food)));
                                }*/
                case "market" :
                                {
                                    result.add(new MarkerOptions().position(new LatLng(lat.get(i), lng.get(i))).title(typeOfPoi[type].toString()).icon(BitmapDescriptorFactory.fromResource(R.drawable.market)));
                                    break;
                                }
                case "nightclub" :
                                {
                                    result.add(new MarkerOptions().position(new LatLng(lat.get(i), lng.get(i))).title(typeOfPoi[type].toString()).icon(BitmapDescriptorFactory.fromResource(R.drawable.nightclub)));
                                    break;
                                }
                case "park" :
                                {
                                    result.add(new MarkerOptions().position(new LatLng(lat.get(i), lng.get(i))).title(typeOfPoi[type].toString()).icon(BitmapDescriptorFactory.fromResource(R.drawable.park)));
                                    break;
                                }
                case "restaurant" :
                                {
                                    result.add(new MarkerOptions().position(new LatLng(lat.get(i), lng.get(i))).title(typeOfPoi[type].toString()).icon(BitmapDescriptorFactory.fromResource(R.drawable.restaurant)));
                                    break;
                                }
                case "shop" :
                                {
                                    result.add(new MarkerOptions().position(new LatLng(lat.get(i), lng.get(i))).title(typeOfPoi[type].toString()).icon(BitmapDescriptorFactory.fromResource(R.drawable.shop)));
                                    break;
                                }
                case "shoppingmall" :
                                {
                                    result.add(new MarkerOptions().position(new LatLng(lat.get(i), lng.get(i))).title(typeOfPoi[type].toString()).icon(BitmapDescriptorFactory.fromResource(R.drawable.shoppingmall)));
                                    break;
                                }
                default: {break;}
            }

        }

        return result;
    }

    public ArrayList<MarkerOptions> getJsonWithSelectedData(int type, LatLng geo, String image) throws IOException
    {
        return getJsonWithSelectedData(type, -1, geo, image);
    }
}
