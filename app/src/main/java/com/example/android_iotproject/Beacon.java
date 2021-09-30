package com.example.android_iotproject;

public class Beacon {
    private String address;
    private int rssi;

    public Beacon(String address, int rssi){
        this.address = address;
        this.rssi = rssi;
    }

    public String getAddress(){
        return address;
    }

    public int getRssi(){
        return rssi;
    }
}
