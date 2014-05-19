package com.wizard.TestLAB;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by Koen on 08-May-14.
 */
public class LayerManager
{
    private ArrayList<LatLng> locationList;


    public LayerManager()
    {
        locationList = new ArrayList<LatLng>();
        locationList.add(new LatLng(50.87410716666667, 5.2745365));
        locationList.add(new LatLng(50.874272833333336, 5.274491833333333));
        locationList.add(new LatLng(50.874243166666666, 5.274399833333334));
        locationList.add(new LatLng(50.8740865, 5.274438));
        locationList.add(new LatLng(50.874089166666664, 5.274469666666667));
        locationList.add(new LatLng(50.87418483333333, 5.2742051666666665));
        locationList.add(new LatLng(50.8741623333333, 5.274212666666667));
        locationList.add(new LatLng(50.874191333333336, 5.274329166666667));
        locationList.add(new LatLng(50.87422, 5.274323833333334));
        locationList.add(new LatLng(50.8742515, 5.2744315));
        locationList.add(new LatLng(50.874073333333335, 5.274140166666666));
        locationList.add(new LatLng(50.874213166666664, 5.274507666666667));
    }

    public int getNumberOfPoints()
    {
        return locationList.size();
    }

    public LatLng getPoint(int index)
    {
        if(index >= 0 && index < locationList.size())
        {
            return locationList.get(index);
        }
        else
        {
            return null;
        }
    }

    //http://www.codecodex.com/wiki/Calculate_Distance_Between_Two_Points_on_a_Globe
    public double distance(double lat_a, double lng_a, double lat_b, double lng_b )
    {
        double earthRadius = 3958.75;
        double latDiff = Math.toRadians(lat_b-lat_a);
        double lngDiff = Math.toRadians(lng_b-lng_a);
        double a = Math.sin(latDiff /2) * Math.sin(latDiff /2) +
                Math.cos(Math.toRadians(lat_a)) * Math.cos(Math.toRadians(lat_b)) *
                        Math.sin(lngDiff /2) * Math.sin(lngDiff /2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double distance = earthRadius * c;

        int meterConversion = 1609;

        return distance * meterConversion;
    }


}
