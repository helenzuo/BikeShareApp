package com.example.BikesShare.ui.profile;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.BikesShare.extensions.FlipListener;
import com.example.BikesShare.MainActivity;
import com.example.BikesShare.R;
import com.example.BikesShare.state.State;
import com.example.BikesShare.trip.TripListAdapter;
import com.example.BikesShare.state.User;
import com.example.BikesShare.ui.login.LoginActivity;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

// Profile fragments shows user information and let's them change their details
// also displays past trips
public class ProfileFragment extends Fragment implements View.OnClickListener {
    Calendar myCalendar;
    DatePickerDialog.OnDateSetListener date;

    private MainActivity main;
    private View root;

    private ListView tripListView;
    private TripListAdapter tripListAdapter;
    private ImageButton editProfileButton;
    private Button saveButton, cancelButton, logout;
    private ValueAnimator flipAnimator;
    private EditText nameEditText, emailEditText, mobileEditText, dobEditText;
    private TextView nameTextView, userTextView;
    private EditText[] editTexts;
    private RadioGroup genderRadioGroup;

    private RelativeLayout cardFront, cardBack;

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // get all the views
        root = inflater.inflate(R.layout.fragment_profile, container, false);
        main = (MainActivity)getActivity();
        logout = root.findViewById(R.id.logout);
        editProfileButton = root.findViewById(R.id.editProfileButton);
        nameTextView = root.findViewById(R.id.name);
        userTextView = root.findViewById(R.id.username);
        saveButton = root.findViewById(R.id.saveButton);
        cancelButton = root.findViewById(R.id.cancelButton);
        nameEditText = root.findViewById(R.id.editNameText);
        emailEditText = root.findViewById(R.id.editEmailText);
        mobileEditText = root.findViewById(R.id.editNumberText);
        dobEditText = root.findViewById(R.id.editDOBText);
        editTexts = new EditText[]{nameEditText, emailEditText, mobileEditText, dobEditText};
        // set listeners for all the edittexts, where pressing enter will hide keyboard and remove focus from editText
        for (final EditText editText : editTexts){
            editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    hideKeyboard();
                    editText.clearFocus();
                    return true;
                }
            });
        }
        // Textwatchers for name and email EditTexts (required fields) that clear the error message when
        // any text is entered
        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                nameEditText.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        emailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                emailEditText.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        genderRadioGroup = root.findViewById(R.id.genderToggle);
        cardFront = root.findViewById(R.id.frontCard);
        cardBack = root.findViewById(R.id.backCard);

        cardBack.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                root.clearFocus();
                hideKeyboard();
                return false;
            }
        });
        // animate the flip of card from front to back
        flipAnimator = ValueAnimator.ofFloat(0f, 1f);
        flipAnimator.addUpdateListener(new FlipListener(cardFront, cardBack));
        flipAnimator.setDuration(700);

        logout.setOnClickListener(this);
        editProfileButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);
        // use a calandar to select date of birth
        // when date selected, update the EditText of the DOB field
        myCalendar = Calendar.getInstance();
        date = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                // TODO Auto-generated method stub
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                updateLabel();
            }
        };
        // bring up the calendar dialog when the DOB field is clicked
        dobEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), date, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
                datePickerDialog.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(Color.BLACK);
                datePickerDialog.getButton(DatePickerDialog.BUTTON_POSITIVE).setTextColor(Color.BLACK);
                Date today = new Date();
                Calendar c = Calendar.getInstance();
                c.setTime(today);
                c.add( Calendar.YEAR, -5 );  // Subtract 5 years (user must be at least 5 years old)
                datePickerDialog.getDatePicker().setMaxDate(c.getTime().getTime());
            }

        });

        // List of past trips
        tripListView = root.findViewById(R.id.tripListView);
        tripListAdapter = new TripListAdapter(getActivity(), R.layout.trips_card, main.getTrips());
        tripListView.setAdapter(tripListAdapter);
        tripListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                int topRowVerticalPosition = (tripListView == null || tripListView.getChildCount() == 0) ? 0 : tripListView.getChildAt(0).getTop();
                main.swipeRefreshLayout.setEnabled(firstVisibleItem == 0 && topRowVerticalPosition >= 0); // disable refresh unless at the top of the list
            }
        });
        updateCard();  // updates both the back and front of the card
        return root;
    }
    // public method that is called by MainActivity when the app is refreshed (ie, the trip list has changed)
    public void refreshTripList(){
        tripListAdapter.notifyDataSetChanged();
    }
    // Called when a date has been selected for DOB. This sets the format for the DOB field string
    private void updateLabel() {
        String myFormat = "dd/MM/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        dobEditText.setText(sdf.format(myCalendar.getTime()));
    }
    // updates the back and front of the card using the attributes stored in the User Class
    public void updateCard(){
        nameTextView.setText(main.user.getName());
        userTextView.setText(main.user.getUserName());
        nameEditText.setText(main.user.getName());
        emailEditText.setText(main.user.getEmail());
        mobileEditText.setText(main.user.getMobile());
        dobEditText.setText(main.user.getDob());

        if (main.user.getGender() == User.FEMALE){
            genderRadioGroup.check(R.id.female);
            root.findViewById(R.id.animationBoy).setVisibility(View.GONE);
            root.findViewById(R.id.animationGirl).setVisibility(View.VISIBLE);
        } else {
            root.findViewById(R.id.animationBoy).setVisibility(View.VISIBLE);
            root.findViewById(R.id.animationGirl).setVisibility(View.GONE);
            if (main.user.getGender() == User.MALE)
                genderRadioGroup.check(R.id.male);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == editProfileButton){  // flip the card around and update the card before doing so
            updateCard();
            flipAnimator.start();
        } else if (v == logout) {
            if (main.state.getBookingState() <= State.RESERVE_BIKE_SELECTION_STATE) {  // if bike hasn't been booked, free to log out
                main.state.logOut();  // update state -> this removes the User class linked with the State Class
                SharedPreferences pref = main.getSharedPreferences("LOG_IN", Context.MODE_PRIVATE);
                SharedPreferences.Editor prefsEditor = pref.edit();
                Gson gson = new Gson();
                String json = gson.toJson(main.state.getUser());  // this will be null now
                prefsEditor.putString(State.USER_KEY, json);
                prefsEditor.putInt(State.LOG_KEY, main.state.getLoggedState()); // "logged out"
                prefsEditor.apply();  // save preferences
                Intent intent = new Intent(main.getBaseContext(), LoginActivity.class);  // start LoginActivity
                main.startActivity(intent);
                main.finish();  // end main activity so that cannot press back key and return
            } else {  // if a bike has been booked, tell the user that they must cancel this booking before logging out!
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("You cannot logout when you have booked a bike! Please cancel the reservation first.");
                builder.setPositiveButton("Ok", null);
                AlertDialog confirmAlert = builder.create();
                confirmAlert.show();
            }
        } else if (v == cancelButton){
            flipAnimator.reverse();
        } else if (v == saveButton){
            if (checkEditText()){  // check if required fields (name and email) are valid format
                main.user.setName(nameEditText.getText().toString());  // if so, update the User Class attribute to what has been entered
                main.user.setEmail(emailEditText.getText().toString());
                main.user.setMobile(mobileEditText.getText().toString());
                main.user.setDob(dobEditText.getText().toString());
                if (genderRadioGroup.getCheckedRadioButtonId() == R.id.female)
                    main.user.setGender(User.FEMALE);
                else if (genderRadioGroup.getCheckedRadioButtonId() == R.id.male)
                    main.user.setGender(User.MALE);
                else
                    main.user.setGender(User.NEUTRAL);
                updateCard();
                main.updateUserInfo();
                flipAnimator.reverse();
            }
        }
    }
    // checks that the name and email fields are valid, returns true if so
    boolean checkEditText(){
        boolean ok = true;
        if (nameEditText.getText().toString().isEmpty()) {  // make sure something is entered in the name, don't force a format bc people have different names..
            ok = false;
            nameEditText.setError("Enter your full name"); // if empty, set an error
        } if (!isEmailValid(emailEditText.getText().toString())) {
            ok = false;
            emailEditText.setError("Enter a valid email address"); // if email doesn't match email format, set an error
        }
        return ok;
    }
    // check if email matches email format
    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    // hide soft input keyboard
    private void hideKeyboard(){
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(root.getApplicationWindowToken(), 0);
    }


}