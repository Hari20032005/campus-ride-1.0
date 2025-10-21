package com.campusride;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private String mode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Get the mode of operation
        mode = getIntent().getStringExtra("MODE");
        
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if ("view_locations".equals(mode)) {
            // Display ride request locations on map
            displayRideRequestLocations();
        } else {
            // TODO: Add route display logic
            // TODO: Add source and destination markers
            // TODO: Implement Google Directions API
        }
    }
    
    /**
     * Displays the source and destination locations of a ride request on the map
     */
    private void displayRideRequestLocations() {
        try {
            // Get location data from intent
            double sourceLat = getIntent().getDoubleExtra("SOURCE_LAT", 0);
            double sourceLng = getIntent().getDoubleExtra("SOURCE_LNG", 0);
            double destinationLat = getIntent().getDoubleExtra("DESTINATION_LAT", 0);
            double destinationLng = getIntent().getDoubleExtra("DESTINATION_LNG", 0);
            String sourceName = getIntent().getStringExtra("SOURCE_NAME");
            String destinationName = getIntent().getStringExtra("DESTINATION_NAME");
            
            // Validate coordinates
            if (sourceLat == 0 && sourceLng == 0) {
                Log.e(TAG, "Invalid source coordinates");
                return;
            }
            
            if (destinationLat == 0 && destinationLng == 0) {
                Log.e(TAG, "Invalid destination coordinates");
                return;
            }
            
            // Create LatLng objects
            LatLng sourceLatLng = new LatLng(sourceLat, sourceLng);
            LatLng destinationLatLng = new LatLng(destinationLat, destinationLng);
            
            // Add markers for source and destination
            mMap.addMarker(new MarkerOptions()
                    .position(sourceLatLng)
                    .title(sourceName != null ? sourceName : "Pickup Location"));
            
            mMap.addMarker(new MarkerOptions()
                    .position(destinationLatLng)
                    .title(destinationName != null ? destinationName : "Drop-off Location"));
            
            // Draw a line between source and destination
            mMap.addPolyline(new PolylineOptions()
                    .add(sourceLatLng, destinationLatLng)
                    .color(getResources().getColor(R.color.primary))
                    .width(8f));
            
            // Adjust camera to show both locations
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(sourceLatLng);
            builder.include(destinationLatLng);
            LatLngBounds bounds = builder.build();
            
            // Add padding to ensure markers aren't cut off
            int padding = getResources().getDimensionPixelOffset(R.dimen.spacing_large);
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
            
        } catch (Exception e) {
            Log.e(TAG, "Error displaying ride request locations on map", e);
        }
    }
}