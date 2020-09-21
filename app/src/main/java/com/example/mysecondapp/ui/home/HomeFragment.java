package com.example.mysecondapp.ui.home;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.airbnb.lottie.LottieAnimationView;
import com.example.mysecondapp.BookingMessageToServer;
import com.example.mysecondapp.CustomTimePickerDialog;
import com.example.mysecondapp.MainActivity;
import com.example.mysecondapp.Message;
import com.example.mysecondapp.MessageListAdapter;
import com.example.mysecondapp.MyAdapter;
import com.example.mysecondapp.OnSwipeTouchListener;
import com.example.mysecondapp.R;
import com.example.mysecondapp.STATIC_DEFINITIONS;
import com.example.mysecondapp.State;
import com.example.mysecondapp.Station;
import com.example.mysecondapp.TimeFormat;
import com.example.mysecondapp.ui.map.MapFragment;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class HomeFragment extends Fragment implements View.OnClickListener, View.OnTouchListener, TextWatcher, MapFragment.updateParentView, RadioGroup.OnCheckedChangeListener {
    private Resources r;

    private ArrayList<RelativeLayout> relativeLayoutContainers = new ArrayList<>();
    private RelativeLayout startStateContainer, bikeReserveDetailsContainer,
            departureStationSelectedContainer, qrScannedLayoutContainer,
            dockReserveDetailsContainer, arrivalStationSelectedContainer;

    private Button startBookingButton;

    private RelativeLayout timeSelectLayout, altStationLayout, distanceSelectLayout;
    private EditText stationEditText;
    private ListView stationListView;
    private MyAdapter stationListAdapter;
    private ImageButton mapSearchButton;
    public EditText timeEditText;
    private RadioGroup altDepartureStationRadioGroup;
    private TextView distanceText;
    private SeekBar seekBar;
    private Button searchButton;
    private Station selectedDepartureStation;
    private int walkDist = 0;

    private Button choice1Button;
    private Button choice2Button;
    private Button choice3Button;
    private ListView QRScanWaitList;
    private ArrayList<Message> QRScanWaitMsgList;

    private int borrowLength = -1;
    private RadioGroup directTravelRadioGroup;
    private TextView borrowLengthProgressTextView;
    private SeekBar borrowLengthSeekBar;
    private Station selectedArrivalStation;

    private MainActivity main;
    private View root;

    private int stationEditTextHeight;

    AlphaAnimation inAnimation;
    AlphaAnimation outAnimation;
    FrameLayout progressBarHolder;


    private void updateView(){
        // Initialise all layouts as invisible first and then set the relevant one as visible
        for (RelativeLayout relativeLayout : relativeLayoutContainers) {
            relativeLayout.setVisibility(GONE);
        }

        relativeLayoutContainers.get(main.state.getBookingState()).setVisibility(VISIBLE);

        switch (main.state.getBookingState()) {
            case State.START_BOOKING_STATE:
                startBookingButton = root.findViewById(R.id.startBookingButton);
                startBookingButton.setOnClickListener(this);
                if (timeEditText != null){
                    timeEditText.setBackgroundTintList(ColorStateList.valueOf(getContext().getResources().getColor(R.color.editTextNoFocus)));
                    if (altDepartureStationRadioGroup.getCheckedRadioButtonId() == R.id.yes || altDepartureStationRadioGroup.getCheckedRadioButtonId() == R.id.no)
                        altDepartureStationRadioGroup.clearCheck();
                }
                break;
            case State.RESERVE_BIKE_SELECTION_STATE:
                loadDepartureStationSelectionPage();
                break;
            case State.DEPARTURE_STATION_SELECTED_STATE:
                loadQRScannerPage();
                break;
            case State.QR_SCANNED_STATE:
                loadQRScannedPage();
                break;
            case State.RESERVE_DOCK_SELECTION_STATE:
                loadArrivalStationSelectionPage();
                break;
            case State.ARRIVAL_STATION_SELECTED_STATE:
                loadDockReservedPage();
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_home, container, false);
        main = (MainActivity) getActivity();
        this.r = getResources();

        relativeLayoutContainers = new ArrayList<>();
        startStateContainer = root.findViewById(R.id.startStateLayoutContainer);
        relativeLayoutContainers.add(startStateContainer);
        bikeReserveDetailsContainer = root.findViewById(R.id.queryDepartureLayoutContainer);
        relativeLayoutContainers.add(bikeReserveDetailsContainer);
        departureStationSelectedContainer = root.findViewById(R.id.departureStationSelectedLayout);
        relativeLayoutContainers.add(departureStationSelectedContainer);
        qrScannedLayoutContainer = root.findViewById(R.id.QRScannedLayoutContainer);
        relativeLayoutContainers.add(qrScannedLayoutContainer);
        dockReserveDetailsContainer = root.findViewById(R.id.queryArrivalLayoutContainer);
        relativeLayoutContainers.add(dockReserveDetailsContainer);
        arrivalStationSelectedContainer = root.findViewById(R.id.arrivalStationSelectedLayout);
        relativeLayoutContainers.add(arrivalStationSelectedContainer);

        updateView();
        return root;
    }

    private void loadDepartureStationSelectionPage(){
        if (timeSelectLayout == null) {
            progressBarHolder = root.findViewById(R.id.progressBarHolder);
            timeSelectLayout = root.findViewById(R.id.departTimePickLayout);
            altStationLayout = root.findViewById(R.id.walkingRelativeLayout);
            distanceSelectLayout = root.findViewById(R.id.distanceSelectLayout);
            stationEditText = root.findViewById(R.id.departureStationEditText);
            stationListView = root.findViewById(R.id.departureStationListView);
            mapSearchButton = root.findViewById(R.id.mapViewButton);
            timeEditText = root.findViewById(R.id.timeEditText);
            altDepartureStationRadioGroup = root.findViewById(R.id.toggleYesNo);
            distanceText = root.findViewById(R.id.progressTextView);
            seekBar = root.findViewById(R.id.walkingDistanceSeekBar);
            searchButton = root.findViewById(R.id.nextButtonTimeSelected);

            // Station search with list view filter
            stationListAdapter = new MyAdapter(getActivity(), R.layout.station_list_card_design, main.getStations());
            stationListView.setAdapter(stationListAdapter);
            stationEditText.addTextChangedListener(this);
            stationEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    updateStationEditTextGraphics(stationEditText);
                    Collections.sort(main.getStations());
                    stationListAdapter.notifyDataSetChanged();
                }
            });

            stationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    stationEditText.setText(((Station) stationListAdapter.getItem(position)).getName());
                    updateScreenGraphics();
                }
            });

            stationListView.setOnScrollListener(new AbsListView.OnScrollListener() {
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

            altDepartureStationRadioGroup.setOnCheckedChangeListener(this);

            // set listener for when seekbar slider is changed, to update the distance text
            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    walkDist = progress * 100;
                    if (progress < 10) {
                        distanceText.setText(String.format(Locale.getDefault(), "%dM", progress * 100));
                    } else if (progress != 20) {
                        distanceText.setText(String.format(Locale.getDefault(), "%.1fKM", progress * .1));
                    } else {
                        distanceText.setText("2KM+");
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            seekBar.setProgress(5);
            walkDist = 0;

            searchButton.setOnClickListener(this);

            // set touch listener to background so that focus is removed when touching it
            root.setOnTouchListener(this);
        }

        updateScreenGraphics();
    }

    private void loadQRScannerPage(){
        if (QRScanWaitList == null) {
            TextView textView = root.findViewById(R.id.textViewSlideUp);
            Animation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(500);
            anim.setStartOffset(500);
            anim.setRepeatMode(Animation.REVERSE);
            anim.setRepeatCount(Animation.INFINITE);
            textView.startAnimation(anim);

            QRScanWaitList = root.findViewById(R.id.waitQRListView);
            QRScanWaitMsgList = new ArrayList<>();
            final MessageListAdapter msgAdapter;

            QRScanWaitMsgList.add(new Message("in", r.getString(R.string.bikeReserved)));
            QRScanWaitMsgList.add(new Message("in", String.format(Locale.getDefault(), "Station: %s", main.state.getDepartingStation().getName())));
            QRScanWaitMsgList.add(new Message("in", String.format(Locale.getDefault(), "Address: %s", main.state.getDepartingStation().getAddress())));
            QRScanWaitMsgList.add(new Message("in", (String.format(Locale.getDefault(), "Reserved Time: %s", main.state.getDepartureTime()))));
            QRScanWaitMsgList.add(new Message("in", r.getString(R.string.QRInstruction)));

            msgAdapter = new MessageListAdapter(getContext(), QRScanWaitMsgList);
            QRScanWaitList.setAdapter(msgAdapter);

            choice1Button = root.findViewById(R.id.choice1Button);
            choice1Button.setText(String.format(Locale.getDefault(), "Get directions to %s", main.state.getDepartingStation().getName()));
            choice2Button = root.findViewById(R.id.choice2Button);
            choice3Button = root.findViewById(R.id.choice3Button);

            choice2Button.setOnClickListener(this);

            choice3Button.setOnClickListener(this);

            choice1Button.setOnClickListener(this);

            root.setOnTouchListener(new OnSwipeTouchListener(getContext()) {
                public void onSwipeTop() {
                    main.startQRScanner();
                }

                public boolean onTouch(View v, MotionEvent event) {
                    return gestureDetector.onTouchEvent(event);
                }
            });
        }
    }

    private void loadQRScannedPage(){
        final ListView listMsg = root.findViewById(R.id.chatListView);
        ArrayList<Message> listMessages;
        listMessages = new ArrayList<>();
        MessageListAdapter msgAdapter;

        listMessages.add(new Message("in", r.getString(R.string.QRScanned)));
        listMessages.add(new Message("in", r.getString(R.string.QRScanned2)));
        msgAdapter = new MessageListAdapter(getContext(), listMessages);
        listMsg.setAdapter(msgAdapter);

        Button startDockSelectionButton = root.findViewById(R.id.startDockSelectionButton);
        startDockSelectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                main.state.bookingStateTransition(true);
                updateView();
            }
        });
    }

    private void loadArrivalStationSelectionPage(){

        timeSelectLayout = root.findViewById(R.id.arriveTimePickLayout);
        altStationLayout = root.findViewById(R.id.walkingArrivalRelativeLayout);
        distanceSelectLayout = root.findViewById(R.id.arrivalDistanceSelectLayout);

        stationEditText = root.findViewById(R.id.arrivalStationEditText);
        stationListView = root.findViewById(R.id.arrivalStationListView);
        mapSearchButton = root.findViewById(R.id.mapViewButtonDockSelect);
        borrowLengthProgressTextView = root.findViewById(R.id.borrowLengthTextView);
        borrowLengthSeekBar = root.findViewById(R.id.borrowLengthSeekBar);
        directTravelRadioGroup = root.findViewById(R.id.toggleYesNoDirectTravel);
        altDepartureStationRadioGroup = root.findViewById(R.id.toggleYesNoNearbyArrival);
        distanceText = root.findViewById(R.id.arrivalDistanceTextView);
        seekBar = root.findViewById(R.id.arrivalDistanceSeekBar);
        searchButton = root.findViewById(R.id.searchDockButton);

        stationListAdapter = new MyAdapter(getActivity(), R.layout.station_list_card_design, main.getStations());
        stationListView.setAdapter(stationListAdapter);
        stationEditText.addTextChangedListener(this);
        mapSearchButton.setOnClickListener(this);
        stationEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                updateStationEditTextGraphics(stationEditText);
                Collections.sort(main.getStations());
                stationListAdapter.notifyDataSetChanged();
            }
        });

        stationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                stationEditText.setText(((Station) stationListAdapter.getItem(position)).getName());
                updateScreenGraphics();
            }
        });

        stationListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                updateScreenGraphics();
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            }
        });
        directTravelRadioGroup.setOnCheckedChangeListener(this);
        altDepartureStationRadioGroup.setOnCheckedChangeListener(this);

        borrowLengthSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                borrowLength = progress * 5;
                if (progress < 12)
                    borrowLengthProgressTextView.setText(String.format(Locale.getDefault(), "%dmin   (%s)", progress * 5, new TimeFormat().currentTimeInString(progress*5)));
                else if (progress < 36)
                    borrowLengthProgressTextView.setText(String.format(Locale.getDefault(), "%dh %dmin   (%s)", progress*5/60, (progress*5)%60, new TimeFormat().currentTimeInString(progress*5)));
                else
                    borrowLengthProgressTextView.setText("Not sure");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        borrowLengthSeekBar.setProgress(6);
        borrowLength = -1;

        // set listener for when seekbar slider is changed, to update the distance text
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                walkDist = progress * 100;
                if (progress < 10) {
                    distanceText.setText(String.format(Locale.getDefault(), "%dM", progress * 100));
                } else if (progress != 20) {
                    distanceText.setText(String.format(Locale.getDefault(), "%.1fKM", progress * .1));
                } else {
                    distanceText.setText("2KM+");
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        seekBar.setProgress(5);
        walkDist = 0;

        searchButton.setOnClickListener(this);
        root.setOnTouchListener(this);
    }

    private void loadDockReservedPage(){
        ListView waitToDockChat  = root.findViewById(R.id.waitToDockChat);
        final ArrayList<Message> waitToDockMsgList  = new ArrayList<>();
        final MessageListAdapter msgAdapter;

        waitToDockMsgList.add(new Message("in", r.getString(R.string.dockReserved)));
        waitToDockMsgList.add(new Message("in", String.format(Locale.getDefault(), "Station: %s",  main.state.getArrivalStation().getName())));
        waitToDockMsgList.add(new Message("in", String.format(Locale.getDefault(), "Address: %s",  main.state.getArrivalStation().getAddress())));
        waitToDockMsgList.add(new Message("in", (String.format(Locale.getDefault(), "Bike reserved until: %s",  new TimeFormat().timeInString(main.state.getArrivalStation().getEstArr())))));
        waitToDockMsgList.add(new Message("in", r.getString(R.string.bikeReturnInstructions)));

        msgAdapter = new MessageListAdapter(getContext(), waitToDockMsgList);
        waitToDockChat.setAdapter(msgAdapter);

        final Button button1 = root.findViewById(R.id.button1);
        final Button button2 = root.findViewById(R.id.button2);

        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                waitToDockMsgList.add(new Message("out", (String) button1.getText()));
                if (button1.getText().equals("Change my reservation")) {
                    waitToDockMsgList.add(new Message("in", "Are you sure you want to change this reservation? This reservation will be cancelled and the occupancy levels at the stations may be different now."));
                    button2.setText("Yes");
                    button1.setText("Nevermind");
                } else {
                    button1.setText("Change my reservation");
                    button2.setText("Get directions");
                }
                msgAdapter.notifyDataSetChanged();
            }
        });

        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                waitToDockMsgList.add(new Message("out", (String) button2.getText()));
                if (button2.getText().equals("Yes")) {
                    main.state.bookingStateTransition(false);
                    main.viewPager.getAdapter().notifyDataSetChanged();
                    updateView();
                    button1.setText("Change my reservation");
                    button2.setText("Get directions");
                } else {

                }
                msgAdapter.notifyDataSetChanged();
            }
        });

    }

    public void addQRScreenMessage(Message message){
        QRScanWaitMsgList.add(message);
        ((MessageListAdapter)QRScanWaitList.getAdapter()).notifyDataSetChanged();
    }

    private void updateStationEditTextGraphics(EditText editText){
        if (stationEditTextHeight == 0){
            stationEditTextHeight = editText.getHeight();
        }
        if (editText.hasFocus()){
            mapSearchButton.setVisibility(GONE);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) editText.getLayoutParams();
            params.setMarginStart(0);
            params.setMarginEnd(0);
            params.height = (int) (stationEditTextHeight * (6f/5f));
            editText.setLayoutParams(params);
            editText.setBackgroundTintList(ColorStateList.valueOf(r.getColor(R.color.editTextFocus)));
            editText.setHintTextColor(r.getColor(R.color.darkGray));
            editText.setTextColor(r.getColor(R.color.almostBlack));
            if (editText.getText().toString().length() > 0) {
                ResourcesCompat.getDrawable(r, R.drawable.edit_text_clear, main.getTheme()).setTint(r.getColor(R.color.almostBlack));
                editText.setCompoundDrawablesWithIntrinsicBounds(null, null, ResourcesCompat.getDrawable(r, R.drawable.edit_text_clear, main.getTheme()), null);
                editText.setOnTouchListener(this);
            } else {
                ResourcesCompat.getDrawable(r, R.drawable.edit_text_clear, main.getTheme()).setTint(r.getColor(R.color.transparent));
                editText.setCompoundDrawablesWithIntrinsicBounds(null, null, ResourcesCompat.getDrawable(r, R.drawable.edit_text_clear, main.getTheme()), null);
                editText.setOnTouchListener(null);
            }
        } else {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) editText.getLayoutParams();
            if (stationEditTextHeight!= 0) {
                params.setMarginStart(pixelToDp(16));
                params.setMarginEnd(pixelToDp(5));
                params.height = stationEditTextHeight;
                editText.setLayoutParams(params);
            }
            mapSearchButton.setVisibility(VISIBLE);
            editText.setHintTextColor(r.getColor(R.color.editTextHintColor));
            ResourcesCompat.getDrawable(r, R.drawable.edit_text_clear, main.getTheme()).setTint(r.getColor(R.color.transparent));
            editText.setCompoundDrawablesWithIntrinsicBounds(null, null, ResourcesCompat.getDrawable(r, R.drawable.edit_text_clear, main.getTheme()), null);
            editText.setOnTouchListener(null);
            Station selectedStation = checkForStationMatch(editText.getText().toString());
            if (selectedStation != null){
                editText.setBackgroundTintList(ColorStateList.valueOf(r.getColor(R.color.edited)));
                editText.setTextColor(r.getColor(R.color.almostBlack));
            } else {
                editText.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.editTextNoFocus)));
                editText.setTextColor(getResources().getColor(R.color.offWhite));
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

    private void updateContainerVisibility() {
        Station selectedStation = checkForStationMatch(stationEditText.getText().toString());
        if (selectedStation == null){
            stationListView.setVisibility(VISIBLE);
            timeSelectLayout.setVisibility(GONE);
            altStationLayout.setVisibility(GONE);
            searchButton.setVisibility(GONE);
        }
        if (selectedStation != null) {
            stationListView.setVisibility(GONE);
            timeSelectLayout.setVisibility(View.VISIBLE);
            if (main.state.getBookingState() == STATIC_DEFINITIONS.SERVER_DEPARTURE_STATION_QUERY) {
                selectedDepartureStation = selectedStation;
                if (timeEditText.getText().toString().length() > 0){
                    altStationLayout.setVisibility(VISIBLE);
                    if (altDepartureStationRadioGroup.getCheckedRadioButtonId() == R.id.yes || altDepartureStationRadioGroup.getCheckedRadioButtonId() == R.id.no){
                        searchButton.setVisibility(VISIBLE);
                    }
                } else {
                    timeEditText.setBackgroundTintList(ColorStateList.valueOf(getContext().getResources().getColor(R.color.editTextNoFocus)));
                    if (altDepartureStationRadioGroup.getCheckedRadioButtonId() == R.id.yes || altDepartureStationRadioGroup.getCheckedRadioButtonId() == R.id.no)
                        altDepartureStationRadioGroup.clearCheck();
                    altStationLayout.setVisibility(GONE);
                    searchButton.setVisibility(GONE);
                }
            } else {
                selectedArrivalStation = selectedStation;
                if (selectedArrivalStation == main.state.getDepartingStation()){
                    root.findViewById(R.id.textView7).setVisibility(GONE);
                    root.findViewById(R.id.textView7b).setVisibility(GONE);
                    root.findViewById(R.id.toggleYesNoDirectTravel).setVisibility(GONE);
                    directTravelRadioGroup.check(R.id.indirect);
                } else {
                    root.findViewById(R.id.textView7).setVisibility(VISIBLE);
                    root.findViewById(R.id.textView7b).setVisibility(VISIBLE);
                    root.findViewById(R.id.toggleYesNoDirectTravel).setVisibility(VISIBLE);
                }
            }
        }
    }

    private void updateScreenGraphics(){
        hideKeyboard();
        updateContainerVisibility();
        switch (main.state.getBookingState()){
            case STATIC_DEFINITIONS.SERVER_DEPARTURE_STATION_QUERY:
                stationEditText.clearFocus();
                timeEditText.clearFocus();
                distanceText.clearFocus();
                break;
            case State.RESERVE_DOCK_SELECTION_STATE:
                stationEditText.clearFocus();
                break;
        }

    }

    private void hideKeyboard(){
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(root.getApplicationWindowToken(), 0);
    }

    public void setDepartureStationFromMap(Station station){
        stationEditText.setText(station.getName());
        updateScreenGraphics();
    }

    public void QRCodeScannedAnimation(){
        departureStationSelectedContainer.setVisibility(View.INVISIBLE);
        final RelativeLayout scanAnim = root.findViewById(R.id.scanAnimation);
        scanAnim.setVisibility(VISIBLE);
        LottieAnimationView lottieAnimationView = root.findViewById(R.id.unlockAnimation);
        lottieAnimationView.playAnimation();
        lottieAnimationView.addAnimatorListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                scanAnim.setVisibility(GONE);
                updateView();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }

    private void showTimePickerDialog(){
        Calendar mcurrentTime = Calendar.getInstance();
        int timeInMinutes = mcurrentTime.get(Calendar.HOUR_OF_DAY) * 60 + mcurrentTime.get(Calendar.MINUTE) + 5;
        final int hour = timeInMinutes/60;
        final int minute = timeInMinutes%60;
        final CustomTimePickerDialog mTimePicker = new CustomTimePickerDialog(getContext(), R.style.themeOnverlay_timePicker, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {

            }
        }, hour, minute, false, this);
        mTimePicker.show();
        Objects.requireNonNull(mTimePicker.getWindow()).setBackgroundDrawableResource(R.drawable.rounded_corners);
        mTimePicker.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE);
        mTimePicker.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE);
    }

    @Override
    public void onClick(View v) {
        if (v == startBookingButton){
            main.state.bookingStateTransition(true);
            updateView();
        } else if (v == timeEditText) {
            showTimePickerDialog();
        } else if (v == mapSearchButton){
            bikeReserveDetailsContainer.setVisibility(GONE);
            replaceFragment(new MapFragment(STATIC_DEFINITIONS.STATION_LOOK_UP));
        } else if (v == searchButton){
            int openFrom;
            if (main.state.getBookingState() == State.RESERVE_BIKE_SELECTION_STATE) { // if checking for bike
                openFrom = STATIC_DEFINITIONS.SERVER_DEPARTURE_STATION_QUERY;
                Calendar mcurrentTime = Calendar.getInstance();
                int currentTime = mcurrentTime.get(Calendar.HOUR_OF_DAY) * 60 + mcurrentTime.get(Calendar.MINUTE);
                int bookedTime = new TimeFormat().timeInInt(timeEditText.getText().toString());
                if (bookedTime < currentTime){
                    timeEditText.setText("");
                    Toast.makeText(getContext(),"Time entered has passed! Re-enter departure time.", Toast.LENGTH_SHORT).show();
                    updateContainerVisibility();
                    return;
                }
                else if (bookedTime == currentTime){
                    bookedTime = currentTime + 1;
                }
                bikeReserveDetailsContainer.setVisibility(GONE);
                main.state.setDepartureTime(new TimeFormat().timeInString(bookedTime));
                updateContainerVisibility();
                main.queryServerStation(new BookingMessageToServer("queryDepart", selectedDepartureStation.getId(), bookedTime, walkDist));
            } else { // if checking for dock
                openFrom = STATIC_DEFINITIONS.SERVER_ARRIVAL_STATION_QUERY;
                dockReserveDetailsContainer.setVisibility(GONE);
                main.state.setArrivalTime(timeEditText.getText().toString());
                main.queryServerStation(new BookingMessageToServer("queryArrival", selectedArrivalStation.getId(), borrowLength, walkDist));
            }
            new SplashPage().execute(openFrom);
        } else if(v == choice2Button) {
            main.startQRScanner();
        } else if (v == choice3Button){
            QRScanWaitMsgList.add(new Message("out", (String) choice3Button.getText()));
            if (choice3Button.getText().equals("Cancel Reservation")) {
                QRScanWaitMsgList.add(new Message("in", "Are you sure you want to cancel this reservation?"));
                ((MessageListAdapter)QRScanWaitList.getAdapter()).notifyDataSetChanged();
                choice3Button.setText("Yes");
                choice1Button.setText("No");
                choice2Button.setVisibility(GONE);
            } else {
                main.state.resetState();
                stationEditText.setText("");
                timeEditText.setText("");
                altDepartureStationRadioGroup.check(R.id.no);
                main.viewPager.getAdapter().notifyDataSetChanged();
                updateView();
                choice2Button.setVisibility(VISIBLE);
                choice3Button.setText("Cancel Reservation");
            }
            ((MessageListAdapter)QRScanWaitList.getAdapter()).notifyDataSetChanged();
        } else if (v == choice1Button){
            QRScanWaitMsgList.add(new Message("out", (String) choice1Button.getText()));
            if (choice1Button.getText().equals("No")){
                choice1Button.setText(String.format(Locale.getDefault(),"Directions to %s", main.state.getDepartingStation().getName()));
                choice3Button.setText("Cancel Reservation");
                choice2Button.setVisibility(VISIBLE);
            }
            else {
                QRScanWaitMsgList.add(new Message("in", "Searching for directions."));
                double latitude =  main.state.getDepartingStation().getLocation().latitude;
                double longitude =  main.state.getDepartingStation().getLocation().longitude;
                String uriBegin = "geo:" + latitude + "," + longitude;
                String query = latitude + "," + longitude + "(" +  main.state.getDepartingStation().getName() + ")";
                String encodedQuery = Uri.encode(query);
                String uriString = uriBegin + "?q=" + encodedQuery + "&z=16";
                Uri uri = Uri.parse(uriString);
                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
            ((MessageListAdapter)QRScanWaitList.getAdapter()).notifyDataSetChanged();
        }
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v == stationEditText){
            final int DRAWABLE_RIGHT = 2;
            if (stationEditText.getCompoundDrawables()[DRAWABLE_RIGHT] != null && event.getAction() == MotionEvent.ACTION_DOWN) {
                if (event.getRawX() >= (stationEditText.getRight() - (stationEditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())*2)) {
                    stationEditText.playSoundEffect(SoundEffectConstants.CLICK);
                    stationEditText.setText("");
                    ResourcesCompat.getDrawable(r, R.drawable.edit_text_clear, main.getTheme()).setTint(r.getColor(R.color.transparent));
                    stationEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, ResourcesCompat.getDrawable(r, R.drawable.edit_text_clear, main.getTheme()), null);
                    stationEditText.setOnTouchListener(null);
                    return true;
                }
            }
        } else if (v == root || v == stationListView){
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
        // Filter the listview depending on text entered
        stationListAdapter.getFilter().filter(s.toString().toLowerCase().trim());
        updateContainerVisibility();
        updateStationEditTextGraphics(stationEditText);

    }

    @Override
    public void updateParentView() {
        this.updateView();
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        // This will get the radiobutton that has changed in its check state
        RadioButton checkedRadioButton = (RadioButton) group.findViewById(checkedId);
        // This puts the value (true/false) into the variable
        if (checkedRadioButton == null) {
            return;
        }
        boolean isChecked = checkedRadioButton.isChecked();
        // If the radiobutton that has changed in check state is now checked...
        if (group == directTravelRadioGroup) {
            if (isChecked && checkedRadioButton.getId() == R.id.direct) {
                borrowLength = -1;
                root.findViewById(R.id.borrowLengthLayout).setVisibility(GONE);
            } else {
                borrowLength = borrowLengthSeekBar.getProgress() * 5;
                root.findViewById(R.id.borrowLengthLayout).setVisibility(VISIBLE);
            }
            altStationLayout.setVisibility(VISIBLE);
        } else {
            if (isChecked && checkedRadioButton.getId() == R.id.yes) {
                walkDist = seekBar.getProgress() * 100;
                distanceSelectLayout.setVisibility(VISIBLE);
            } else {
                walkDist = 0;
                distanceSelectLayout.setVisibility(GONE);
            }
            searchButton.setVisibility(VISIBLE);
        }

    }

    private class SplashPage extends AsyncTask<Integer, Void, Integer> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            searchButton.setEnabled(false);
            inAnimation = new AlphaAnimation(0f, 1f);
            inAnimation.setDuration(200);
            progressBarHolder.setAnimation(inAnimation);
            progressBarHolder.setVisibility(View.VISIBLE);
        }
        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            outAnimation = new AlphaAnimation(1f, 0f);
            outAnimation.setDuration(200);
            progressBarHolder.setAnimation(outAnimation);
            progressBarHolder.setVisibility(GONE);
            searchButton.setEnabled(true);
            replaceFragment(new MapFragment(integer));
        }
        @Override
        protected Integer doInBackground(Integer... integers) {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return integers[0];
        }
    }

    private int pixelToDp(int pixel){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixel, getResources().getDisplayMetrics());
    }
}