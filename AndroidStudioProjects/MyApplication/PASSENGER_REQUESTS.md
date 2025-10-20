# Passenger Ride Requests

## Overview
The Passenger Ride Requests feature allows passengers to view the status of their ride requests and receive real-time notifications when drivers accept or reject their requests. This creates a complete feedback loop for the ride sharing workflow.

## Implementation Details

### PassengerRideRequestsActivity
The `PassengerRideRequestsActivity` is responsible for:
1. Displaying a list of all ride requests made by the current passenger
2. Showing the current status of each request (pending, accepted, rejected)
3. Providing real-time updates when request statuses change
4. Notifying passengers when their requests are accepted or rejected

### Key Features

#### Request Display
- Passengers can view all their ride requests in a RecyclerView
- Each request shows:
  - Driver name
  - Ride source and destination
  - Ride date and time
  - Current status with color coding:
    - Orange for pending requests
    - Green for accepted requests
    - Red for rejected requests

#### Real-time Updates
- Requests are loaded using Firebase ValueEventListener for automatic updates
- The list automatically refreshes when request statuses change
- Passengers receive immediate feedback when drivers update their requests

#### Status Notifications
- Passengers receive Toast notifications when their request status changes
- Notifications include the driver's name and the new status
- Notifications help passengers stay informed without constantly checking the app

### Data Flow

#### Loading Requests
1. When the activity starts, it connects to Firebase
2. It queries the "ride_requests" node for requests with the current user's passengerId
3. For each request, it fetches the associated ride details from the "rides" node
4. Requests are displayed in the RecyclerView with current status

#### Status Updates
1. Firebase ValueEventListener monitors for changes to ride requests
2. When a status changes, the UI is automatically updated
3. Passengers receive Toast notifications about status changes
4. Status colors update to reflect the new state

## How It Works

### For Passengers
1. After requesting a ride, passengers can access their requests from the Home dashboard
2. The "My Ride Requests" screen shows all requests with current statuses
3. When a driver accepts or rejects a request, passengers receive an immediate notification
4. The request status updates in real-time in the list

### For Drivers
The workflow remains the same:
1. Drivers view pending requests in RideRequestsActivity
2. Drivers accept or reject requests using the buttons
3. Status changes are immediately reflected in Firebase
4. Passengers are automatically notified of the changes

## Future Enhancements

### Enhanced Notifications
- Implement push notifications for better visibility
- Add sound alerts for status changes
- Include more detailed information in notifications

### Improved User Experience
- Add filtering options (e.g., show only pending requests)
- Implement pull-to-refresh functionality
- Add request cancellation feature for passengers

### Advanced Features
- Add messaging between passengers and drivers
- Include estimated pickup times
- Add ride rating system after completion
- Implement ride history with detailed information