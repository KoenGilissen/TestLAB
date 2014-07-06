package com.wizard.TestLAB;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Koen on 29-Jun-14.
 *
 * Based on Android example BluetoothChat project
 * This class does all the work for setting up and managing Bluetooth connections with other devices.
 *
 */
public class BlueService
{
    //Debugging
    private static final String DEBUGTAG = "BlueService";
    private static final boolean DEBUGGINGMODE = true;
    // Name for the SDP record when creating server socket
    private static final String NAME = "BluetoothConnection";
    // Unique UUID for this application
    // Very important to use a SPP (Serial Port Protocol) UUID ...
    // If you are connecting to a Bluetooth serial board then try using the well-known SPP UUID 00001101-0000-1000-8000-00805F9B34FB
    // http://developer.android.com/reference/android/bluetooth/BluetoothDevice.html
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // Member fields
    private final BluetoothAdapter bluetoothAdapter;
    private final Handler handler;
    private AcceptThread acceptThread;
    private ConnectThread connectThread;
    private ConnectedThread connectedThread;
    private int blueState;
    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    /**
     * Constructor. Prepares a new Bluetooth session.
     * @param context  The UI Activity Context
     * @param handler  A Handler to send messages back to the UI Activity
     */
    public BlueService(Context context, Handler handler)
    {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        blueState = STATE_NONE;
        this.handler = handler;
    }

    /**
     * Set the current state of the connection
     * @param state  An integer defining the current connection state
     */
    private synchronized void setState(int state)
    {
        if (DEBUGGINGMODE)
        {
            Log.d(DEBUGTAG, "setState() " + blueState + " -> " + state);
        }
        this.blueState = state;
        // Give the new state to the Handler so the UI Activity can update
        handler.obtainMessage(MainActivity.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state. */
    public synchronized int getState()
    {
        return blueState;
    }

    /**
     * Start the Bluetooth service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume() */
    public synchronized void start()
    {
        if (DEBUGGINGMODE)
        {
            Log.d(DEBUGTAG, "start");
        }
        // Cancel any thread attempting to make a connection
        if (connectThread != null)
        {
            connectThread.cancel();
            connectThread = null;
        }
        // Cancel any thread currently running a connection
        if (connectedThread != null)
        {
            connectedThread.cancel();
            connectedThread = null;
        }
        // Start the thread to listen on a BluetoothServerSocket
        if (acceptThread == null)
        {
            acceptThread = new AcceptThread();
            acceptThread.start();
        }
        setState(STATE_LISTEN);
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device) {
        if (DEBUGGINGMODE)
        {
            Log.d(DEBUGTAG, "connect to: " + device);
        }
        // Cancel any thread attempting to make a connection
        if (blueState == STATE_CONNECTING)
        {
            if (connectThread != null)
            {
                connectThread.cancel();
                connectThread = null;
            }
        }
        // Cancel any thread currently running a connection
        if (connectedThread != null)
        {
            connectedThread.cancel();
            connectedThread = null;
        }
        // Start the thread to connect with the given device
        connectThread = new ConnectThread(device);
        connectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (DEBUGGINGMODE)
        {
            Log.d(DEBUGTAG, "connected");
        }
        // Cancel the thread that completed the connection
        if (connectThread != null)
        {
            connectThread.cancel();
            connectThread = null;
        }
        // Cancel any thread currently running a connection
        if (connectedThread != null)
        {
            connectedThread.cancel();
            connectedThread = null;
        }
        // Cancel the accept thread because we only want to connect to one device
        if (acceptThread != null)
        {
            acceptThread.cancel();
            acceptThread = null;
        }
        // Start the thread to manage the connection and perform transmissions
        connectedThread = new ConnectedThread(socket);
        connectedThread.start();
        // Send the name of the connected device back to the UI Activity
        Message msg = handler.obtainMessage(MainActivity.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        Log.d(DEBUGTAG, msg.toString());
        handler.sendMessage(msg);
        setState(STATE_CONNECTED);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (DEBUGGINGMODE)
        {
            Log.d(DEBUGTAG, "stop");
        }
        if (connectThread != null)
        {
            connectThread.cancel();
            connectThread = null;
        }
        if (connectedThread != null)
        {
            connectedThread.cancel();
            connectedThread = null;
        }
        if (acceptThread != null)
        {
            acceptThread.cancel();
            acceptThread = null;
        }
        setState(STATE_NONE);
    }

    /**
     *
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out)
    {
        // Create temporary object
        ConnectedThread r;
        // Synchronize a copy of the ConnectedThread
        synchronized (this)
        {
            if (blueState != STATE_CONNECTED)
                return;
            r = connectedThread;
        }
        // Perform the write unsynchronized
        r.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed()
    {
        setState(STATE_LISTEN);
        // Send a failure message back to the Activity
        Message msg = handler.obtainMessage(MainActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.TOAST, "Unable to connect device");
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost()
    {
        setState(STATE_LISTEN);
        // Send a failure message back to the Activity
        Message msg = handler.obtainMessage(MainActivity.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(MainActivity.TOAST, "Device connection was lost");
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    /*-------------------------------------------------------------------------------*/

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread
    {
        // The local server socket
        private final BluetoothServerSocket mmServerSocket;
        public AcceptThread()
        {
            BluetoothServerSocket tmp = null;
            // Create a new listening server socket
            try
            {
                tmp = bluetoothAdapter.listenUsingInsecureRfcommWithServiceRecord(NAME, SPP_UUID);
            }
            catch (IOException e)
            {
                Log.e(DEBUGTAG, "listen() failed", e);
            }
            mmServerSocket = tmp;
        }

        public void run()
        {
            if (DEBUGGINGMODE)
            {
                Log.d(DEBUGTAG, "BEGIN mAcceptThread" + this);
            }
            setName("AcceptThread");
            BluetoothSocket socket = null;
            // Listen to the server socket if we're not connected
            while (blueState != STATE_CONNECTED)
            {
                try
                {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mmServerSocket.accept();
                }
                catch (IOException e)
                {
                    Log.e(DEBUGTAG, "accept() failed", e);
                    break;
                }
                // If a connection was accepted
                if (socket != null) {
                    synchronized (BlueService.this) {
                        switch (blueState)
                        {
                            case STATE_LISTEN:
                            case STATE_CONNECTING:
                                // Situation normal. Start the connected thread.
                                connected(socket, socket.getRemoteDevice());
                                break;
                            case STATE_NONE:
                            case STATE_CONNECTED:
                                // Either not ready or already connected. Terminate new socket.
                                try {
                                    socket.close();
                                } catch (IOException e) {
                                    Log.e(DEBUGTAG, "Could not close unwanted socket", e);
                                }
                                break;
                        }
                    }
                }
            }
            if (DEBUGGINGMODE)
                Log.d(DEBUGTAG, "END mAcceptThread");
        }
        public void cancel() {
            if (DEBUGGINGMODE)
                Log.d(DEBUGTAG, "cancel " + this);
            try
            {
                mmServerSocket.close();
            }
            catch (IOException e)
            {
                Log.d(DEBUGTAG, "close() of server failed", e);
            }
        }
    }


    /*-------------------------------------------------------------------------------*/

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread
    {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        public ConnectThread(BluetoothDevice device)
        {
            mmDevice = device;
            BluetoothSocket tmp = null;
            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try
            {
                tmp = device.createInsecureRfcommSocketToServiceRecord(SPP_UUID);
                Log.d(DEBUGTAG, "Created insecure RFcomm Socket ;-)");
            }
            catch (IOException e)
            {
                Log.d(DEBUGTAG, "create() failed", e);
            }
            mmSocket = tmp;
        }
        public void run() {
            Log.d(DEBUGTAG, "BEGIN mConnectThread");
            setName("ConnectThread");
            // Always cancel discovery because it will slow down a connection
            bluetoothAdapter.cancelDiscovery();
            // Make a connection to the BluetoothSocket
            try
            {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();

            }
            catch (IOException e)
            {
                Log.d(DEBUGTAG, "Trying to connect FAILED");
                Log.d(DEBUGTAG, "Error: " + e.toString());
                connectionFailed();
                // Close the socket
                try
                {
                    mmSocket.close();
                }
                catch (IOException e2)
                {
                    Log.d(DEBUGTAG, "unable to close() socket during connection failure", e2);
                }
                // Start the service over to restart listening mode
                BlueService.this.start();
                return;
            }
            // Reset the ConnectThread because we're done
            synchronized (BlueService.this)
            {
                connectThread = null;
            }
            // Start the connected thread
            connected(mmSocket, mmDevice);
        }
        public void cancel()
        {
            try
            {
                mmSocket.close();
            } catch (IOException e) {
                Log.d(DEBUGTAG, "close() of connect socket failed", e);
            }
        }
    }

    /*-------------------------------------------------------------------------------*/

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread
    {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;
        public ConnectedThread(BluetoothSocket socket)
        {
            Log.d(DEBUGTAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            // Get the BluetoothSocket input and output streams
            try
            {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
                Log.d(DEBUGTAG, "Created I/O Streams");
            }
            catch (IOException e)
            {
                Log.d(DEBUGTAG, "temp sockets not created", e);
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        public void run()
        {
            Log.d(DEBUGTAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;
            // Keep listening to the InputStream while connected
            while (true)
            {
                try
                {
                    /* From Javadoc because Android documentation SUCKS!!!
                     * Reads some number of bytes from the input stream and stores them into the buffer array 'buffer'
                     * The number of bytes actually read is returned as an integer
                     * Returns -1 if the end of the stream has been reached.
                     * Blocks until one byte has been read, the end of the source stream is detected or an exception is thrown.
                     */
                    bytes = mmInStream.read(buffer); //Blocking Call
                    byte[] bufferCopy = new byte[bytes]; //Create 'bufferCopy' byte array of size 'bytes'
                    System.arraycopy(buffer, 0, bufferCopy, 0, bytes); // ( Object src, int sourcePosition, Object dst, int destinationPosition, int length)

                    // Send the obtained bytes to the UI Activity
                    handler.obtainMessage(MainActivity.MESSAGE_READ, bytes, -1, bufferCopy).sendToTarget();
                }
                catch (IOException e)
                {
                    Log.d(DEBUGTAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         */
        public void write(byte[] buffer)
        {
            try {
                mmOutStream.write(buffer);
                // Share the sent message back to the UI Activity
                handler.obtainMessage(MainActivity.MESSAGE_WRITE, -1, -1, buffer).sendToTarget();
            }
            catch (IOException e)
            {
                Log.d(DEBUGTAG, "Exception during write", e);
            }
        }
        public void cancel()
        {
            try
            {
                mmSocket.close();
            }
            catch (IOException e)
            {
                Log.d(DEBUGTAG, "close() of connect socket failed", e);
            }
        }
    }

}
