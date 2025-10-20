package com.campusride.utils;

import android.content.Context;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseUtil {
    private static final String TAG = "FirebaseUtil";
    private static FirebaseAuth mAuth;
    private static FirebaseDatabase mDatabase;
    private static boolean isInitialized = false;

    public static synchronized void initialize(Context context) {
        Log.d(TAG, "initialize() called");
        if (!isInitialized) {
            try {
                if (FirebaseApp.getApps(context).isEmpty()) {
                    FirebaseApp.initializeApp(context);
                    Log.d(TAG, "FirebaseApp initialized successfully");
                } else {
                    Log.d(TAG, "FirebaseApp already initialized");
                }
                isInitialized = true;
            } catch (Exception e) {
                Log.e(TAG, "Error initializing FirebaseApp", e);
            }
        } else {
            Log.d(TAG, "FirebaseUtil already initialized, skipping initialization");
        }
    }

    public static FirebaseAuth getAuth() {
        Log.d(TAG, "getAuth() called");
        if (mAuth == null) {
            try {
                mAuth = FirebaseAuth.getInstance();
                Log.d(TAG, "FirebaseAuth instance created successfully");
            } catch (Exception e) {
                Log.e(TAG, "Error creating FirebaseAuth instance", e);
            }
        }
        return mAuth;
    }

    public static FirebaseDatabase getDatabase() {
        Log.d(TAG, "getDatabase() called");
        try {
            // Always try to get a fresh instance to handle reconnection after logout
            if (mDatabase == null) {
                Log.d(TAG, "Creating new FirebaseDatabase instance");
                mDatabase = FirebaseDatabase.getInstance();
                Log.d(TAG, "FirebaseDatabase instance created successfully");
            } else {
                Log.d(TAG, "Using existing FirebaseDatabase instance");
            }
            return mDatabase;
        } catch (Exception e) {
            Log.e(TAG, "Error creating FirebaseDatabase instance", e);
            // Try to create a new instance in case of failure
            try {
                Log.d(TAG, "Attempting to recreate FirebaseDatabase instance");
                mDatabase = FirebaseDatabase.getInstance();
                Log.d(TAG, "FirebaseDatabase instance recreated successfully");
                return mDatabase;
            } catch (Exception recreateException) {
                Log.e(TAG, "Error recreating FirebaseDatabase instance", recreateException);
                return null;
            }
        }
    }
    
    // Method to reset Firebase instances after logout
    public static void resetFirebaseInstances() {
        Log.d(TAG, "Resetting Firebase instances");
        mAuth = null;
        mDatabase = null;
        isInitialized = false;
        Log.d(TAG, "Firebase instances reset completed");
    }
}