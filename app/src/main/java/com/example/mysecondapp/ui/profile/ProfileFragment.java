package com.example.mysecondapp.ui.profile;

import android.animation.LayoutTransition;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.mysecondapp.FlipListener;
import com.example.mysecondapp.MainActivity;
import com.example.mysecondapp.MyAdapter;
import com.example.mysecondapp.OnSwipeTouchListener;
import com.example.mysecondapp.R;
import com.example.mysecondapp.State;
import com.example.mysecondapp.User;
import com.example.mysecondapp.ui.login.LoginActivity;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ProfileFragment extends Fragment implements View.OnClickListener {
    Calendar myCalendar;
    DatePickerDialog.OnDateSetListener date;

    private MainActivity main;
    private View root;

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
        for (final EditText editText : editTexts){
            editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus){
                        editText.setTextColor(getResources().getColor(R.color.offWhite));
                    }
                }
            });
            editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                @Override
                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                    hideKeyboard();
                    editText.clearFocus();
                    return true;
                }
            });
        }
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
        flipAnimator = ValueAnimator.ofFloat(0f, 1f);
        flipAnimator.addUpdateListener(new FlipListener(cardFront, cardBack));
        flipAnimator.setDuration(700);

        logout.setOnClickListener(this);
        editProfileButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);

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
                c.add( Calendar.YEAR, -5 );  // Subtract 5 years
                datePickerDialog.getDatePicker().setMaxDate(c.getTime().getTime());
            }

        });

        ListView tripListView = root.findViewById(R.id.tripListView);
        final MyAdapter stationListAdapter = new MyAdapter(getActivity(), R.layout.station_list_card_design, main.getStations());
        tripListView.setAdapter(stationListAdapter);
        tripListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                main.swipeRefreshLayout.setEnabled(firstVisibleItem == 0);
            }
        });
        updateCard();
        return root;
    }

    private void updateLabel() {
        String myFormat = "dd/MM/yy"; //In which you need put here
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.US);
        dobEditText.setText(sdf.format(myCalendar.getTime()));
    }

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
        if (v == editProfileButton){
            flipAnimator.start();
        } else if (v == logout) {
            main.state.logOut();
            SharedPreferences pref = main.getSharedPreferences("LOG_IN", Context.MODE_PRIVATE);
            SharedPreferences.Editor prefsEditor = pref.edit();
            Gson gson = new Gson();
            String json = gson.toJson(main.state.getUser());
            prefsEditor.putString(State.USER_KEY, json);
            prefsEditor.putInt(State.LOG_KEY, main.state.getLoggedState());
            prefsEditor.apply();
            System.out.println(json);
            Intent intent = new Intent(main.getBaseContext(), LoginActivity.class);
            main.startActivity(intent);
            main.finish();
        } else if (v == cancelButton){
            flipAnimator.reverse();
        } else if (v == saveButton){
            if (checkEditText()){
                main.user.setName(nameEditText.getText().toString());
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
            } else {
                Toast.makeText(getContext(), "Input(s) invalid", Toast.LENGTH_SHORT).show();
            }
        }
    }

    boolean checkEditText(){
        boolean ok = true;
        if (nameEditText.getText().toString().isEmpty())
            ok = false;
        if (!isEmailValid(emailEditText.getText().toString())) {
            emailEditText.setTextColor(Color.RED);
            ok = false;
        }
        if (!isPhoneValid(mobileEditText.getText().toString())) {
            mobileEditText.setTextColor(Color.RED);
            ok = false;
        }
        return ok;
    }

    boolean isEmailValid(CharSequence email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }
    boolean isPhoneValid(String number){
        number = number.trim();
        mobileEditText.setText(number);
        System.out.println(number.length());
        if (number.length() < 8){
            return false;
        }
        return number.substring(0, 2).equals("04");
    }

    private void hideKeyboard(){
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(root.getApplicationWindowToken(), 0);
    }


}