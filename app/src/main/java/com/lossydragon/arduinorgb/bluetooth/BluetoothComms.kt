package com.lossydragon.arduinorgb.bluetooth

/*
 *  Sampled from various threads on Stackoverflow and Arduino tutorial sites.
 *  This class performs various basic bluetooth discovery instructions.
 */

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent

class BluetoothComms(private val context: Activity) {
    private var adapter: BluetoothAdapter? = null

    //Return true if Bluetooth is enabled.
    val isEnabled: Boolean
        get() = adapter!!.isEnabled

    //Get Bluetooth devices already paired to phone.
    val pairedDevices: Set<BluetoothDevice>
        get() = adapter!!.bondedDevices

    init {
        getBluetoothAdapter()
    }

    //Return true if Bluetooth is available.
    //public boolean isAvailable() {
    //    return available;
    //}

    //Get Bluetooth adapter from mobile phone.
    private fun getBluetoothAdapter() {
        this.adapter = BluetoothAdapter.getDefaultAdapter()
    }

    //Request to enable Bluetooth if turned off.
    fun requestBluetooth() {
        this.context.startActivityForResult(Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), 1)
    }

}
