package com.campusride.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.campusride.models.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

public class UserProfileUtil {
    private static final String TAG = "UserProfileUtil";
    
    public interface UserProfileCallback {
        void onUserProfileLoaded(User user);
        void onError(String error);
    }
    
    /**
     * Fetch user profile by user ID
     * This can be used by both drivers and passengers to view each other's profiles
     */
    public static void getUserProfileById(String userId, UserProfileCallback callback) {
        if (userId == null || userId.isEmpty()) {
            callback.onError("Invalid user ID");
            return;
        }
        
        DatabaseReference userRef = FirebaseUtil.getDatabase().getReference("users").child(userId);
        userRef.get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (task.isSuccessful() && task.getResult().exists()) {
                    User user = task.getResult().getValue(User.class);
                    if (user != null) {
                        callback.onUserProfileLoaded(user);
                    } else {
                        callback.onError("Failed to parse user data");
                    }
                } else {
                    String error = task.getException() != null ? 
                        task.getException().getMessage() : "User not found";
                    callback.onError(error);
                }
            }
        });
    }
    
    /**
     * Listen for real-time updates to user profile
     * Useful for when user updates their profile and other users need to see the changes
     */
    public static void listenForUserProfileUpdates(String userId, UserProfileCallback callback) {
        if (userId == null || userId.isEmpty()) {
            callback.onError("Invalid user ID");
            return;
        }
        
        DatabaseReference userRef = FirebaseUtil.getDatabase().getReference("users").child(userId);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User user = snapshot.getValue(User.class);
                    if (user != null) {
                        callback.onUserProfileLoaded(user);
                    } else {
                        callback.onError("Failed to parse user data");
                    }
                } else {
                    callback.onError("User not found");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to listen for user profile updates", error.toException());
                callback.onError(error.getMessage());
            }
        });
    }
}