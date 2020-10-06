package com.example.BikesShare.message;

// Message class that is used to display the messages in the chatlist format listviews in the
// booking pages
// type = "in" or "out"
// message is what is displayed in the chat bubbles
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
