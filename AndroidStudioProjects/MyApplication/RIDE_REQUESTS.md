# Ride Request Workflow

## Overview
The ride request workflow allows passengers to request rides from drivers and enables drivers to accept or reject those requests. This creates a complete end-to-end flow for ride sharing in the Campus Ride app.

## Implementation Details

### Ride Request Creation (Passenger Side)
1. **Ride Selection**: Passengers find a ride in the RideSearchActivity and click on it
2. **Confirmation**: A confirmation dialog shows the ride details
3. **Request Creation**: Upon confirmation, a RideRequest object is created with:
   - Unique request ID
   - Ride ID
   - Driver ID (to link the request to the correct driver)
   - Passenger ID and name
   - Pickup location (placeholder values in current implementation)
   - "pending" status
4. **Firebase Storage**: The request is saved to Firebase under "ride_requests"

### Ride Request Management (Driver Side)
1. **Request Loading**: Drivers open the RideRequestsActivity which:
   - Loads all ride requests from Firebase
   - Filters requests to show only those for rides created by the current driver
   - Displays only pending requests
2. **Request Display**: Requests are shown in a RecyclerView with:
   - Passenger name
   - Pickup location
   - Accept and Reject buttons
3. **Request Handling**: When drivers click Accept or Reject:
   - The request status is updated in Firebase
   - Accepted requests change to "accepted" status
   - Rejected requests change to "rejected" status
   - The request is removed from the driver's pending requests list

### Data Models

#### RideRequest Model
The RideRequest model includes:
- requestId: Unique identifier for the request
- rideId: ID of the ride being requested
- driverId: ID of the driver who created the ride
- passengerId: ID of the passenger making the request
- passengerName: Name of the passenger
- status: Current status (pending, accepted, rejected)
- pickupLat/pickupLng: Coordinates of pickup location
- pickupLocation: Text description of pickup location

### Key Features

#### Secure Request Routing
- Requests are automatically routed to the correct driver based on the ride they created
- Drivers only see requests for their own rides
- Passenger information is protected and only visible to the relevant driver

#### Real-time Updates
- Ride requests are loaded using Firebase ValueEventListener
- The request list automatically updates when new requests arrive or existing requests are updated
- Status changes are immediately reflected in the UI

#### User Feedback
- Passengers receive confirmation when their request is sent
- Drivers receive feedback when they accept or reject requests
- Error messages are shown if Firebase operations fail

## How It Works

### For Passengers
1. Search for available rides in RideSearchActivity
2. Click on a ride to request it
3. Confirm the request in the dialog
4. Receive confirmation that the request was sent
5. Wait for the driver to accept or reject the request

### For Drivers
1. Create rides in RideCreationActivity
2. Open RideRequestsActivity to view pending requests
3. Review requests from passengers
4. Accept or reject requests using the buttons
5. See requests disappear from the list when handled

## Future Enhancements

### Enhanced Request Features
- Add passenger pickup location selection using Google Maps
- Implement real-time notifications for request status changes
- Add messaging between passengers and drivers
- Include estimated pickup time and distance

### Improved User Experience
- Add request history for both passengers and drivers
- Implement request cancellation functionality
- Add rating system for completed rides
- Include photo verification for passenger/driver matching

### Advanced Functionality
- Add price negotiation between passengers and drivers
- Implement ride tracking during the trip
- Add emergency contact features
- Include ride sharing (multiple passengers per ride)