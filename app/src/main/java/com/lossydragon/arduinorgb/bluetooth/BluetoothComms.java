package com.lossydragon.arduinorgb.bluetooth;

/*
 *  Sampled from various threads on Stackoverflow and Arduino tutorial sites.
 *  This class performs various basic bluetooth discovery instructions.
 */

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;

import java.util.Set;

public class BluetoothComms {

    private final Activity context;
    private BluetoothAdapter adapter;
    //private boolean available = false;

    public BluetoothComms(Activity context) {
        this.context = context;
        getBluetoothAdapter();
    }

    //Return true if Bluetooth is enabled.
    public boolean isEnabled() {
        return adapter.isEnabled();
    }

    //Return true if Bluetooth is available.
    //public boolean isAvailable() {
    //    return available;
    //}

    //Get Bluetooth adapter from mobile phone.
    private void getBluetoothAdapter() {
        this.adapter = BluetoothAdapter.getDefaultAdapter();
    }

    //Request to enable Bluetooth if turned off.
    public void requestBluetooth() {
        this.context.startActivityForResult(new
                Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), 1);
    }

    //Get Bluetooth devices already paired to phone.
    public Set<BluetoothDevice> getPairedDevices() {
        return adapter.getBondedDevices();
    }

}
