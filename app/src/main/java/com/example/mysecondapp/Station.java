package com.example.mysecondapp;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

public class Station {
     private String mName;
     private LatLng mLocation;
     private int mCapacity;
     private int mOccupancy;
     private float fillLevel;
     private float distanceFrom;
     private String mAddress;

     public Station(String name, double longitude, double latitude, int capacity, int occupancy, Context context) throws IOException {
        mName = name;
        mLocation = new LatLng(latitude, longitude);
        mAddress = latLngToAddress(mLocation, context);
        System.out.println(mAddress);
        mCapacity = capacity;
        mOccupancy = occupancy;
        fillLevel = (float) occupancy/capacity;
     }
    public String getName(){
         return mName;
     }

    public LatLng getLocation(){
        return mLocation;
    }

    public int getCapacity(){
         return mCapacity;
     }

    public int getOccupancy(){
        return mOccupancy;
    }

    public void setOccupancy(int newOcc){
         mOccupancy = newOcc;
         fillLevel = (float) newOcc/mCapacity;
    }

    public float getFillLevel(){
        return fillLevel;
    }

    private String latLngToAddress(LatLng latLng, Context context) throws IOException {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(context, Locale.getDefault());
        addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        return addresses.get(0).getAddressLine(0);
    }

    public String getAddress(){
         return mAddress;
    }

    public void updateDistanceFrom(Location currentLocation){
        Location stationLocation = new Location("");
        stationLocation.setLatitude(mLocation.latitude);
        stationLocation.setLongitude(mLocation.longitude);
        distanceFrom = currentLocation.distanceTo(stationLocation);
        System.out.println(distanceFrom);
    }

    public String getDistanceFrom(){
         if (distanceFrom >= 1000) {
             DecimalFormat df = new DecimalFormat("0.00");
             float distanceInKm = distanceFrom / 1000;
             return df.format(distanceInKm) + "km";
         } else {
             return Math.round(distanceFrom) + "m";
         }
    }

}
