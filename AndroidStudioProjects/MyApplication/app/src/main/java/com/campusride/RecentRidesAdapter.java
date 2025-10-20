package com.campusride;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.campusride.models.PassengerRideRequest;

import java.util.List;

public class RecentRidesAdapter extends RecyclerView.Adapter<RecentRidesAdapter.RequestViewHolder> {
    private List<PassengerRideRequest> requests;

    public RecentRidesAdapter(List<PassengerRideRequest> requests) {
        this.requests = requests;
    }

    @NonNull
    @Override
    public RequestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_my_ride_request, parent, false);
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
        private TextView sourceTextView, destinationTextView, dateTextView, timeTextView, 
                        statusTextView, driverNameTextView, driverMobileTextView;
        private LinearLayout driverInfoLayout;

        public RequestViewHolder(@NonNull View itemView) {
            super(itemView);
            sourceTextView = itemView.findViewById(R.id.sourceTextView);
            destinationTextView = itemView.findViewById(R.id.destinationTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            timeTextView = itemView.findViewById(R.id.timeTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            driverNameTextView = itemView.findViewById(R.id.driverNameTextView);
            driverMobileTextView = itemView.findViewById(R.id.driverMobileTextView);
            driverInfoLayout = itemView.findViewById(R.id.driverInfoLayout);
        }

        public void bind(PassengerRideRequest request) {
            sourceTextView.setText(request.getSource());
            destinationTextView.setText(request.getDestination());
            dateTextView.setText(request.getDate());
            timeTextView.setText(request.getTime());
            
            // Set status with appropriate color
            statusTextView.setText(request.getStatus());
            switch (request.getStatus()) {
                case "accepted":
                    statusTextView.setTextColor(itemView.getContext().getResources().getColor(R.color.green_500));
                    // Show driver information
                    if (request.getDriverName() != null && !request.getDriverName().isEmpty()) {
                        driverNameTextView.setText(request.getDriverName());
                        if (request.getDriverMobile() != null && !request.getDriverMobile().isEmpty()) {
                            driverMobileTextView.setText("Mobile: " + request.getDriverMobile());
                        } else {
                            driverMobileTextView.setText("Mobile: Not provided");
                        }
                        driverInfoLayout.setVisibility(View.VISIBLE);
                    } else {
                        driverInfoLayout.setVisibility(View.GONE);
                    }
                    break;
                case "completed":
                    statusTextView.setTextColor(itemView.getContext().getResources().getColor(R.color.green_500));
                    // Show driver information for completed rides too
                    if (request.getDriverName() != null && !request.getDriverName().isEmpty()) {
                        driverNameTextView.setText(request.getDriverName());
                        if (request.getDriverMobile() != null && !request.getDriverMobile().isEmpty()) {
                            driverMobileTextView.setText("Mobile: " + request.getDriverMobile());
                        } else {
                            driverMobileTextView.setText("Mobile: Not provided");
                        }
                        driverInfoLayout.setVisibility(View.VISIBLE);
                    } else {
                        driverInfoLayout.setVisibility(View.GONE);
                    }
                    break;
                case "cancelled":
                    statusTextView.setTextColor(itemView.getContext().getResources().getColor(R.color.red_500));
                    driverInfoLayout.setVisibility(View.GONE);
                    break;
                case "pending":
                default:
                    statusTextView.setTextColor(itemView.getContext().getResources().getColor(R.color.orange_500));
                    driverInfoLayout.setVisibility(View.GONE);
                    break;
            }
        }
    }
}