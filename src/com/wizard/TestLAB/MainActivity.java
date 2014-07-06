package com.wizard.TestLAB;

import android.app.*;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.*;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends Activity
{

    private static final String DEBUGTAG = "MainActivity";
    private final static boolean DEBUGGINGMODE = true;
    private TextView statusOne;
    private TextView statusTwo;
    private TextView statusThree;
    private TextView statusFour;
    private TextView statusFive;

    private LatLng currentLocation;
    private LayerManager layerManager;

    // Bluetooth...
    private BluetoothAdapter bluetoothAdapter = null;
    private boolean bluetoothAvailable;
    private Set<BluetoothDevice> pairedDevices;
    private ArrayList<String> discoveredDevices  = null;
    private BlueService blueService  = null;

    // Key names received from the BlueService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_CONNECT_DEVICE = 2;

    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;


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
            if(DEBUGGINGMODE)
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

        layerManager = new LayerManager();
        //dirty cast ;-)
        MapCanvasFragment mcf = (MapCanvasFragment) frag;

        for(int index = 0; index < layerManager.getNumberOfPoints(); index++)
        {
            mcf.addMarker(layerManager.getPoint(index), "0x0"+Integer.toHexString(index).toUpperCase(), layerManager.getPoint(index).toString(), index);
        }

        //Intialize global activity variables
        discoveredDevices = new ArrayList<String>();

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(broadcastReceiver, filter); // Don't forget to unregister during onDestroy
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(broadcastReceiver, filter);

        //Test if Bluetooth hardware is available on this device
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if(bluetoothAdapter == null)
        {
            //Bluetooth not supported on this device...
            bluetoothAvailable = false;
            //Gracefully exit: activity is done and should be closed!
            finish();
        }
        else
        {
            if(DEBUGGINGMODE)
                Log.d(DEBUGTAG, "Bluetooth hardware available on this device");
            bluetoothAvailable = true;
        }

        if(bluetoothAvailable)
        {
            if (!bluetoothAdapter.isEnabled())
            {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                //enable bluetooth dialog ...
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            }
        }
        // Check paired devices
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices(); //Returns unmodifiable set of BluetoothDevice, or null on error
        /**
        // If there are paired devices
        if(pairedDevices != null) // Could be null if Bluetooth Hardware is not available or turned off!
        {
            if (pairedDevices.size() > 0)
            {
                mainTextView.setText("Found Paired devices: " + Integer.toString(pairedDevices.size()) + "\n");
                //TODO do something I guess...
                for(BluetoothDevice device : pairedDevices)
                {
                    mainTextView.append(device.getName() + " : " + device.getAddress() + " \n");
                }
            }
        }
         // Bluetooth device discovery
         if(bluetoothAdapter.isEnabled())
         {
         Log.d(DEBUGTAG, "Starting device discovery...");
         discoverDevices();
         }
         **/

    }

    @Override
    protected synchronized void onResume()
    {
        super.onResume();
        if(DEBUGGINGMODE)
            Log.d(DEBUGTAG, "onResume():");
        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (blueService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (blueService.getState() == blueService.STATE_NONE) {
                // Start the Bluetooth services
                blueService.start();
            }
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        // keep screen on, do not put screen into sleep!
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if(bluetoothAdapter.enable())
        {
            setupConnection();
        }
    }

    @Override
    protected void onPause()
    {
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_ENABLE_BT)
        {
            if(resultCode == RESULT_OK)
            {
                if(DEBUGGINGMODE)
                    Log.d(DEBUGTAG, "Bluetooth Enabled!");
                setupConnection();
            }
        }

    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if(DEBUGGINGMODE)
            Log.d(DEBUGTAG,"onDestroy():");
        //kill off broadcast receiver
        if(DEBUGGINGMODE)
            Log.d(DEBUGTAG, "Killing Broadcast receivers");
        unregisterReceiver(broadcastReceiver);
        //Kill bluetooth service
        if(DEBUGGINGMODE)
            Log.d(DEBUGTAG, "Killing bluetooth service");
        if (blueService != null)
        {
            blueService.stop();
        }
    }

    private void setupConnection()
    {

        // Initialize the BluetoothService to perform bluetooth connections
        blueService = new BlueService(this, handler);
        //BluetoothDevice bluetoothHC06 = bluetoothAdapter.getRemoteDevice("00:13:12:26:71:88");
        BluetoothDevice bluetoothHC06 = bluetoothAdapter.getRemoteDevice("00:13:12:26:75:67");
        String deviceName = bluetoothHC06.getName();
        String deviceAdrress = bluetoothHC06.getAddress();
        String deviceClass = bluetoothHC06.getBluetoothClass().toString();
        statusOne.setText("Connecting to : " + deviceName + "  [" + deviceAdrress + "]\n");
        blueService.connect(bluetoothHC06);
    }

    // The Handler that gets information back from the BlueService
    private final Handler handler = new Handler()
    {
        String line = "";
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MESSAGE_STATE_CHANGE:
                    Log.i(DEBUGTAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1)
                    {
                        case BlueService.STATE_CONNECTED:
                            statusOne.setText("[BlueService State]: Connected! \n");
                            break;
                        case BlueService.STATE_CONNECTING:
                            statusOne.setText("[BlueService State]: Connecting... \n");
                            break;
                        case BlueService.STATE_LISTEN:
                            statusOne.setText("[BlueService State]: Listening... \n");
                        case BlueService.STATE_NONE:
                            statusOne.setText("[BlueService State]: Idle \n");
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    Log.d(DEBUGTAG, "Msg send: " + writeMessage);
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    if(readMessage.contains("\n"))
                    {
                        line = line + readMessage;
                        if(line.contains("$GPGGA"))
                        {
                            GpggaLineParser gpggaData = new GpggaLineParser(line);
                            if(gpggaData.isValid())
                            {
                                double latitude = gpggaData.getLatitude();
                                double longitude = gpggaData.getLongitude();

                                //Update MapCanvasFragment with new location and orientation
                                currentLocation = new LatLng( latitude,  longitude);
                                MapCanvasFragment mapFrag = (MapCanvasFragment) getFragmentManager().findFragmentById(R.id.main_fragment_container);
                                mapFrag.moveCurrentPositionMarker(currentLocation, 0); //do not change bearing, compass sensor drifts....

                                //statusOne.setText(Double.toString(latitude));
                                //statusTwo.setText(Double.toString(longitude));
                                statusOne.setText("Altitude: " + gpggaData.getMslAltitude());
                                statusTwo.setText("");
                                statusThree.setText("");
                                statusFour.setText("Sat in View: " + gpggaData.getSatellitesUsed());
                                statusFive.setText("pfi: " + gpggaData.getPositionFixIndicator());
                            }
                            else
                            {
                                statusOne.setText("Waiting for GPS Fix ....");
                            }
                        }
                        line = "";
                    }
                    else
                    {
                        line = line + readMessage;
                    }
                    break;
                case MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    String connectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to " + connectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                            Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };


    private boolean discoverDevices()
    {
        Log.d(DEBUGTAG, "Starting Device Discovery");
        setProgressBarIndeterminateVisibility(true);
        // If we're already discovering, stop it
        if (bluetoothAdapter.isDiscovering())
        {
            bluetoothAdapter.cancelDiscovery();
        }

        // Discover devices:
        boolean succesfullStartOfDiscovery = bluetoothAdapter.startDiscovery();
        return succesfullStartOfDiscovery;
    }

    // Create a BroadcastReceiver for ACTION_FOUND
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver()
    {
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action))
            {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add the name and address to an array adapter to show in a ListView
                String deviceFound = device.getName() + ": " + device.getAddress();
                Log.d(DEBUGTAG, deviceFound);
                discoveredDevices.add(deviceFound);
                statusOne.append(deviceFound + "\n");
            }
            // When discovery is finished do:
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                setProgressBarIndeterminateVisibility(false);

            }
        }
    };



}
