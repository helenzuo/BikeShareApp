package com.example.mysecondapp.ui.notifications;

import android.animation.LayoutTransition;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
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
import android.widget.Button;
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

import java.util.Calendar;

public class NotificationsFragment extends Fragment implements View.OnClickListener {

    private MainActivity main;
    private View root;

    private ImageButton editProfileButton;
    private Button saveButton, cancelButton;
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

        editProfileButton.setOnClickListener(this);
        saveButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);

        // add mask to dob to format as date
        TextWatcher tw = new TextWatcher() {
            private String current = "";
            private String ddmmyyyy = "DDMMYYYY";
            private Calendar cal = Calendar.getInstance();

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!s.toString().equals(current)) {
                    String clean = s.toString().replaceAll("[^\\d.]|\\.", "");
                    String cleanC = current.replaceAll("[^\\d.]|\\.", "");

                    int cl = clean.length();
                    int sel = cl;
                    for (int i = 2; i <= cl && i < 6; i += 2) {
                        sel++;
                    }
                    //Fix for pressing delete next to a forward slash
                    if (clean.equals(cleanC)) sel--;

                    if (clean.length() < 8) {
                        clean = clean + ddmmyyyy.substring(clean.length());
                    } else {
                        //This part makes sure that when we finish entering numbers
                        //the date is correct, fixing it otherwise
                        int day = Integer.parseInt(clean.substring(0, 2));
                        int mon = Integer.parseInt(clean.substring(2, 4));
                        int year = Integer.parseInt(clean.substring(4, 8));

                        mon = mon < 1 ? 1 : Math.min(mon, 12);
                        cal.set(Calendar.MONTH, mon - 1);
                        year = (year < 1900) ? 1900 : Math.min(year, Calendar.getInstance().get(Calendar.YEAR));
                        cal.set(Calendar.YEAR, year);
                        // ^ first set year for the line below to work correctly
                        //with leap years - otherwise, date e.g. 29/02/2012
                        //would be automatically corrected to 28/02/2012

                        day = Math.min(day, cal.getActualMaximum(Calendar.DATE));
                        clean = String.format("%02d%02d%02d", day, mon, year);
                    }

                    clean = String.format("%s/%s/%s", clean.substring(0, 2),
                            clean.substring(2, 4),
                            clean.substring(4, 8));

                    sel = Math.max(sel, 0);
                    current = clean;
                    dobEditText.setText(current);
                    dobEditText.setSelection(Math.min(sel, current.length()));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };

        dobEditText.addTextChangedListener(tw);

        ListView tripListView = root.findViewById(R.id.tripListView);
        final MyAdapter stationListAdapter = new MyAdapter(getActivity(), R.layout.station_list_card_design, main.getStations());
        tripListView.setAdapter(stationListAdapter);

        tripListView.setOnTouchListener(new OnSwipeTouchListener(getContext()){
            public void onSwipeTop() {
                ((RelativeLayout)root).setLayoutTransition(new LayoutTransition());
                root.findViewById(R.id.cardLayout).setVisibility(View.GONE);
            }
            public void onSwipeBottom() {
                ((RelativeLayout)root).setLayoutTransition(null);
                root.findViewById(R.id.cardLayout).setVisibility(View.VISIBLE);
            }
            public boolean onTouch(View v, MotionEvent event) {
                root.clearFocus();
                hideKeyboard();
                return gestureDetector.onTouchEvent(event);
            }
        });

        updateCard();
        return root;
    }

    public void updateCard(){
        nameTextView.setText(main.user.getName());
        userTextView.setText(main.user.getUserName());
        nameEditText.setText(main.user.getName());
        emailEditText.setText(main.user.getEmail());
        mobileEditText.setText(main.user.getMobile());
        if (main.user.getGender() == R.id.female){
            root.findViewById(R.id.animationBoy).setVisibility(View.GONE);
            root.findViewById(R.id.animationGirl).setVisibility(View.VISIBLE);
        } else {
            root.findViewById(R.id.animationBoy).setVisibility(View.VISIBLE);
            root.findViewById(R.id.animationGirl).setVisibility(View.GONE);
        }
    }


    @Override
    public void onClick(View v) {
        if (v == editProfileButton){
            flipAnimator.start();
        } else if (v == cancelButton){
            flipAnimator.reverse();
        } else if (v == saveButton){
            if (checkEditText()){
                main.user.setName(nameEditText.getText().toString());
                main.user.setEmail(emailEditText.getText().toString());
                main.user.setMobile(mobileEditText.getText().toString());
                main.user.setDob(dobEditText.getText().toString());
                main.user.setGender(genderRadioGroup.getCheckedRadioButtonId());
                updateCard();
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