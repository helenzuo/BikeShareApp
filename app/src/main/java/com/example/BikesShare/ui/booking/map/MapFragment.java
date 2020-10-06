package com.example.BikesShare.ui.booking.map;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import com.airbnb.lottie.LottieAnimationView;
import com.example.BikesShare.jsonFormat.BookingMessageToServer;
import com.example.BikesShare.extensions.IgnorePageViewSwipe;
import com.example.BikesShare.MainActivity;
import com.example.BikesShare.extensions.MapViewInScroll;
import com.example.BikesShare.R;
import com.example.BikesShare.state.State;
import com.example.BikesShare.station.Station;
import com.example.BikesShare.TimeFormat;
import com.example.BikesShare.ui.booking.BookingFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

// MapFragment is the popup fragment of the map that appears when the user searches for stations in the booking
// page or send a query to the server for bike or dock bookings
public class MapFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener, GoogleMap.OnMarkerClickListener {
    private MapViewInScroll mapView;
    private GoogleMap googleMap;
    private View root;
    private MainActivity main;
    private MapFragment.updateParentView mParentListener;
    private HashMap<String, Marker> markerMap = new HashMap<String, Marker>();
    private AppCompatImageButton closeMapButton;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private Marker prevSelectedStation;
    private StationCardAdapter stationCardAdapter;
    private Button selectButton;
    private Station centredStation;

    private ArrayList<Station> stationList;

    // Interface that will be implemented to allow the parent fragment (the booking fragment) to be updated
    public interface updateParentView {
        void updateParentView();
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

    // Centers on the marker associated with the station that is passed through the first parameter
    // animate to select if the pan will be animated or not
    public void centreOnStation(Station station, Boolean animate){
        Marker marker = (Marker)markerMap.get(station.getId());
        centredStation = station;
        if (main.state.getBookingState() == State.RESERVE_BIKE_SELECTION_STATE || main.state.getBookingState() == State.RESERVE_DOCK_SELECTION_STATE) {
            if (prevSelectedStation != null) {
                prevSelectedStation.setAlpha((float) 0.5);
            }
            marker.setAlpha(1);
            prevSelectedStation = marker;
        }
        if (animate) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 16F));
        } else {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), 16F));
        }
    }


    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        recyclerView = root.findViewById(R.id.cardViewRecycler);  // cardview at the bottom of the map
        selectButton = root.findViewById(R.id.selectBorrowStationButton);
        closeMapButton = root.findViewById(R.id.closeMapPopUpButton);
        main.swipeRefreshLayout.setEnabled(false);  // disable swipe to refresh while the map popup open

        // if opened from the booking page -> ie: just searching for station via map (not opened from query response)
        if (main.state.getBookingState() != State.RESERVE_BIKE_SELECTION_STATE && main.state.getBookingState() != State.RESERVE_DOCK_SELECTION_STATE){
            stationList = main.getStations();
            markerMap = loadNMarkers();
            setUpCardView();
            layoutManager.scrollToPosition(0);
            centreOnStation(main.getStations().get(0), false);
            String buttonText;
            buttonText = "Select " + main.getStations().get(1).getName();
            selectButton.setText(buttonText);
        } else {  // opened due after querying the server
            stationList = main.interchangeables;
            markerMap = loadNMarkers();
            setUpCardView();
            layoutManager.scrollToPosition(stationList.indexOf(main.assigned));
            centreOnStation(main.assigned, false);
            String buttonText;
            if (main.state.getBookingState() == State.RESERVE_BIKE_SELECTION_STATE) {  // reserving bike
                buttonText = "Reserve Bike at " + main.assigned.getName();
            } else { // reserving dock
                buttonText = "Reserve Dock at " + main.assigned.getName();
            }
            selectButton.setText(buttonText);
        }
    }

    // setting up the map by implementing the card recycler view at the bottom page and adding listeners to buttons etc.
    private void setUpCardView(){
        selectButton.setOnClickListener(this);
        selectButton.setOnTouchListener(new IgnorePageViewSwipe(main));
        closeMapButton.setOnClickListener(this);
        main.updateUserLocation();

        // update centre current location button to bottom right
        View locationButton = ((View) mapView.findViewById(Integer.parseInt("1")).getParent()).findViewById(Integer.parseInt("2"));
        RelativeLayout.LayoutParams rlp = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
        rlp.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
        rlp.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
        rlp.setMargins(0, 0, 30, 30);
        googleMap.setOnMarkerClickListener(this);

        // Bottom card view of stations (horizontal scroll)
        stationCardAdapter = new StationCardAdapter(getContext(), stationList);
        recyclerView.setAdapter(stationCardAdapter);
        layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        recyclerView.setLayoutManager(layoutManager);
        SnapHelper helper = new LinearSnapHelper();
        helper.attachToRecyclerView(recyclerView);  // snaps to items in the adapter
        // Listen for scroll of the cards at the bottom
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                int position = layoutManager.findFirstCompletelyVisibleItemPosition();
                if (position != -1) {  // if stopped moving
                    Station station = stationList.get(position);
                    centreOnStation(station, true);
                    layoutManager.findViewByPosition(position).requestFocus();  // request focus to enlarge the card at the center
                    String buttonText;
                    // set the text of the button according to where the map has been opened from
                    if (main.state.getBookingState() != State.RESERVE_BIKE_SELECTION_STATE && main.state.getBookingState() != State.RESERVE_DOCK_SELECTION_STATE){
                        buttonText = "Select "+ station.getName();
                    } else if (main.state.getBookingState() == State.RESERVE_BIKE_SELECTION_STATE) {
                        buttonText = "Reserve Bike at " + station.getName();
                    } else {
                        buttonText = "Reserve Dock at " + station.getName();
                    }
                    selectButton.setText(buttonText);
                    selectButton.setClickable(true);
                    selectButton.animate().alpha(1.0f).setDuration(200).start();  // Make the button visible again
                } else {  // still scrolling
                    selectButton.setClickable(false);  //disable the select button and hide it
                    selectButton.animate().alpha(0.0f).setDuration(200).start();
                }
                if (newState == 0){  // released (stopped scrolling and released)
                    main.viewPager.setUserInputEnabled(true);  // Allow the view pager to swipe between fragments again
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
    }
    // when the marker is clicked, scroll to the correct location on the card recycler view and
    // center the map on the marker
    @Override
    public boolean onMarkerClick(Marker marker) {
        layoutManager.smoothScrollToPosition(recyclerView, null, stationList.indexOf(main.stationMap.get(marker.getTag())));
        centreOnStation(main.stationMap.get(marker.getTag()), true);
        return true;
    }

    // create and return a hashmap of the markers (called when the fragment first created) to allow
    // access to the markers later
    private HashMap<String, Marker> loadNMarkers(){
        HashMap<String, Marker> map = new HashMap<String, Marker>();
        if (googleMap != null) {
            googleMap.clear();
        }
        float alpha = (float) 0.5;
        if (main.state.getBookingState() != State.RESERVE_BIKE_SELECTION_STATE && main.state.getBookingState() != State.RESERVE_DOCK_SELECTION_STATE){
            alpha = 1;
        }
        for (Station station : stationList) {
            float fillLevel = station.getFillLevel();  // change the marker colors depending on the station fill levels
            float colour = fillLevel * 120;
            Marker marker = googleMap.addMarker(new MarkerOptions().position(station.getLocation()).title(station.getName()).snippet(Integer.toString(station.getOccupancy())).icon(BitmapDescriptorFactory.defaultMarker(colour)).alpha(alpha));
            marker.setTag(station.getId());  // set tag to reference markers later
            map.put(station.getId(), marker);
        }
        return map;
    }

    @Override
    public void onClick(View v) {
        if (v == selectButton) {
            // if searching for station via map view:
            if (main.state.getBookingState() != State.RESERVE_BIKE_SELECTION_STATE && main.state.getBookingState() != State.RESERVE_DOCK_SELECTION_STATE){
                root.setOnTouchListener(null);
                main.state.bookingStateTransition(true);
                main.state.setMapFragment(null);
                ((BookingFragment)getParentFragment()).setStationFromMap(centredStation);  //set edittext text as the station name of centred station
                getParentFragmentManager().popBackStackImmediate();
                mParentListener.updateParentView();
                main.swipeRefreshLayout.setEnabled(true);  // re-enable swipe to refresh for when the map pop-up closes
            } else {  // map fragment opened after querying the server
                Calendar mcurrentTime = Calendar.getInstance();
                int currentTime = mcurrentTime.get(Calendar.HOUR_OF_DAY) * 60 + mcurrentTime.get(Calendar.MINUTE);
                if (currentTime > new TimeFormat().timeInInt(main.state.getDepartureTime())) {  // if the booked time has already passed
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage("Requested departure time has already passed! Please search again.");  // set an error msg for user
                    builder.setCancelable(false);
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            getParentFragmentManager().popBackStackImmediate();
                            mParentListener.updateParentView();
                        }
                    });
                    AlertDialog confirmAlert = builder.create();
                    confirmAlert.show();
                } else { // if ok
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setMessage("Confirm reservation?"); // pop up for the user to confirm they want to reserve
                    builder.setCancelable(true);
                    builder.setPositiveButton(
                            "Confirm", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    selectButton.setEnabled(false);
                                    if (main.state.getBookingState() == State.RESERVE_BIKE_SELECTION_STATE) {
                                        main.state.setDepartingStation(centredStation);
                                        main.queryServerStation(new BookingMessageToServer("confirmDepartingStation", centredStation.getId(), new TimeFormat().timeInInt(main.state.getDepartureTime()), -1));
                                    } else {
                                        main.state.setArrivalStation(centredStation);
                                        main.state.setArrivalTime(new TimeFormat().timeInString(centredStation.getEstArr()));
                                        main.queryServerStation(new BookingMessageToServer("confirmArrivalStation", centredStation.getId(), new TimeFormat().timeInInt(main.state.getArrivalTime()), -1));
                                    }
                                    main.state.bookingStateTransition(true);
                                    root.findViewById(R.id.doneScreen).setVisibility(View.VISIBLE);
                                    final LottieAnimationView doneAnimation = root.findViewById(R.id.reservationDoneAnimation);  // start lottie animation tick
                                    doneAnimation.playAnimation();
                                    doneAnimation.addAnimatorListener(new Animator.AnimatorListener() {
                                        @Override
                                        public void onAnimationStart(Animator animation) {
                                        }

                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            getParentFragmentManager().popBackStackImmediate();
                                            mParentListener.updateParentView();
                                            main.swipeRefreshLayout.setEnabled(true);  // re-enable swipe to refresh
                                            main.state.setMapFragment(null);  // and pop the map fragment to return back to previous view
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
            }
        } else if (v == closeMapButton){  // if close map button pressed, pop current fragment from backstack and return to previous view
            if (main.state.getBookingState() != State.RESERVE_BIKE_SELECTION_STATE && main.state.getBookingState() != State.RESERVE_DOCK_SELECTION_STATE)
                main.state.bookingStateTransition(true);
            main.state.setMapFragment(null);
            getParentFragmentManager().popBackStackImmediate();
            mParentListener.updateParentView();
            main.swipeRefreshLayout.setEnabled(true);
        }
    }

    public void updateMarkers(){  // updateMarkers to the current fill levels of stations (if refreshed while the map fragment is open)
        markerMap = new HashMap<>();
        googleMap.clear();
        for (Station station : main.getStations()) {
            float fillLevel = station.getFillLevel();
            float colour = fillLevel * 120;
            Marker marker = googleMap.addMarker(new MarkerOptions().position(station.getLocation()).title(station.getName()).snippet(Integer.toString(station.getOccupancy())).icon(BitmapDescriptorFactory.defaultMarker(colour)));
            marker.setTag(station.getId());
            markerMap.put(station.getId(), marker);
        }
    }


}

