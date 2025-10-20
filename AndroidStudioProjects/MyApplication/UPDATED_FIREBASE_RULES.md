# Updated Firebase Security Rules

## Setting up Security Rules for the New App Structure

After restructuring the app to match the Rapido model, we need to update the Firebase security rules to include the new `passenger_ride_requests` collection.

1. In the Firebase Console, go to your Realtime Database
2. Click on the "Rules" tab
3. Replace the existing rules with these updated rules:

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
    },
    "passenger_ride_requests": {
      ".read": "auth != null",
      ".write": "auth != null",
      "$requestId": {
        // Allow passengers to read and write their own requests
        ".read": "auth != null && (!data.exists() || data.child('passengerId').val() === auth.uid) || (!newData.exists() || newData.child('passengerId').val() === auth.uid)",
        ".write": "auth != null && (!data.exists() || data.child('passengerId').val() === auth.uid) || (!newData.exists() || newData.child('passengerId').val() === auth.uid)"
      }
    }
  }
}
```

4. Click "Publish"

## Explanation of the New Rules

- **passenger_ride_requests**: New collection for passenger-initiated ride requests
- **$requestId**: Wildcard rule for individual request documents
- **Read/Write Permissions**: 
  - Users can read/write their own requests (based on passengerId matching auth.uid)
  - Users can read all pending requests (for drivers to browse)
  - Users can write to update request status (for drivers to accept)

## Additional Security Considerations

For production, you might want to implement more granular rules:

```json
{
  "rules": {
    "rides": {
      ".read": "auth != null",
      ".write": "auth != null"
    },
    "users": {
      ".read": "auth != null",
      ".write": "auth != null",
      "$userId": {
        ".read": "auth != null && auth.uid === $userId",
        ".write": "auth != null && auth.uid === $userId"
      }
    },
    "ride_requests": {
      ".read": "auth != null",
      ".write": "auth != null"
    },
    "passenger_ride_requests": {
      ".read": "auth != null",
      ".write": "auth != null",
      "$requestId": {
        // Passengers can create and modify their own requests
        ".write": "auth != null && (!data.exists() && newData.child('passengerId').val() === auth.uid) || (data.exists() && data.child('passengerId').val() === auth.uid && newData.child('passengerId').val() === auth.uid)",
        // Everyone can read pending requests
        ".read": "auth != null && (!data.exists() || data.child('status').val() === 'pending') || (!newData.exists() || newData.child('status').val() === 'pending') || (auth != null && data.child('passengerId').val() === auth.uid) || (auth != null && data.child('driverId').val() === auth.uid)"
      }
    }
  }
}
```

## Testing the Setup

After updating the rules:

1. Run your app
2. Log in with a valid account
3. Try posting a ride request
4. Check that the request appears in the Firebase database
5. Try browsing requests as a driver
6. Try accepting a request as a driver

You should now be able to perform all operations without permission errors.