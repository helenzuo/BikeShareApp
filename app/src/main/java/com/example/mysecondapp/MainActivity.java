package com.example.mysecondapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.ref.WeakReference;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private int selectionState;
    public Station reservedDepartureStation;
    public Station selectedDepartureStation;
    public Station selectedArrivalStation;
    public String departureTime = "";
    public String distanceWalking = "";
    public boolean customDistance = false;
    public String reservationTime;

    public String firstName;
    public String surname;
    public String email;
    public String gender;
    public NavController navController;
    public BottomNavigationView navView;
    private ArrayList<Station> stations;
    private ArrayList<Station> favouriteStations;

    private Socket clientSocket;
    private BufferedWriter out;
    private DataInputStream in;
    boolean ping = false;

    public HashMap<String, Station> stationMap = new HashMap<String, Station>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        selectionState = STATIC_DEFINITIONS.START_BOOKING_STATE;
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                navView.setVisibility(View.VISIBLE);
            }
        });
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        stations = new ArrayList<Station>();
        Station tempStation = null;
        try {
            tempStation = new Station("Bourke", 144.96803330048928, -37.81393990463194, 22, 15, this);
            stations.add(tempStation);
            stationMap.put(tempStation.getName(), tempStation);
            tempStation = new Station("Collins", 144.97192836840318, -37.815022142571, 22, 8, this);
            stations.add(tempStation);
            stationMap.put(tempStation.getName(), tempStation);
            tempStation = new Station("Flagstaff", 144.9606810878229, -37.81493656474232, 22, 20, this);
            stations.add(tempStation);
            stationMap.put(tempStation.getName(), tempStation);
            tempStation = new Station("Flinders", 144.9699301383418, -37.81673328727038, 22, 20, this);
            stations.add(tempStation);
            stationMap.put(tempStation.getName(), tempStation);
            tempStation = new Station("Lcollins", 144.97003459169014, -37.81451269649511, 22, 8, this);
            stations.add(tempStation);
            stationMap.put(tempStation.getName(), tempStation);
            tempStation = new Station("Lygon", 144.96766165512835, -37.81182608904015, 22, 15, this);
            stations.add(tempStation);
            stationMap.put(tempStation.getName(), tempStation);
            tempStation = new Station("mc", 144.9647403339459, -37.81155259053488, 22, 16, this);
            stations.add(tempStation);
            stationMap.put(tempStation.getName(), tempStation);
            tempStation = new Station("Parliament", 144.97304788833318, -37.811337130250315, 22, 16, this);
            stations.add(tempStation);
            stationMap.put(tempStation.getName(), tempStation);
            tempStation = new Station("Queens", 144.96316335849318, -37.81421830705699, 22, 10, this);
            stations.add(tempStation);
            stationMap.put(tempStation.getName(), tempStation);
            tempStation = new Station("Rmit", 144.96381831022302, -37.80956830069795, 22, 13, this);
            stations.add(tempStation);
            stationMap.put(tempStation.getName(), tempStation);
            tempStation = new Station("Yarra", 144.97239159357775, -37.81601925202357, 22, 20, this);
            stations.add(tempStation);
            stationMap.put(tempStation.getName(), tempStation);
        } catch (IOException e) {
            e.printStackTrace();
        }

        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        favouriteStations = new ArrayList<>();
        appendFavouriteStation(stations.get(1));

//        new Connect(this).execute();

    }

    public void bookingStateTransition(boolean forward){
        if (forward){
            selectionState++;
        } else {
            selectionState--;
        }
    }

    public int getBookingState(){
        return selectionState;
    }

    public void startQRScanner() {
        Intent myIntent = new Intent(MainActivity.this, QRScanner.class);
        MainActivity.this.startActivityForResult(myIntent, 1);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getSupportActionBar().show();
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
//                String result = data.getExtras().getString("QRCode");
                bookingStateTransition(true);
                navController.popBackStack(R.id.navigation_home, true);
                navController.navigate(R.id.navigation_home);
            }
            else if (resultCode == RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
//        new CloseSocket(this).execute();
    }

    @Override
    protected void onResume(){
        super.onResume();
//        new Connect(this).execute();
    }

    public ArrayList<Station> getStations(){
        return stations;
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.top_action_bar, menu);
        MenuItem searchItem = menu.findItem(R.id.search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        LinearLayout searchEditFrame = (LinearLayout) searchView.findViewById(R.id.search_edit_frame); // Get the Linear Layout
        // Get the associated LayoutParams and set leftMargin
        ((LinearLayout.LayoutParams) searchEditFrame.getLayoutParams()).leftMargin = 0;
        ((LinearLayout.LayoutParams) searchEditFrame.getLayoutParams()).rightMargin = 0;
        searchView.onActionViewExpanded();
        return super.onCreateOptionsMenu(menu);
    }

    public void appendFavouriteStation(Station station) {
        favouriteStations.add(station);
    }

    public void removeFavouriteStation(Station station) {
        favouriteStations.remove(station);
    }

    public ArrayList<Station> getFavouriteStations(){
        return favouriteStations;
    }


    @Override public void onBackPressed() {
        navView.setVisibility(View.VISIBLE);
        Objects.requireNonNull(getSupportActionBar()).show();
        super.onBackPressed();
    }

    public void queryDepartureStation(int minutesUntilBorrow, Station departingStation, Station arrivingStation){
        new SendMessage(this).execute(Integer.toString(minutesUntilBorrow), departingStation.getName(), arrivingStation.getName());
    }

    private static class Connect extends AsyncTask<Void, Void, Boolean> {
        private WeakReference<MainActivity> activityReference;
        // only retain a weak reference to the activity
        Connect(MainActivity context) {
            activityReference = new WeakReference<>(context);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            MainActivity activity = activityReference.get();
            if (!activity.ping) {
                try {
                    activity.clientSocket = new Socket("10.0.2.2", 8080);
                    activity.out = new BufferedWriter(new OutputStreamWriter(activity.clientSocket.getOutputStream()));
                    activity.in = new DataInputStream(activity.clientSocket.getInputStream());
                    activity.ping = true;
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return activity.ping;
        }
    }
    // Called to perform work in a worker thread.
    private static class SendMessage extends AsyncTask<String, Void, Void> {
        private WeakReference<MainActivity> activityReference;
        // only retain a weak reference to the activity
        SendMessage(MainActivity context) {
            activityReference = new WeakReference<>(context);
        }
        protected Void doInBackground(String... strings) {
            MainActivity activity = activityReference.get();
            if (activity.ping) {
                try {
                    String outputMessage = "QUERY START";
                    for (String string : strings) {
                        outputMessage += "#" + string;
                    }
                    outputMessage += "#QUERY END" ;
                    activity.out.write(outputMessage);
                    activity.out.flush();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            return null;
        }
    }


    // Called to perform work in a worker thread.
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
                    activity.out.write("quit");
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

    public String readUTF8() throws IOException {
        String msg = "";
        if (ping) {
            if (in.available() > 0) {
                int length = in.readInt();
                byte[] encoded = new byte[length];
                in.readFully(encoded);
                msg = new String(encoded, StandardCharsets.UTF_8);
            }
        }
        return msg;
    }

    public boolean getPing(){
        return ping;
    }

}