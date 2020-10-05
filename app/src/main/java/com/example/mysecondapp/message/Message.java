package com.example.mysecondapp.message;

public class Message {
    private String type;
    private String message;

    public Message(String type, String message){
        this.type = type;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }
}
