package com.lossydragon.arduinorgb.bluetooth

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.lossydragon.arduinorgb.R
import kotlinx.android.synthetic.main.diaglog_bt_connect.view.*


class BluetoothConnectAdapter(context: Context, private val devicesList: List<Devices>) :
        RecyclerView.Adapter<BluetoothConnectAdapter.DeviceViewHolder>() {

    var callback: ConnectCallback? = null

    init {
        callback = context as ConnectCallback
    }

    interface ConnectCallback {
        fun onCallback(string: String)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.diaglog_bt_connect, parent, false)
        return DeviceViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.deviceName.text = devicesList[position].deviceName
        holder.deviceMac.text = devicesList[position].deviceMac

        holder.itemView.setOnClickListener {
            callback!!.onCallback(devicesList[position].deviceMac + "," + devicesList[position].deviceName)
        }
    }

    override fun getItemCount() = devicesList.size

    inner class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var deviceName: TextView = itemView.device_name
        var deviceMac: TextView = itemView.device_mac
    }
}
