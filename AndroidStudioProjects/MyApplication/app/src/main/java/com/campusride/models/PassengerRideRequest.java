package com.campusride.models;

import java.io.Serializable;

public class PassengerRideRequest implements Serializable {
    private String requestId;
    private String passengerId;
    private String passengerName;
    private String passengerMobile;
    private String passengerRegNo;
    private String source;
    private String destination;
    private String date;
    private String time;
    private String timestamp;
    private String status; // pending, accepted, completed, cancelled
    private String driverId; // null when pending, set when accepted
    private String driverName;
    private String driverMobile;
    private double sourceLat;
    private double sourceLng;
    private double destinationLat;
    private double destinationLng;
    private long createdAt;
    private long acceptedAt;
    private long completedAt;
    
    // Required empty constructor for Firebase
    public PassengerRideRequest() {}
    
    public PassengerRideRequest(String requestId, String passengerId, String passengerName, 
                               String passengerMobile, String passengerRegNo, String source, 
                               String destination, String date, String time, String timestamp) {
        this.requestId = requestId;
        this.passengerId = passengerId;
        this.passengerName = passengerName;
        this.passengerMobile = passengerMobile;
        this.passengerRegNo = passengerRegNo;
        this.source = source;
        this.destination = destination;
        this.date = date;
        this.time = time;
        this.timestamp = timestamp;
        this.status = "pending";
        this.driverId = null;
        this.driverName = null;
        this.driverMobile = null;
        this.sourceLat = 0.0;
        this.sourceLng = 0.0;
        this.destinationLat = 0.0;
        this.destinationLng = 0.0;
        this.createdAt = System.currentTimeMillis();
        this.acceptedAt = 0;
        this.completedAt = 0;
    }
    
    // New constructor with coordinates for map-based ride requests
    public PassengerRideRequest(String requestId, String passengerId, String passengerName, 
                               String passengerMobile, String passengerRegNo, String source, 
                               String sourceAddress, String destination, String destinationAddress,
                               String date, String time, String timestamp, double sourceLat, 
                               double sourceLng, double destinationLat, double destinationLng) {
        this.requestId = requestId;
        this.passengerId = passengerId;
        this.passengerName = passengerName;
        this.passengerMobile = passengerMobile;
        this.passengerRegNo = passengerRegNo;
        this.source = sourceAddress;  // Human-readable address for display
        this.destination = destinationAddress;  // Human-readable address for display
        this.date = date;
        this.time = time;
        this.timestamp = timestamp;
        this.status = "pending";
        this.driverId = null;
        this.driverName = null;
        this.driverMobile = null;
        this.sourceLat = sourceLat;  // Coordinates stored internally
        this.sourceLng = sourceLng;  // Coordinates stored internally
        this.destinationLat = destinationLat;  // Coordinates stored internally
        this.destinationLng = destinationLng;  // Coordinates stored internally
        this.createdAt = System.currentTimeMillis();
        this.acceptedAt = 0;
        this.completedAt = 0;
    }
    
    // Getters and setters
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public String getPassengerId() {
        return passengerId;
    }
    
    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
    }
    
    public String getPassengerName() {
        return passengerName;
    }
    
    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }
    
    public String getPassengerMobile() {
        return passengerMobile;
    }
    
    public void setPassengerMobile(String passengerMobile) {
        this.passengerMobile = passengerMobile;
    }
    
    public String getPassengerRegNo() {
        return passengerRegNo;
    }
    
    public void setPassengerRegNo(String passengerRegNo) {
        this.passengerRegNo = passengerRegNo;
    }
    
    public String getSource() {
        return source;
    }
    
    public void setSource(String source) {
        this.source = source;
    }
    
    public String getDestination() {
        return destination;
    }
    
    public void setDestination(String destination) {
        this.destination = destination;
    }
    
    public String getDate() {
        return date;
    }
    
    public void setDate(String date) {
        this.date = date;
    }
    
    public String getTime() {
        return time;
    }
    
    public void setTime(String time) {
        this.time = time;
    }
    
    public String getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getDriverId() {
        return driverId;
    }
    
    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }
    
    public String getDriverName() {
        return driverName;
    }
    
    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }
    
    public String getDriverMobile() {
        return driverMobile;
    }
    
    public void setDriverMobile(String driverMobile) {
        this.driverMobile = driverMobile;
    }
    
    public double getSourceLat() {
        return sourceLat;
    }
    
    public void setSourceLat(double sourceLat) {
        this.sourceLat = sourceLat;
    }
    
    public double getSourceLng() {
        return sourceLng;
    }
    
    public void setSourceLng(double sourceLng) {
        this.sourceLng = sourceLng;
    }
    
    public double getDestinationLat() {
        return destinationLat;
    }
    
    public void setDestinationLat(double destinationLat) {
        this.destinationLat = destinationLat;
    }
    
    public double getDestinationLng() {
        return destinationLng;
    }
    
    public void setDestinationLng(double destinationLng) {
        this.destinationLng = destinationLng;
    }
    
    public long getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
    
    public long getAcceptedAt() {
        return acceptedAt;
    }
    
    public void setAcceptedAt(long acceptedAt) {
        this.acceptedAt = acceptedAt;
    }
    
    public long getCompletedAt() {
        return completedAt;
    }
    
    public void setCompletedAt(long completedAt) {
        this.completedAt = completedAt;
    }
}