package com.example.mysecondapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.mysecondapp.ui.stationSearch.DashboardFragment;

import java.util.ArrayList;

public class SearchListAdapter extends MyAdapter {
    private ArrayList<Station> arr;
    private ArrayList<Station> orig; // Original Values

    private int resourceLayout;
    private Context context;
    private DashboardFragment dashboardFragment;


    public SearchListAdapter(Context context, int resource, DashboardFragment dashboardFragment, ArrayList<Station> arr) {
        super(context, resource, arr);
        this.context = context;
        this.resourceLayout = resource;
        this.dashboardFragment = dashboardFragment;
        this.arr = arr;
    }

    public static class Holder{
        TextView stationName;
        TextView stationAddress;
        TextView stationDistance;
        ToggleButton favToggle;
        ImageButton moreButton;
        RelativeLayout moreInfoLayout;
        Button mapButton;
        TextView bikeText;
        TextView dockText;
        SeekBar fillLevelSeekBar;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final Holder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(resourceLayout, parent, false);
            holder = new Holder();
            holder.stationName = (TextView) convertView.findViewById(R.id.stationName);
            holder.stationAddress = convertView.findViewById(R.id.stationAddress);
            holder.stationDistance = convertView.findViewById(R.id.stationDistance);
            holder.favToggle = (ToggleButton) convertView.findViewById(R.id.favouritesToggle);
            holder.moreButton = convertView.findViewById(R.id.moreButton);
            holder.moreInfoLayout = convertView.findViewById(R.id.bottomView);
            holder.bikeText = convertView.findViewById(R.id.bikeText);
            holder.dockText = convertView.findViewById(R.id.dockText);
            holder.mapButton = convertView.findViewById(R.id.mapButton);
            holder.fillLevelSeekBar = convertView.findViewById(R.id.fillLevelSeekBar);
            holder.fillLevelSeekBar.setEnabled(false);
            convertView.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        // Set data into textviews
        holder.stationName.setText(arr.get(position).getName());
        holder.stationAddress.setText(String.format("%s", arr.get(position).getAddress()));
        holder.stationDistance.setText(String.format("%s away", arr.get(position).getDistanceFrom()));
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

        holder.moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Station station = arr.get(position);
                if (holder.moreInfoLayout.getVisibility() == (View.VISIBLE)) {
                    holder.moreInfoLayout.setVisibility(View.GONE);
                    holder.mapButton.setOnClickListener(null);
                } else {
                    holder.moreInfoLayout.setVisibility(View.VISIBLE);
                    float fl = station.getFillLevel();
                    holder.bikeText.setText(String.format("%d free bikes", (int) (fl*station.getCapacity())));
                    holder.dockText.setText(String.format("%d free docks", (int) ((1-fl)*station.getCapacity())));
                    holder.fillLevelSeekBar.setProgress((int)(fl*100));
                    holder.mapButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dashboardFragment.stationSelectedFromList(station);
                        }
                    });
                }

            }
        });

        if (holder.moreInfoLayout.getVisibility() == (View.VISIBLE)) {
            holder.moreInfoLayout.setVisibility(View.GONE);
            holder.mapButton.setOnClickListener(null);
        }
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

}
