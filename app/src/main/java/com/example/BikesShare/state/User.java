package com.example.BikesShare.state;

import java.util.ArrayList;

// All the information unique to the client currently logged in. This information is retrieved from the
// server in JSON format and then stored into this class for the app to use
public class User {
    private String name;
    private String email;
    private String dob;
    private String mobile;
    private String username;
    private String password;
    private String newUser;
    private ArrayList<String> favStations;
    private int gender;

    public static final int MALE = 1;
    public static final int FEMALE = 0;
    public static final int NEUTRAL = 2;

    public User(String name, String email, String mobile, String userName, String password, String newUser){
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.username = userName;
        this.password = password;
        this.newUser = newUser;
        this.gender = NEUTRAL;
        this.dob = "";
        favStations = new ArrayList<>();
    }

    public void addFavStation(String station){
        favStations.add(station);
    }

    public void removeFavStation(String station){
        favStations.remove(station);
    }

    public ArrayList<String> getFavStations() {
        return favStations;
    }

    public String getNewUser() {
        return newUser;
    }

    public void confirmUser() {
        this.newUser = "loggedIn";
    }

    public void saveUser() {
        this.newUser = "logIn";
    }

    public String getUserName() {
        return username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDob(String dob){
        this.dob = dob;
    }

    public void setGender(int gender) { this.gender = gender; }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getDob() {
        return dob;
    }

    public String getEmail() {
        return email;
    }

    public int getGender() {
        return gender;
    }

    public String getMobile() {
        return mobile;
    }

    public String getName() {
        return name;
    }
}
