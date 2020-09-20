package com.example.mysecondapp;

public class State {

    public static final int START_BOOKING_STATE = 0;
    public static final int RESERVE_BIKE_SELECTION_STATE = 1;
    public static final int DEPARTURE_STATION_SELECTED_STATE = 2;
    public static final int QR_SCANNED_STATE = 3;
    public static final int RESERVE_DOCK_SELECTION_STATE = 4;
    public static final int ARRIVAL_STATION_SELECTED_STATE = 5;
    public static final int BIKE_DOCKED_STATE = 6;


    private int bookingState;
    private Station departingStation, arrivalStation;
    private String departureTime, arrivalTime; //in minutes of the day

    public void bookingStateTransition(boolean forward) {
        if (forward) {
            bookingState++;
            if (bookingState == 7){
                resetState();
            }
        } else {
            bookingState--;
            if (bookingState == RESERVE_BIKE_SELECTION_STATE){
                departureTime = null;
                departingStation = null;
            }
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

    public Station getArrivalStation() {
        return arrivalStation;
    }

    public Station getDepartingStation() {
        return departingStation;
    }

    public void setArrivalStation(Station arrivalStation) {
        this.arrivalStation = arrivalStation;
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
    }
}
