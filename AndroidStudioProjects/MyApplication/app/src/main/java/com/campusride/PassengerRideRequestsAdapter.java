package com.campusride;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.campusride.models.PassengerRideRequest;

import java.util.List;

public class PassengerRideRequestsAdapter extends RecyclerView.Adapter<PassengerRideRequestsAdapter.RequestViewHolder> {
    private List<PassengerRideRequest> requests;
    private OnRequestActionListener listener;

    public interface OnRequestActionListener {
        void onAcceptRequest(PassengerRideRequest request);
    }

    public PassengerRideRequestsAdapter(List<PassengerRideRequest> requests, OnRequestActionListener listener) {
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
                        dateTextView, timeTextView;
        private Button acceptButton;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            passengerNameTextView = itemView.findViewById(R.id.passengerNameTextView);
            sourceTextView = itemView.findViewById(R.id.sourceTextView);
            destinationTextView = itemView.findViewById(R.id.destinationTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            acceptButton = itemView.findViewById(R.id.acceptButton);
        }

        public void bind(PassengerRideRequest request) {
            passengerNameTextView.setText(request.getPassengerName());
            sourceTextView.setText(request.getSource());
            destinationTextView.setText(request.getDestination());
            dateTextView.setText(request.getDate());
            timeTextView.setText(request.getTime());

            acceptButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onAcceptRequest(request);
                }
            });
        }
    }
}