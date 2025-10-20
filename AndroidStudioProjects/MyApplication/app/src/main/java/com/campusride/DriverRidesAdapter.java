package com.campusride;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.campusride.models.PassengerRideRequest;

import java.util.List;

public class DriverRidesAdapter extends RecyclerView.Adapter<DriverRidesAdapter.RideViewHolder> {
    private List<PassengerRideRequest> rides;
    private Context context;
    private String filter; // "accepted" or "completed"

    public DriverRidesAdapter(Context context, List<PassengerRideRequest> rides, String filter) {
        this.context = context;
        this.rides = rides;
        this.filter = filter;
    }

    @NonNull
    @Override
    public RideViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_driver_ride, parent, false);
        return new RideViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RideViewHolder holder, int position) {
        PassengerRideRequest ride = rides.get(position);
        holder.bind(ride);
    }

    @Override
    public int getItemCount() {
        return rides.size();
    }

    public void updateRides(List<PassengerRideRequest> newRides) {
        this.rides = newRides;
        notifyDataSetChanged();
    }

    class RideViewHolder extends RecyclerView.ViewHolder {
        private TextView sourceTextView, destinationTextView, dateTextView, timeTextView, 
                        statusTextView, passengerNameTextView;
        private Button viewDetailsButton, completeRideButton;

        public RideViewHolder(@NonNull View itemView) {
            super(itemView);
            sourceTextView = itemView.findViewById(R.id.sourceTextView);
            destinationTextView = itemView.findViewById(R.id.destinationTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            passengerNameTextView = itemView.findViewById(R.id.passengerNameTextView);
            viewDetailsButton = itemView.findViewById(R.id.viewDetailsButton);
            completeRideButton = itemView.findViewById(R.id.completeRideButton);
        }

        public void bind(PassengerRideRequest ride) {
            sourceTextView.setText(ride.getSource());
            destinationTextView.setText(ride.getDestination());
            dateTextView.setText(ride.getDate());
            timeTextView.setText(ride.getTime());
            passengerNameTextView.setText(ride.getPassengerName());
            
            // Set status with appropriate color
            statusTextView.setText(ride.getStatus());
            switch (ride.getStatus()) {
                case "accepted":
                    statusTextView.setTextColor(context.getResources().getColor(R.color.green_500));
                    completeRideButton.setVisibility(View.VISIBLE);
                    completeRideButton.setEnabled(true);
                    completeRideButton.setText("Complete Ride");
                    break;
                case "completed":
                    statusTextView.setTextColor(context.getResources().getColor(R.color.green_500));
                    completeRideButton.setVisibility(View.VISIBLE);
                    completeRideButton.setEnabled(false);
                    completeRideButton.setText("Ride Completed");
                    break;
                default:
                    // For any other status, hide the complete ride button
                    completeRideButton.setVisibility(View.GONE);
                    break;
            }
            
            // Set click listeners
            viewDetailsButton.setOnClickListener(v -> {
                Intent intent = new Intent(context, DriverRideRequestDetailsActivity.class);
                intent.putExtra("REQUEST_ID", ride.getRequestId());
                context.startActivity(intent);
            });
            
            completeRideButton.setOnClickListener(v -> {
                if (context instanceof MyAcceptedRidesActivity) {
                    ((MyAcceptedRidesActivity) context).completeRide(ride);
                }
            });
        }
    }
}