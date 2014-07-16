package com.wizard.TestLAB;

import android.app.Activity;
import android.app.DialogFragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Koen on 06-Jul-14.
 */
public class BluetoothConnectDialogFragment extends DialogFragment
{
    private final static String DEBUGTAG = "BluetoothConnectDialogFragment";
    private final static boolean DEBUGMODE = true;

    private BluetoothAdapter bluetoothAdapter;
    private Set<BluetoothDevice> pairedDevices;

    private TextView pairedDevicesTextView;
    private ListView listViewPairedDevices;
    private final static String NOPAIREDDEVICES = "No Paired Devices!";

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
            IonDialogDoneListener test = (IonDialogDoneListener) activity;
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
        pairedDevices = bluetoothAdapter.getBondedDevices();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        super.onCreateView(inflater, container, savedInstanceState);
        getDialog().setTitle("Bluetooth Connection");
        View v = inflater.inflate(R.layout.bluetooth_connect_dialog_fragment, container);

        pairedDevicesTextView = (TextView) v.findViewById(R.id.textView_paired_devices);
        // List view
        listViewPairedDevices = (ListView) v.findViewById(R.id.listView_dlgfrag_paired_devices);
        // Only ONE device can be selected...
        // Define the choice behavior for the view:
        // Does not trigger the event ()
        listViewPairedDevices.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        pairedDevicesTextView = (TextView) v.findViewById(R.id.textView_paired_devices);
        pairedDevicesTextView.setText("Paired Devices:");
        this.populatePairedDevicesList(pairedDevices);
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
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Method to populate the list view
     * @param pairedDevices the list of paired device to populate the listview with
     */
    private void populatePairedDevicesList(Set<BluetoothDevice> pairedDevices)
    {
        ArrayList<String> pd = new ArrayList<String>();
        if(pairedDevices.size() == 0)
        {
            // Display 1 entry to list as msg to user
            pd.add(NOPAIREDDEVICES);
            // Nothing to select
            listViewPairedDevices.setChoiceMode(ListView.CHOICE_MODE_NONE);
        }
        else
        {
            for(BluetoothDevice device : pairedDevices)
            {
                pd.add(device.getName() +"\n"+ device.getAddress());
            }
        }
        pd.add("another device");
        pd.add("yet another device");

        //TODO change list layout
        ArrayAdapter<String> pairedDevicesList = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_activated_1, pd);
        listViewPairedDevices.setAdapter(pairedDevicesList);
    }
}
