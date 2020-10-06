package com.example.BikesShare.ui.booking;

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
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.airbnb.lottie.LottieAnimationView;
import com.example.BikesShare.jsonFormat.BookingMessageToServer;
import com.example.BikesShare.extensions.CustomTimePickerDialog;
import com.example.BikesShare.MainActivity;
import com.example.BikesShare.message.Message;
import com.example.BikesShare.message.MessageListAdapter;
import com.example.BikesShare.station.StationAdapter;
import com.example.BikesShare.extensions.OnSwipeTouchListener;
import com.example.BikesShare.R;
import com.example.BikesShare.state.State;
import com.example.BikesShare.station.Station;
import com.example.BikesShare.TimeFormat;
import com.example.BikesShare.ui.booking.map.MapFragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Locale;
import java.util.Objects;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

// BookingFragment -> ui for users to book a bike/dock and most of the comm between client and server
// occurs on this page
public class BookingFragment extends Fragment implements View.OnClickListener, View.OnTouchListener, TextWatcher, MapFragment.updateParentView, RadioGroup.OnCheckedChangeListener {
    private Resources r;
    private ArrayList<RelativeLayout> relativeLayoutContainers = new ArrayList<>();
    // Relative layouts contain the views of each state (booking) and are hidden/made
    // visible depending on the book state
    private RelativeLayout startStateContainer, bikeReserveDetailsContainer,
            departureStationSelectedContainer, qrScannedLayoutContainer,
            dockReserveDetailsContainer, arrivalStationSelectedContainer, bikeDockedContainer;
    // views associated with the start state
    private Button startBookingButton;
    // views associated with state to book a bike (query server)
    private RelativeLayout timeSelectLayout, altStationLayout, distanceSelectLayout;
    private EditText stationEditText;
    private ListView stationListView;
    private StationAdapter stationListAdapter;
    private ImageButton mapSearchButton;
    public EditText timeEditText;
    private RadioGroup altDepartureStationRadioGroup;
    private TextView distanceText;
    private SeekBar seekBar;
    private Button searchButton;
    private Station selectedDepartureStation;
    private int walkDist = 0;
    // views associated with state after confirming which station to depart from
    private Button choice1Button;
    private Button choice2Button;
    private Button choice3Button;
    private ListView QRScanWaitList;
    private ArrayList<Message> QRScanWaitMsgList;
    // views associated with state after QR code as been scanned and need to book dock
    private int borrowLength = -1;
    private RadioGroup directTravelRadioGroup;
    private TextView borrowLengthProgressTextView;
    private SeekBar borrowLengthSeekBar;
    private Station selectedArrivalStation;

    private MainActivity main;
    private View root;

    private int stationEditTextHeight;

    public boolean splashPage;
    AlphaAnimation inAnimation;
    AlphaAnimation outAnimation;
    FrameLayout progressBarHolder;
    // called to update the visibility of the relative view containers and the views nested within them...
    private void updateView(){
        // Initialise all layouts as invisible first and then set the relevant one as visible
        for (RelativeLayout relativeLayout : relativeLayoutContainers) {
            relativeLayout.setVisibility(GONE);
        }
        relativeLayoutContainers.get(main.state.getBookingState()).setVisibility(VISIBLE);
        // switch case for current booking state..
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
                break;
            case State.BIKE_DOCKED_STATE :
                loadBikeDockedPage();
                break;
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_home, container, false);
        main = (MainActivity) getActivity();
        this.r = getResources();
        // initialise all the layout containers to start with
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
        bikeDockedContainer = root.findViewById(R.id.bikeDockedLayoutContainer);
        relativeLayoutContainers.add(bikeDockedContainer);
        // make the relevant container visible depending on booking state
        updateView();
        return root;
    }
    // If booking state = selected the departure station, this is called
    private void loadDepartureStationSelectionPage(){
        if (timeSelectLayout == null) { // only do all this if this is the first time app is seeing views below..
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
            stationListAdapter = new StationAdapter(getActivity(), R.layout.station_list_card_design, main.getStations());
            stationListView.setAdapter(stationListAdapter);
            stationEditText.addTextChangedListener(this);
            stationEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    updateStationEditTextGraphics(stationEditText);  // when the edittext is focused on, the graphics of it change (highlights it)
                    Collections.sort(main.getStations());  // sort the stations list in case changes were made
                    stationListAdapter.notifyDataSetChanged();
                }
            });

            stationListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    stationEditText.setText(((Station) stationListAdapter.getItem(position)).getName());
                    updateScreenGraphics(); // after selecting an item from list, updating the graphics of the screen
                    // will make certain views that were gone before, visible
                }
            });

            stationListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    updateScreenGraphics();  // update the view visibilities
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    int topRowVerticalPosition = (stationListView == null || stationListView.getChildCount() == 0) ? 0 : stationListView.getChildAt(0).getTop();
                    main.swipeRefreshLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);  // disable swipe to refresh unless at top of list
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
            seekBar.setProgress(5);  // default distance = 500m
            walkDist = 0;

            searchButton.setOnClickListener(this);

            // set touch listener to background so that focus is removed when touching it
            root.setOnTouchListener(this);
        }
        updateScreenGraphics();
    }
    // after a departure station has been selected, prompt user to scan qr code upon arrival
    private void loadQRScannerPage(){
        if (QRScanWaitList == null) {
            // Animate the swipe up visual to open QR scanner
            TextView textView = root.findViewById(R.id.textViewSlideUp);
            Animation anim = new AlphaAnimation(0.0f, 1.0f);
            anim.setDuration(500);
            anim.setStartOffset(500);
            anim.setRepeatMode(Animation.REVERSE);
            anim.setRepeatCount(Animation.INFINITE);
            textView.startAnimation(anim);

            // Chat style list view format
            QRScanWaitList = root.findViewById(R.id.waitQRListView);
            QRScanWaitMsgList = new ArrayList<>();
            final MessageListAdapter msgAdapter;
            // contains information about booking of bike
            QRScanWaitMsgList.add(new Message("in", r.getString(R.string.bikeReserved)));
            QRScanWaitMsgList.add(new Message("in", String.format(Locale.getDefault(), "Station: %s", main.state.getDepartingStation().getName())));
            QRScanWaitMsgList.add(new Message("in", String.format(Locale.getDefault(), "Address: %s", main.state.getDepartingStation().getAddress())));
            QRScanWaitMsgList.add(new Message("in", (String.format(Locale.getDefault(), "Reserved Time: %s", main.state.getDepartureTime()))));
            QRScanWaitMsgList.add(new Message("in", r.getString(R.string.QRInstruction)));

            msgAdapter = new MessageListAdapter(getContext(), QRScanWaitMsgList);
            QRScanWaitList.setAdapter(msgAdapter);
            QRScanWaitList.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {

                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                    int topRowVerticalPosition = (QRScanWaitList == null || QRScanWaitList.getChildCount() == 0) ? 0 : QRScanWaitList.getChildAt(0).getTop();
                    main.swipeRefreshLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);  // disable swipe down to refresh unless top of list
                }
            });

            choice1Button = root.findViewById(R.id.choice1Button);  // allow user to ask for directions/cancel booking using these choice buttons
            choice1Button.setText(String.format(Locale.getDefault(), "Get directions to %s", main.state.getDepartingStation().getName()));
            choice2Button = root.findViewById(R.id.choice2Button);
            choice3Button = root.findViewById(R.id.choice3Button);
            // set onclicklisteners for the choice buttons
            choice2Button.setOnClickListener(this);
            choice3Button.setOnClickListener(this);
            choice1Button.setOnClickListener(this);

            root.setOnTouchListener(new OnSwipeTouchListener(getContext()) { // swipe up to open the QE scanner; set ontouchlistener
                public void onSwipeTop() {
                    main.startQRScanner();
                }

                public boolean onTouch(View v, MotionEvent event) {
                    return gestureDetector.onTouchEvent(event);
                }
            });
        }
    }
    // Page after QR code has been touched. Contains instructions on what to do next
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
            public void onClick(View v) {  // after clicking ok, transition to next booking state
                main.state.bookingStateTransition(true);
                updateView();
            }
        });
    }
    // If booking state = select the arrival station, this is called
    private void loadArrivalStationSelectionPage(){
        timeSelectLayout = root.findViewById(R.id.arriveTimePickLayout);
        altStationLayout = root.findViewById(R.id.walkingArrivalRelativeLayout);
        distanceSelectLayout = root.findViewById(R.id.arrivalDistanceSelectLayout);
        // get the all the views (widgets etc)
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
        // Station search with list view filter
        stationListAdapter = new StationAdapter(getActivity(), R.layout.station_list_card_design, main.getStations());
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
        // works same way as the bike booking page...
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
                int topRowVerticalPosition = (stationListView == null || stationListView.getChildCount() == 0) ? 0 : stationListView.getChildAt(0).getTop();
                main.swipeRefreshLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0);
            }
        });

        // asks the user if they are travelling directly to the arrival station from departing station
        // if yes, then don't need to use seekbar to estimate borrowing time
        // if no, use seekbar to select how long to borrow the bike for
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

        // set listener for when seekbar slider is changed, to update the distance text (distance they are willing to walk)
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
        seekBar.setProgress(5);  // default walking distance is 500m
        walkDist = 0;

        searchButton.setOnClickListener(this);
        root.setOnTouchListener(this);
    }
    // after the arrival station has been selected and dock has been booked, call this function
    private void loadDockReservedPage(){
        ListView waitToDockChat  = root.findViewById(R.id.waitToDockChat);
        final ArrayList<Message> waitToDockMsgList  = new ArrayList<>();
        final MessageListAdapter msgAdapter;
        // chat listliew style where user can use the button1 and button2 to interact with the app and
        // send requests to the server (get directions/cancel booking)
        waitToDockMsgList.add(new Message("in", r.getString(R.string.dockReserved)));
        waitToDockMsgList.add(new Message("in", String.format(Locale.getDefault(), "Station: %s",  main.state.getArrivalStation().getName())));
        waitToDockMsgList.add(new Message("in", String.format(Locale.getDefault(), "Address: %s",  main.state.getArrivalStation().getAddress())));
        waitToDockMsgList.add(new Message("in", (String.format(Locale.getDefault(), "Bike reserved until: %s",  main.state.getArrivalTime()))));
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
                    main.queryServerStation(new BookingMessageToServer("cancelArrival", main.state.getArrivalStation().getId(), new TimeFormat().timeInInt(main.state.getArrivalTime()), -1));
                    main.state.setArrivalStation(null);
                    main.state.setArrivalTime(null);
                } else {

                }
                msgAdapter.notifyDataSetChanged();
            }
        });
    }
    // dock has been docked (this info is received from the server)
    private void loadBikeDockedPage(){
        final ListView listMsg = root.findViewById(R.id.chatListViewDocked);
        ArrayList<Message> listMessages;
        listMessages = new ArrayList<>();
        MessageListAdapter msgAdapter;
        // informs the user that the bike has been docked at x station and that trip is complete
        listMessages.add(new Message("in", String.format("You've arrived at %s and docked your bike successfully.", main.state.getDockedStation().getName())));
        listMessages.add(new Message("in", "Thanks for using us and we'll see you next time!"));
        msgAdapter = new MessageListAdapter(getContext(), listMessages);
        listMsg.setAdapter(msgAdapter);

        Button doneButton = root.findViewById(R.id.doneButton);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateView();
            }
        });

        main.state.resetState();
    }
    // public method that is used by MainActivity to add a msg to the msg ListView when waiting to
    // scan the QR code
    public void addQRScreenMessage(Message message){
        QRScanWaitMsgList.add(message);
        ((MessageListAdapter)QRScanWaitList.getAdapter()).notifyDataSetChanged();
    }
    // This method is changes the visual appearance of the station search bar (in the bike and dock
    // booking pages).
    // Adds the clear x button and listener for it if the edittext is not empty
    // The appearance of the edittext differs depending on if the view has focus or not too
    private void updateStationEditTextGraphics(EditText editText){
        if (stationEditTextHeight == 0){
            stationEditTextHeight = editText.getHeight();
        }
        if (editText.hasFocus()){  // expand the edittext and make background pink
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
            if (selectedStation != null){  // retract edittext and make background pink if valid station entered
                editText.setBackgroundTintList(ColorStateList.valueOf(r.getColor(R.color.edited)));
                editText.setTextColor(r.getColor(R.color.almostBlack));
            } else { // retract edittext and make background blue if nothing entered
                editText.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.editTextNoFocus)));
                editText.setTextColor(getResources().getColor(R.color.offWhite));
            }
            updateScreenGraphics();  // updates the remainder of the page according to input into edittext
        }
    }
    // Called to bring up the pop-up map. Adds the transaction to backstack so that pressing back or
    // popping the map fragment brings user back to this parent view by removing the map pop-up
    private void replaceFragment(Fragment fragment) {
        try {
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.replace(R.id.resultsMapFragmentContainer, fragment ); // give your fragment container id in first parameter
            transaction.addToBackStack(null);  // if written, this transaction will be added to backstack
            transaction.commit();
        } catch (Exception ignored) { }
    }
    // check if the entered station in the station search bar matches any of the stations in the stations list
    // trim and convert to lower case
    private Station checkForStationMatch(String enteredStation){
        for (Station station : main.getStations()){
            if (station.getName().toLowerCase().equals(enteredStation.toLowerCase().trim())){
                return station;
            }
        }
        return null;
    }
    // this updates the visibility of all the different views within the departure booking/arrival booking containers
    // the views are shown in a staggered manner, depending on what the user has selected for a smoother
    // user interface
    private void updateContainerVisibility() {
        Station selectedStation = checkForStationMatch(stationEditText.getText().toString());
        // If station selected doesnt match one in the station list
        if (selectedStation == null){
            stationListView.setVisibility(VISIBLE);
            timeSelectLayout.setVisibility(GONE);  // hide all steps following the station selection stage
            altStationLayout.setVisibility(GONE);
            searchButton.setVisibility(GONE);
        }
        if (selectedStation != null) { // station selected matches on in the station list
            stationListView.setVisibility(GONE);  // collapse the list view to make space for the other components of the app
            timeSelectLayout.setVisibility(View.VISIBLE); // next step is to select the departure time/arrival time
            if (main.state.getBookingState() == State.RESERVE_BIKE_SELECTION_STATE) { // if selecting departure station
                selectedDepartureStation = selectedStation;  // allocate departure station (temp)
                if (timeEditText.getText().toString().length() > 0){  // if departure time has been selected
                    altStationLayout.setVisibility(VISIBLE); // next step is to choose how far they are willing to walk
                    if (altDepartureStationRadioGroup.getCheckedRadioButtonId() == R.id.yes || altDepartureStationRadioGroup.getCheckedRadioButtonId() == R.id.no){
                        searchButton.setVisibility(VISIBLE);  // if user has choosen how far they are willing to walk, allow them to make request to server
                    }
                } else {  // if departure time hasn't been selected, update the time edittext visually
                    timeEditText.setBackgroundTintList(ColorStateList.valueOf(getContext().getResources().getColor(R.color.editTextNoFocus)));
                    if (altDepartureStationRadioGroup.getCheckedRadioButtonId() == R.id.yes || altDepartureStationRadioGroup.getCheckedRadioButtonId() == R.id.no)
                        altDepartureStationRadioGroup.clearCheck();
                    altStationLayout.setVisibility(GONE); // and hide the next steps
                    searchButton.setVisibility(GONE);
                }
            } else {  // if selecting arrival station
                selectedArrivalStation = selectedStation;  // allocate the arrival station (temp)
                if (selectedArrivalStation == main.state.getDepartingStation()){ // if the arrival station is the same as departure station
                    root.findViewById(R.id.textView7).setVisibility(GONE); // then the user is obviously not travelling directly there, so remove that option
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
    // Calling this function will update the visibility of the views. hide the keyboard and clear focus of all edittexts
    private void updateScreenGraphics(){
        hideKeyboard();
        updateContainerVisibility();
        switch (main.state.getBookingState()){
            case State.RESERVE_BIKE_SELECTION_STATE:
                stationEditText.clearFocus();
                timeEditText.clearFocus();
                distanceText.clearFocus();
                break;
            case State.RESERVE_DOCK_SELECTION_STATE:
                stationEditText.clearFocus();
                break;
        }

    }
    // hides soft keyboard
    private void hideKeyboard(){
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(root.getApplicationWindowToken(), 0);
    }
    // This is a public method used by the pop-up map to select a station from the pop-up map
    public void setStationFromMap(Station station){
        stationEditText.setText(station.getName());
        updateScreenGraphics();  // update the view visibility after selecting station
    }
    // Animation after a QR code has been scanned to be called from MainActivity after QRScannerActivity returns a
    // result
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
    // Called to show the TimePickerDialog (custom one)
    private void showTimePickerDialog(){
        Calendar mcurrentTime = Calendar.getInstance();
        int timeInMinutes = mcurrentTime.get(Calendar.HOUR_OF_DAY) * 60 + mcurrentTime.get(Calendar.MINUTE) + 5;  // default time on TimePicker is 5 minutes from now
        final int hour = timeInMinutes/60;
        final int minute = timeInMinutes%60;
        final CustomTimePickerDialog mTimePicker = new CustomTimePickerDialog(getContext(), R.style.themeOnverlay_timePicker, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                // we don't do anything here, all the logic is in the custom TimePickerDialog class
            }
        }, hour, minute, false, this);
        mTimePicker.show();
        Objects.requireNonNull(mTimePicker.getWindow()).setBackgroundDrawableResource(R.drawable.rounded_corners);
        mTimePicker.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE);
        mTimePicker.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE);
    }
    // OnClick for different views in the fragment
    @Override
    public void onClick(View v) {
        if (v == startBookingButton){
            main.state.bookingStateTransition(true);
            updateView();
        } else if (v == timeEditText) {
            showTimePickerDialog();
        } else if (v == mapSearchButton){  // mapSearchButton is the map marker icon next to searchbar
            bikeReserveDetailsContainer.setVisibility(GONE);
            main.state.bookingStateTransition(false);
            MapFragment mapFragment = new MapFragment();
            main.state.setMapFragment(mapFragment);
            replaceFragment(mapFragment); // pop up map
        } else if (v == searchButton){
            if (main.state.getBookingState() == State.RESERVE_BIKE_SELECTION_STATE) { // if checking for bike
                Calendar mcurrentTime = Calendar.getInstance();
                int currentTime = mcurrentTime.get(Calendar.HOUR_OF_DAY) * 60 + mcurrentTime.get(Calendar.MINUTE);
                int bookedTime = new TimeFormat().timeInInt(timeEditText.getText().toString());
                if (bookedTime < currentTime){  // If booked time is in the past..
                    timeEditText.setText(""); // let the user know
                    Toast.makeText(getContext(),"Time entered has passed! Re-enter departure time.", Toast.LENGTH_SHORT).show();
                    updateContainerVisibility();
                    return;
                }
                else if (bookedTime == currentTime){  // if the booked time is right now, increment booking time by a minute for safety measures
                    bookedTime = currentTime + 1;
                }
                bikeReserveDetailsContainer.setVisibility(GONE);
                main.state.setDepartureTime(new TimeFormat().timeInString(bookedTime));
                updateContainerVisibility();
                toggleSplashPage();  // start splash page (stopped by the MainActivity after server responds to the msg below )
                main.queryServerStation(new BookingMessageToServer("queryDepart", selectedDepartureStation.getId(), bookedTime, walkDist));
            } else { // if checking for dock
                dockReserveDetailsContainer.setVisibility(GONE);
                toggleSplashPage();  // start splash page
                main.queryServerStation(new BookingMessageToServer("queryArrival", selectedArrivalStation.getId(), borrowLength, walkDist));
            }

        } else if(v == choice2Button) {  // these are the "choice buttons" for the page when waiting for QR code to be scanned
            main.startQRScanner();  // initially, [directions, QR, cancel booking] are the 3 choices, and they update depending on what button is clicked
        } else if (v == choice3Button){
            QRScanWaitMsgList.add(new Message("out", (String) choice3Button.getText()));
            if (choice3Button.getText().equals("Cancel Reservation")) {
                QRScanWaitMsgList.add(new Message("in", "Are you sure you want to cancel this reservation?"));
                ((MessageListAdapter)QRScanWaitList.getAdapter()).notifyDataSetChanged();
                choice3Button.setText("Yes");
                choice1Button.setText("No");
                choice2Button.setVisibility(GONE);
            } else {
                main.queryServerStation(new BookingMessageToServer("cancelDeparture", main.state.getDepartingStation().getId(), new TimeFormat().timeInInt(main.state.getDepartureTime()), -1));
                main.state.resetState();
                if (stationEditText != null) {
                    stationEditText.setText("");
                    timeEditText.setText("");
                    altDepartureStationRadioGroup.check(R.id.no);
                }
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
                startActivity(intent);  // opens Google Maps app and gets directions to station. Bug as doesn't show the title of the address :(
            }
            ((MessageListAdapter)QRScanWaitList.getAdapter()).notifyDataSetChanged();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v == stationEditText){  // This is for the clear text button on the search buttons
            final int DRAWABLE_RIGHT = 2;
            if (stationEditText.getCompoundDrawables()[DRAWABLE_RIGHT] != null && event.getAction() == MotionEvent.ACTION_DOWN) {
                if (event.getRawX() >= (stationEditText.getRight() - (stationEditText.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())*2)) {
                    stationEditText.playSoundEffect(SoundEffectConstants.CLICK);
                    stationEditText.setText("");
                    ResourcesCompat.getDrawable(r, R.drawable.edit_text_clear, main.getTheme()).setTint(r.getColor(R.color.transparent));
                    stationEditText.setCompoundDrawablesWithIntrinsicBounds(null, null, ResourcesCompat.getDrawable(r, R.drawable.edit_text_clear, main.getTheme()), null);
                    stationEditText.setOnTouchListener(null);  // after clearing text, remove the on touch listener
                    return true;
                }
            }
        } else if (v == root || v == stationListView){  // hide keyboard and clear focus if touching outside edit texts
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
    public void afterTextChanged(Editable s) { //when text in search bar changes, filter the listview to match what has been entered
        // Filter the listview depending on text entered
        stationListAdapter.getFilter().filter(s.toString().toLowerCase().trim());
        updateContainerVisibility();
        updateStationEditTextGraphics(stationEditText);
    }

    // for the map pop-up to call, to refresh the view of the booking fragment
    @Override
    public void updateParentView() {
        this.updateView();
    }
    // when a box is checked on the radiogroups, this is called
    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        // This will get the radiobutton that has changed in its check state
        RadioButton checkedRadioButton = (RadioButton) group.findViewById(checkedId);
        // This puts the value (true/false) into the variable
        if (checkedRadioButton == null) { // if the radiogroup is getting cleared, break out
            return;
        }
        boolean isChecked = checkedRadioButton.isChecked();  // get the id of the checked button
        // If the radiobutton that has changed in check state is now checked...
        if (group == directTravelRadioGroup) {
            if (isChecked && checkedRadioButton.getId() == R.id.direct) {  // for the direct travel radio group (arrival case)
                borrowLength = -1;  // if traveling directly, user doesn't need to select anything on the seekbar
                root.findViewById(R.id.borrowLengthLayout).setVisibility(GONE);
            } else { // if not travelling directly, user should select how long they would like to borrow the bike for
                borrowLength = borrowLengthSeekBar.getProgress() * 5;
                root.findViewById(R.id.borrowLengthLayout).setVisibility(VISIBLE);
            }
            altStationLayout.setVisibility(VISIBLE);  // after choosing, can move to the next step
        } else {  // for the question asking if they are willing to walk to an alternative station
            if (isChecked && checkedRadioButton.getId() == R.id.yes) {  // if yes
                walkDist = seekBar.getProgress() * 100;  // then select how far on the seekbar
                distanceSelectLayout.setVisibility(VISIBLE);
            } else { // if no, then no need to select on the seekbar
                walkDist = 0;
                distanceSelectLayout.setVisibility(GONE);
            }
            searchButton.setVisibility(VISIBLE);  // can move onto next step after choosing
        }
    }
    // public method to toggle the splash screen on and off (called by the mainactivity class when
    // the app has successfully received packets from the TCP socket to toggle off)
    public void toggleSplashPage(){
        if (progressBarHolder == null){
            progressBarHolder = root.findViewById(R.id.progressBarHolder);
        }
        if (!splashPage){
            inAnimation = new AlphaAnimation(0f, 1f);  // show splashscreen animation if off
            inAnimation.setDuration(200);
            progressBarHolder.setAnimation(inAnimation);
            progressBarHolder.setVisibility(View.VISIBLE);
        } else {  // if splash screen running, turn off
            outAnimation = new AlphaAnimation(1f, 0f);
            outAnimation.setDuration(200);
            progressBarHolder.setAnimation(outAnimation);
            progressBarHolder.setVisibility(GONE);
            if (main.state.isQuerySuccess()) {  // if successfully received the response
                MapFragment mapFragment = new MapFragment();  // can show results on the map pop-up page
                main.state.setMapFragment(mapFragment);
                replaceFragment(mapFragment);
                main.state.setQuerySuccess(false);
            } else {  // if failed, then reset the booking fragment and display an error for the user
                reset();
            }
        }
        splashPage = !splashPage;  // toggle
    }

    // converts pixel to Dp to set the layout of certain views in code
    private int pixelToDp(int pixel){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, pixel, getResources().getDisplayMetrics());
    }
    // clears all the textviews and radiogroups if they have been changed.
    public void reset(){
        if (stationEditText != null)
            stationEditText.setText("");
        if (timeEditText != null)
            timeEditText.setText("");
        if (altDepartureStationRadioGroup != null)
            altDepartureStationRadioGroup.clearCheck();
        if (seekBar != null)
            seekBar.setProgress(5);
        if (directTravelRadioGroup != null)
            directTravelRadioGroup.clearCheck();
        if (borrowLengthSeekBar != null)
            borrowLengthSeekBar.setProgress(6);
    }
}