package com.wizard.TestLAB;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.Color;
import android.location.*;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends Activity implements IonDialogDoneListener, ImarkerInRange
{

    private static final String DEBUGTAG = "MainActivity";
    private TextView statusOne;
    private TextView statusTwo;
    private TextView statusThree;
    private TextView statusFour;
    private TextView statusFive;

    private LocationManager locationManager;
    private LocationProvider locationProvider;
    private boolean gpsSetup;
    private boolean gpsFix;
    private LatLng currentLocation;
    private LayerManager layerManager;



    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //Map fragment
        FragmentManager fm = getFragmentManager();
        Fragment frag = fm.findFragmentById(R.id.main_fragment_container);
        if(frag == null)
        {
            Log.d(DEBUGTAG, "Creating new Instance of MapCanvasFragment");
            frag = MapCanvasFragment.newInstance();
            fm.beginTransaction().add(R.id.main_fragment_container, frag).commit();
        }

        statusOne = (TextView) findViewById(R.id.status_one);
        statusOne.setText("No GPS FIX!");
        statusOne.setTextColor(Color.RED);

        statusTwo = (TextView) findViewById(R.id.status_two);
        statusThree = (TextView) findViewById(R.id.status_three);
        statusFour = (TextView) findViewById(R.id.status_four);
        statusFive = (TextView) findViewById(R.id.status_five);

        gpsSetup = false;
        gpsFix = false;
        layerManager = new LayerManager();
        //dirty cast ;-)
        MapCanvasFragment mcf = (MapCanvasFragment) frag;

        for(int index = 0; index < layerManager.getNumberOfPoints(); index++)
        {
            mcf.addMarker(layerManager.getPoint(index), "0x0"+Integer.toHexString(index).toUpperCase(), layerManager.getPoint(index).toString(), index);
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        // keep screen on, do not put screen into sleep!
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        // setup GPS location provider service...
        if(!gpsSetup)
        {
            this.setupGpsController();
        }

        if(!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
        {
            Log.d(DEBUGTAG, "GPS hardware is not enabled");
        }


    }


    private void showOnPointDialog()
    {
        OnPointDialogFragment dlg = OnPointDialogFragment.newInstance();
        FragmentManager manager = getFragmentManager();
        FragmentTransaction ft = manager.beginTransaction();
        dlg.show(manager, "OnPoint");
    }

    @Override
    public void onDialogDone(String tag, boolean cancelled, String message) 
    {
        Log.d(DEBUGTAG, "Tag: " + tag + " canceled: " + Boolean.toString(cancelled) + " message: " + message);
    }

    @Override
    public void markerInRange()
    {
        showOnPointDialog();
    }

    /**
     * Method to setup the GPS controller
     */
    public void setupGpsController()
    {
        try
        {
            // According to: http://developer.android.com/training/basics/location/locationmanager.html
            // Acquire a reference to the system Location Manager
            locationManager = (LocationManager)  this.getSystemService(Context.LOCATION_SERVICE);
            locationProvider = locationManager.getProvider(LocationManager.GPS_PROVIDER);
            locationManager.addGpsStatusListener(gpsStatusListener);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, locationListener);
            // ENABLE TEST PROVIDER ?
            locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
            gpsSetup = true;
        }
        catch(Exception exp)
        {
            Log.d(DEBUGTAG,"Error: failed setupGpsController: " + exp.toString());
            gpsSetup = false;
        }
    }

    /**
     * Location Listener for on-board GPS
     */
    private LocationListener locationListener = new LocationListener()
    {

        public void onStatusChanged(String provider, int status, Bundle extras)
        {
            // TODO Auto-generated method stub
        }

        public void onProviderEnabled(String provider)
        {
            // TODO Auto-generated method stub
        }

        public void onProviderDisabled(String provider)
        {
            // TODO Auto-generated method stub
        }

        public void onLocationChanged(Location location)
        {
            currentLocation = new LatLng( location.getLatitude(),  location.getLongitude());
            MapCanvasFragment frag = (MapCanvasFragment) getFragmentManager().findFragmentById(R.id.main_fragment_container);
            frag.moveCurrentPositionMarker(currentLocation);

            //If current position is close to marker send callback to main act
            double newLocLat = location.getLatitude();
            double newLocLon = location.getLongitude();
/*        for(LatLng loc : locationsList)
        {
            if (distance(newLocLat, newLocLon, loc.latitude, loc.longitude) < 1 )
            {
                markerInRangeListener.markerInRange();
            }
        }*/
            for(int i = 0; i < layerManager.getNumberOfPoints(); i++)
            {
                if (layerManager.distance(newLocLat, newLocLon, layerManager.getPoint(i).latitude, layerManager.getPoint(i).longitude) < 1 )
                {
                    markerInRange();
                }

            }


            statusTwo.setText("Lat/Lon: " + location.getLatitude() + "/" + location.getLongitude());
            statusThree.setText("height: " + Double.toString(location.getAltitude()));
            statusFour.setText("Accuracy: " + location.getAccuracy() + "m");
            statusFive.setText(Long.toString(location.getTime()));
        }
    };

    /**
     * On-board GPS Status Listener
     */
    // http://developer.android.com/reference/android/location/GpsStatus.Listener.html
    private GpsStatus.Listener gpsStatusListener = new GpsStatus.Listener()
    {

        public void onGpsStatusChanged(int event)
        {
            switch(event)
            {
                case(GpsStatus.GPS_EVENT_FIRST_FIX):
                    gpsFix = true;
                    statusOne.setText("GPS FIXED!");
                    statusOne.setTextColor(Color.GREEN);
                    break;
                case(GpsStatus.GPS_EVENT_SATELLITE_STATUS):
                    break;
                case(GpsStatus.GPS_EVENT_STARTED):
                    break;
                case(GpsStatus.GPS_EVENT_STOPPED):
                    break;
                default:
                    break;
            }
        }
    };

}
