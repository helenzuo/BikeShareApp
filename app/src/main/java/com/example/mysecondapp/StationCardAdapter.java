package com.example.mysecondapp;

import android.content.Context;
import android.graphics.PointF;
import android.text.SpannableString;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class StationCardAdapter extends RecyclerView.Adapter<StationCardAdapter.MyRecyclerHolder> {
    private LayoutInflater inflater;
    private ArrayList<Station> list;
    private Context context;
    View view;

    public StationCardAdapter(Context context, ArrayList<Station> list) {
        inflater= LayoutInflater.from(context);
        this.list = list;
        this.context = context;
    }

    @Override
    public MyRecyclerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        view = inflater.inflate(R.layout.station_card, parent, false);
        return new MyRecyclerHolder(view);
    }

    @Override
    public void onBindViewHolder(MyRecyclerHolder holder, int position) {
        Station station = list.get(position);
        String name = station.getName();
        String distance = station.getDistanceFrom();
        String address = station.getAddress();
        String occupancy = "Available bikes:  " + station.getOccupancy();
        holder.stationName.setText(name);
        holder.stationDistance.setText(distance);
        holder.stationAddress.setText(address);
        holder.stationOccupancy.setText(occupancy);
    }


    @Override
    public int getItemCount() {
        return list.size();
    }


    public class MyRecyclerHolder extends RecyclerView.ViewHolder {
        private TextView stationName;
        private TextView stationDistance;
        private TextView stationAddress;
        private TextView stationOccupancy;


        public MyRecyclerHolder(final View itemView) {
            super(itemView);
            stationName = (TextView) itemView.findViewById(R.id.stationCardTitle);
            stationDistance = (TextView) itemView.findViewById(R.id.stationCardDistance);
            stationAddress = (TextView) itemView.findViewById(R.id.stationCardAddress);
            stationOccupancy = (TextView) itemView.findViewById(R.id.stationOccupancy);

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
        }


    }

}
