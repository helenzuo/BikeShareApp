package com.example.mysecondapp.ui.stationSearch;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.mysecondapp.MainActivity;
import com.example.mysecondapp.extensions.MapViewInScroll;
import com.example.mysecondapp.R;
import com.example.mysecondapp.station.SearchListAdapter;
import com.example.mysecondapp.station.Station;
import com.example.mysecondapp.station.StationComparator;
import com.example.mysecondapp.station.StationSearchBar;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Collections;
import java.util.HashMap;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class SearchFragment extends Fragment implements OnMapReadyCallback {
    private MapViewInScroll mapView;
    private GoogleMap googleMap;
    private View root;
    private MainActivity main;
    HashMap<String, Marker> markerMap;
    RadioGroup radioGroup;
    private StationSearchBar searchBar;
    private Button sortButton, refreshButton;
    private SearchListAdapter stationListAdapter;
    private ListView stationListView;
    private GoogleMap.OnCameraMoveStartedListener onCameraMoveListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_station_map, container, false);
        main = (MainActivity) getActivity();
        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = (MapViewInScroll) view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
    }


    @SuppressLint("MissingPermission")
    public void centreOnCurrentLocation(Location location) {
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        googleMap.setMyLocationEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 16F));
    }

    public void centreOnStation(String station){
        Marker marker = markerMap.get(station);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 16F));
        marker.showInfoWindow();
    }

    public void stationSelectedFromList(Station station){
        radioGroup.check(R.id.mapToggle);
        centreOnStation(station.getName());
    }

    public void updateMarkers(){
        markerMap = new HashMap<>();
        googleMap.clear();
        for (Station station : main.getStations()) {
            float fillLevel = station.getFillLevel();
            float colour = fillLevel * 120;
            Marker marker = googleMap.addMarker(new MarkerOptions().position(station.getLocation()).title(station.getName()).snippet(Integer.toString(station.getOccupancy())).icon(BitmapDescriptorFactory.defaultMarker(colour)));
            markerMap.put(station.getName(), marker);
        }
        if (stationListAdapter != null)
            stationListAdapter.notifyDataSetChanged();
    }


    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            main.updateUserLocation();
            centreOnCurrentLocation(main.lastKnownLocation);
        }

        updateMarkers();

        stationListAdapter = new SearchListAdapter(getActivity(), R.layout.station_list_search_design, this, main.getStations());
        stationListView = root.findViewById(R.id.stationsListView);
        stationListView.setAdapter(stationListAdapter);
        searchBar = root.findViewById(R.id.stationSearchBar);
        sortButton = root.findViewById(R.id.sortButton);
        searchBar.setExternalViews(stationListView, sortButton);
        refreshButton = root.findViewById(R.id.refreshButton);

        radioGroup = root.findViewById(R.id.toggleMapList);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // This will get the radiobutton that has changed in its check state
                RadioButton checkedRadioButton = (RadioButton) group.findViewById(checkedId);
                // This puts the value (true/false) into the variable
                boolean isChecked = checkedRadioButton.isChecked();
                // If the radiobutton that has changed in check state is now checked...
                if (isChecked && checkedRadioButton.getId() == R.id.mapToggle) {
                    root.findViewById(R.id.listView).setVisibility(GONE);
                    refreshButton.setVisibility(VISIBLE);
                    root.findViewById(R.id.mapView).setVisibility(VISIBLE);
                    main.swipeRefreshLayout.setEnabled(false);
                    updateMarkers();
                } else {
                    root.findViewById(R.id.listView).setVisibility(VISIBLE);
                    root.findViewById(R.id.mapView).setVisibility(GONE);
                    refreshButton.setVisibility(GONE);
                    main.swipeRefreshLayout.setEnabled(true);
                }
            }
        });

        stationListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                searchBar.clearFocus();
                InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(root.getApplicationWindowToken(), 0);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (stationListView == null || stationListView.getChildCount() == 0) ? 0 : stationListView.getChildAt(0).getTop();
                main.swipeRefreshLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
            }
        });

        sortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(getContext(), sortButton);
                //Inflating the Popup using xml file
                popup.getMenuInflater()
                        .inflate(R.menu.pop_up_sort_menu, popup.getMenu());

                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.alpha:
                                Collections.sort(main.getStations(), new StationComparator(0));
                                break;
                            case R.id.alphaRev:
                                Collections.sort(main.getStations(), new StationComparator(1));
                                break;
                            case R.id.distance:
                                Collections.sort(main.getStations(), new StationComparator(2));
                                break;
                            case R.id.bike:
                                Collections.sort(main.getStations(), new StationComparator(3));
                                break;
                            case R.id.bikeDown:
                                Collections.sort(main.getStations(), new StationComparator(4));
                                break;
                            case R.id.favs:
                                Collections.sort(main.getStations());
                                break;
                        }
                        stationListAdapter.notifyDataSetChanged();
                        return true;
                    }
                });
                popup.show(); //showing popup menu
            }
        });

        onCameraMoveListener = new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                refreshButton.animate().alpha(0.0f).setDuration(200).start();
                refreshButton.setEnabled(false);
            }
        };

        googleMap.setOnCameraMoveStartedListener(onCameraMoveListener);

        googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                refreshButton.animate().alpha(1.0f).setDuration(200).start();
                refreshButton.setEnabled(true);
            }
        });

        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main.refreshData(-1);
            }
        });
    }

    public void refreshing(){
        refreshButton.setText("Refreshing...");
        refreshButton.setEnabled(false);
        googleMap.setOnCameraMoveStartedListener(null);
    }

    public void refreshed(){
        refreshButton.setText("Refresh");
        refreshButton.setEnabled(true);
        googleMap.setOnCameraMoveStartedListener(onCameraMoveListener);
        updateMarkers();
    }

}

