package com.campusride;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.campusride.models.PassengerRideRequest;
import com.campusride.models.User;
import com.campusride.utils.FirebaseUtil;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class PostRideRequestActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "PostRideRequest";
    
    private EditText sourceEditText, destinationEditText, dateEditText, timeEditText;
    private Button postRequestButton, useCurrentLocationButton;
    private TextView distanceTextView, timeTextView, fareTextView;
    private SupportMapFragment mapFragment;
    private GoogleMap mMap;
    private LatLng sourceLatLng, destinationLatLng;
    private Polyline routePolyline;
    private String sourceAddress, destinationAddress;
    
    // For Places API
    private AutocompleteSupportFragment sourceAutocomplete, destinationAutocomplete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_ride_request);

        // Initialize Google Places API
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), "AIzaSyA3UtobCuNo28yqCwd_3jTFiNiFwH-cfzU");
        }

        initViews();
        initMap();
        setupPlacesAutocomplete();
        setClickListeners();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        
        // Set up map properties
        mMap.setMinZoomPreference(10);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        
        // Enable My Location if permission is granted
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == 
            PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }
    
    private void updateMarkersAndRoute() {
        if (mMap == null) return;
        
        // Clear previous markers and route
        mMap.clear();
        
        if (sourceLatLng != null && destinationLatLng != null) {
            // Add markers for source and destination
            mMap.addMarker(new MarkerOptions()
                .position(sourceLatLng)
                .title("Pickup Location"));
            
            mMap.addMarker(new MarkerOptions()
                .position(destinationLatLng)
                .title("Drop Location"));
            
            // Draw route between source and destination
            drawRoute(sourceLatLng, destinationLatLng);
            
            // Calculate distance and fare
            calculateDistanceAndFare();
            
            // Zoom to show both locations
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(sourceLatLng);
            builder.include(destinationLatLng);
            LatLngBounds bounds = builder.build();
            
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
        } else if (sourceLatLng != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sourceLatLng, 15));
        } else if (destinationLatLng != null) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(destinationLatLng, 15));
        }
    }
    
    private void drawRoute(LatLng origin, LatLng destination) {
        if (routePolyline != null) {
            routePolyline.remove();
        }
        
        // For now, we'll still draw a straight line, but we'll enhance later to use actual directions
        // Create a simple straight line for now - in a real app this would use the Directions API
        PolylineOptions polylineOptions = new PolylineOptions()
            .add(origin)
            .add(destination)
            .color(getResources().getColor(R.color.primary, getTheme()))
            .width(8f);
        
        routePolyline = mMap.addPolyline(polylineOptions);
    }
    
    // Method to get directions from Google Directions API (will require API call)
    private void getDirections(LatLng origin, LatLng destination) {
        // To implement proper directions with road routes, we need to make an API call to Google Directions API
        // This is an HTTP request that needs to be made asynchronously
        String url = getDirectionsUrl(origin, destination);
        
        // In a real implementation, you would make an HTTP request to the URL
        // and parse the response to get route points for accurate polyline drawing
        // For now we'll keep the straight-line implementation but provide the method structure
        drawRoute(origin, destination); // Fallback to straight line
    }
    
    private String getDirectionsUrl(LatLng origin, LatLng destination) {
        // Prepare URL for Google Directions API
        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + destination.latitude + "," + destination.longitude;
        String sensor = "sensor=false";
        String mode = "mode=driving";
        String alternatives = "alternatives=true";
        
        // You would need to add your API key here
        String key = "key=AIzaSyA3UtobCuNo28yqCwd_3jTFiNiFwH-cfzU";
        
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + mode + "&" + alternatives + "&" + key;
        String output = "json";
        
        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
    }
    
    // Method to get accurate distance and time from directions
    private void getDistanceAndTime(LatLng origin, LatLng destination) {
        // This would make a call to the Directions API to get accurate distance and time
        // For now, we'll use the Haversine formula but provide the structure for API implementation
        calculateDistanceAndFare();
    }
    
    private void calculateDistanceAndFare() {
        if (sourceLatLng != null && destinationLatLng != null) {
            // Calculate straight-line distance using Haversine formula for now
            // In a real implementation, this should use the actual route distance from Directions API
            float[] results = new float[1];
            android.location.Location.distanceBetween(
                sourceLatLng.latitude, sourceLatLng.longitude,
                destinationLatLng.latitude, destinationLatLng.longitude,
                results
            );
            
            // Convert meters to kilometers
            double distanceInKm = results[0] / 1000.0;
            
            // Apply a factor to make straight-line distance more realistic for road distance
            // (straight-line is usually shorter than road distance)
            double estimatedRoadDistance = distanceInKm * 1.3; // Approximate road distance factor
            
            // Calculate estimated time (assuming average speed appropriate for campus/urban area)
            // 25 km/h is a reasonable average for auto/bike in traffic
            double estimatedTimeInMinutes = (estimatedRoadDistance / 25.0) * 60.0;
            
            // Calculate fare (₹10 per km as per requirements)
            double fare = estimatedRoadDistance * 10;
            
            // Update UI with calculated values
            distanceTextView.setText(String.format("%.2f km", estimatedRoadDistance));
            timeTextView.setText(String.format("%.0f min", estimatedTimeInMinutes));
            fareTextView.setText(String.format("₹%.0f", fare));
        } else {
            // Reset UI if locations are not set
            distanceTextView.setText("0 km");
            timeTextView.setText("0 min");
            fareTextView.setText("₹0");
        }
    }
    
    // Enhanced method to calculate distance and time using Google Directions API
    // This would require an HTTP request to get accurate values
    private void calculateDistanceAndTimeFromDirectionsAPI() {
        if (sourceLatLng != null && destinationLatLng != null) {
            // This method would make an API call to Google Directions API
            // to get the actual route distance and estimated time
            // For now, we'll continue using the approximation method
            calculateDistanceAndFare();
        }
    }

    private void initViews() {
        sourceEditText = findViewById(R.id.sourceEditText);
        destinationEditText = findViewById(R.id.destinationEditText);
        dateEditText = findViewById(R.id.dateEditText);
        timeEditText = findViewById(R.id.timeEditText);
        postRequestButton = findViewById(R.id.postRequestButton);
        useCurrentLocationButton = findViewById(R.id.useCurrentLocationButton);
        distanceTextView = findViewById(R.id.distanceTextView);
        timeTextView = findViewById(R.id.timeTextView);
        fareTextView = findViewById(R.id.fareTextView);
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
    }
    
    private void initMap() {
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }
    
    private void setupPlacesAutocomplete() {
        // Initialize Google Places SDK if needed (though it's already done in onCreate)
        
        // Set up click listeners for both source and destination to trigger Places Autocomplete
        sourceEditText.setOnClickListener(v -> {
            openPlacesAutocomplete(1000); // 1000 for source
        });
        
        destinationEditText.setOnClickListener(v -> {
            openPlacesAutocomplete(1001); // 1001 for destination
        });
        
        // Also maintain the geocoding functionality for manual text input
        sourceEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String text = sourceEditText.getText().toString().trim();
                // Only geocode if it's a user-entered location (not one set by Places)
                if (!text.startsWith("Current Location") && !isSelectingFromPlaces) {
                    geocodeLocation(text, 0); // 0 for source
                }
            }
        });
        
        destinationEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String text = destinationEditText.getText().toString().trim();
                // Only geocode if it's a user-entered location (not one set by Places)
                if (!isSelectingFromPlaces) {
                    geocodeLocation(text, 1); // 1 for destination
                }
            }
        });
        
        // Add text watcher to geocode as user types (with delay to avoid too many calls)
        sourceEditText.addTextChangedListener(new TextWatcher() {
            private Handler handler = new Handler();
            private Runnable geocodeRunnable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Cancel any pending geocoding when text changes
                if (geocodeRunnable != null) {
                    handler.removeCallbacks(geocodeRunnable);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString().trim();
                // Only geocode if it's a user-entered location (not one set by Places)
                if (!text.startsWith("Current Location") && !isSelectingFromPlaces && !text.isEmpty()) {
                    // Post a new runnable to geocode after a delay
                    geocodeRunnable = () -> geocodeLocation(text, 0); // 0 for source
                    handler.postDelayed(geocodeRunnable, 1000); // 1 second delay
                }
            }
        });
        
        destinationEditText.addTextChangedListener(new TextWatcher() {
            private Handler handler = new Handler();
            private Runnable geocodeRunnable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Cancel any pending geocoding when text changes
                if (geocodeRunnable != null) {
                    handler.removeCallbacks(geocodeRunnable);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                String text = s.toString().trim();
                // Only geocode if it's a user-entered location (not one set by Places)
                if (!isSelectingFromPlaces && !text.isEmpty()) {
                    // Post a new runnable to geocode after a delay
                    geocodeRunnable = () -> geocodeLocation(text, 1); // 1 for destination
                    handler.postDelayed(geocodeRunnable, 1000); // 1 second delay
                }
            }
        });
    }
    
    // Add a flag to track when user is selecting from Places Autocomplete
    private boolean isSelectingFromPlaces = false;
    
    private void openPlacesAutocomplete(int requestCode) {
        // Create a list of fields to return from the Place object
        List<com.google.android.libraries.places.api.model.Place.Field> fields = 
            Arrays.asList(
                com.google.android.libraries.places.api.model.Place.Field.ID,
                com.google.android.libraries.places.api.model.Place.Field.NAME,
                com.google.android.libraries.places.api.model.Place.Field.LAT_LNG,
                com.google.android.libraries.places.api.model.Place.Field.ADDRESS
            );

        // Create the autocomplete intent
        com.google.android.libraries.places.widget.Autocomplete.IntentBuilder builder = 
            new com.google.android.libraries.places.widget.Autocomplete.IntentBuilder(
                com.google.android.libraries.places.widget.AutocompleteActivityMode.OVERLAY, fields);
        
        // Limit to India
        builder.setLocationRestriction(
            com.google.android.gms.maps.model.LatLngBounds.builder()
                .include(new com.google.android.gms.maps.model.LatLng(8.0, 68.0))  // SW corner of India
                .include(new com.google.android.gms.maps.model.LatLng(37.0, 98.0)) // NE corner of India
                .build()
        );

        try {
            Intent intent = builder.build(this);
            startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            Log.e(TAG, "Places API error: " + e.getMessage());
            // Fallback: Show a message to the user to enter location manually
            Toast.makeText(this, "Places API not available. Please enter location manually.", Toast.LENGTH_LONG).show();
        }
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if (requestCode == 1000) { // Source selection
            com.google.android.libraries.places.api.model.Place place = 
                com.google.android.libraries.places.widget.Autocomplete
                    .getPlaceFromIntent(data);
            if (resultCode == RESULT_OK && place != null) {
                isSelectingFromPlaces = true;
                sourceLatLng = place.getLatLng();
                sourceAddress = place.getAddress();
                sourceEditText.setText(place.getName());
                
                // Update the map
                updateMarkersAndRoute();
                
                // Reset the flag after a short delay to avoid triggering geocoding
                new Handler().postDelayed(() -> isSelectingFromPlaces = false, 500);
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the operation
                Log.d(TAG, "Source location selection cancelled");
            } else {
                com.google.android.gms.common.api.Status status = 
                    com.google.android.libraries.places.widget.Autocomplete
                        .getStatusFromIntent(data);
                Log.e(TAG, "Place selection error: " + status.getStatusMessage());
                Toast.makeText(this, "Error selecting source: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 1001) { // Destination selection
            com.google.android.libraries.places.api.model.Place place = 
                com.google.android.libraries.places.widget.Autocomplete
                    .getPlaceFromIntent(data);
            if (resultCode == RESULT_OK && place != null) {
                isSelectingFromPlaces = true;
                destinationLatLng = place.getLatLng();
                destinationAddress = place.getAddress();
                destinationEditText.setText(place.getName());
                
                // Update the map
                updateMarkersAndRoute();
                
                // Reset the flag after a short delay to avoid triggering geocoding
                new Handler().postDelayed(() -> isSelectingFromPlaces = false, 500);
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the operation
                Log.d(TAG, "Destination location selection cancelled");
            } else {
                com.google.android.gms.common.api.Status status = 
                    com.google.android.libraries.places.widget.Autocomplete
                        .getStatusFromIntent(data);
                Log.e(TAG, "Place selection error: " + status.getStatusMessage());
                Toast.makeText(this, "Error selecting destination: " + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
    
    /**
     * Geocodes a location string to coordinates using Android's Geocoder
     * @param location The location string to geocode
     * @param locationType 0 for source, 1 for destination
     */
    private void geocodeLocation(String location, int locationType) {
        if (location.isEmpty()) return;
        
        // Show a simple progress indicator or message
        Log.d(TAG, "Geocoding location: " + location);
        
        // Use Geocoder to convert address to coordinates
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocationName(location, 1);
            
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                double latitude = address.getLatitude();
                double longitude = address.getLongitude();
                
                // Update the map and variables
                if (locationType == 0) { // Source
                    sourceLatLng = new LatLng(latitude, longitude);
                    sourceAddress = location; // Use the user-entered location text
                    Log.d(TAG, "Geocoded source: " + location + " -> " + latitude + ", " + longitude);
                } else { // Destination
                    destinationLatLng = new LatLng(latitude, longitude);
                    destinationAddress = location; // Use the user-entered location text
                    Log.d(TAG, "Geocoded destination: " + location + " -> " + latitude + ", " + longitude);
                }
                
                // Update markers and route on map
                updateMarkersAndRoute();
                
                // Show success feedback to user
                String locationName = locationType == 0 ? "source" : "destination";
                Toast.makeText(this, "✓ " + locationName + " location set", Toast.LENGTH_SHORT).show();
            } else {
                Log.w(TAG, "No results found for location: " + location);
                String locationName = locationType == 0 ? "Source" : "Destination";
                Toast.makeText(this, locationName + " location not found. Please be more specific.", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoding error: " + e.getMessage());
            String locationName = locationType == 0 ? "Source" : "Destination";
            Toast.makeText(this, "Error geocoding " + locationName.toLowerCase() + " location", Toast.LENGTH_SHORT).show();
        }
    }
    


    private void setClickListeners() {
        // Date and time pickers remain the same
        dateEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDatePicker();
            }
        });

        // Add time picker functionality
        timeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker();
            }
        });

        postRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postRideRequest();
            }
        });

        useCurrentLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                useCurrentLocationAsSource();
            }
        });
    }
    
    private void useCurrentLocationAsSource() {
        // Check for location permissions
        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != 
            PackageManager.PERMISSION_GRANTED) {
            // Request permission
            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }
        
        // Get the current location using FusedLocationProviderClient
        com.google.android.gms.location.FusedLocationProviderClient fusedLocationClient = 
            com.google.android.gms.location.LocationServices.getFusedLocationProviderClient(this);
        
        fusedLocationClient.getLastLocation()
            .addOnSuccessListener(this, location -> {
                if (location != null) {
                    // Use current location as source
                    sourceLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    
                    // Reverse geocode to get the actual address for display
                    Geocoder geocoder = new Geocoder(this, Locale.getDefault());
                    try {
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        
                        if (addresses != null && !addresses.isEmpty()) {
                            // Get a more user-friendly address
                            Address addr = addresses.get(0);
                            StringBuilder addressText = new StringBuilder();
                            
                            // Try to build a concise address string
                            String thoroughfare = addr.getThoroughfare(); // Street name
                            String locality = addr.getLocality(); // City
                            String subLocality = addr.getSubLocality(); // Area/neighbourhood
                            
                            if (thoroughfare != null) {
                                addressText.append(thoroughfare);
                                if (subLocality != null) {
                                    addressText.append(", ").append(subLocality);
                                } else if (locality != null) {
                                    addressText.append(", ").append(locality);
                                }
                            } else if (subLocality != null) {
                                addressText.append(subLocality);
                                if (locality != null) {
                                    addressText.append(", ").append(locality);
                                }
                            } else if (locality != null) {
                                addressText.append(locality);
                            } else {
                                // Fallback: use coordinates as string
                                addressText.append("Current Location (").append(location.getLatitude()).append(", ").append(location.getLongitude()).append(")");
                            }
                            
                            sourceAddress = addressText.toString();
                        } else {
                            // If reverse geocoding fails, just use coordinates
                            sourceAddress = "Current Location (" + location.getLatitude() + ", " + location.getLongitude() + ")";
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reverse geocoding location: " + e.getMessage());
                        // If reverse geocoding fails, just use coordinates
                        sourceAddress = "Current Location (" + location.getLatitude() + ", " + location.getLongitude() + ")";
                    }
                    
                    // Set the address in the text field
                    sourceEditText.setText(sourceAddress);
                    
                    updateMarkersAndRoute();
                    
                    Toast.makeText(this, "Current location set as source", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Unable to get current location", Toast.LENGTH_SHORT).show();
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Error getting current location: " + e.getMessage());
                Toast.makeText(this, "Error getting current location", Toast.LENGTH_SHORT).show();
            });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                PostRideRequestActivity.this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // Month is 0-based, so add 1
                        String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                        dateEditText.setText(selectedDate);
                    }
                },
                year, month, day);
        datePickerDialog.show();
    }

    private void showTimePicker() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                PostRideRequestActivity.this,
                new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        String selectedTime = String.format("%02d:%02d", hourOfDay, minute);
                        timeEditText.setText(selectedTime);
                    }
                },
                hour, minute, true); // true for 24-hour format
        timePickerDialog.show();
    }

    private void postRideRequest() {
        String date = dateEditText.getText().toString().trim();
        String time = timeEditText.getText().toString().trim();

        // Validate that both source and destination locations are entered and geocoded
        String sourceText = sourceEditText.getText().toString().trim();
        String destinationText = destinationEditText.getText().toString().trim();
        
        if (sourceText.isEmpty() || destinationText.isEmpty()) {
            if (sourceText.isEmpty()) {
                sourceEditText.setError("Source location is required");
                sourceEditText.requestFocus();
            }
            if (destinationText.isEmpty()) {
                destinationEditText.setError("Destination location is required");
                destinationEditText.requestFocus();
            }
            return;
        }
        
        if (sourceLatLng == null || destinationLatLng == null) {
            if (sourceLatLng == null) {
                Toast.makeText(this, "Please verify the source location by ensuring it's properly entered", Toast.LENGTH_LONG).show();
                sourceEditText.requestFocus();
            } else if (destinationLatLng == null) {
                Toast.makeText(this, "Please verify the destination location by ensuring it's properly entered", Toast.LENGTH_LONG).show();
                destinationEditText.requestFocus();
            }
            return;
        }

        if (date.isEmpty()) {
            dateEditText.setError("Please select date");
            dateEditText.requestFocus();
            return;
        }

        if (time.isEmpty()) {
            timeEditText.setError("Please select time");
            timeEditText.requestFocus();
            return;
        }

        FirebaseUser currentUser = FirebaseUtil.getAuth().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        // Fetch passenger details from Firebase before creating request
        DatabaseReference userRef = FirebaseUtil.getDatabase().getReference("users").child(currentUser.getUid());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    User passenger = snapshot.getValue(User.class);
                    if (passenger != null) {
                        // Create a new ride request with coordinates
                        String requestId = UUID.randomUUID().toString();
                        String timestamp = String.valueOf(System.currentTimeMillis());
                        
                        // Calculate distance for the request
                        float[] results = new float[1];
                        android.location.Location.distanceBetween(
                            sourceLatLng.latitude, sourceLatLng.longitude,
                            destinationLatLng.latitude, destinationLatLng.longitude,
                            results
                        );
                        double distanceInKm = results[0] / 1000.0;
                        
                        PassengerRideRequest rideRequest = new PassengerRideRequest(
                                requestId,
                                currentUser.getUid(),
                                passenger.getName() != null ? passenger.getName() : "Unknown Passenger",
                                passenger.getMobile() != null ? passenger.getMobile() : "",
                                passenger.getRegNo() != null ? passenger.getRegNo() : "",
                                sourceLatLng != null ? (sourceLatLng.latitude + "," + sourceLatLng.longitude) : "",
                                sourceAddress != null ? sourceAddress : sourceEditText.getText().toString().trim(),
                                destinationLatLng != null ? (destinationLatLng.latitude + "," + destinationLatLng.longitude) : "",
                                destinationAddress != null ? destinationAddress : destinationEditText.getText().toString().trim(),
                                date,
                                time,
                                timestamp,
                                sourceLatLng != null ? sourceLatLng.latitude : 0.0,
                                sourceLatLng != null ? sourceLatLng.longitude : 0.0,
                                destinationLatLng != null ? destinationLatLng.latitude : 0.0,
                                destinationLatLng != null ? destinationLatLng.longitude : 0.0
                        );

                        // Save to Firebase
                        DatabaseReference requestsRef = FirebaseUtil.getDatabase().getReference("passenger_ride_requests");
                        Log.d(TAG, "Attempting to save ride request: " + requestId);
                        Log.d(TAG, "User ID: " + currentUser.getUid());
                        Log.d(TAG, "Database reference: " + requestsRef.toString());
                        
                        requestsRef.child(requestId).setValue(rideRequest)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "Ride request created successfully");
                                        Toast.makeText(PostRideRequestActivity.this, "Ride request posted successfully", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Log.e(TAG, "Failed to create ride request", task.getException());
                                        String errorMessage = task.getException() != null ? task.getException().getMessage() : "Unknown error";
                                        Log.e(TAG, "Error details: " + errorMessage);
                                        Toast.makeText(PostRideRequestActivity.this, "Failed to post ride request: " + errorMessage, Toast.LENGTH_LONG).show();
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Failed to create ride request with exception", e);
                                    Toast.makeText(PostRideRequestActivity.this, "Failed to post ride request: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                });
                    } else {
                        Toast.makeText(PostRideRequestActivity.this, "Failed to load passenger information", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PostRideRequestActivity.this, "Passenger profile not found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load passenger information", error.toException());
                Toast.makeText(PostRideRequestActivity.this, "Failed to load passenger information: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}