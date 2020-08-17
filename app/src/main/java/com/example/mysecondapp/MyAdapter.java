package com.example.mysecondapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;

public class MyAdapter extends BaseAdapter implements Filterable {
    private ArrayList<Station> arr;
    private ArrayList<Station> fav;
    private ArrayList<Station> orig; // Original Values
    private int resourceLayout;
    private Context context;
    private View rowView;
    private MainActivity main;

    public MyAdapter(Context context, int resource, ArrayList<Station> arr, ArrayList<Station> fav, MainActivity main) {
        this.resourceLayout = resource;
        this.context = context;
        this.arr = arr;
        this.fav = fav;
        this.main = main;
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


    public class StationNameHolder{
        TextView stationName;
        ToggleButton favToggle;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
//        if (resourceLayout == R.layout.station_list_design) {
            final StationNameHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(resourceLayout, parent, false);
                holder = new StationNameHolder();
                holder.stationName = (TextView) convertView.findViewById(R.id.stationName);
                holder.favToggle = (ToggleButton) convertView.findViewById(R.id.favouritesToggle);
                convertView.setTag(holder);

                rowView = convertView;

            } else {
                holder = (StationNameHolder) convertView.getTag();
                rowView = convertView;
            }
            // Set data into textviews
            holder.stationName.setText(arr.get(position).getName());
            if (fav.contains(arr.get(position))){
                holder.favToggle.setChecked(true);
            } else {
                holder.favToggle.setChecked(false);
            }

            holder.favToggle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (fav.contains(arr.get(position))){
                        main.removeFavouriteStation(arr.get(position));
                    } else {
                        main.appendFavouriteStation(arr.get(position));
                    }
                }

            });
//        }
        return rowView;
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

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }


}