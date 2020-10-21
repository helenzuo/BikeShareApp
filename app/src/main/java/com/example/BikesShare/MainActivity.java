package com.example.BikesShare;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.viewpager2.widget.ViewPager2;

import com.example.BikesShare.extensions.NoBounceLinearLayoutManager;
import com.example.BikesShare.extensions.PickerAdapter;
import com.example.BikesShare.extensions.ViewPagerAdapter;
import com.example.BikesShare.jsonFormat.BookingMessageToServer;
import com.example.BikesShare.message.Message;
import com.example.BikesShare.state.State;
import com.example.BikesShare.state.User;
import com.example.BikesShare.station.Station;
import com.example.BikesShare.trip.Trip;
import com.example.BikesShare.ui.booking.BookingFragment;
import com.example.BikesShare.ui.profile.ProfileFragment;
import com.example.BikesShare.ui.stationSearch.SearchFragment;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import travel.ithaka.android.horizontalpickerlib.PickerLayoutManager;

// MainActivity contains all the booking/station search/profile fragments etc..
public class MainActivity extends AppCompatActivity {

    private Thread dockCheckThread;
    public State state;
    public User user;
    public LocationManager locationManager;
    public Location lastKnownLocation;
    private ArrayList<Station> stations = new ArrayList<Station>();
    public ArrayList<String> stationNames = new ArrayList<>();
    private ArrayList<Trip> trips = new ArrayList<>();
    private Socket clientSocket;
    private BufferedWriter out;
    private DataInputStream in;
    boolean ping = false;
    private boolean serverRestart;

    boolean openingQR = false;
    public HashMap<String, Station> stationMap = new HashMap<String, Station>();
    public ViewPager2 viewPager;
    public SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView rvNavigationPicker;
    private PickerAdapter navigationAdapter;
    private List<String> fragmentTitles;
    private NoBounceLinearLayoutManager pickerLayoutManager;
    private int currentFrag = 1;
    public ArrayList<Station> interchangeables = new ArrayList<>();
    public Station assigned;
    private Context context;

    private String IPHost = "192.168.20.9";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = this;
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();  // hide the action bar

        Intent intent = getIntent();  // check if location access permitted, if not wait till it is granted
        if (intent.getIntExtra("Place Number", 0) == 0) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                updateUserLocation();
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }

        Bundle extras = intent.getExtras();
        if (extras != null) {
            String value = extras.getString(State.USER_KEY);
            user = new Gson().fromJson(value, User.class);  // get the user information that has been passed over from the Login Activity
        }

        state = new State();
        state.logIn(user);

        // set up the spinner at top of page
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        rvNavigationPicker = findViewById(R.id.rvNavigationPicker);  // spinner at the top of the page used to select the fragment of viewpager
        viewPager = findViewById(R.id.view_pager);
        swipeRefreshLayout = findViewById(R.id.container);
        pickerLayoutManager = new NoBounceLinearLayoutManager(this, PickerLayoutManager.HORIZONTAL, false);
        pickerLayoutManager.setScaleDownBy(0.25f);
        pickerLayoutManager.setScaleDownDistance(0.7f);
        SnapHelper snapHelper = new LinearSnapHelper();
        fragmentTitles = new ArrayList<>();
        fragmentTitles.add("Profile");
        fragmentTitles.add("Reservation");
        fragmentTitles.add("Stations List");
        navigationAdapter = new PickerAdapter(this, fragmentTitles, rvNavigationPicker, snapHelper, pickerLayoutManager);
        snapHelper.attachToRecyclerView(rvNavigationPicker);
        rvNavigationPicker.setLayoutManager(pickerLayoutManager);
        rvNavigationPicker.setAdapter(navigationAdapter);
        rvNavigationPicker.scrollToPosition(1);
        rvNavigationPicker.smoothScrollBy(-1, 0);

        // Connect to the server
        new Connect(this).execute();
        // Get the station/trip information needed to set up the other fragments
        new GetMsg(this, "initialise").execute();
    }

    // This class stores the relevant information of the current state of the app that needs to be carried
    // over to the next time the app is relaunched
    private class SavedState {
        int bookingState;
        String departureStation, arrivalStation;
        private String departureTime, arrivalTime;
        private boolean checkingDock;
        SavedState(State state){
            bookingState = state.getBookingState();
            checkingDock = state.isCheckingDock();
            if (state.getDepartingStation() != null) {
                departureStation = state.getDepartingStation().getId();
                departureTime = state.getDepartureTime();
            }
            if (state.getArrivalStation() != null) {
                arrivalStation = state.getArrivalStation().getId();
                arrivalTime = state.getArrivalTime();
            }
        }
    }


    @Override
    protected void onDestroy() {
        // save stuff into share preferences for the next time app is opened
        // ie: which user is logged in right now, where they are up to in their booking status...
        SharedPreferences pref = getSharedPreferences("LOG_IN", Context.MODE_PRIVATE);
        SharedPreferences.Editor prefsEditor = pref.edit();
        Gson gson = new Gson();
        String json = gson.toJson(state.getUser());
        prefsEditor.putString(State.USER_KEY, json);
        prefsEditor.putInt(State.LOG_KEY, state.getLoggedState());
        json = gson.toJson(new SavedState(state));
        prefsEditor.putString(State.STATE_KEY, json);
        prefsEditor.apply();
        super.onDestroy();
    }

    // This is called to set up the screen once the server has returned the needed information on
    // trips and stations for initialisation
    private void setUpScreen(){
        viewPager.setOffscreenPageLimit(1);
        viewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        viewPager.setAdapter(new ViewPagerAdapter(getSupportFragmentManager(), getLifecycle()));

        // Scroll listener for the spinner at the top of the screen to change pages on view pages when appropriate
        pickerLayoutManager.setOnScrollStopListener(new PickerLayoutManager.onScrollStopListener() {
            @Override
            public void selectedView(View view) {
                currentFrag = fragmentTitles.indexOf(((TextView) view).getText().toString());
                viewPager.setCurrentItem(currentFrag, true);
            }
        });

        // Used to update the spinner at the top of screen when page changed and disable refreshing while changing the page
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (currentFrag != position) {
                    rvNavigationPicker.smoothScrollToPosition(position);
                }
                currentFrag = position;
            }

            @Override
            public void onPageScrollStateChanged(int s) {
                toggleRefreshing(s == ViewPager2.SCROLL_STATE_IDLE);
                if (state.getMapFragment() != null){
                    if (currentFrag == 1){
                        swipeRefreshLayout.setEnabled(false);
                    }
                }
            }
        });
        // start the app on the booking screen
        viewPager.setCurrentItem(1, false);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData(viewPager.getCurrentItem()); // called when the page is refreshed..
            }
        });
        // set the color scheme of the refresh icon and size
        swipeRefreshLayout.setProgressBackgroundColorSchemeColor(getColor(R.color.buttonColor));
        swipeRefreshLayout.setColorSchemeColors(getColor(R.color.offWhite));
        swipeRefreshLayout.setSize(1);

        rvNavigationPicker.setVisibility(View.VISIBLE);

        // get shares preferences -> the saved state of booking status and update departure/arrival stations accordingly
        SharedPreferences pref = getSharedPreferences("LOG_IN", Context.MODE_PRIVATE);
        String state_string = pref.getString(State.STATE_KEY, "null");
        if (!state_string.equals("null") && !serverRestart) {
            SavedState savedState = new Gson().fromJson(state_string, SavedState.class);
            if (savedState.bookingState > State.RESERVE_BIKE_SELECTION_STATE) {
                state.setBookingState(savedState.bookingState);
                if (savedState.departureStation != null) {
                    state.setDepartureTime(savedState.departureTime);
                    state.setDepartingStation(stationMap.get(savedState.departureStation));
                }
                if (savedState.arrivalStation != null) {
                    state.setArrivalTime(savedState.arrivalTime);
                    state.setArrivalStation(stationMap.get(savedState.arrivalStation));
                }
                if (savedState.checkingDock)
                    checkDockStatus();
            }
        }
    }

    // enables/disables refreshing page
    public void toggleRefreshing(boolean enabled) {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setEnabled(enabled);
        }
    }

    // called when the user pulls down to refresh the page
    public void refreshData(int page){
        new SendMessage(this, "refresh").execute(new Gson().toJson(new BookingMessageToServer("refresh", "", -1, -1)));
        new Refresh(this).execute();
        if (page == -1){ // page = -1 when refreshed from the map view page (Position 2 in view pager)
            final SearchFragment searchFragment = ((SearchFragment)((ViewPagerAdapter) viewPager.getAdapter()).getFragment(2));
            searchFragment.refreshing();
            new Handler().postDelayed(new Runnable() {
                @Override public void run() {
                    searchFragment.refreshed(); // updates the map markers
                    ((ProfileFragment)((ViewPagerAdapter) viewPager.getAdapter()).getFragment(0)).refreshTripList(); // update the trips list
                }
            }, 1500);  //keep refreshing for 1.5s and update the map fragment page after 1.5s
        } else { // refreshed from any other page
            new Handler().postDelayed(new Runnable() {
                @Override public void run() {
                    swipeRefreshLayout.setRefreshing(false);
                    if (state.getMapFragment() != null){ // this shouldn't happen but just in case, update the map markers of a pop-up map
                        state.getMapFragment().updateMarkers();
                    }
                    ((ProfileFragment)((ViewPagerAdapter) viewPager.getAdapter()).getFragment(0)).refreshTripList(); // update the trips list
                }
            }, 1500);
        }
    }

    // Called once permission for location has been granted for the first time
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                updateUserLocation();
            }
        }
    }

    // Starts the QR Scanner Activity
    public void startQRScanner() {
        openingQR = true;
        Intent myIntent = new Intent(MainActivity.this, QRScannerActivity.class);
        MainActivity.this.startActivityForResult(myIntent, 1);
    }

    // Get the last known location by cycling through all the providers and returns the highest accuracy location
    private Location getLastKnownLocation() {
        locationManager = (LocationManager)getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            @SuppressLint("MissingPermission") Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }

    public void updateUserLocation() {
        lastKnownLocation = getLastKnownLocation();
        for (Station station : getStations()){ // update the distance from station to current location
            station.updateDistanceFrom(lastKnownLocation);
        }
    }

    // Called when returning from QR Scanner Activity after scanning a code
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                queryServerStation(new BookingMessageToServer("QRScanned", data.getExtras().getString("QRCode").replaceAll("\\D+",""), -1, -1));
                ((BookingFragment)((ViewPagerAdapter)viewPager.getAdapter()).getFragment(1)).QRCodeScannedAnimation();
            }
            else if (resultCode == RESULT_CANCELED) {
                //Write your code if no result
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!openingQR) {  // if not opening the QR scanner
            updateUserInfo();
            new CloseSocket(this).execute();  // close the socket and update the user information when activity stops
        }
        openingQR = false;
    }

    @Override
    protected void onResume(){
        super.onResume();
        new Connect(this).execute();  // reconnect to the socket
    }

    // public method that can be used to send a request to server from any of the fragments
    public void queryServerStation(BookingMessageToServer msg){
        new SendMessage(this, msg.getKey()).execute(new Gson().toJson(msg));
    }

    // called when the user updates their profile page and when the user leaves the app (to save favourite stations etc)
    public void updateUserInfo(){
        new SendMessage(this,"updateUser").execute(new Gson().toJson(user));
    }

    // After the bike has been borrowed, the app should start checking with the server to see if bike has been docked
    public void checkDockStatus() {
        if (!state.isCheckingDock()) {
            state.setCheckingDock(true);
        }
        dockCheckThread = new Thread(new Runnable() {
            @Override
            public void run() {
                // TODO Auto-generated method stub
                    while(state.isCheckingDock()) {
                        SystemClock.sleep(3000); // check every 3 seconds
                        new SendMessage((MainActivity) context, "checkDock").execute(new Gson().toJson(new BookingMessageToServer("checkDock", "", -1, -1)));
                    }
            }
        });
        dockCheckThread.start();
    }

    // AsyncTask used to connect to the server initially
    private static class Connect extends AsyncTask<Void, Void, Void> {
        private WeakReference<MainActivity> activityReference;

        // only retain a weak reference to the activity
        Connect(MainActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            MainActivity activity = activityReference.get();
            if (!activity.ping) {
                try {
                    activity.clientSocket = new Socket(activity.IPHost, 8080); // ip address of computer
                    activity.out = new BufferedWriter(new OutputStreamWriter(activity.clientSocket.getOutputStream()));
                    activity.in = new DataInputStream(activity.clientSocket.getInputStream());
                    activity.out.write(new Gson().toJson(activity.user));
                    activity.out.flush();
                    activity.ping = true;
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    // Called to close the socket (sends a request to the server to close connection on their end too)
    private static class CloseSocket extends AsyncTask<Void, Void, Void> {
        private WeakReference<MainActivity> activityReference;
        // only retain a weak reference to the activity
        CloseSocket(MainActivity context) {
            activityReference = new WeakReference<>(context);
        }
        protected Void doInBackground(Void... voids) {
            MainActivity activity = activityReference.get();
            if (activity.ping) {
                try {
                    if (!activity.clientSocket.isClosed()) activity.clientSocket.close();
                    activity.clientSocket = new Socket(activity.IPHost, 8080);
                    activity.out = new BufferedWriter(new OutputStreamWriter(activity.clientSocket.getOutputStream()));
                    activity.in = new DataInputStream(activity.clientSocket.getInputStream());
                    activity.out.write(new Gson().toJson(new BookingMessageToServer("quit", "", -1, -1)));
                    activity.out.flush();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            MainActivity activity = activityReference.get();
            activity.ping = false;
            try {
                activity.clientSocket.close();
                activity.in.close();
                activity.out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Async task to send msg to client (requests)
    private static class SendMessage extends AsyncTask<String, Void, Void> {
        private WeakReference<MainActivity> activityReference;
        private String key;
        // only retain a weak reference to the activity
        SendMessage(MainActivity context, String key) {
            activityReference = new WeakReference<>(context);
            this.key = key;
        }

        @Override
        protected void onPostExecute(Void avoid) {  // listen for response after sending the request
            if (!key.equals("refresh") && !key.equals("updateUser") && activityReference.get().ping)
                new GetMsg(activityReference.get(), key).execute();
        }

        protected Void doInBackground(String... strings) {
            MainActivity activity = activityReference.get();
            if (activity.ping) {
                try {
                    if (!activity.clientSocket.isClosed()) {
                        activity.clientSocket.close();
                    }
                    activity.clientSocket = new Socket(activity.IPHost, 8080);
                    activity.out = new BufferedWriter(new OutputStreamWriter(activity.clientSocket.getOutputStream()));
                    activity.in = new DataInputStream(activity.clientSocket.getInputStream());
                    activity.out.write(strings[0]);
                    activity.out.flush();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return null;
        }
    }

    // Called to Refresh the page (request to server) in a Worker Thread
    private static class Refresh extends AsyncTask<Void, Void, Void> {
        private WeakReference<MainActivity> activityReference;
        private SearchFragment searchFragment;
        // only retain a weak reference to the activity
        Refresh(MainActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected Void doInBackground(Void... aVoid) {
            MainActivity activity = activityReference.get();
            if (activity.clientSocket.isClosed()) {
                try {
                    activity.clientSocket = new Socket(activity.IPHost, 8080);
                    activity.out = new BufferedWriter(new OutputStreamWriter(activity.clientSocket.getOutputStream()));
                    activity.in = new DataInputStream(activity.clientSocket.getInputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            String result = activity.readUTF8();
            activity.refreshLists(result);

            return null;
        }
    }

    // Called to listen to socket and receive info from server in a worker Thread
    private static class GetMsg extends AsyncTask<Void, Void, String> {
        private WeakReference<MainActivity> activityReference;
        private String key;
        // only retain a weak reference to the activity
        GetMsg(MainActivity context, String key) {
            this.key = key;
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected void onPostExecute(String result) { // after the info has been read, do something with the result...
            MainActivity activity = activityReference.get();
            if (activity.ping) {
                switch (key) { // switch case depending on the request key (ie: we do different stuff with the info received depending on this)
                    case "initialise":
                        activity.initialiseFromServer(result);
                        activity.setUpScreen();
                        break;
                    case "queryDepart":
                        activity.departQueryResults(result);
                        break;
                    case "QRScanned":
                        System.out.println(result);
                        activity.QRScanned(result);
                        break;
                    case "queryArrival":
                        activity.arrivalQueryResults(result);
                        break;
                    case "refresh":
                        activity.updateStationInfo(result);
                        ((SearchFragment) ((ViewPagerAdapter) activity.viewPager.getAdapter()).getFragment(2)).updateMarkers();
                        activity.swipeRefreshLayout.setRefreshing(false);
                        break;
                    case "checkDock":
                        if (!result.equals("")) { // if we receive something then the bike has been docked...
                            activity.state.setCheckingDock(false);
                            activity.state.setDockedStation(activity.stationMap.get(result));
                            activity.state.setBookingState(State.BIKE_DOCKED_STATE);
                            ((BookingFragment) ((ViewPagerAdapter) activity.viewPager.getAdapter()).getFragment(1)).updateParentView();
                            ((BookingFragment) ((ViewPagerAdapter) activity.viewPager.getAdapter()).getFragment(1)).reset();
                        }
                        break;
                }
            }
        }

        @Override
        protected String doInBackground(Void... aVoid) {
            MainActivity activity = activityReference.get();
            if (activity.ping) {
                if (activity.clientSocket.isClosed()) {
                    try {
                        activity.clientSocket = new Socket(activity.IPHost, 8080);
                        activity.out = new BufferedWriter(new OutputStreamWriter(activity.clientSocket.getOutputStream()));
                        activity.in = new DataInputStream(activity.clientSocket.getInputStream());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return activity.readUTF8(); // listen to InputStream and get the string
            }
            return null;
        }
    }

    // Called to extract the station and trip info on instantiation and store Station and Trip objects into respective lists/maps etc.
    private void initialiseFromServer(String s){
        try {
            JSONObject jsonObject  = new JSONObject(s);
            JSONArray staticInfo = new JSONArray(jsonObject.getString("staticInfo"));  // jsonArrays are stored in a dictionary format
            for (int i = 0; i < staticInfo.length(); i++) {
                JSONObject jsonObj = staticInfo.getJSONObject(i);
                Station station = new Station(this, jsonObj.getDouble("lat"), jsonObj.getDouble("long"), jsonObj.getInt("cap"), jsonObj.getString("id"));
                stations.add(station);
                stationMap.put(station.getId(), station);
                stationNames.add(station.getName());
            }
            updateStationInfo(jsonObject.getString("dynamicInfo"));

            JSONArray tripInfo = new JSONArray(jsonObject.getString("tripInfo"));
            for (int i = 0; i < tripInfo.length(); i++) {
                JSONObject jsonObj = tripInfo.getJSONObject(i);
                Trip trip = new Trip(jsonObj.getString("date"), stationMap.get(jsonObj.getString("startStation")).getName(),
                        stationMap.get(jsonObj.getString("endStation")).getName(),
                        jsonObj.getInt("startTime"), jsonObj.getInt("endTime"),
                        jsonObj.getString("bike"), jsonObj.getInt("duration"));
                trips.add(trip);
            }

            if (jsonObject.getString("server").equals("serverRestart")){
                serverRestart = true;
            } else {
                serverRestart = false;
            }

        } catch (JSONException | IOException e){
            System.out.println(e);
        }
        for (String stationId : user.getFavStations()){ // get the favourite stations of the user and update Station Objects
            stationMap.get(stationId).setAsFavourite();
        }
        Collections.sort(stations);  // sort the stations by alphabetical order and push the favourites to the top...
    }

    // Updates the Station and Trip lists to reflect current station occupancies
    private void refreshLists(String s){
        trips = new ArrayList<>();
        try {
            JSONObject jsonObject  = new JSONObject(s);
            updateStationInfo(jsonObject.getString("stationInfo")); // jsonArrays are stored in a dictionary format

            JSONArray tripsArr =  new JSONArray(jsonObject.getString("tripInfo"));
            for (int i = 0; i < tripsArr.length(); i++) {
                JSONObject jsonObj = tripsArr.getJSONObject(i);
                Trip trip = new Trip(jsonObj.getString("date"), stationMap.get(jsonObj.getString("startStation")).getName(),
                        stationMap.get(jsonObj.getString("endStation")).getName(),
                        jsonObj.getInt("startTime"), jsonObj.getInt("endTime"),
                        jsonObj.getString("bike"), jsonObj.getInt("duration"));
                trips.add(trip);
            }
        } catch (JSONException e){
            System.out.println(e);
        }
    }

    // This updates the dynamic station information (ie: their fill levels)
    private void updateStationInfo(String s){
        try {
            JSONArray jsonArr  = new JSONArray(s);
            for (int i = 0; i < jsonArr.length(); i++) {
                JSONObject jsonObj = jsonArr.getJSONObject(i);
                stationMap.get(jsonObj.getString("id")).setOccupancy(jsonObj.getInt("occ"));
            }
        } catch (JSONException e){
            System.out.println(e);
        }
    }

    // Called after the server returns the results of the query to book a bike
    private void departQueryResults(String s){
        try {
            if (s.equals("empty")){  // if server returns all docks are empty at nearby stations, reset the booking state
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("We're sorry... all stations within walkable distance are predicted to be empty. Please try again later or change your reservation.");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        state.resetState();
                        ((BookingFragment)((ViewPagerAdapter) viewPager.getAdapter()).getFragment(1)).updateParentView();
                        ((BookingFragment) ((ViewPagerAdapter) viewPager.getAdapter()).getFragment(1)).toggleSplashPage();
                    }
                });
                AlertDialog confirmAlert = builder.create();
                confirmAlert.show();
                return;
            }
            state.setQuerySuccess(true);
            JSONArray jsonArr  = new JSONArray(s);
            interchangeables = new ArrayList<>(); // this is used to set-up the map markers and cards
            for (int i = 0; i < jsonArr.length(); i++) {
                JSONObject jsonObj = jsonArr.getJSONObject(i);
                Station station = stationMap.get(jsonObj.getString("id"));
                station.setOccupancy(jsonObj.getInt("occ"));
                station.setPredictedOcc(jsonObj.getInt("predictedOcc"));
                interchangeables.add(stationMap.get(jsonObj.getString("id")));
                if (jsonObj.getInt("assigned") == 1) {
                    assigned = station;
                }
            }
            ((BookingFragment) ((ViewPagerAdapter) viewPager.getAdapter()).getFragment(1)).toggleSplashPage();
        } catch (JSONException e){
            System.out.println(e);
        }
    }

    // Called after the server acknowledges that the QR code has been scanned, and returns whether the scanned QR is valid
    private void QRScanned(String s){
        if (s.equals("success")) {  // if scanned the correct station and valid dock
            state.bookingStateTransition(true);  // go to the next step of booking
            checkDockStatus();
        } else if (s.equals("empty")){  // the scanned station is completely empty
            ((BookingFragment)((ViewPagerAdapter) viewPager.getAdapter()).getFragment(1))
                    .addQRScreenMessage(new Message("out",
                            String.format("%s.", assigned.getName())));
            ((BookingFragment)((ViewPagerAdapter) viewPager.getAdapter()).getFragment(1))
                    .addQRScreenMessage(new Message("in",
                            "We're sorry... the station is completely out of bikes. Please try waiting a couple minutes or changing this reservation"));
        } else {  // QR code scanned belongs to another station (compared to reserved one)
            ((BookingFragment)((ViewPagerAdapter) viewPager.getAdapter()).getFragment(1))
                .addQRScreenMessage(new Message("out",
                        String.format("%s.", stationMap.get(s).getName())));
            ((BookingFragment)((ViewPagerAdapter) viewPager.getAdapter()).getFragment(1))
                    .addQRScreenMessage(new Message("in",
                            String.format("QR Code scanned belongs to %s! You booked a departure from %s.", stationMap.get(s).getName(), state.getDepartingStation().getName())));
            ((BookingFragment)((ViewPagerAdapter) viewPager.getAdapter()).getFragment(1))
                    .addQRScreenMessage(new Message("in", "If you want to depart from here, please rebook by cancelling this reservation!"));
        }
    }

    // Called after the server returns the results of the query to book a dock (server will always return something)
    private void arrivalQueryResults(String s){
        try {
            JSONArray jsonArr  = new JSONArray(s);
            interchangeables = new ArrayList<>();
            for (int i = 0; i < jsonArr.length(); i++) {
                JSONObject jsonObj = jsonArr.getJSONObject(i);
                Station station = stationMap.get(jsonObj.getString("id"));
                station.setOccupancy(jsonObj.getInt("occ"));
                station.setPredictedOcc(station.getCapacity() - jsonObj.getInt("predictedDocks"));
                station.setEstArr(jsonObj.getInt("estArr"));
                interchangeables.add(stationMap.get(jsonObj.getString("id")));
                if (jsonObj.getInt("assigned") == 1) {
                    assigned = station;
                }
            }
        } catch (JSONException e){
            System.out.println(e);
        }
        state.setQuerySuccess(true);
        ((BookingFragment) ((ViewPagerAdapter) viewPager.getAdapter()).getFragment(1)).toggleSplashPage();
    }

    // Returns string until end of line (-1) of the input stream
    private String readUTF8(){
        int n;
        char[] buffer = new char[1024 * 4];
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(in, "UTF8");
            StringWriter writer = new StringWriter();
            while (ping){
                n = reader.read(buffer);
                if (n == -1){
                    return writer.toString();
                }
                writer.write(buffer, 0, n);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    // getter functions
    public ArrayList<Station> getStations(){
        return stations;
    }

    public ArrayList<Trip> getTrips() {
        return trips;
    }
}