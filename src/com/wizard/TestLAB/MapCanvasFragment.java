package com.wizard.TestLAB;

import android.app.Activity;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.*;

import java.util.ArrayList;


public class MapCanvasFragment extends MapFragment
{
    private static final String DEBUGTAG = "MapFragment";
    private static final int MAXZOOM = 21;
    private static final LatLng defaultLocation = new LatLng(50.8741965, 5.274338); // $DEV_HOME ;-)
    private Marker currentPositionMarker;
    private GoogleMap map;

    private ArrayList<LatLng> locationsList;

    private ImarkerInRange markerInRangeListener;

    /**
     * Creates a new instance of the mapfragment
     * @return new mapCanvasFragment instance
     */
    public static MapCanvasFragment newInstance()
    {
        Log.d(DEBUGTAG, "Created a new instance of MapCanvasFragment...");
        MapCanvasFragment mapCanvasFragment = new MapCanvasFragment();
        return mapCanvasFragment;
    }

    /*
     * called once the fragment is associated with its activity.
     */
    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        try
        {
            markerInRangeListener = (ImarkerInRange) activity;
        }
        catch(ClassCastException cce)
        {
            Log.d(DEBUGTAG, "Activity is not listening to interface ImarkerInRange");
            Log.d(DEBUGTAG, cce.toString());
        }
    }

    /*
     * Called to do initial creation of a fragment
     * Retrieves attributes from savedInstanceState bundle or arguments set
     * Initializes local instance variables
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Log.d(DEBUGTAG, "onCreate MapCanvasFragment");

        locationsList = new ArrayList<LatLng>();
        locationsList.clear();
        locationsList.add(new LatLng(50.87410716666667, 5.2745365));
        locationsList.add(new LatLng(50.874272833333336, 5.274491833333333));
        locationsList.add(new LatLng(50.874243166666666, 5.274399833333334));
        locationsList.add(new LatLng(50.8740865, 5.274438));
        locationsList.add(new LatLng(50.874089166666664, 5.274469666666667));
        locationsList.add(new LatLng(50.87418483333333, 5.2742051666666665));
        locationsList.add(new LatLng(50.8741623333333, 5.274212666666667));
        locationsList.add(new LatLng(50.874191333333336, 5.274329166666667));
        locationsList.add(new LatLng(50.87422, 5.274323833333334));
        locationsList.add(new LatLng(50.8742515, 5.2744315));
        locationsList.add(new LatLng(50.874073333333335, 5.274140166666666));
        locationsList.add(new LatLng(50.874213166666664, 5.274507666666667));

    }

    /*
     * Called to have the fragment instantiate its user interface view
     * Map is instantiated here
     */
    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle)
    {
        View fragmentView = super.onCreateView(layoutInflater, viewGroup, bundle);
        if(map == null)
        {
            map = getMap();
            Log.d(DEBUGTAG, "fetching map object");
            if(map != null)
            {
                Log.d(DEBUGTAG, "Map Object acquired");
                initMap();
            }
        }
        return fragmentView;
    }

    /*
     * makes the fragment visible to the user (based on its containing activity being started)
     */
    @Override
    public void onStart()
    {
        super.onStart();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle)
    {
        super.onSaveInstanceState(bundle);
    }

    /*
     * makes the fragment interacting with the user (based on its containing activity being resumed).
     */
    @Override
    public void onResume()
    {
        super.onResume();
    }

    /*
     *  fragment is no longer interacting with the user either because its activity is being paused or a fragment operation is modifying it in the activity.
     */
    @Override
    public void onPause()
    {
        super.onPause();    //To change body of overridden methods use File | Settings | File Templates.
    }

    /*
     * allows the fragment to clean up resources associated with its View.
     */
    @Override
    public void onDestroyView()
    {
        super.onDestroyView();    //To change body of overridden methods use File | Settings | File Templates.
    }

    /*
     *  called to do final cleanup of the fragment's state.
     */
    @Override
    public void onDestroy()
    {
        super.onDestroy();    //To change body of overridden methods use File | Settings | File Templates.
    }

    /**
     *  Internal method for initializing the Map object, can only be used if map object exists!
     *  sets map type to normal
     *  sets default values for infowindow title and snippet
     *  moves camera to default position
     */
    private void initMap()
    {
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.getUiSettings().setAllGesturesEnabled(true);
        map.getUiSettings().setZoomControlsEnabled(true);
        currentPositionMarker = map.addMarker(new MarkerOptions().position(defaultLocation));
        currentPositionMarker.setTitle("Wizard's Test Lab");
        currentPositionMarker.setSnippet(defaultLocation.toString());
        currentPositionMarker.setIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_map_current_position_arrow_small));
        currentPositionMarker.setAnchor(0.5f, 0.5f);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, MAXZOOM));
        populateMap();
    }

    public void moveCurrentPositionMarker(LatLng newLocation)
    {
        //Center the map view to newLocation
        map.moveCamera(CameraUpdateFactory.newLatLng(newLocation));
        //Move The Marker
        currentPositionMarker.setPosition(newLocation);
        //If current position is close to marker send callback to main act
        double newLocLat = newLocation.latitude;
        double newLocLon = newLocation.longitude;
        for(LatLng loc : locationsList)
        {
            if (distance(newLocLat, newLocLon, loc.latitude, loc.longitude) < 1 )
            {
                markerInRangeListener.markerInRange();
            }
        }
    }

    private void populateMap()
    {

        LatLng position = locationsList.get(0);
        map.addMarker(new MarkerOptions()
                .position(position)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .title("0x00")
                .snippet(position.toString()));

        position = locationsList.get(1);
        map.addMarker(new MarkerOptions()
                .position(position)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .title("0x01")
                .snippet(position.toString()));

        position = locationsList.get(2);
        map.addMarker(new MarkerOptions()
                .position(position)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                .title("0x02")
                .snippet(position.toString()));

        position = locationsList.get(3);
        map.addMarker(new MarkerOptions()
                .position(position)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                .title("0x03")
                .snippet(position.toString()));

        position = locationsList.get(4);
        map.addMarker(new MarkerOptions()
                .position(position)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .title("0x04")
                .snippet(position.toString()));

        position = locationsList.get(5);
        map.addMarker(new MarkerOptions()
                .position(position)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                .title("0x05")
                .snippet(position.toString()));

        position = locationsList.get(6);
        map.addMarker(new MarkerOptions()
                .position(position)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                .title("0x06")
                .snippet(position.toString()));

        position = locationsList.get(7);
        map.addMarker(new MarkerOptions()
                .position(position)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                .title("0x07")
                .snippet(position.toString()));

        position = locationsList.get(8);
        map.addMarker(new MarkerOptions()
                .position(position)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ROSE))
                .title("0x08")
                .snippet(position.toString()));

        position = locationsList.get(9);
        map.addMarker(new MarkerOptions()
                .position(position)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                .title("0x09")
                .snippet(position.toString()));

        position = locationsList.get(10);
        map.addMarker(new MarkerOptions()
                .position(position)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE))
                .title("0x0A")
                .snippet(position.toString()));

        position = locationsList.get(11);
        map.addMarker(new MarkerOptions()
                .position(position)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))
                .title("Refentie Punt op straat")
                .snippet(position.toString()));
    }


    private boolean markerProximity(LatLng currentPosition)
    {
        boolean markerClose = false;
        for(LatLng location : locationsList)
        {
            if(distance(currentPosition.latitude, currentPosition.longitude, location.latitude, location.latitude) <= 2)
            {
                markerClose = true;
            }
        }

        return markerClose;
    }

    //http://www.codecodex.com/wiki/Calculate_Distance_Between_Two_Points_on_a_Globe
    private double distance(double lat_a, double lng_a, double lat_b, double lng_b )
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