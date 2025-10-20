package com.campusride;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.campusride.utils.FirebaseUtil;

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize Firebase in all activities
        FirebaseUtil.initialize(this);
    }
}