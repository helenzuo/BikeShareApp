package com.example.mysecondapp.ui.booking.map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.mysecondapp.extensions.IgnorePageViewSwipe;
import com.example.mysecondapp.MainActivity;
import com.example.mysecondapp.R;
import com.example.mysecondapp.state.State;
import com.example.mysecondapp.station.Station;
import com.example.mysecondapp.TimeFormat;

import java.util.ArrayList;

// Adapter for the cards at the bottom of the map fragment
public class StationCardAdapter extends RecyclerView.Adapter<StationCardAdapter.MyRecyclerHolder> {
    private LayoutInflater inflater;
    private ArrayList<Station> list;
    private MainActivity context;
    View view;

    public StationCardAdapter(Context context, ArrayList<Station> list) {
        inflater= LayoutInflater.from(context);
        this.list = list;
        this.context = (MainActivity) context;
    }

    @Override
    public MyRecyclerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = inflater.inflate(R.layout.station_card, parent, false);
        return new MyRecyclerHolder(view);
    }

    @Override
    public void onBindViewHolder(MyRecyclerHolder holder, int position) {
        Station station = list.get(position);
        if (context.state.getBookingState() != State.RESERVE_BIKE_SELECTION_STATE && context.state.getBookingState() != State.RESERVE_DOCK_SELECTION_STATE) {
            if (position == 0) {
                if (holder.init) {  // if first time, request focus of the first in position to make it larger
                    holder.init = false;
                    holder.view.requestFocus();
                }
            }
        } else {
            if (context.assigned == station) {  // if first time and opened after requesting booking from server, enlarge the assigned station
                if (holder.init) {
                    holder.init = false;
                    holder.view.requestFocus();
                }
                holder.assignedText.setVisibility(View.VISIBLE);
            }
        }
        String name = station.getName();
        String distance = station.getDistanceFrom();
        String address = station.getAddress();
        String bike;
        String dock;
        String description;

        if (context.state.getBookingState() == State.RESERVE_DOCK_SELECTION_STATE || context.state.getBookingState() == State.RESERVE_BIKE_SELECTION_STATE){
            int docks = station.getCapacity() - station.getPredictedOcc();  // if opened after requesting booking
            if (docks < 0) { // bikes are waiting to dock...
                dock = "0 docks";  // then there are no docks available at the moment
                bike = station.getCapacity() + " bikes docked + " + (-docks) + " waiting";
                holder.seekBar.setProgress(100);  // station at full capacity, plus more
            } else { // if no bikes are waiting to dock...
                bike  = station.getPredictedOcc() + " bikes";
                dock = docks + " docks";
                holder.seekBar.setProgress((int)((((float) station.getPredictedOcc()/(float) station.getCapacity())) * 100));
            }
            if (context.state.getBookingState() == State.RESERVE_BIKE_SELECTION_STATE )  // opened after requesting bike
                description = "Predicted station occupancy at " + context.state.getDepartureTime();
            else  // opened after requesting dock (estArr = what was asked for or returned from the server if not requested)
                description = "Predicted station occupancy at " + new TimeFormat().timeInString(station.getEstArr());
        } else {  // opened from the search map button
            int docks = station.getCapacity() - station.getOccupancy();
            if (docks < 0) { // bikes are waiting to dock using current occupancy levels
                dock = "0 docks";
                bike = station.getCapacity() + " bikes docked + " + (-docks) + " waiting";
                holder.seekBar.setProgress(100);
            } else {
                bike = station.getOccupancy() + " bikes";
                dock = docks + " docks";
                holder.seekBar.setProgress((int)(station.getFillLevel()*100));
            }
            description = "Current station occupancy:";
        }
        holder.stationName.setText(name);  // set text of the textviews....
        holder.stationDistance.setText(distance);
        holder.stationAddress.setText(address);
        holder.stationBike.setText(bike);
        holder.stationDock.setText(dock);
        holder.stationOccText.setText(description);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyRecyclerHolder extends RecyclerView.ViewHolder {
        private TextView stationName, stationDistance, stationAddress, stationBike, stationDock, stationOccText, assignedText;
        private SeekBar seekBar;
        private boolean init = true;
        private View view;

        public MyRecyclerHolder(final View itemView) {
            super(itemView);
            stationName = itemView.findViewById(R.id.stationNameCard);
            assignedText = itemView.findViewById(R.id.assigned);
            stationDistance = itemView.findViewById(R.id.stationDistanceCard);
            stationAddress = itemView.findViewById(R.id.stationAddressCard);
            stationBike = itemView.findViewById(R.id.bikeTextCard);
            stationDock = itemView.findViewById(R.id.dockTextCard);
            seekBar = itemView.findViewById(R.id.fillLevelSeekBarCard);
            stationOccText = itemView.findViewById(R.id.stationOccText);
            view = itemView;

            itemView.setAlpha((float) 0.8);

            itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {  // when the cardview has focus, it is made larger and opaque
                        itemView.setAlpha((float) 1);
                        // run scale animation and make it bigger
                        Animation anim = AnimationUtils.loadAnimation(context, R.anim.scale_in);
                        itemView.startAnimation(anim);
                        anim.setFillAfter(true);
                    } else {  // other cardviews are smaller and slightly transparent
                        // run scale animation and make it smaller
                        Animation anim = AnimationUtils.loadAnimation(context, R.anim.scale_out);
                        itemView.startAnimation(anim);
                        anim.setFillAfter(true);
                        itemView.setAlpha((float) 0.8);
                    }
                }
            });
            itemView.setOnTouchListener(new IgnorePageViewSwipe(context));  // allow horizontal swiping (don't trigger viewpager)
        }
    }



}
