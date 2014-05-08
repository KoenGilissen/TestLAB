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


}
