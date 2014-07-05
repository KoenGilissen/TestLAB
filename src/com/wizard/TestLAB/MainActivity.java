package com.wizard.TestLAB;

import android.app.*;
import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.*;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends Activity implements IonDialogDoneListener, SensorEventListener
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
    private LatLng currentLocation;
    private LayerManager layerManager;
    private double currentShortestDistance;
    private String shortestMeasurementPointInfo;

    // record the compass picture angle turned
    private int currentDegree;
    private SensorManager mSensorManager;


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
        layerManager = new LayerManager();
        currentShortestDistance = Double.MAX_VALUE;
        shortestMeasurementPointInfo = "";
        //dirty cast ;-)
        MapCanvasFragment mcf = (MapCanvasFragment) frag;

        for(int index = 0; index < layerManager.getNumberOfPoints(); index++)
        {
            mcf.addMarker(layerManager.getPoint(index), "0x0"+Integer.toHexString(index).toUpperCase(), layerManager.getPoint(index).toString(), index);
        }

        // initialize sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        // Register sensor listener
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_NORMAL);
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

    @Override
    protected void onPause()
    {
        super.onPause();
        //unregister sensorlistener to save battery power
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onDialogDone(String tag, boolean cancelled, String message) 
    {
        Log.d(DEBUGTAG, "Tag: " + tag + " canceled: " + Boolean.toString(cancelled) + " message: " + message);
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
            //Update MapCanvasFragment with new location and orientation
            currentLocation = new LatLng( location.getLatitude(),  location.getLongitude());
            MapCanvasFragment mapFrag = (MapCanvasFragment) getFragmentManager().findFragmentById(R.id.main_fragment_container);
            mapFrag.moveCurrentPositionMarker(currentLocation, 0); //do not change bearing, compass sensor drifts....

            //Check if the new location is close to measurement point
            double newLocLat = location.getLatitude();
            double newLocLon = location.getLongitude();
            double shortestDistance = Double.MAX_VALUE;
            double currentDistance;
            int measurementPointIndex = 0;

            for(int i = 0; i < layerManager.getNumberOfPoints(); i++)
            {
                currentDistance = layerManager.distance(newLocLat, newLocLon, layerManager.getPoint(i).latitude, layerManager.getPoint(i).longitude);
                if(currentDistance < shortestDistance)
                {
                    shortestDistance = currentDistance;
                    measurementPointIndex = i;
                }
            }
            currentDistance = shortestDistance;

            // Get the dialog fragment
            OnPointDialogFragment dialogFrag = (OnPointDialogFragment) getFragmentManager().findFragmentByTag("OnPoint");
            OnPointDialogFragment dlg = OnPointDialogFragment.newInstance();
            FragmentManager manager = getFragmentManager();
            FragmentTransaction ft = manager.beginTransaction();
            if ( currentDistance < 2 )
            {
                // Check if the dialog fragment is (already) alive
                if(dialogFrag != null) // dialog is already alive ;-)
                {
                    //Update the dialog's content
                    dialogFrag.setTitle("Measurment Point in range");
                    dialogFrag.setContent( "Measurement Point: " + layerManager.getPoint(measurementPointIndex).toString() + "\n" +
                            "Distance: " + Double.toString(currentDistance) + "\n" +
                            "Height: " +  Double.toString(location.getAltitude()));
                }
                else // Show dialog
                {
                    dlg.show(manager, "OnPoint");
                }
            }
            else // If all measurement points are out of range
            {
                if(dialogFrag != null) //if the dialog is alive and user has gone out of range
                {
                    //ft.remove(dlg); //Kill it http://developer.android.com/reference/android/app/DialogFragment.html
                    // ft.addToBackStack(null); Do not remember this... shity developersguide google...
                    dialogFrag.dismiss();
                }
            }

            //Update statusbar elements
            statusTwo.setText("Lat/Lon: " + location.getLatitude() + "/" + location.getLongitude());
            statusThree.setText("height: " + Double.toString(location.getAltitude()));
            statusFour.setText("Accuracy: " + location.getAccuracy() + "m");
            statusFive.setText("Closest MP: " + shortestDistance + "m" );
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

    @Override
    public void onSensorChanged(SensorEvent event)
    {
        if(event.sensor.getType() == Sensor.TYPE_ORIENTATION)
        {
            // values[0]: Azimuth, angle between the magnetic north direction and the y-axis, around the z-axis (0 to 359). 0=North, 90=East, 180=South, 270=West
            currentDegree = (int)  event.values[0];
            //Log.d(DEBUGTAG, "Current Angle: " + Integer.toString(currentDegree) + " degrees");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public double getShortestDistance()
    {
        return currentShortestDistance;
    }

    public String getShortestMeasurementPointInfo()
    {
        return "";
    }
}
