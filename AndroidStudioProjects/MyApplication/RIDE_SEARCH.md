# Ride Search Functionality

## Overview
The ride search feature allows passengers to find available rides posted by drivers. The search functionality includes filtering by destination and time, with results sorted to show the most relevant rides first. Users can also request rides directly from the search results.

## Implementation Details

### RideSearchActivity
The `RideSearchActivity` is responsible for:
1. Displaying the search interface with filter fields
2. Loading all available rides from Firebase Realtime Database
3. Filtering rides based on user input
4. Sorting results to show the most relevant rides first
5. Displaying results in a RecyclerView
6. Handling ride requests from users

### Key Features

#### Real-time Data Loading
- The activity uses Firebase ValueEventListener to listen for changes in the rides data
- All rides are loaded when the activity starts and automatically updated when data changes

#### Search and Filtering
- Users can filter rides by destination and/or time
- Filters are applied when the user clicks the "Search" button
- Empty filters return all rides

#### Smart Sorting
- When searching by destination, rides matching the destination are prioritized
- Matching rides appear at the top of the list
- Non-matching rides appear below

#### Ride Booking
- When users click on a ride, they see a confirmation dialog
- Upon confirmation, a ride request is created and saved to Firebase
- Users receive feedback on whether their request was successful

#### User Interface
- Clean, Material Design interface with filter fields and search button
- RecyclerView displays rides in card format
- Each ride card shows driver name, source, destination, date, and time
- Confirmation dialog for ride requests

## How It Works

1. **Data Loading**: When the activity starts, it connects to Firebase and loads all rides
2. **Initial Display**: All rides are displayed in the RecyclerView
3. **Filtering**: When the user enters filter criteria and clicks "Search":
   - The app filters the rides based on destination and time
   - Matching rides are sorted to appear first
   - Results are displayed in the RecyclerView
4. **Ride Booking**: When a user clicks on a ride:
   - A confirmation dialog is shown with ride details
   - If the user confirms, a ride request is created
   - The request is saved to Firebase under "ride_requests"
   - The user receives feedback on the request status
5. **Real-time Updates**: If new rides are added or existing rides are updated, the list automatically refreshes

## Future Enhancements

### Advanced Filtering
- Add date range filtering
- Add proximity-based filtering using Google Maps
- Implement price filtering

### Enhanced Sorting
- Sort by proximity to passenger's location
- Sort by driver ratings
- Sort by price

### Ride Request Functionality
- Add pickup location selection using Google Maps
- Implement ride request status tracking
- Add notifications for request status changes

### Search Improvements
- Add autocomplete for destination field
- Implement fuzzy matching for destinations
- Add saved search history