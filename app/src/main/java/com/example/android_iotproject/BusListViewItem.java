package com.example.android_iotproject;

//import android.graphics.drawable.Drawable;

public class BusListViewItem {

    private String name ;
    private int rssi;
    private String bell;


    public BusListViewItem(String name,int rssi)
    {
        this.name=name;
        this.rssi= rssi;

    }

    //public void setIcon(Drawable icon) {iconDrawable = icon ;}

    //public void setTitle(String title) {titleStr = title ;}

    // public Drawable getIcon() { return this.iconDrawable ; }
    public String getName()
    { return this.name ; }

    public int getRssi()
    { return this.rssi ; }

    public void setName(String name){
        this.name=name;
    }

    public void setRssi(int rssi){
        this.rssi=rssi;
    }
}