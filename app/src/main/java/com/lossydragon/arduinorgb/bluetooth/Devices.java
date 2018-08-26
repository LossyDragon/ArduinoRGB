package com.lossydragon.arduinorgb.bluetooth;

public class Devices {
    private final String deviceName;
    private final String deviceMac;

    public Devices(String deviceName, String deviceMac){
        this.deviceName = deviceName;
        this.deviceMac = deviceMac;
    }

    String getDeviceName() {
        return deviceName;
    }

    String getDeviceMac() {
        return deviceMac;
    }

}