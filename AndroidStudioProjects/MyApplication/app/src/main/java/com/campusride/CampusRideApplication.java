package com.campusride;

import android.app.Application;
import android.util.Log;

import com.campusride.utils.FirebaseUtil;
import com.google.firebase.FirebaseApp;

public class CampusRideApplication extends Application {
    private static final String TAG = "CampusRideApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "CampusRideApplication.onCreate() called");
        
        // Ensure FirebaseApp is initialized first
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this);
            Log.d(TAG, "FirebaseApp initialized in Application.onCreate()");
        } else {
            Log.d(TAG, "FirebaseApp already initialized");
        }
        
        // Then initialize our FirebaseUtil
        FirebaseUtil.initialize(this);
        
        Log.d(TAG, "Firebase initialized in Application.onCreate()");
    }
}