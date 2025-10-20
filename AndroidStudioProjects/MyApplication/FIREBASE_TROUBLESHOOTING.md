# Firebase Configuration & Troubleshooting Guide

## Issues Identified:
1. **App Not Responding**: Likely due to Firebase initialization issues
2. **OTP Generation Failed**: Firebase Realtime Database not properly configured

## Firebase Console Setup Checklist

### 1. Firebase Authentication Setup
Go to [Firebase Console](https://console.firebase.google.com/project/campusride-6dcbc):

**Step 1**: Enable Authentication
- Click "Authentication" in left sidebar
- Go to "Sign-in method" tab
- Enable "Email/Password" provider
- Save the changes

**Step 2**: Configure Email/Password Settings
- In Authentication > Settings
- Under "User actions" enable:
  ✅ "Create user account"
  ✅ "Sign in"

### 2. Firebase Realtime Database Setup
**Step 1**: Create Realtime Database
- Click "Realtime Database" in left sidebar
- Click "Create Database"
- Choose "Start in test mode" 
- Select your preferred location
- Click "Done"

**Step 2**: Configure Database Rules
Replace the default rules with:
```json
{
  "rules": {
    ".read": false,
    ".write": false,
    "otp_codes": {
      ".read": true,
      ".write": true
    },
    "users": {
      ".read": "auth != null",
      ".write": "auth != null"
    },
    "rides": {
      ".read": "auth != null",
      ".write": "auth != null"
    },
    "ride_requests": {
      ".read": "auth != null",
      ".write": "auth != null"
    }
  }
}
```
- Click "Publish" to save rules

### 3. Project Configuration Verification
Your project ID: `campusride-6dcbc`
Your package name: `com.campusride`
✅ These match your google-services.json file

## Android Studio Fixes

### Fix 1: Clean and Rebuild Project
```bash
# In Android Studio, go to:
Build > Clean Project
Build > Rebuild Project
```

### Fix 2: Gradle Sync
```bash
# In Android Studio:
File > Sync Project with Gradle Files
```

### Fix 3: Check Internet Permissions
Verify AndroidManifest.xml has:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## Debugging Steps

### 1. Check Logcat for Errors
In Android Studio:
- Open Logcat (View > Tool Windows > Logcat)
- Filter by "campusride" or "Firebase"
- Look for error messages

### 2. Test Firebase Connection
Add this to your LoginActivity onCreate() for debugging:
```java
Log.d("Firebase", "Project ID: " + FirebaseApp.getInstance().getOptions().getProjectId());
Log.d("Firebase", "App ID: " + FirebaseApp.getInstance().getOptions().getApplicationId());
```

### 3. Network Connectivity Test
Ensure your device/emulator has internet connection.

## Common Error Messages & Solutions

### "FirebaseApp is not initialized"
**Solution**: 
- Ensure google-services.json is in app/ folder
- Clean and rebuild project
- Check Firebase dependency versions

### "Database connection failed"
**Solution**:
- Verify Realtime Database is created
- Check database rules allow read/write access
- Ensure internet connectivity

### "Authentication failed"
**Solution**:
- Enable Email/Password authentication in Firebase Console
- Check if email domain restrictions are properly set

### "Permission denied"
**Solution**:
- Update database rules to allow access to otp_codes
- Ensure rules are published correctly

## Testing Instructions

### Manual Firebase Test:
1. Go to Firebase Console > Realtime Database
2. Manually add test data:
   ```json
   otp_codes: {
     "test_vitstudent_ac_in": {
       "otp": "123456",
       "email": "test@vitstudent.ac.in",
       "timestamp": 1640995200000,
       "used": false
     }
   }
   ```
3. Verify you can read/write this data

### App Testing Steps:
1. Build and run the app
2. Enter email: `test@vitstudent.ac.in`
3. Click "Send OTP"
4. Check Logcat for Firebase connection status
5. Check Firebase Console for generated OTP data

## Quick Fixes to Try Now:

1. **Restart Android Studio**
2. **Clean Project**: Build > Clean Project
3. **Invalidate Caches**: File > Invalidate Caches and Restart
4. **Check Firebase Console**: Ensure Authentication and Database are enabled
5. **Test on Different Network**: Try mobile data if on WiFi (or vice versa)

## Firebase Console URLs:
- Main Console: https://console.firebase.google.com/project/campusride-6dcbc
- Authentication: https://console.firebase.google.com/project/campusride-6dcbc/authentication
- Database: https://console.firebase.google.com/project/campusride-6dcbc/database

## Next Steps:
1. Complete Firebase console setup as described above
2. Clean and rebuild your Android project
3. Test the OTP generation again
4. Check Logcat for specific error messages if issues persist
