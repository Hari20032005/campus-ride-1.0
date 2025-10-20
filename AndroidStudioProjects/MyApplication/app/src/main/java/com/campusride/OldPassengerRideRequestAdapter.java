package com.campusride;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.campusride.models.RideRequest;

import java.util.List;

public class OldPassengerRideRequestAdapter extends RecyclerView.Adapter<OldPassengerRideRequestAdapter.RequestViewHolder> {
    private List<RideRequest> requests;

    public OldPassengerRideRequestAdapter(List<RideRequest> requests) {
        this.requests = requests;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_passenger_ride_request, parent, false);
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

        public void bind(RideRequest request) {
            passengerNameTextView.setText(request.getPassengerName());
            sourceTextView.setText(request.getSource());
            destinationTextView.setText(request.getDestination());
            dateTextView.setText(request.getDate());
            timeTextView.setText(request.getTime());

            // For the old adapter, we don't need click listeners
        }
    }
}