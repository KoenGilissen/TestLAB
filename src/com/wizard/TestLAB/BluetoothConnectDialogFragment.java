package com.wizard.TestLAB;

import android.app.Activity;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.*;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;

/**
 * Created by Koen on 06-Jul-14.
 *
 * Dialog Fragment to manage the connection to the position hardware via bluetooth
 *
 */
public class BluetoothConnectDialogFragment extends DialogFragment
{
    private static final  String DEBUGTAG = "BluetoothConnectDialogFragment";
    private static final boolean DEBUGMODE = true;

    private IonDialogDoneListener onDialogDoneListener;

    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<BluetoothDevice> bluetoothDevices;
    private ArrayList<String> stringArrayListBluetoothDevices;

    private ImageButton deviceDiscoveryImageButton;
    private ProgressBar progressBarDeviceDiscovery;
    private TextView listLabelDevicesTextView;
    private ListView listViewDiscoveredDevices;
    private Button connectButton;
    private Button cancelButton;

    private BluetoothDevice selectedDevice;
    private SharedPreferences preferences;
    private static final String PREF_DEVICE_NAME = "PreviousDeviceName";
    private static final String PREF_DEVICE_MAC = "PrevDeviceMAC";
    private static final String PREF_DEVICE_NAME_DEFAULT = "No Device Found!";
    private static final String PREF_DEVICE_MAC_DEFAULT = "";


    public static BluetoothConnectDialogFragment newInstance()
    {
        BluetoothConnectDialogFragment dlg = new BluetoothConnectDialogFragment();
        //Bundle args = new Bundle();
        return dlg;
    }

    @Override
    public void onAttach(Activity activity)
    {
        super.onAttach(activity);
        // If the activity you're being attached to has
        // not implemented the IonDialogDoneListener
        // interface, the following line will throw a ClassCastException.
        // Basically is it a well-behaved activity?
        try
        {
            if(DEBUGMODE)
                Log.d(DEBUGTAG, "onAttach()");
            onDialogDoneListener = (IonDialogDoneListener) activity;
        }
        catch (ClassCastException cce)
        {
            // Here is where we fail gracefully.
            Log.d(DEBUGTAG, "Activity is not listening");
            Log.d(DEBUGTAG, cce.toString());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, android.R.style.Theme_Holo_Dialog);
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        stringArrayListBluetoothDevices = new ArrayList<String>();
        bluetoothDevices = new ArrayList<BluetoothDevice>();
        selectedDevice = null;

        // Register intent filters and broadcast receivers
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getActivity().registerReceiver(broadcastReceiver, filter); // Don't forget to unregister during onDestroy
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getActivity().registerReceiver(broadcastReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        preferences = getActivity().getPreferences(Context.MODE_PRIVATE);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        // Set Dialog title
        getDialog().setTitle(getResources().getString(R.string.bluetooth_connection_dialog_fragment_title));
        // Create a view object from layout XML
        View v = inflater.inflate(R.layout.bluetooth_connect_dialog_fragment, container);
        // Get GUI elements
        deviceDiscoveryImageButton = (ImageButton) v.findViewById(R.id.bluetoothconnection_dlg_frag_imageButton_device_discovery);

        progressBarDeviceDiscovery = (ProgressBar) v.findViewById(R.id.bluetoothconnection_dlg_frag_progressBar_discovery);
        progressBarDeviceDiscovery.setMax(100);
        progressBarDeviceDiscovery.setProgress(0);

        listViewDiscoveredDevices = (ListView) v.findViewById(R.id.bluetoothconnection_dlg_frag_listView_devices);
        listViewDiscoveredDevices.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);

        connectButton = (Button) v.findViewById(R.id.bluetoothconnection_dlg_frag_connect_button);
        cancelButton = (Button) v.findViewById(R.id.bluetoothconnection_dlg_frag_cancel_button);

        // Register event listeners...
        listViewDiscoveredDevices.setOnItemClickListener(onListItemClickListener);
        deviceDiscoveryImageButton.setOnClickListener(buttonClicklistener);
        connectButton.setOnClickListener(buttonClicklistener);
        cancelButton.setOnClickListener(buttonClicklistener);


        // Get last used device from SharedPreferences
        listLabelDevicesTextView = (TextView) v.findViewById(R.id.bluetoothconnection_dlg_frag_textView_devices_list);
        listLabelDevicesTextView.setText("Previously used device: ");
        String prevDeviceName = this.loadPreviousDeviceName();
        String prevDeviceMac = this.loadPreviousDeviceMAC();
        // If the stored device mac is not equal to default value AND the MAC is valid
        if(!prevDeviceMac.equals(PREF_DEVICE_MAC_DEFAULT) && BluetoothAdapter.checkBluetoothAddress(prevDeviceMac))
        {
            if(DEBUGMODE)
                Log.d(DEBUGTAG, "Loaded " + prevDeviceName + " from SharedPreferences");
            BluetoothDevice preferedDevice = bluetoothAdapter.getRemoteDevice(prevDeviceMac);
            bluetoothDevices.add(preferedDevice);
            populateDiscoveredDevicesList(bluetoothDevices);
        }
        else
        {
            if(DEBUGMODE)
                Log.d(DEBUGTAG, "Device Loaded From user Preferences is invalid or non-existing");
        }
        return v;
    }

    //This is where you can do final tweaks to the user interface before the user sees it
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }

    //Now your fragment is visible to the user.
    @Override
    public void onStart()
    {
        super.onStart();
    }

    //The last callback before the user can interact with this fragment
    @Override
    public void onResume()
    {
        super.onResume();
    }

    // When the user presses the Back button while the dialog fragment is
    // displayed
    @Override
    public void onCancel(DialogInterface dialog)
    {
        super.onCancel(dialog);
    }

    // Called when dialog is dismissed via dismiss()
    // Also Called when the device changes state
    @Override
    public void onDismiss(DialogInterface dialog)
    {
        super.onDismiss(dialog);
    }

    /*  To prevent memory problems, be careful about what you save
     *	into this bundle. Only save what you need. If you need to keep a reference to another
     *	fragment, save its tag instead of trying to save the other fragment.
     */
    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStop()
    {
        super.onStop();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        getActivity().unregisterReceiver(broadcastReceiver);
    }

    /**
     * Method to populate the ListView with the discovered (Bluetooth) devices found
     * @param devicesFoundList (BluetootDevice) Arraylist of discovered bluetooth devices
     */
    private void populateDiscoveredDevicesList(ArrayList<BluetoothDevice> devicesFoundList)
    {
        // if there are no devices found:
        if(devicesFoundList.isEmpty())
        {
            // add item in list to notify the user of the fact that no devices where found
            stringArrayListBluetoothDevices.add(getResources().getString(R.string.bluetooth_connection_dialog_fragment_no_devices_discovered));
            // Make shure the user cannot select the item...
            listViewDiscoveredDevices.setChoiceMode(ListView.CHOICE_MODE_NONE);
        }
        else // If there are device found:
        {
            // Extract the (String) name and MAC to add to the ListView
            for(BluetoothDevice device : devicesFoundList)
            {
                stringArrayListBluetoothDevices.add(device.getName() + " [" + device.getAddress() + "]");
            }
        }
        // Create a new arrayAdapter object to populate the listview from the arraylist of strings containing the bluetooth devices
        ArrayAdapter<String> discoveredDevicesList = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_activated_1, stringArrayListBluetoothDevices);
        // Set the adapter of the listview
        listViewDiscoveredDevices.setAdapter(discoveredDevicesList);
    }


    /**
     *  Anonymous Inner Class of type Broadcast Receiver
     *  This boradcast receiver is setup to receive Bluetooth actions
     */
    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            // if a bluetooth device is discovered
            if(action.equals(BluetoothDevice.ACTION_FOUND))
            {
                // construct the bluetoothdevice object
                BluetoothDevice btD = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Add is to the ArrayList of BluetoothDevices
                bluetoothDevices.add(btD);
                // Increment The GUI Progress bar
                if(progressBarDeviceDiscovery.getProgress() + 10 < progressBarDeviceDiscovery.getMax())
                    progressBarDeviceDiscovery.setProgress(progressBarDeviceDiscovery.getProgress() + 10 );
            }
            // If the discovery has finished
            else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))
            {
                // Set progress of progressbar to MAX and populate the listview with the discovered devices
                progressBarDeviceDiscovery.setProgress(progressBarDeviceDiscovery.getMax());
                populateDiscoveredDevicesList(bluetoothDevices);

            }
            else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED))
            {
                //do nothing
            }
        }
    };

    /**
     * Method to start device discovery
     * @return (boolean) true if discovery started successful | false if unsuccessful discovery start
     */
    private boolean discoverDevices()
    {
        Log.d(DEBUGTAG, "Starting Device Discovery");
        listLabelDevicesTextView.setText("Devices found in range: ");
        // If we're already discovering, stop it
        if (bluetoothAdapter.isDiscovering())
        {
            bluetoothAdapter.cancelDiscovery();
        }

        progressBarDeviceDiscovery.setProgress(0);
        stringArrayListBluetoothDevices.clear();
        bluetoothDevices.clear();
        listViewDiscoveredDevices.setAdapter(null);

        // Discover devices:
        boolean succesfullStartOfDiscovery = bluetoothAdapter.startDiscovery();
        return succesfullStartOfDiscovery;
    }

    /**
     * Event Listener to listen to the button clicks
     */
    private View.OnClickListener buttonClicklistener = new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            switch(v.getId())
            {
                case(R.id.bluetoothconnection_dlg_frag_imageButton_device_discovery):
                    discoverDevices();
                    break;
                case(R.id.bluetoothconnection_dlg_frag_connect_button):
                    onDialogDoneListener.onDialogDone(getTag(), false, selectedDevice);
                    savePreviousDevice(selectedDevice);
                    getDialog().dismiss();
                    break;
                case(R.id.bluetoothconnection_dlg_frag_cancel_button):
                    onDialogDoneListener.onDialogDone(getTag(), true, (BluetoothDevice) null); // Not some ambiguous now are we ? :p
                    getDialog().dismiss();
                    break;
                default:
                    break;
            }

        }
    };

    /**
     *  Event listener to listen to listview item clicks
     */
    private AdapterView.OnItemClickListener onListItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            if(position <= bluetoothDevices.size())
            {
                selectedDevice = bluetoothDevices.get(position);
                if(DEBUGMODE)
                    Log.d(DEBUGTAG, "selected: " + selectedDevice.getName() + " - " + selectedDevice.getAddress());
            }

        }
    };

    /**
     * Method to load previously used bluetooth device name from shared preferences
     * @return (String) the device name or default value
     */
    private String loadPreviousDeviceName()
    {
        String deviceName = preferences.getString(PREF_DEVICE_NAME, PREF_DEVICE_NAME_DEFAULT);
        return deviceName;
    }

    /**
     * Method to load previously used bluetooth device MAC address from shared preferences
     * @return (String) the device MAC address or default value
     */
    private String loadPreviousDeviceMAC()
    {
        String mac = preferences.getString(PREF_DEVICE_MAC, PREF_DEVICE_MAC_DEFAULT);
        return mac;
    }

    /**
     * Method to store (persistent) the selected device in shared preferences
     * @param bluetoothDevice The device to store 
     */
    private void savePreviousDevice(BluetoothDevice bluetoothDevice)
    {
        if(DEBUGMODE)
            Log.d(DEBUGTAG, "Saving device: " + bluetoothDevice.toString());
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(PREF_DEVICE_NAME, bluetoothDevice.getName());
        editor.putString(PREF_DEVICE_MAC, bluetoothDevice.getAddress());
        editor.commit();
    }
}
