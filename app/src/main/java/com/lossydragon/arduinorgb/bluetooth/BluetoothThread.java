package com.lossydragon.arduinorgb.bluetooth;

/*
 *  Based off of https://github.com/hmartiro/android-arduino-bluetooth
 */

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.InputStream;
import java.io.OutputStream;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.UUID;

public class BluetoothThread extends Thread {

    private static final String TAG = BluetoothThread.class.getSimpleName();
    private static final char DELIMITER = '\n';

    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private final String address;
    private BluetoothSocket socket;

    private OutputStream outStream;
    private InputStream inStream;

    private final Handler readHandler;
    private final Handler writeHandler;

    private String rx_buffer = "";

    private int state;

    @SuppressLint("HandlerLeak")
    public BluetoothThread(String address, Handler handler) {

        this.address = address.toUpperCase();
        this.readHandler = handler;

        writeHandler = new Handler() {
            @Override
            public void handleMessage(Message message) {
                write((String) message.obj);
            }
        };
    }

    public boolean isConnected() {
        Log.v(TAG, "isConnected() called - result:" + state);
        return state == 1;
    }

    /**
     * Return the write handler for this connection. Messages received by this
     * handler will be written to the Bluetooth socket.
     */
    public Handler getWriteHandler() {
        return writeHandler;
    }

    /**
     * Connect to a remote Bluetooth socket, or throw an exception if it fails.
     */
    private void connect() throws Exception {

        Log.i(TAG, "Attempting connection to " + address + "...");

        // Get this device's Bluetooth adapter
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if ((adapter == null) || (!adapter.isEnabled())) {
            throw new Exception("Bluetooth adapter not found or not enabled!");
        }

        // Find the remote device
        BluetoothDevice remoteDevice = adapter.getRemoteDevice(address);

        // Create a socket with the remote device using this protocol
        socket = remoteDevice.createRfcommSocketToServiceRecord(uuid);

        // Make sure Bluetooth adapter is not in discovery mode
        adapter.cancelDiscovery();

        // Connect to the socket
        socket.connect();


        // Get input and output streams from the socket
        outStream = socket.getOutputStream();
        inStream = socket.getInputStream();

        Log.i(TAG, "Connected successfully to " + address + ".");
    }

    /**
     * Disconnect the streams and socket.
     */
    private void disconnect() {

        if (inStream != null) {
            try {
                inStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (outStream != null) {
            try {
                outStream.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (socket != null) {
            try {
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Return data read from the socket, or a blank string.
     */
    private String read() {

        String s = "";

        try {
            // Check if there are bytes available
            if (inStream.available() > 0) {

                // Read bytes into a buffer
                byte[] inBuffer = new byte[1024];
                int bytesRead = inStream.read(inBuffer);

                // Convert read bytes into a string
                s = new String(inBuffer, "ASCII");
                s = s.substring(0, bytesRead);
            }

        } catch (Exception e) {
            Log.e(TAG, "Read failed!", e);
        }

        return s;
    }

    /**
     * Write data to the socket.
     */
    private void write(String s) {

        try {
            // Add the delimiter
            s += DELIMITER;

            // Convert to bytes and write
            outStream.write(s.getBytes());
            Log.i(TAG, "[SENT] " + s);

        } catch (Exception e) {
            Log.e(TAG, "Write failed!", e);
        }
    }

    /**
     * Pass a message to the read handler.
     */
    private void sendToReadHandler(String s) {

        Message msg = Message.obtain();
        msg.obj = s;
        readHandler.sendMessage(msg);
        Log.i(TAG, "[RECV] " + s);
    }

    /**
     * Send complete messages from the rx_buffer to the read handler.
     */
    private void parseMessages() {

        int inx = rx_buffer.indexOf(DELIMITER);

        if (inx == -1)
            return;

        String s = rx_buffer.substring(0, inx);

        rx_buffer = rx_buffer.substring(inx + 1);

        // Send to read handler
        sendToReadHandler(s);

        parseMessages();
    }

    /**
     * Entry point when thread.start() is called.
     */
    @SuppressWarnings("StringConcatenationInLoop")
    public void run() {

        // Attempt to connect and exit the thread if it failed
        try {
            connect();
            sendToReadHandler("CONNECTED");
            state = 1;
        } catch (Exception e) {
            Log.e(TAG, "Failed to connect!", e);
            sendToReadHandler("CONNECTION FAILED");
            disconnect();
            state = 0;
            return;
        }

        // Loop continuously, reading data, until thread.interrupt() is called
        while (!this.isInterrupted()) {

            // Make sure things haven't gone wrong
            if ((inStream == null) || (outStream == null)) {
                Log.e(TAG, "Lost bluetooth connection!");
                break;
            }

            // Read data and add it to the buffer
            String s = read();
            if (s.length() > 0)
                rx_buffer += s;

            // Look for complete messages
            parseMessages();
        }

        // If thread is interrupted, close connections
        disconnect();
        sendToReadHandler("DISCONNECTED");
        state = 0;
    }
}