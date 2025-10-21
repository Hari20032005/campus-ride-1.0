package com.campusride;

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

public class BrowseRideRequestsAdapter extends RecyclerView.Adapter<BrowseRideRequestsAdapter.RequestViewHolder> {
    private List<PassengerRideRequest> requests;
    private OnRequestActionListener listener;

    public interface OnRequestActionListener {
        void onAcceptRequest(PassengerRideRequest request);
        void onViewDetails(PassengerRideRequest request);
        void onViewOnMap(PassengerRideRequest request);
        void onNavigate(PassengerRideRequest request);
    }

    public BrowseRideRequestsAdapter(List<PassengerRideRequest> requests, OnRequestActionListener listener) {
        this.requests = requests;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_passenger_ride_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        PassengerRideRequest request = requests.get(position);
        holder.bind(request);
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public void updateRequests(List<PassengerRideRequest> newRequests) {
        this.requests = newRequests;
        notifyDataSetChanged();
    }

    class RequestViewHolder extends RecyclerView.ViewHolder {
        private TextView passengerNameTextView, sourceTextView, destinationTextView, 
                        dateTextView, timeTextView, statusTextView;
        private Button acceptButton, viewDetailsButton, viewOnMapButton, navigateButton;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            passengerNameTextView = itemView.findViewById(R.id.passengerNameTextView);
            sourceTextView = itemView.findViewById(R.id.sourceTextView);
            destinationTextView = itemView.findViewById(R.id.destinationTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            viewDetailsButton = itemView.findViewById(R.id.viewDetailsButton);
            viewOnMapButton = itemView.findViewById(R.id.viewOnMapButton);
            navigateButton = itemView.findViewById(R.id.navigateButton);
        }

        public void bind(PassengerRideRequest request) {
            passengerNameTextView.setText(request.getPassengerName());
            sourceTextView.setText(request.getSource());
            destinationTextView.setText(request.getDestination());
            dateTextView.setText(request.getDate());
            timeTextView.setText(request.getTime());
            
            // Set status with appropriate color
            statusTextView.setText(request.getStatus());
            switch (request.getStatus()) {
                case "accepted":
                    statusTextView.setTextColor(itemView.getContext().getResources().getColor(R.color.green_500));
                    acceptButton.setVisibility(View.GONE);
                    viewDetailsButton.setVisibility(View.VISIBLE);
                    break;
                case "pending":
                    statusTextView.setTextColor(itemView.getContext().getResources().getColor(R.color.orange_500));
                    acceptButton.setVisibility(View.VISIBLE);
                    viewDetailsButton.setVisibility(View.GONE);
                    break;
                default:
                    statusTextView.setTextColor(itemView.getContext().getResources().getColor(R.color.red_500));
                    acceptButton.setVisibility(View.GONE);
                    viewDetailsButton.setVisibility(View.GONE);
                    break;
            }

            acceptButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAcceptRequest(request);
                }
            });
            
            viewDetailsButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewDetails(request);
                }
            });
            
            viewOnMapButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewOnMap(request);
                }
            });
            
            navigateButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onNavigate(request);
                }
            });
        }
    }
}