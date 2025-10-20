# Firebase Setup Instructions

## Setting up Realtime Database

Since you've already created the Firebase project and added the google-services.json file, you need to complete these steps:

1. Go to the Firebase Console: https://console.firebase.google.com/
2. Select your project "campusride-6dcbc"
3. In the left sidebar, click on "Realtime Database"
4. Click "Create Database"
5. Choose "Start in test mode" (for development only)
6. Click "Enable"

## Setting up Security Rules

After creating the database, you need to set up appropriate security rules:

1. In the Realtime Database section, click on the "Rules" tab
2. Replace the existing rules with these:

```json
{
  "rules": {
    "rides": {
      ".read": "auth != null",
      ".write": "auth != null"
    },
    "users": {
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

3. Click "Publish"

## Testing the Setup

After completing these steps:

1. Run your app
2. Log in with a valid account
3. Go to "Post a Ride"
4. Fill in all the fields
5. Click "Create Ride"

You should now see a success message, and the ride should appear in your database.

## Common Issues

If you're still experiencing issues:

1. Check that you have an active internet connection
2. Verify that the google-services.json file is in the app/ directory
3. Make sure you're using the correct package name (com.campusride)
4. Check the Logcat in Android Studio for any error messages