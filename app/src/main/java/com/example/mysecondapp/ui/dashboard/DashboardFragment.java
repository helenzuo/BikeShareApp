package com.example.mysecondapp.ui.dashboard;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.mysecondapp.MainActivity;
import com.example.mysecondapp.R;
import com.example.mysecondapp.Station;
import com.example.mysecondapp.ui.home.HomeFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Objects;
import java.util.Set;

import static android.content.Context.LOCATION_SERVICE;

public class DashboardFragment extends Fragment implements OnMapReadyCallback, StationsListFragment.OnChildFragmentInteractionListener {
    private MapView mapView;
    private GoogleMap googleMap;
    private View root;
    private MainActivity main;
    private LocationManager locationManager;
    private boolean choiceMade = false;
    private ViewGroup fragmentContainer;
    private StationsListFragment stationListFragment;
    HashMap markerMap = new HashMap();


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        fragmentContainer = container;
        root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        main = (MainActivity) getActivity();
        setHasOptionsMenu(true);
        FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
        stationListFragment = new StationsListFragment();
        transaction.add(R.id.listViewFragment, stationListFragment, "stationsList"); // give your fragment container id in first parameter
        transaction.commit();
        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = (MapView) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
//        if (fragmentContainer.getId() == R.id.fragment2) {
            setupUI(view);
//        }

    }


    @SuppressLint("MissingPermission")
    public void centreOnCurrentLocation(Location location) {
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        googleMap.setMyLocationEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 16F));
    }

    public void centreOnStation(String station){
        Marker marker = (Marker)markerMap.get(station);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 16F));
        Set<String> keySet = markerMap.keySet();
        for (String key : keySet){
            Marker tempMarker = (Marker) markerMap.get(key);
            if (key != station) {
//                BitmapDescriptor bd = BitmapDescriptorFactory.fromResource(android.R.drawable.ic_map_marker);
//                mMap.addMarker(new MarkerOptions().icon(bd).position(pos));
                tempMarker.setVisible(false);
            } else {
                tempMarker.setVisible(true);;
            }

        }
        marker.showInfoWindow();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                centreOnCurrentLocation(lastKnownLocation);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        Intent intent = getActivity().getIntent();
        if (intent.getIntExtra("Place Number", 0) == 0) {

            // Zoom into users location
            locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);

            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                final boolean[] done = {false};
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnownLocation == null) {
                    locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            done[0] = true;
                        }

                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {
                        }

                        @Override
                        public void onProviderEnabled(String provider) {
                        }

                        @Override
                        public void onProviderDisabled(String provider) {
                        }
                    }, null);
                    while (!done[0]) {
                    }
                    lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                }
                centreOnCurrentLocation(lastKnownLocation);
            } else {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }

            for (Station station : main.getStations()) {
                float fillLevel = station.getFillLevel();
                float colour = fillLevel * 120;
                Marker marker = googleMap.addMarker(new MarkerOptions().position(station.getLocation()).title(station.getName()).snippet(Integer.toString(station.getOccupancy())).icon(BitmapDescriptorFactory.defaultMarker(colour)));
                markerMap.put(station.getName(), marker);
            }
        }
    }

    public void setupUI(View view) {
        view.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                stationListFragment.collapseSearchView();
                return false;
            }
        });

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.saveProfileSettings).setVisible(false);
        menu.findItem(R.id.editProfile).setVisible(false);
//        if (fragmentContainer.getId() != R.id.fragment2) {
//            menu.findItem(R.id.search).setVisible(false);
//            menu.findItem(R.id.filter).setVisible(false);
//            menu.findItem(R.id.sort).setVisible(false);
//        }
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void messageFromChildToParent(String message) {
        centreOnStation(message);
    }
}

