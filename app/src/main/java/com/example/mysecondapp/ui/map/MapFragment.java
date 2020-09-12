package com.example.mysecondapp.ui.map;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.airbnb.lottie.LottieAnimationView;
import com.example.mysecondapp.IgnorePageViewSwipe;
import com.example.mysecondapp.MainActivity;
import com.example.mysecondapp.MapViewInScroll;
import com.example.mysecondapp.R;
import com.example.mysecondapp.STATIC_DEFINITIONS;
import com.example.mysecondapp.Station;
import com.example.mysecondapp.StationCardAdapter;
import com.example.mysecondapp.ui.home.HomeFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;

public class MapFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener {
    private MapViewInScroll mapView;
    private GoogleMap googleMap;
    private View root;
    private MainActivity main;
    private LocationManager locationManager;
    private MapFragment.updateParentView mParentListener;
    private HashMap<String, Marker> markerMap = new HashMap<String, Marker>();
    private AppCompatImageButton closeMapButton;
    private String allocatedDepartureStationName;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private Marker prevSelectedStation;
    private StationCardAdapter stationCardAdapter;
    private Button selectButton;
    private Station centredStation;
    Location lastKnownLocation;

    private int openedFrom;

    public interface updateParentView {
        void updateParentView();
    }

    public MapFragment(int openedFrom){
        this.openedFrom = openedFrom;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_maps, container, false);
        main = (MainActivity) getActivity();

        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = (MapViewInScroll) view.findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.onResume();
        mapView.getMapAsync(this);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // check if parent Fragment implements listener
        if (getParentFragment() instanceof MapFragment.updateParentView) {
            mParentListener = (updateParentView) getParentFragment();
        }
    }

    @SuppressLint("MissingPermission")
    public void centreOnCurrentLocation(Location location) {
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        googleMap.setMyLocationEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 16F));
    }

    public void centreOnStation(String stationName){
        Marker marker = (Marker)markerMap.get(stationName);
        centredStation = main.stationMap.get(stationName);
        if (prevSelectedStation != null){
            prevSelectedStation.setAlpha((float) 0.5);
        }
        marker.setAlpha(1);
        prevSelectedStation = marker;
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 16F));
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

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;
        markerMap = loadNMarkers();
        recyclerView = (RecyclerView) root.findViewById(R.id.cardViewRecycler);
        selectButton = root.findViewById(R.id.selectBorrowStationButton);
        closeMapButton = root.findViewById(R.id.closeMapPopUpButton);

        selectButton.setOnClickListener(this);
        selectButton.setOnTouchListener(new IgnorePageViewSwipe(main));
        closeMapButton.setOnClickListener(this);

        main.updateUserLocation();
        centreOnCurrentLocation(main.lastKnownLocation);

        // update centre current location button to bottom right
        View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        rlp.setMargins(0, 0, 30, 30);
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                layoutManager.smoothScrollToPosition(recyclerView, null, main.getStations().indexOf(main.stationMap.get(marker.getTitle())));
                centreOnStation(marker.getTitle());
                return true;
            }
        });

        // Bottom card view of stations (horizontal scroll)
        stationCardAdapter = new StationCardAdapter(getContext(), main.getStations());
        recyclerView.setAdapter(stationCardAdapter);
        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        SnapHelper helper = new LinearSnapHelper();
        helper.attachToRecyclerView(recyclerView);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                int position = layoutManager.findFirstCompletelyVisibleItemPosition();
                if (position != -1) {
                    Station station = main.getStations().get(position);
                    centreOnStation(station.getName());
                    layoutManager.findViewByPosition(position).requestFocus();
                    String buttonText = "";
                    if (openedFrom == STATIC_DEFINITIONS.STATION_LOOK_UP) {
                        buttonText = "Select "+ station.getName() + " as departure station";
                    } else if (openedFrom == STATIC_DEFINITIONS.SERVER_DEPARTURE_STATION_QUERY) {
                        buttonText = "Reserve Bike at " + station.getName();
                    }
                    selectButton.setText(buttonText);
                    selectButton.setClickable(true);
                    selectButton.animate().alpha(1.0f).setDuration(200).start();
                } else {
                    selectButton.setClickable(false);
                    selectButton.animate().alpha(0.0f).setDuration(200).start();
                }
                if (newState == 0){
                    main.viewPager.setUserInputEnabled(true);
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        if (openedFrom == STATIC_DEFINITIONS.STATION_LOOK_UP){
            layoutManager.smoothScrollToPosition(recyclerView, null, main.getStations().indexOf(main.stationMap.get("Flinders")));
            String buttonText = "Select Flinders as departure station";
            selectButton.setText(buttonText);
        } else if (openedFrom == STATIC_DEFINITIONS.SERVER_DEPARTURE_STATION_QUERY) {
            //            new RetrieveMessage(main, this).execute();
            layoutManager.smoothScrollToPosition(recyclerView, null, main.getStations().indexOf(main.stationMap.get("Flinders")));
            allocatedDepartureStationName = "Flinders";
            String buttonText = "Reserve Bike at " + allocatedDepartureStationName;
            selectButton.setText(buttonText);
        }
    }

    private HashMap<String, Marker> loadNMarkers(){
        HashMap<String, Marker> map = new HashMap<String, Marker>();
        if (googleMap != null) {
            googleMap.clear();
        }
        float alpha = (float) 0.5;
        if (openedFrom == STATIC_DEFINITIONS.STATION_LOOK_UP){
            alpha = 1;
        }
        for (Station station : main.getStations()) {
            float fillLevel = station.getFillLevel();
            float colour = fillLevel * 120;
            Marker marker = googleMap.addMarker(new MarkerOptions().position(station.getLocation()).title(station.getName()).snippet(Integer.toString(station.getOccupancy())).icon(BitmapDescriptorFactory.defaultMarker(colour)).alpha(alpha));
            map.put(station.getName(), marker);
        }
        return map;
    }

    @Override
    public void onClick(View v) {
        if (v == selectButton) {
            if (openedFrom == STATIC_DEFINITIONS.STATION_LOOK_UP){
                ((HomeFragment)getParentFragment()).setDepartureStationFromMap(centredStation);
                getParentFragmentManager().popBackStackImmediate();
                mParentListener.updateParentView();
            } else if (openedFrom == STATIC_DEFINITIONS.SERVER_DEPARTURE_STATION_QUERY) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Confirm bike reservation?");
                builder.setCancelable(true);
                builder.setPositiveButton(
                        "Confirm",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                selectButton.setEnabled(false);
                                main.state.bookingStateTransition(true);
                                main.state.setDepartingStation(centredStation);
                                root.findViewById(R.id.doneScreen).setVisibility(View.VISIBLE);
                                final LottieAnimationView doneAnimation = root.findViewById(R.id.reservationDoneAnimation);
                                doneAnimation.playAnimation();
                                doneAnimation.addAnimatorListener(new Animator.AnimatorListener() {
                                    @Override
                                    public void onAnimationStart(Animator animation) {
                                    }

                                    @Override
                                    public void onAnimationEnd(Animator animation) {
                                        closeMapButton.callOnClick();
                                        selectButton.setEnabled(true);
                                    }

                                    @Override
                                    public void onAnimationCancel(Animator animation) {

                                    }

                                    @Override
                                    public void onAnimationRepeat(Animator animation) {

                                    }
                                });
                            }
                        });
                builder.setNegativeButton(
                        "Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

                AlertDialog confirmAlert = builder.create();
                confirmAlert.show();
            }
        } else if (v == closeMapButton){
            getParentFragmentManager().popBackStackImmediate();
            mParentListener.updateParentView();
        }
    }


    private static class RetrieveMessage extends AsyncTask<Void, Void, Void> {
        private WeakReference<MainActivity> activityReference;
        private WeakReference<MapFragment> fragmentReference;
        // only retain a weak reference to the activity
        RetrieveMessage(MainActivity context1, MapFragment context2) {
            activityReference = new WeakReference<>(context1);
            fragmentReference = new WeakReference<>(context2);
        }
        @Override
        protected Void doInBackground(Void... params) {
            MainActivity main = activityReference.get();
            MapFragment mapFragment = fragmentReference.get();
            while (true) {
                try {
                    String msg = main.readUTF8() ;
                    if (msg != null){
                        String[] message = msg.split("#");
                        for (int i = 0; i < message.length-2; i = i + 2){
                            main.stationMap.get(message[i]).setOccupancy(Integer.parseInt(message[i+1]));
                        }
                        mapFragment.allocatedDepartureStationName = message[message.length-1];
                        break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }
}

