package com.example.mysecondapp.trip;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.mysecondapp.MainActivity;
import com.example.mysecondapp.R;
import com.example.mysecondapp.TimeFormat;

import java.util.ArrayList;
import java.util.Locale;

public class TripListAdapter extends BaseAdapter {
    private ArrayList<Trip> arr;
    private int resourceLayout;
    private Context context;

    public TripListAdapter(Context context, int resource, ArrayList<Trip> arr) {
        this.resourceLayout = resource;
        this.context = context;
        this.arr = arr;
    }

    @Override
    public int getCount() {
        return arr.size();
    }

    @Override
    public Object getItem(int position) {
        return arr.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    public static class TripHolder{
        TextView date, startStation, endStation, startTime, endTime, duration, bike;
    }


    @Override
    public void notifyDataSetChanged() {
        arr = ((MainActivity)context).getTrips();
        super.notifyDataSetChanged();
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final TripHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(resourceLayout, parent, false);
            holder = new TripHolder();
            holder.date = convertView.findViewById(R.id.date);
            holder.startStation = convertView.findViewById(R.id.startStation);
            holder.endStation = convertView.findViewById(R.id.endStation);
            holder.startTime = convertView.findViewById(R.id.startTime);
            holder.endTime = convertView.findViewById(R.id.endTime);
            holder.duration = convertView.findViewById(R.id.duration);
            holder.bike = convertView.findViewById(R.id.bikeNo);
            convertView.setTag(holder);
        } else {
            holder = (TripHolder) convertView.getTag();
        }

        Trip trip = arr.get(position);
        // Set data into textviews
        holder.date.setText(trip.getDate());
        holder.startStation.setText(trip.getStartStation());
        holder.endStation.setText(trip.getEndStation());
        holder.startTime.setText(new TimeFormat().timeInString(trip.getStartTime()));
        holder.endTime.setText(new TimeFormat().timeInString(trip.getEndTime()));
        holder.bike.setText(String.format("Bike ID: %s", trip.getBike()));
        holder.duration.setText(String.format(Locale.getDefault(),"Duration: %d min, %d sec", (((int)trip.getDuration())/60), (int) trip.getDuration()%60));
        return convertView;
    }


}