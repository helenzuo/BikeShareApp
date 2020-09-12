package com.example.mysecondapp;

public class User {
    private String name;
    private String email;
    private String dob;
    private String mobile;
    private String userName;
    private int gender = 2;

    public User(String name, String email, String mobile, String userName){
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
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

    public void setGender(int gender) {
        this.gender = gender;
    }

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
