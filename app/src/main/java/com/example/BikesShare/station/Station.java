package com.example.BikesShare.station;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import com.example.BikesShare.MainActivity;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

// Station object contains all the important information associated to each station that is retrieved
// from the server.
// Info is originally in JSON format and then assigned to the corresponding attribute here
public class Station implements Comparable<Station>{
     private String id;
     private String mName;
     private LatLng mLocation;
     private int mCapacity;
     private int mOccupancy;
     private float fillLevel;
     private float distanceFrom;
     private String mAddress;
     private boolean favourite;
     private Context context;
     private int predictedOcc;
     private int estArr;


    public Station(Context context, double lat, double lon, int cap, String id) throws IOException {
        this.context = context;
        this.id = id;
        mLocation = new LatLng(lat, lon);
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(context, Locale.getDefault());
        addresses = geocoder.getFromLocation(lat, lon, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        mName = addresses.get(0).getThoroughfare();
        if (mName == null){
            mName = addresses.get(0).getFeatureName();
        }
        String tempName = mName;
        int i = 1;
        while (((MainActivity)context).stationNames.contains(tempName)){
            tempName  = mName + " " + i;
            i++;
        }
        mName = tempName;
        mAddress = addresses.get(0).getAddressLine(0);
        mCapacity = cap;
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

    public void toggleFavourite(){
         favourite = !favourite;
    }

    public void setAsFavourite(){
        favourite = true;
    }

    public boolean getFavourite(){
         return favourite;
    }


    public void setOccupancy(int newOcc){
         mOccupancy = newOcc;
         fillLevel = (float) newOcc/mCapacity;
    }

    public float getFillLevel(){
        return fillLevel;
    }

    public String getAddress(){
         return mAddress;
    }

    public String getId(){ return id; }

    public void updateDistanceFrom(Location currentLocation){
        Location stationLocation = new Location("");
        stationLocation.setLatitude(mLocation.latitude);
        stationLocation.setLongitude(mLocation.longitude);
        distanceFrom = currentLocation.distanceTo(stationLocation);
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

    public float getDistanceFloat(){
         return distanceFrom;
    }

    public void setPredictedOcc(int predictedOcc) {this.predictedOcc = predictedOcc; }

    public int getPredictedOcc() { return predictedOcc;}

    public void setEstArr(int estArr){this.estArr = estArr;}

    public int getEstArr(){return  estArr;}

    // default sort
    @Override
    public int compareTo(Station station) {
        if (favourite && !station.getFavourite()){
            return -1;
        } else if (!favourite && station.getFavourite()) {
            return 1;
        } else {
            return mName.compareToIgnoreCase(station.getName());
        }
    }
}
