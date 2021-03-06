package com.example.BikesShare.station;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.BikesShare.R;

import java.util.ArrayList;

// StationAdapter is a custom adapter that is used to display the stations in the listviews in the booking
// fragments. It is filterable to allow the client to search using the edittext.
public class StationAdapter extends BaseAdapter implements Filterable {
    private ArrayList<Station> arr;
    private ArrayList<Station> orig; // Original Values
    private int resourceLayout;
    private Context context;

    public StationAdapter(Context context, int resource, ArrayList<Station> arr) {
        this.resourceLayout = resource;
        this.context = context;
        this.arr = arr;
    }

    @Override
    public int getCount() {
        if (arr != null) {
            return arr.size();
        } else {
            return 0;
        }
    }

    @Override
    public Object getItem(int position) {
        return arr.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    public static class StationNameHolder{
        TextView stationName;
        TextView stationAddress;
        TextView stationDistance;
        ToggleButton favToggle;
    }


    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final StationNameHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(resourceLayout, parent, false);
            holder = new StationNameHolder();
            holder.stationName = (TextView) convertView.findViewById(R.id.stationName);
            holder.stationAddress = convertView.findViewById(R.id.stationAddress);
            holder.stationDistance = convertView.findViewById(R.id.stationDistance);
            holder.favToggle = (ToggleButton) convertView.findViewById(R.id.favouritesToggle);
            convertView.setTag(holder);
        } else {
            holder = (StationNameHolder) convertView.getTag();
        }

        // Set data into textviews
        holder.stationName.setText(arr.get(position).getName());
        holder.stationAddress.setText(arr.get(position).getAddress());
        holder.stationDistance.setText(arr.get(position).getDistanceFrom());
        if (arr.get(position).getFavourite()){
            holder.favToggle.setChecked(true);
        } else {
            holder.favToggle.setChecked(false);
        }
        holder.favToggle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                arr.get(position).toggleFavourite();
            }

        });
        return convertView;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                final FilterResults oReturn = new FilterResults();
                final ArrayList<Station> results = new ArrayList<>();

                if (orig == null) {
                    orig = arr;
                }
                if (constraint != null) {
                    if (orig != null && orig.size() > 0) {
                        for (final Station station : orig) {
                            if (station.getName().toLowerCase().startsWith(constraint.toString().toLowerCase())) {
                                results.add(station);
                            }

                        }
                    }
                    oReturn.values = results;
                } else {
                    oReturn.values = orig;
                }
                return oReturn;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                arr = (ArrayList<Station>) results.values;
                notifyDataSetChanged();
            }
        };
    }

}