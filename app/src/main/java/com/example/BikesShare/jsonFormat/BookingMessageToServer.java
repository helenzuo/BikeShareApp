package com.example.BikesShare.jsonFormat;

// Object that is to be converted to JSON format to request info from the server using
// the TCP socket
public class BookingMessageToServer {
    private String key;
    private String id;
    private int time;
    private int distance;

    public BookingMessageToServer(String key, String id, int time, int distance){
        this.key = key;
        this.id = id;
        this.time = time;
        this.distance = distance;
    }

    public String getKey(){ return key;}
}
