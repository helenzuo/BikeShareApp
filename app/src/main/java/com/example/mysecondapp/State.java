package com.example.mysecondapp;

import com.example.mysecondapp.ui.map.MapFragment;

public class State {

    public static final int START_BOOKING_STATE = 0;
    public static final int RESERVE_BIKE_SELECTION_STATE = 1;
    public static final int DEPARTURE_STATION_SELECTED_STATE = 2;
    public static final int QR_SCANNED_STATE = 3;
    public static final int RESERVE_DOCK_SELECTION_STATE = 4;
    public static final int ARRIVAL_STATION_SELECTED_STATE = 5;
    public static final int BIKE_DOCKED_STATE = 6;

    public static final int LOGGED_OUT = 0;
    public static final int LOGGED_IN = 1;

    public static final String LOG_KEY = "LOG_KEY";
    public static final String USER_KEY = "USER_KEY";
    public static final String STATE_KEY = "STATE_KEY";

    private int bookingState;
    private Station departingStation, arrivalStation, dockedStation;
    private String departureTime, arrivalTime; //in minutes of the day
    private int loggedState;
    private User user;

    private MapFragment mapFragment;

    public void setMapFragment(MapFragment mapFragment){
        this.mapFragment = mapFragment;
    }

    public MapFragment getMapFragment() {return  mapFragment;}

    public void setUser(User user){ this.user = user;}

    public User getUser(){return user;}

    public int getLoggedState() {
        return loggedState;
    }

    public void logIn(User user){
        loggedState = LOGGED_IN;
        setUser(user);
    }

    public void logOut(){
        loggedState = LOGGED_OUT ;
        setUser(null);
    }

    public void bookingStateTransition(boolean forward) {
        if (forward) {
            bookingState++;
            if (bookingState == 7){
                resetState();
            }
        } else {
            bookingState--;
        }
    }

    public int getBookingState() {
        return bookingState;
    }

    public void setBookingState(int state){
        bookingState = state;
    }

    public String getDepartureTime() {
        return departureTime;
    }

    public String getArrivalTime() {
        return arrivalTime;
    }

    public Station getDockedStation() {
        return dockedStation;
    }

    public Station getArrivalStation() {
        return arrivalStation;
    }

    public Station getDepartingStation() {
        return departingStation;
    }

    public void setArrivalStation(Station arrivalStation) {
        this.arrivalStation = arrivalStation;
    }

    public void setDockedStation(Station dockedStation) {
        this.dockedStation = dockedStation;
    }

    public void setArrivalTime(String arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public void setDepartingStation(Station departingStation) {
        this.departingStation = departingStation;
    }

    public void setDepartureTime(String departureTime) {
        this.departureTime = departureTime;
    }

    public void resetState(){
        bookingState = 0;
        departingStation = null;
        arrivalStation = null;
        departureTime = null;
        arrivalTime = null;
        dockedStation = null;
    }

}

