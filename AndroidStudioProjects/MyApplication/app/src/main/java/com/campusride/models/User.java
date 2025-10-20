package com.campusride.models;

public class User {
    private String userId;
    private String name;
    private String email;
    private String mobile;
    private String regNo;
    private int ridesAsDriver;
    private int ridesAsPassenger;
    private boolean profileComplete;
    
    // Required empty constructor for Firebase
    public User() {}
    
    public User(String userId, String name, String email, String mobile, String regNo) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.regNo = regNo;
        this.ridesAsDriver = 0;
        this.ridesAsPassenger = 0;
        this.profileComplete = false;
    }
    
    // Getters and setters
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getMobile() {
        return mobile;
    }
    
    public void setMobile(String mobile) {
        this.mobile = mobile;
    }
    
    public String getRegNo() {
        return regNo;
    }
    
    public void setRegNo(String regNo) {
        this.regNo = regNo;
    }
    
    public int getRidesAsDriver() {
        return ridesAsDriver;
    }
    
    public void setRidesAsDriver(int ridesAsDriver) {
        this.ridesAsDriver = ridesAsDriver;
    }
    
    public int getRidesAsPassenger() {
        return ridesAsPassenger;
    }
    
    public void setRidesAsPassenger(int ridesAsPassenger) {
        this.ridesAsPassenger = ridesAsPassenger;
    }
    
    public boolean isProfileComplete() {
        return profileComplete;
    }
    
    public void setProfileComplete(boolean profileComplete) {
        this.profileComplete = profileComplete;
    }
}