package com.example.mysecondapp.trip;

// Trip Class contains all the relevant information of trips that have previously been
// taken by the user that is logged in
// This info is retrieved from the server and stored into the appropriate attributes of this class
public class Trip {

    private String date;
    private String startStation;
    private String endStation;
    private int startTime;
    private int endTime;
    private String bike;
    private int duration;

    public Trip (String date, String startStation, String endStation, int startTime, int endTime, String bike, int duration){
        this.date = date;
        this.startStation = startStation;
        this.endStation = endStation;
        this.startTime = startTime;
        this.endTime = endTime;
        this.bike = bike;
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }

    public int getEndTime() {
        return endTime;
    }

    public int getStartTime() {
        return startTime;
    }

    public String getBike() {
        return bike;
    }

    public String getDate() {
        return date;
    }

    public String getEndStation() {
        return endStation;
    }

    public String getStartStation() {
        return startStation;
    }

}
