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
 */
public class BluetoothConnectDialogFragment extends DialogFragment
{
    private final static String DEBUGTAG = "BluetoothConnectDialogFragment";
    private final static boolean DEBUGMODE = true;

    private IonDialogDoneListener onDialogDoneListener;

    private BluetoothAdapter bluetoothAdapter;
    private ArrayList<BluetoothDevice> discoveredDevices;
    private ArrayList<String> stringArrayListDiscoveredDevices;

    private Button startDeviceDiscoveryButton;
    private ProgressBar progressBarDeviceDiscovery;
    private TextView discoveredDevicesTextView;
    private ListView listViewDiscoveredDevices;
    private final static String NODISCOVEREDDEVICES = "No Devices Found!";
    private Button connectButton;
    private Button cancelButton;

    private BluetoothDevice selectedDevice;


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
        // interface, the following line will throw a
        // ClassCastException. This is the earliest you
        // can test if you have a well-behaved activity.
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
        stringArrayListDiscoveredDevices = new ArrayList<String>();
        discoveredDevices = new ArrayList<BluetoothDevice>();
        selectedDevice = null;

        // Register intent filters and broadcast receivers
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        getActivity().registerReceiver(broadcastReceiver, filter); // Don't forget to unregister during onDestroy
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        getActivity().registerReceiver(broadcastReceiver, filter);
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        getActivity().registerReceiver(broadcastReceiver, filter);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        getDialog().setTitle("Bluetooth Connection");
        View v = inflater.inflate(R.layout.bluetooth_connect_dialog_fragment, container);
        startDeviceDiscoveryButton = (Button) v.findViewById(R.id.bluetoothconnection_dlg_frag_discover_button);
        progressBarDeviceDiscovery = (ProgressBar) v.findViewById(R.id.bluetoothconnection_dlg_frag_progressBar_discovery);
        progressBarDeviceDiscovery.setMax(100);
        progressBarDeviceDiscovery.setProgress(0);
        discoveredDevicesTextView = (TextView) v.findViewById(R.id.bluetoothconnection_dlg_frag_textView_discovered_devices);
        discoveredDevicesTextView.setText("Devices found in range: ");
        listViewDiscoveredDevices = (ListView) v.findViewById(R.id.bluetoothconnection_dlg_frag_listView_discovered_devices);
        listViewDiscoveredDevices.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        connectButton = (Button) v.findViewById(R.id.bluetoothconnection_dlg_frag_connect_button);
        cancelButton = (Button) v.findViewById(R.id.bluetoothconnection_dlg_frag_cancel_button);

        listViewDiscoveredDevices.setOnItemClickListener(onListItemClickListener);
        startDeviceDiscoveryButton.setOnClickListener(buttonClicklistener);
        connectButton.setOnClickListener(buttonClicklistener);
        cancelButton.setOnClickListener(buttonClicklistener);
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

    private void populateDiscoveredDevicesList(ArrayList<BluetoothDevice> devicesFoundList)
    {

        if(devicesFoundList.isEmpty())
        {
            stringArrayListDiscoveredDevices.add(NODISCOVEREDDEVICES);
            listViewDiscoveredDevices.setChoiceMode(ListView.CHOICE_MODE_NONE);
        }
        else
        {
            for(BluetoothDevice device : devicesFoundList)
            {
                stringArrayListDiscoveredDevices.add(device.getName() + "\n" + device.getAddress());
            }
        }
        ArrayAdapter<String> discoveredDevicesList = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_activated_1, stringArrayListDiscoveredDevices);
        listViewDiscoveredDevices.setAdapter(discoveredDevicesList);
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();
            if(action.equals(BluetoothDevice.ACTION_FOUND))
            {
                BluetoothDevice btD = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                discoveredDevices.add(btD);
                progressBarDeviceDiscovery.setProgress(progressBarDeviceDiscovery.getProgress() + 10 );
            }
            else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))
            {
                progressBarDeviceDiscovery.setProgress(progressBarDeviceDiscovery.getMax());
                populateDiscoveredDevicesList(discoveredDevices);

            }
            else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED))
            {

            }
        }
    };

    private boolean discoverDevices()
    {
        Log.d(DEBUGTAG, "Starting Device Discovery");
        // If we're already discovering, stop it
        if (bluetoothAdapter.isDiscovering())
        {
            bluetoothAdapter.cancelDiscovery();
        }

        progressBarDeviceDiscovery.setProgress(0);
        stringArrayListDiscoveredDevices.clear();
        discoveredDevices.clear();
        listViewDiscoveredDevices.setAdapter(null);

        // Discover devices:
        boolean succesfullStartOfDiscovery = bluetoothAdapter.startDiscovery();
        return succesfullStartOfDiscovery;
    }

    private View.OnClickListener buttonClicklistener = new View.OnClickListener() {
        @Override
        public void onClick(View v)
        {
            switch(v.getId())
            {
                case(R.id.bluetoothconnection_dlg_frag_discover_button):
                    discoverDevices();
                    break;
                case(R.id.bluetoothconnection_dlg_frag_connect_button):
                    onDialogDoneListener.onDialogDone(getTag(), false, selectedDevice);
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

    private AdapterView.OnItemClickListener onListItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id)
        {
            if(position <= discoveredDevices.size())
            {
                selectedDevice = discoveredDevices.get(position);
                if(DEBUGMODE)
                    Log.d(DEBUGTAG, "selected: " + selectedDevice.getName() + " - " + selectedDevice.getAddress());
            }

        }
    };
}
