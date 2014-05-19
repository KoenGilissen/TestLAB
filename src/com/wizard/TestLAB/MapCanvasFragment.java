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
    }

    public void moveCurrentPositionMarker(LatLng newLocation)
    {
        //Center the map view to newLocation
        map.moveCamera(CameraUpdateFactory.newLatLng(newLocation));
        //Move The Marker
        currentPositionMarker.setPosition(newLocation);
    }


    public void addMarker(LatLng position, String title, String snippet, int color)
    {
        MarkerOptions mOptions = new MarkerOptions();
        mOptions.title(title);
        mOptions.snippet(snippet);
        mOptions.icon(BitmapDescriptorFactory.defaultMarker(resolveColorOfMarker(color)));
        mOptions.position(position);
        map.addMarker(mOptions);
    }

    public float resolveColorOfMarker(int color)
    {

        if(color == 0)
            return BitmapDescriptorFactory.HUE_AZURE;
        else if(color == 1)
            return BitmapDescriptorFactory.HUE_BLUE;
        else if(color == 2)
            return BitmapDescriptorFactory.HUE_CYAN;
        else if(color == 3)
            return BitmapDescriptorFactory.HUE_GREEN;
        else if(color == 4)
            return BitmapDescriptorFactory.HUE_MAGENTA;
        else if(color == 5)
            return BitmapDescriptorFactory.HUE_ORANGE;
        else if(color == 6)
            return BitmapDescriptorFactory.HUE_RED;
        else if(color == 6)
            return BitmapDescriptorFactory.HUE_ROSE;
        else if(color == 7)
            return BitmapDescriptorFactory.HUE_VIOLET;
        else if(color == 8)
            return BitmapDescriptorFactory.HUE_YELLOW;
        else
            return BitmapDescriptorFactory.HUE_RED;
    }


}