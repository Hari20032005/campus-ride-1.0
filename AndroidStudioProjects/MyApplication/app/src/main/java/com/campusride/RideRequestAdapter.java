package com.campusride;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.campusride.models.RideRequest;

import java.util.List;

public class RideRequestAdapter extends RecyclerView.Adapter<RideRequestAdapter.RequestViewHolder> {
    private List<RideRequest> requests;
    private OnRequestActionListener listener;

    public interface OnRequestActionListener {
        void onAcceptRequest(RideRequest request);
        void onRejectRequest(RideRequest request);
        void onCompleteRide(RideRequest request);
    }

    public RideRequestAdapter(List<RideRequest> requests, OnRequestActionListener listener) {
        this.requests = requests;
        this.listener = listener;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ride_request, parent, false);
        return new RequestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestViewHolder holder, int position) {
        RideRequest request = requests.get(position);
        holder.bind(request);
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public void updateRequests(List<RideRequest> newRequests) {
        this.requests = newRequests;
        notifyDataSetChanged();
    }

    class RequestViewHolder extends RecyclerView.ViewHolder {
        private TextView passengerNameTextView, pickupLocationTextView;
        private Button acceptButton, rejectButton, completeRideButton;
        private LinearLayout actionButtonsLayout;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            passengerNameTextView = itemView.findViewById(R.id.passengerNameTextView);
            pickupLocationTextView = itemView.findViewById(R.id.pickupLocationTextView);
            acceptButton = itemView.findViewById(R.id.acceptButton);
            rejectButton = itemView.findViewById(R.id.rejectButton);
            completeRideButton = itemView.findViewById(R.id.completeRideButton);
            actionButtonsLayout = itemView.findViewById(R.id.actionButtonsLayout);
        }

        public void bind(RideRequest request) {
            passengerNameTextView.setText(request.getPassengerName());
            pickupLocationTextView.setText(request.getPickupLocation());

            // Show appropriate buttons based on request status
            if ("accepted".equals(request.getStatus())) {
                // Show complete ride button, hide accept/reject buttons
                actionButtonsLayout.setVisibility(View.GONE);
                completeRideButton.setVisibility(View.VISIBLE);
            } else if ("pending".equals(request.getStatus())) {
                // Show accept/reject buttons, hide complete ride button
                actionButtonsLayout.setVisibility(View.VISIBLE);
                completeRideButton.setVisibility(View.GONE);
            } else {
                // For rejected requests, hide all action buttons
                actionButtonsLayout.setVisibility(View.GONE);
                completeRideButton.setVisibility(View.GONE);
            }

            acceptButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAcceptRequest(request);
                }
            });

            rejectButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRejectRequest(request);
                }
            });

            completeRideButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCompleteRide(request);
                }
            });
        }
    }
}