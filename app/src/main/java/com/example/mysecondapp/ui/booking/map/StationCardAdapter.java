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
                if (holder.init) {
                    holder.init = false;
                    holder.view.requestFocus();
                }
            }
        } else {
            if (context.assigned == station) {
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
            int docks = station.getCapacity() - station.getPredictedOcc();
            if (docks < 0) { // bikes are waiting to dock
                dock = "0 docks";
                bike = station.getCapacity() + " bikes docked + " + (-docks) + " waiting";
                holder.seekBar.setProgress(100);
            } else {
                bike  = station.getPredictedOcc() + " bikes";
                dock = docks + " docks";
                holder.seekBar.setProgress((int)((((float) station.getPredictedOcc()/(float) station.getCapacity())) * 100));
            }
            if (context.state.getBookingState() == State.RESERVE_BIKE_SELECTION_STATE )
                description = "Predicted station occupancy at " + context.state.getDepartureTime();
            else
                description = "Predicted station occupancy at " + new TimeFormat().timeInString(station.getEstArr());
        } else {
            int docks = station.getCapacity() - station.getOccupancy();
            if (docks < 0) { // bikes are waiting to dock
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
        holder.stationName.setText(name);
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
            stationName = (TextView) itemView.findViewById(R.id.stationNameCard);
            assignedText = itemView.findViewById(R.id.assigned);
            stationDistance = (TextView) itemView.findViewById(R.id.stationDistanceCard);
            stationAddress = itemView.findViewById(R.id.stationAddressCard);
            stationBike = (TextView) itemView.findViewById(R.id.bikeTextCard);
            stationDock = itemView.findViewById(R.id.dockTextCard);
            seekBar = itemView.findViewById(R.id.fillLevelSeekBarCard);
            stationOccText = itemView.findViewById(R.id.stationOccText);
            view = itemView;

            itemView.setAlpha((float) 0.8);

            itemView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        itemView.setAlpha((float) 1);
                        // run scale animation and make it bigger
                        Animation anim = AnimationUtils.loadAnimation(context, R.anim.scale_in);
                        itemView.startAnimation(anim);
                        anim.setFillAfter(true);
                    } else {
                        // run scale animation and make it smaller
                        Animation anim = AnimationUtils.loadAnimation(context, R.anim.scale_out);
                        itemView.startAnimation(anim);
                        anim.setFillAfter(true);
                        itemView.setAlpha((float) 0.8);
                    }
                }
            });
            itemView.setOnTouchListener(new IgnorePageViewSwipe(context));
        }
    }



}
