package com.campusride;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.campusride.utils.FirebaseUtil;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class FirebaseTestActivity extends AppCompatActivity {
    private static final String TAG = "FirebaseTest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Test Firebase connection
        testFirebaseConnection();
    }

    private void testFirebaseConnection() {
        Log.d(TAG, "=== Starting Firebase Connection Test ===");
        
        try {
            // Test 1: Check Firebase App initialization
            Log.d(TAG, "Test 1: Checking Firebase App initialization");
            if (FirebaseApp.getApps(this).isEmpty()) {
                Log.e(TAG, "❌ No Firebase App instances found");
                Toast.makeText(this, "No Firebase App instances found", Toast.LENGTH_LONG).show();
                finish();
                return;
            } else {
                FirebaseApp app = FirebaseApp.getInstance();
                Log.d(TAG, "✅ Firebase App instance exists");
                Log.d(TAG, "Project ID: " + app.getOptions().getProjectId());
                Log.d(TAG, "Application ID: " + app.getOptions().getApplicationId());
            }

            // Test 2: Check Firebase Database instance
            Log.d(TAG, "Test 2: Checking Firebase Database instance");
            FirebaseDatabase database = FirebaseUtil.getDatabase();
            if (database == null) {
                Log.e(TAG, "❌ Firebase Database instance is null");
                Toast.makeText(this, "Firebase Database initialization failed", Toast.LENGTH_LONG).show();
                finish();
                return;
            } else {
                Log.d(TAG, "✅ Firebase Database instance exists");
            }

            // Test 3: Test actual database connectivity
            Log.d(TAG, "Test 3: Testing database connectivity");
            DatabaseReference testRef = database.getReference("test_connection");
            
            // Test writing data
            String testValue = "connection_test_" + System.currentTimeMillis();
            testRef.setValue(testValue)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Successfully wrote to Firebase Database!");
                    
                    // Test reading data
                    testRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                String retrievedValue = dataSnapshot.getValue(String.class);
                                if (testValue.equals(retrievedValue)) {
                                    Log.d(TAG, "✅ Successfully read from Firebase Database!");
                                    Log.d(TAG, "=== All Firebase tests passed! ===");
                                    Toast.makeText(FirebaseTestActivity.this, "✅ Firebase connection successful!", Toast.LENGTH_LONG).show();
                                } else {
                                    Log.e(TAG, "❌ Data mismatch. Expected: " + testValue + ", Got: " + retrievedValue);
                                    Toast.makeText(FirebaseTestActivity.this, "Data mismatch in Firebase", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                Log.e(TAG, "❌ Failed to read data from Firebase Database");
                                Toast.makeText(FirebaseTestActivity.this, "Failed to read from Firebase Database", Toast.LENGTH_LONG).show();
                            }
                            finish();
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e(TAG, "❌ Database read cancelled", databaseError.toException());
                            Toast.makeText(FirebaseTestActivity.this, "Database read failed: " + databaseError.getMessage(), Toast.LENGTH_LONG).show();
                            finish();
                        }
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Failed to write to Firebase Database", e);
                    Toast.makeText(this, "Firebase write failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });

        } catch (Exception e) {
            Log.e(TAG, "❌ Exception during Firebase test", e);
            Toast.makeText(this, "Firebase test failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }
}
