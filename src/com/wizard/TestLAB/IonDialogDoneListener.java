package com.wizard.TestLAB;

import android.bluetooth.BluetoothDevice;

/**
 * Interface for Dialog Fragment callbacks
 * @author K. Gilissen
 * @version 1.0
 */
public interface IonDialogDoneListener
{
    void onDialogDone(String tag, boolean cancelled, String message);
    void onDialogDone(String tag, boolean cancelled, BluetoothDevice bluetoothDevice);

}
