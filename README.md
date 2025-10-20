# Campus Ride - Ride Sharing App

## Overview
Campus Ride is a ride-sharing application designed for college students to easily share rides within the campus and nearby areas. The app allows students to post rides as drivers or search for available rides as passengers.

## Features
1. **Login/Register Page**
   - Firebase Authentication (Email + Password login)
   - Accept any valid email address for registration

2. **Home Dashboard**
   - Navigation options: Post a Ride, Search Ride, Ride Requests, My Ride Requests, Profile
   - RecyclerView for recent rides

3. **Ride Creation Page (Driver)**
   - Input fields: Source, Destination (Google Maps Place Picker), Date & Time
   - Store ride details in Firebase Realtime Database

4. **Ride Request Page (Passenger)**
   - List available rides from Firebase (RecyclerView)
   - Search filter by destination and time
   - When a passenger clicks a ride, send a request stored in Firebase

5. **Ride Matching / Confirmation (Driver)**
   - Driver sees pending ride requests in a RecyclerView
   - Accept/Reject buttons update Firebase in real-time

6. **Ride Status Notifications (Passenger)**
   - Passengers can view their ride request statuses
   - Real-time notifications when requests are accepted/rejected

7. **Maps Page (Route View)**
   - Integrate Google Maps SDK
   - Show driver's route (source → destination)
   - Use Google Directions API to check if passenger's location is along the route

8. **Ride History Page**
   - Show all completed rides from Firebase
   - Display with date, time, and route details

9. **Profile / Settings Page**
   - Show student name, email, and rides count
   - Allow logout via Firebase Auth

## Setup Instructions

### Prerequisites
- Android Studio
- Firebase Account
- Google Maps API Key

### Firebase Setup
1. Create a new Firebase project at https://console.firebase.google.com/
2. Add an Android app to your Firebase project with package name `com.campusride`
3. Download the `google-services.json` file and place it in the `app/` directory
4. Enable Email/Password authentication in Firebase Authentication
5. Set up Firebase Realtime Database (see FIREBASE_SETUP.md for detailed instructions)

### Google Maps Setup
1. Obtain a Google Maps API key from Google Cloud Console
2. Enable the following APIs:
   - Maps SDK for Android
   - Directions API
3. Replace `YOUR_GOOGLE_MAPS_API_KEY_HERE` in `AndroidManifest.xml` with your actual API key

### Dependencies
The app uses the following major dependencies:
- Firebase Authentication
- Firebase Realtime Database
- Google Maps SDK
- Material Design Components

## Project Structure
```
app/
├── src/main/java/com/campusride/
│   ├── models/                 # Data models (User, Ride, RideRequest)
│   ├── utils/                  # Utility classes (FirebaseUtil)
│   ├── LoginActivity.java      # User authentication
│   ├── HomeActivity.java       # Main dashboard
│   ├── RideCreationActivity.java # Driver ride creation
│   ├── RideSearchActivity.java # Passenger ride search
│   ├── RideRequestsActivity.java # Driver ride requests
│   ├── PassengerRideRequestsActivity.java # Passenger request status
│   ├── MapsActivity.java       # Google Maps integration
│   ├── RideHistoryActivity.java # Ride history
│   ├── ProfileActivity.java    # User profile
│   ├── RideAdapter.java        # RecyclerView adapter for rides
│   ├── RideRequestAdapter.java # RecyclerView adapter for driver requests
│   └── PassengerRideRequestAdapter.java # RecyclerView adapter for passenger requests
└── src/main/res/
    ├── layout/                 # XML layout files
    ├── values/                 # String and color resources
    └── drawable/               # Image resources
```

## Implementation Notes
- The app uses Material Design components for a clean, modern UI
- Firebase is used for authentication and data storage
- Google Maps SDK is integrated for location services
- RecyclerView is used for efficient list display
- Real-time updates are implemented using Firebase listeners

## Detailed Documentation
- [Firebase Setup Instructions](FIREBASE_SETUP.md)
- [Ride Search Functionality](RIDE_SEARCH.md)
- [Ride Request Workflow](RIDE_REQUESTS.md)
- [Passenger Ride Requests](PASSENGER_REQUESTS.md)

## TODO
- Implement Google Maps Place Picker for location selection
- Implement Google Directions API for route matching
- Implement real-time updates with Firebase listeners
- Add unit tests
# campus-ride-1.0
