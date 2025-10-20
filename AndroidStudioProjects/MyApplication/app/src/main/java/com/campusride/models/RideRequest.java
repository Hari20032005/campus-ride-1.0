package com.campusride.models;

public class RideRequest {
    private String requestId;
    private String rideId;
    private String passengerId;
    private String driverId;
    private String status; // pending, accepted, rejected
    private String driverName;
    private String driverRegNo;
    private String driverMobile; // Only populated when request is accepted
    private String passengerName;
    private String passengerMobile;
    private String passengerRegNo;
    private String source;
    private String destination;
    private String date;
    private String time;
    private double pickupLat;
    private double pickupLng;
    private String pickupLocation;
    
    // Required empty constructor for Firebase
    public RideRequest() {}
    
    public RideRequest(String requestId, String rideId, String passengerId, String driverId,
                       String status, String driverName, String driverRegNo, String driverMobile,
                       String passengerName, String passengerMobile, String passengerRegNo,
                       String source, String destination, String date, String time) {
        this.requestId = requestId;
        this.rideId = rideId;
        this.passengerId = passengerId;
        this.driverId = driverId;
        this.status = status;
        this.driverName = driverName;
        this.driverRegNo = driverRegNo;
        this.driverMobile = driverMobile;
        this.passengerName = passengerName;
        this.passengerMobile = passengerMobile;
        this.passengerRegNo = passengerRegNo;
        this.source = source;
        this.destination = destination;
        this.date = date;
        this.time = time;
    }
    
    // Constructor for the old format (may be used in existing code)
    public RideRequest(String requestId, String rideId, String driverId, String passengerId,
                       String passengerName, double pickupLat, double pickupLng, String pickupLocation) {
        this.requestId = requestId;
        this.rideId = rideId;
        this.driverId = driverId;
        this.passengerId = passengerId;
        this.passengerName = passengerName;
        this.pickupLat = pickupLat;
        this.pickupLng = pickupLng;
        this.pickupLocation = pickupLocation;
        this.status = "pending";
    }
    
    // Getters and setters
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public String getRideId() {
        return rideId;
    }
    
    public void setRideId(String rideId) {
        this.rideId = rideId;
    }
    
    public String getPassengerId() {
        return passengerId;
    }
    
    public void setPassengerId(String passengerId) {
        this.passengerId = passengerId;
    }
    
    public String getDriverId() {
        return driverId;
    }
    
    public void setDriverId(String driverId) {
        this.driverId = driverId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getDriverName() {
        return driverName;
    }
    
    public void setDriverName(String driverName) {
        this.driverName = driverName;
    }
    
    public String getDriverRegNo() {
        return driverRegNo;
    }
    
    public void setDriverRegNo(String driverRegNo) {
        this.driverRegNo = driverRegNo;
    }
    
    public String getDriverMobile() {
        return driverMobile;
    }
    
    public void setDriverMobile(String driverMobile) {
        this.driverMobile = driverMobile;
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
    
    public double getPickupLat() {
        return pickupLat;
    }
    
    public void setPickupLat(double pickupLat) {
        this.pickupLat = pickupLat;
    }
    
    public double getPickupLng() {
        return pickupLng;
    }
    
    public void setPickupLng(double pickupLng) {
        this.pickupLng = pickupLng;
    }
    
    public String getPickupLocation() {
        return pickupLocation;
    }
    
    public void setPickupLocation(String pickupLocation) {
        this.pickupLocation = pickupLocation;
    }
}