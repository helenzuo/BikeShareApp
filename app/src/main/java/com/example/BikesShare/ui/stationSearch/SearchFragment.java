package com.example.BikesShare.ui.stationSearch;

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

import com.example.BikesShare.MainActivity;
import com.example.BikesShare.extensions.MapViewInScroll;
import com.example.BikesShare.R;
import com.example.BikesShare.station.SearchListAdapter;
import com.example.BikesShare.station.Station;
import com.example.BikesShare.station.StationComparator;
import com.example.BikesShare.station.StationSearchBar;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

// SearchFragment allows user to see where the stations are on map view or in a list format
// Live info on what the occupancies of each of the stations are
public class SearchFragment extends Fragment implements OnMapReadyCallback {
    SearchFragment searchFragment;
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
        searchFragment = this;
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

    // center on current location (with animation)
    @SuppressLint("MissingPermission")
    private void centreOnCurrentLocation(Location location) {
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        googleMap.setMyLocationEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 16F));
    }
    // center on station passed in as a name with animation. Also show marker info pop-up
    private void centreOnStation(String station){
        Marker marker = markerMap.get(station);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 16F));
        marker.showInfoWindow();
    }
    // public method for when map button clicked on the drop-down list view of stations
    // This toggles to map view and centers on the station corresponding to the map button that was pressed
    public void stationSelectedFromList(Station station){
        radioGroup.check(R.id.mapToggle);
        centreOnStation(station.getName());
    }
    // public method to reload all the map markers to reflect the new fill levels of stations
    // called by the MainActivity after refreshing
    public void updateMarkers(){
        markerMap = new HashMap<>();
        googleMap.clear();
        for (Station station : main.getStations()) {
            float fillLevel = station.getFillLevel();
            float colour = fillLevel * 120;
            Marker marker = googleMap.addMarker(new MarkerOptions().position(station.getLocation()).title(station.getName()).snippet(String.format(Locale.getDefault(),"Bikes available: %d", station.getOccupancy())).icon(BitmapDescriptorFactory.defaultMarker(colour)));
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
                if (isChecked && checkedRadioButton.getId() == R.id.mapToggle) {  // map view selected
                    root.findViewById(R.id.listView).setVisibility(GONE);  // hide the listview and show the map view
                    refreshButton.setVisibility(VISIBLE);
                    root.findViewById(R.id.mapView).setVisibility(VISIBLE);
                    main.swipeRefreshLayout.setEnabled(false);  // disable refreshing by pull down when on the mapview
                    updateMarkers();
                } else {  // listview selected
                    root.findViewById(R.id.listView).setVisibility(VISIBLE); // hide the mapview and show the listview
                    root.findViewById(R.id.mapView).setVisibility(GONE);
                    refreshButton.setVisibility(GONE);
                    main.swipeRefreshLayout.setEnabled(true);
                }
            }
        });

        stationListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                searchBar.clearFocus();  // hide the keyboard and clear focus when user scrolls on the listview
                InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(root.getApplicationWindowToken(), 0);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (stationListView == null || stationListView.getChildCount() == 0) ? 0 : stationListView.getChildAt(0).getTop();
                main.swipeRefreshLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);  // allow refresh only when at top of listview
            }
        });

        sortButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {  // use this button to reorder the stations list
                //Creating the instance of PopupMenu
                PopupMenu popup = new PopupMenu(getContext(), sortButton);
                //Inflating the Popup using xml file
                popup.getMenuInflater()
                        .inflate(R.menu.pop_up_sort_menu, popup.getMenu());
                //registering popup with OnMenuItemClickListener
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        switch (item.getItemId()){
                            case R.id.alpha:  // a -> z
                                Collections.sort(main.getStations(), new StationComparator(0));
                                break;
                            case R.id.alphaRev:  // z -> a
                                Collections.sort(main.getStations(), new StationComparator(1));
                                break;
                            case R.id.distance: // closest -> furthest
                                Collections.sort(main.getStations(), new StationComparator(2));
                                break;
                            case R.id.bike:  // most bikes to least
                                Collections.sort(main.getStations(), new StationComparator(3));
                                break;
                            case R.id.bikeDown:  // least bikes to most
                                Collections.sort(main.getStations(), new StationComparator(4));
                                break;
                            case R.id.favs:  // push favourited stations to top (this is the default sorting order)
                                Collections.sort(main.getStations());
                                break;
                        }
                        stationListAdapter = new SearchListAdapter(getActivity(), R.layout.station_list_search_design, searchFragment, main.getStations());
//                        stationListAdapter.notifyDataSetChanged();
                        stationListView.setAdapter(stationListAdapter);

//                        System.out.print(stationListAdapter.getCount());
                        return true;
                    }
                });
                popup.show(); //show popup menu when sort button pressed
            }
        });

        onCameraMoveListener = new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {  // hide the "refresh" button when user is navigating on the map
                refreshButton.animate().alpha(0.0f).setDuration(200).start();
                refreshButton.setEnabled(false);
            }
        };
        googleMap.setOnCameraMoveStartedListener(onCameraMoveListener);

        googleMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {  // show the refresh button when the user has stopped moving the map
                refreshButton.animate().alpha(1.0f).setDuration(200).start();
                refreshButton.setEnabled(true);
            }
        });
        // request for the station info to be refreshed when the refresh button is clicked in the map view mode
        refreshButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main.refreshData(-1);
            }
        });
    }
    // Public method called by MainActivity when first requesting refreshed info from server
    // update the refresh button text and disable it while server has yet to respond
    public void refreshing(){
        refreshButton.setText("Refreshing...");
        refreshButton.setEnabled(false);
        googleMap.setOnCameraMoveStartedListener(null);
    }
    // once the map view has finished being refresh, re-enable the refresh button
    public void refreshed(){
        refreshButton.setText("Refresh");
        refreshButton.setEnabled(true);
        googleMap.setOnCameraMoveStartedListener(onCameraMoveListener);
        updateMarkers();
    }

}

