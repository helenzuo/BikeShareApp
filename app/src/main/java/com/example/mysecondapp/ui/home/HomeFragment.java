package com.example.mysecondapp.ui.home;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.airbnb.lottie.LottieAnimationView;
import com.example.mysecondapp.CustomTimePickeerDialog;
import com.example.mysecondapp.MainActivity;
import com.example.mysecondapp.MyAdapter;
import com.example.mysecondapp.OnSwipeTouchListener;
import com.example.mysecondapp.R;
import com.example.mysecondapp.STATIC_DEFINITIONS;
import com.example.mysecondapp.Station;
import com.example.mysecondapp.ui.map.MapFragment;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class HomeFragment extends Fragment implements View.OnClickListener, View.OnTouchListener, TextWatcher {

    private ArrayList<RelativeLayout> relativeLayoutContainers = new ArrayList<>();
    private RelativeLayout startStateContainer;
    private RelativeLayout bikeReserveDetailsContainer;
    private RelativeLayout departureStationSelectedContainer;
    private RelativeLayout arrivalStationSelectionContainer;

    private Button startBookingButton;

    private RelativeLayout timeSelectLayout;
    private RelativeLayout distanceSelectLayout;
    private EditText departureStationEditText;
    private ColorFilter standardEditTextLine;
    private Drawable standardEditTextBackground;
    private ListView departureStationListView;
    private ImageButton mapSearchButton;
    private EditText timeEditText;
    private EditText distanceText;
    private SeekBar seekBar;
    private SwitchCompat customDistanceSwitch;
    private Button searchButton;

    private ImageButton directionsButton;
    private Button cancelReservationButton;

    private RelativeLayout arrivalTimeSelectLayout;
    private RelativeLayout arrivalDistanceSelectLayout;
    private EditText arrivalStationEditText;
    private ListView arrivalStationListView;
    private ImageButton mapArrivalSearchButton;
    private EditText arrivalTimeEditText;
    private SeekBar borrowLengthSeekBar;
    private EditText arrivalDistanceText;
    private SeekBar arrivalDistanceSeekBar;
    private SwitchCompat customArrivalDistanceSwitch;
    private Button searchArrivalButton;

    private MainActivity main;
    private View root;

    private Station selectedDepartingStation;
    private Station selectedArrivalStation;

    AlphaAnimation inAnimation;
    AlphaAnimation outAnimation;
    FrameLayout progressBarHolder;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_home, container, false);
        main = (MainActivity) getActivity();

        // Get the relative layout  containers for each state and store into array list
        relativeLayoutContainers = new ArrayList<>();
        startStateContainer = root.findViewById(R.id.startStateLayoutContainer);
        relativeLayoutContainers.add(startStateContainer);
        bikeReserveDetailsContainer = root.findViewById(R.id.queryDepartureLayoutContainer);
        relativeLayoutContainers.add(bikeReserveDetailsContainer);
        departureStationSelectedContainer = root.findViewById(R.id.departureStationSelectedLayout);
        relativeLayoutContainers.add(departureStationSelectedContainer);
        arrivalStationSelectionContainer = root.findViewById(R.id.queryArrivalLayoutContainer);
        relativeLayoutContainers.add(arrivalStationSelectionContainer);

        // Initialise all layouts as invisible first and then set the relevant one as visible
        for (RelativeLayout relativeLayout : relativeLayoutContainers) {
            relativeLayout.setVisibility(GONE);
        }
        relativeLayoutContainers.get(main.getBookingState()).setVisibility(VISIBLE);

        switch (main.getBookingState()) {
            case STATIC_DEFINITIONS.START_BOOKING_STATE:
                startBookingButton = root.findViewById(R.id.startBookingButton);
                startBookingButton.setOnClickListener(this);
                break;
            case STATIC_DEFINITIONS.RESERVE_BIKE_SELECTION_STATE:
                loadDepartureStationSelectionPage();
                break;
            case STATIC_DEFINITIONS.DEPARTURE_STATION_SELECTED_STATE:
                loadQRScannerPage();
                break;
            case STATIC_DEFINITIONS.QR_SCANNED_STATE:
                loadArrivalStationSelectionPage();
                break;
        }
        return root;
    }
    private void loadArrivalStationSelectionPage(){
        arrivalTimeSelectLayout = root.findViewById(R.id.arriveTimePickLayout);
        arrivalDistanceSelectLayout = root.findViewById(R.id.walkingRelativeLayoutArrival);

        arrivalStationEditText = root.findViewById(R.id.arrivalStationEditText);
        arrivalStationListView = root.findViewById(R.id.arrivalStationListView);
        mapArrivalSearchButton = root.findViewById(R.id.mapViewButtonArrival);
        arrivalTimeEditText = root.findViewById(R.id.progressBorrowLengthTextView);
        borrowLengthSeekBar = root.findViewById(R.id.borrowLengthSeekBar);
        arrivalDistanceText = root.findViewById(R.id.progressTextViewArrival);
        arrivalDistanceSeekBar = root.findViewById(R.id.walkingDistanceSeekBarArrival);
        customArrivalDistanceSwitch = root.findViewById(R.id.customDistanceSwitchArrival);
        searchArrivalButton = root.findViewById(R.id.nextButtonTimeSelectedArrival);
        // Get the standard colour of the edit line colour for future use
        standardEditTextLine = arrivalStationEditText.getBackground().mutate().getColorFilter();

        // Station search with list view filter
        final MyAdapter arrivalAdapter = new MyAdapter(getActivity(), R.layout.station_list_card_design, main.getStations(), main.getFavouriteStations(), main);
        arrivalStationListView.setAdapter(arrivalAdapter);
        arrivalAdapter.notifyDataSetChanged();
        standardEditTextBackground = arrivalStationEditText.getBackground();
        arrivalStationEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // make the clear text drawable visible depending on if text has been entered
//                updateDepartureStationEditGraphics();
                // Filter the listview depending on text entered
                arrivalAdapter.getFilter().filter(s.toString().toLowerCase().trim());
            }
        });
        arrivalStationEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
//                updateDepartureStationEditGraphics();
            }
        });

        arrivalStationEditText.setOnTouchListener(this);
        arrivalStationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                arrivalStationEditText.setText(((Station) arrivalAdapter.getItem(position)).getName());
                selectedArrivalStation = (Station) arrivalAdapter.getItem(position);
                updateScreenGraphics();
            }
        });
        // set listener for button next to "search bar" that allows user to select station from map
        mapArrivalSearchButton.setOnClickListener(this);

        // Do not show keyboard for time edit text and set listener
//        arrivalTimeEditText.setShowSoftInputOnFocus(false);
        borrowLengthSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                arrivalTimeEditText.setText(String.format(Locale.getDefault(),"%d",progress * 5));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // set listener for when seekbar slider is changed, to update the distance text
        arrivalDistanceSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                arrivalDistanceText.setText(String.format(Locale.getDefault(),"%d",progress * 100));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // set listener to toggle between using the slider and entering distance with keyboard
//        customDistanceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked){
//                    main.customDistance = true;
//                    distanceText.setEnabled(true);
//                    seekBar.setVisibility(View.INVISIBLE);
//                } else {
//                    main.customDistance = false;
//                    distanceText.setEnabled(false);
//                    seekBar.setVisibility(VISIBLE);
//                    seekBar.setProgress(Integer.min(Integer.parseInt(distanceText.getText().toString())/100,15));
//                }
//                hideKeyboardState1();
//            }
//        });

        searchArrivalButton.setVisibility(GONE);
        searchArrivalButton.setOnClickListener(this);

//        if (main.selectedDepartureStation != null)
//            departureStationEditText.setText(main.selectedDepartureStation.getName());
//        if (!main.departureTime.isEmpty())
//            timeEditText.setText(main.departureTime);
//        if (!main.distanceWalking.isEmpty()) {
//            distanceText.setText(main.distanceWalking);
//            if (main.customDistance){
//                seekBar.setVisibility(View.INVISIBLE);
//            } else {
//                seekBar.setProgress(Integer.parseInt(main.distanceWalking)/100);
//            }
//        } else {
//            System.out.println(main.distanceWalking);
//            seekBar.setProgress(5);
//        }
//        customDistanceSwitch.setChecked(main.customDistance);
        updateScreenGraphics();

//        distanceText.addTextChangedListener(this);
//        timeEditText.addTextChangedListener(this);

        // set touch listener to background so that focus is removed when touching it
        root.setOnTouchListener(this);
    }

    private void loadDepartureStationSelectionPage(){
        timeSelectLayout = root.findViewById(R.id.departTimePickLayout);
        distanceSelectLayout = root.findViewById(R.id.walkingRelativeLayout);

        departureStationEditText = root.findViewById(R.id.departureStationEditText);
        departureStationListView = root.findViewById(R.id.departureStationListView);
        mapSearchButton = root.findViewById(R.id.mapViewButton);
        timeEditText = root.findViewById(R.id.timeEditText);
        distanceText = root.findViewById(R.id.progressTextView);
        seekBar = root.findViewById(R.id.walkingDistanceSeekBar);
        customDistanceSwitch = root.findViewById(R.id.customDistanceSwitch);
        searchButton = root.findViewById(R.id.nextButtonTimeSelected);
        // Get the standard colour of the edit line colour for future use
        standardEditTextLine = departureStationEditText.getBackground().mutate().getColorFilter();

        // Station search with list view filter
        final MyAdapter departureAdapter = new MyAdapter(getActivity(), R.layout.station_list_card_design, main.getStations(), main.getFavouriteStations(), main);
        departureStationListView.setAdapter(departureAdapter);
        departureAdapter.notifyDataSetChanged();
        standardEditTextBackground = departureStationEditText.getBackground();
        departureStationEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                // make the clear text drawable visible depending on if text has been entered
                updateDepartureStationEditGraphics();
                // Filter the listview depending on text entered
                departureAdapter.getFilter().filter(s.toString().toLowerCase().trim());
            }
        });
        departureStationEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                updateDepartureStationEditGraphics();
            }
        });

        departureStationEditText.setOnTouchListener(this);
        departureStationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                departureStationEditText.setText(((Station) departureAdapter.getItem(position)).getName());
                selectedDepartingStation = (Station) departureAdapter.getItem(position);
                updateScreenGraphics();
            }
        });
        departureStationListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                updateScreenGraphics();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });

        // set listener for button next to "search bar" that allows user to select station from map
        mapSearchButton.setOnClickListener(this);

        // Do not show keyboard for time edit text and set listener
        timeEditText.setShowSoftInputOnFocus(false);
        timeEditText.setOnClickListener(this);
        // set listener for when seekbar slider is changed, to update the distance text
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                distanceText.setText(String.format(Locale.getDefault(),"%d",progress * 100));
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        // set listener to toggle between using the slider and entering distance with keyboard
        customDistanceSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    main.customDistance = true;
                    distanceText.setEnabled(true);
                    seekBar.setVisibility(View.INVISIBLE);
                } else {
                    main.customDistance = false;
                    distanceText.setEnabled(false);
                    seekBar.setVisibility(VISIBLE);
                    seekBar.setProgress(Integer.min(Integer.parseInt(distanceText.getText().toString())/100,15));
                }
                updateScreenGraphics();
            }
        });

        searchButton.setVisibility(GONE);
        searchButton.setOnClickListener(this);

        if (main.selectedDepartureStation != null)
            departureStationEditText.setText(main.selectedDepartureStation.getName());
        if (!main.departureTime.isEmpty())
            timeEditText.setText(main.departureTime);
        if (!main.distanceWalking.isEmpty()) {
            distanceText.setText(main.distanceWalking);
            if (main.customDistance){
                seekBar.setVisibility(View.INVISIBLE);
            } else {
                seekBar.setProgress(Integer.parseInt(main.distanceWalking)/100);
            }
        } else {
            seekBar.setProgress(5);
        }
        customDistanceSwitch.setChecked(main.customDistance);
        updateScreenGraphics();

        distanceText.addTextChangedListener(this);
        timeEditText.addTextChangedListener(this);

        // set touch listener to background so that focus is removed when touching it
        root.setOnTouchListener(this);
    }

    private void loadQRScannerPage(){
        TextView textView = root.findViewById(R.id.textViewSlideDown);
        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(500);
        anim.setStartOffset(500);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        textView.startAnimation(anim);
        ((TextView)root.findViewById(R.id.bookedDepartureStationNameTextView)).setText(main.reservedDepartureStation.getName());
        ((TextView)root.findViewById(R.id.reservedTimeDetails)).setText(main.departureTime);
        ((TextView)root.findViewById(R.id.reservedAddressDetails)).setText(main.reservedDepartureStation.getAddress());
        directionsButton = root.findViewById(R.id.directionsButton);
        cancelReservationButton = root.findViewById(R.id.cancelReservationButton);
        directionsButton.setOnClickListener(this);
        cancelReservationButton.setOnClickListener(this);
        root.setOnTouchListener(new OnSwipeTouchListener(getContext()){
            public void onSwipeBottom() {
                main.startQRScanner();
            }
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
    }


    private void updateDepartureStationEditGraphics(){
        if (departureStationEditText.hasFocus()){
            departureStationListView.setVisibility(View.VISIBLE);
            departureStationEditText.setBackgroundResource(R.drawable.edit_text_focus);
            root.findViewById(R.id.departTimePickLayout).setVisibility(GONE);
            root.findViewById(R.id.walkingRelativeLayout).setVisibility(View.GONE);
            searchButton.setVisibility(GONE);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) departureStationEditText.getLayoutParams();
            params.setMarginStart((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics()));
            departureStationEditText.setLayoutParams(params);
            departureStationEditText.setTextColor(Color.BLACK);
            if (departureStationEditText.getText().toString().length() > 0)
                departureStationEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, ResourcesCompat.getDrawable(getResources(), R.drawable.edit_text_clear, main.getTheme()), null);
        } else if (departureStationEditText.getText().toString().length() == 0 || !departureStationEditText.hasFocus()) {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) departureStationEditText.getLayoutParams();
            params.setMarginStart((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, getResources().getDisplayMetrics()));
            departureStationEditText.setLayoutParams(params);
            departureStationEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
            departureStationEditText.setBackground(standardEditTextBackground);
            departureStationEditText.setTextColor(getResources().getColor(R.color.offWhite));
            selectedDepartingStation = checkForStationMatch(departureStationEditText.getText().toString());
            if (selectedDepartingStation != null){
                departureStationEditText.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.holo_green_light), PorterDuff.Mode.SRC_ATOP);
            } else if (departureStationEditText.getText().toString().length() > 0){
                departureStationEditText.getBackground().mutate().setColorFilter(getResources().getColor(android.R.color.holo_red_light), PorterDuff.Mode.SRC_ATOP);
                Toast.makeText(getContext(),"Please select a station that exists", Toast.LENGTH_SHORT).show();
            } else {
                departureStationEditText.getBackground().mutate().setColorFilter(standardEditTextLine);
            }
            updateScreenGraphics();
        }
    }

    private void replaceFragment(Fragment fragment) {
        try {
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.replace(R.id.resultsMapFragmentContainer, fragment ); // give your fragment container id in first parameter
            transaction.addToBackStack(null);  // if written, this transaction will be added to backstack
            transaction.commit();
        } catch (Exception ignored) { }
    }

    private Station checkForStationMatch(String enteredStation){
        for (Station station : main.getStations()){
            if (station.getName().toLowerCase().equals(enteredStation.toLowerCase().trim())){
                return station;
            }
        }
        return null;
    }

    private void updateBikeReserveDetailsContainerVisibility() {
        selectedDepartingStation = checkForStationMatch(departureStationEditText.getText().toString());
        if (selectedDepartingStation == null){
            timeSelectLayout.setVisibility(GONE);
            distanceSelectLayout.setVisibility(GONE);
            searchButton.setVisibility(GONE);
        }
        if (selectedDepartingStation != null) {
            main.selectedDepartureStation = selectedDepartingStation;
            departureStationListView.setVisibility(GONE);
            timeSelectLayout.setVisibility(View.VISIBLE);
            if (timeEditText.getText().toString().length() > 0){
                Calendar now = Calendar.getInstance();
                int minutes = now.get(Calendar.HOUR_OF_DAY)*60 + now.get(Calendar.MINUTE);
                if (timeInInt(timeEditText.getText().toString()) > minutes) {
                    distanceSelectLayout.setVisibility(VISIBLE);
                    if (customDistanceSwitch.isChecked()) {
                        if (distanceText.getText().toString().trim().length() > 0) {
                            distanceText.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_green_light)));
                            searchButton.setVisibility(VISIBLE);
                        } else {
                            distanceText.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_red_light)));
                            searchButton.setVisibility(GONE);
                        }
                    } else {
                        searchButton.setVisibility(VISIBLE);
                        distanceText.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
                    }
                } else {
                    distanceSelectLayout.setVisibility(GONE);
                    timeEditText.setText("");
                }
            }
        }
    }

    private void updateDockReserveDetailsContainerVisibility() {
        selectedArrivalStation = checkForStationMatch(arrivalStationEditText.getText().toString());
        if (selectedArrivalStation == null){
            arrivalTimeSelectLayout.setVisibility(GONE);
            arrivalDistanceSelectLayout.setVisibility(GONE);
            searchArrivalButton.setVisibility(GONE);
        }
        if (selectedArrivalStation != null) {
            main.selectedArrivalStation = selectedArrivalStation;
            arrivalStationListView.setVisibility(GONE);
            arrivalTimeSelectLayout.setVisibility(View.VISIBLE);
            if (arrivalTimeEditText.getText().toString().length() > 0){
//                Calendar now = Calendar.getInstance();
//                int minutes = now.get(Calendar.HOUR_OF_DAY)*60 + now.get(Calendar.MINUTE);
                if (Integer.parseInt(arrivalTimeEditText.getText().toString()) < 180) {
                    arrivalDistanceSelectLayout.setVisibility(VISIBLE);
                    if (customArrivalDistanceSwitch.isChecked()) {
                        if (arrivalDistanceText.getText().toString().trim().length() > 0) {
                            arrivalDistanceText.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_green_light)));
                            searchArrivalButton.setVisibility(VISIBLE);
                        } else {
                            arrivalDistanceText.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(android.R.color.holo_red_light)));
                            searchArrivalButton.setVisibility(GONE);
                        }
                    } else {
                        searchArrivalButton.setVisibility(VISIBLE);
                        arrivalDistanceText.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
                    }
                } else {
                    arrivalDistanceSelectLayout.setVisibility(GONE);
//                    arrivalTimeEditText.setText("");
                }
            }
        }
    }
    private int timeInInt(String s) {
        s = s.trim();
        String[] hourMinAP = s.split(":");
        int hour = Integer.parseInt(hourMinAP[0]);
        String[] minAP = hourMinAP[1].split(" ");
        int min = Integer.parseInt(minAP[0]);
        if (minAP[1].equals("PM") && hour != 12){
            hour += 12;
        }
        return hour * 60 + min;
    }

    private void updateScreenGraphics(){
        hideKeyboard();
        switch (main.getBookingState()){
            case STATIC_DEFINITIONS.SERVER_DEPARTURE_STATION_QUERY:
                updateBikeReserveDetailsContainerVisibility();
                departureStationEditText.clearFocus();
                timeEditText.clearFocus();
                distanceText.clearFocus();
                break;
            case STATIC_DEFINITIONS.QR_SCANNED_STATE:
                updateDockReserveDetailsContainerVisibility();
                arrivalTimeEditText.clearFocus();
                arrivalTimeEditText.clearFocus();
                arrivalDistanceText.clearFocus();
        }

    }

    private void hideKeyboard(){
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(root.getApplicationWindowToken(), 0);
    }

    public void setDepartureStationFromMap(Station station){
        departureStationEditText.setText(station.getName());
        updateScreenGraphics();
    }

    private void showTimePickerDialog(){
        Calendar mcurrentTime = Calendar.getInstance();
        int timeInMinutes = mcurrentTime.get(Calendar.HOUR_OF_DAY) * 60 + mcurrentTime.get(Calendar.MINUTE) + 5;
        final int hour = timeInMinutes/60;
        final int minute = timeInMinutes%60;
        final CustomTimePickeerDialog mTimePicker = new CustomTimePickeerDialog(getContext(), R.style.themeOnverlay_timePicker, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

            }
        }, hour, minute, false, timeEditText, distanceSelectLayout, searchButton);
        mTimePicker.show();
        Objects.requireNonNull(mTimePicker.getWindow()).setBackgroundDrawableResource(R.drawable.rounded_corners);
        mTimePicker.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE);
        mTimePicker.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE);
    }

    @Override
    public void onClick(View v) {
        if (v == startBookingButton){
            main.bookingStateTransition(true);
            main.viewPager.getAdapter().notifyDataSetChanged();
        } else if (v == timeEditText) {
            showTimePickerDialog();
        } else if (v == mapSearchButton){
            bikeReserveDetailsContainer.setVisibility(GONE);
            replaceFragment(new MapFragment(STATIC_DEFINITIONS.STATION_LOOK_UP));
        } else if (v == searchButton){
            bikeReserveDetailsContainer.setVisibility(GONE);
            replaceFragment(new MapFragment(STATIC_DEFINITIONS.SERVER_DEPARTURE_STATION_QUERY));
        } else if (v == directionsButton){
            double latitude = main.reservedDepartureStation.getLocation().latitude;
            double longitude = main.reservedDepartureStation.getLocation().longitude;
            String uriBegin = "geo:" + latitude + "," + longitude;
            String query = latitude + "," + longitude + "(" + main.reservedDepartureStation.getName() + ")";
            String encodedQuery = Uri.encode(query);
            String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";
            Uri uri = Uri.parse(uriString);
            Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
            startActivity(intent);
        } else if (v == cancelReservationButton){
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("Cancel bike reservation?");
            builder.setCancelable(true);
            builder.setPositiveButton(
                    "Confirm",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            main.bookingStateTransition(false);
                            main.viewPager.getAdapter().notifyDataSetChanged();
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


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v == departureStationEditText){
            final int DRAWABLE_RIGHT = 2;
            if (departureStationEditText.getCompoundDrawables()[DRAWABLE_RIGHT] != null && event.getAction() == MotionEvent.ACTION_DOWN) {
                if (event.getRawX() >= (departureStationEditText.getRight() - (departureStationEditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width()) + 10)) {
                    departureStationEditText.playSoundEffect(SoundEffectConstants.CLICK);
                    departureStationEditText.setText("");
                    departureStationEditText.setCompoundDrawablesWithIntrinsicBounds(null,null, null,null);
                    return true;
                }
            }
        } else if (v == root){
            updateScreenGraphics();
        }
        return false;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }
    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }
    @Override
    public void afterTextChanged(Editable s) {
        main.departureTime = timeEditText.getText().toString();
        main.distanceWalking = distanceText.getText().toString();
    }

    private class splashPage extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            main.navView.setVisibility(GONE);
            searchButton.setEnabled(false);
            inAnimation = new AlphaAnimation(0f, 1f);
            inAnimation.setDuration(200);
            progressBarHolder.setAnimation(inAnimation);
            progressBarHolder.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            outAnimation = new AlphaAnimation(1f, 0f);
            outAnimation.setDuration(200);
            progressBarHolder.setAnimation(outAnimation);
            progressBarHolder.setVisibility(GONE);
            searchButton.setEnabled(true);
            replaceFragment(new MapFragment(STATIC_DEFINITIONS.SERVER_DEPARTURE_STATION_QUERY));
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                for (int i = 0; i < 2; i++) {
                    TimeUnit.SECONDS.sleep(1);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}