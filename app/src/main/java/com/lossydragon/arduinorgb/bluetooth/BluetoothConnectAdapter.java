package com.lossydragon.arduinorgb.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lossydragon.arduinorgb.R;

import java.util.List;

public class BluetoothConnectAdapter extends RecyclerView.Adapter<BluetoothConnectAdapter.ListVH> {

    private final static String TAG = BluetoothAdapter.class.getSimpleName();
    private final List<Devices> devicesList;
    private DeviceCallback deviceCallback;

    @NonNull
    @Override
    public ListVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.diaglog_bt_connect, parent, false);

        return new ListVH(itemView, this);
    }

    @Override
    public void onBindViewHolder(@NonNull ListVH holder, int position) {
        Devices devices = devicesList.get(position);
        holder.name.setText(devices.getDeviceName());
        holder.mac.setText(devices.getDeviceMac());
    }

    @Override
    public int getItemCount() {
        return devicesList.size();
    }
    
    public void setCallbacks(DeviceCallback macCallback){
        this.deviceCallback = macCallback;
    }

    public interface DeviceCallback {
        void onItemClicked(String itemIndex);
    }

    static class ListVH extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView name;
        final TextView mac;
        final BluetoothConnectAdapter adapter;

        ListVH(View view, BluetoothConnectAdapter adapter) {
            super(view);
            name = view.findViewById(R.id.device_name);
            mac = view.findViewById(R.id.device_mac);
            this.adapter = adapter;

            view.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            if (adapter.deviceCallback == null) {
                Log.d(TAG, "Returned NULL");
                return;
            }
            
            String mac_addr = ((TextView) view.findViewById(R.id.device_mac)).getText().toString();
            String dev_name = ((TextView) view.findViewById(R.id.device_name)).getText().toString();
            adapter.deviceCallback.onItemClicked(mac_addr+","+dev_name);

        }
    }

    public BluetoothConnectAdapter(List<Devices> devicesList) {
        this.devicesList = devicesList;
    }
}