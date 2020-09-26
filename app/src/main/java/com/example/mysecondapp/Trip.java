package com.example.mysecondapp;

import java.util.Calendar;

public class Trip {

    private String startStation;
    private String endStation;
    private Calendar startTime;
    private Calendar endTime;
    private String bike;

    public Trip (String startStation, String endStation, Calendar startTime, Calendar endTime, String bike){
        this.startStation = startStation;
        this.endStation = endStation;
        this.startTime = startTime;
        this.endTime =endTime;
        this.bike = bike;
    }

}
