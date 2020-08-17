package com.example.mysecondapp;

import android.app.TimePickerDialog;
import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Locale;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class CustomTimePickeerDialog extends TimePickerDialog {

    TimePicker mTimePicker;
    EditText editText;
    Context context;
    Button nextButton;
    RelativeLayout distanceRelativeView;
    public CustomTimePickeerDialog(Context context, int themeResId, OnTimeSetListener listener, int hourOfDay, int minute, boolean is24HourView, EditText editText, RelativeLayout distanceRelativeView, Button nextButton) {
        super(context, themeResId, listener, hourOfDay, minute, is24HourView);
        this.editText = editText;
        this.context = context;
        this.distanceRelativeView = distanceRelativeView;
        this.nextButton = nextButton;
    }

    @Override
    protected void onStop()
    {
        // override and do nothing
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Class<?> rClass = Class.forName("com.android.internal.R$id");
            Field timePicker = rClass.getField("timePicker");
            this.mTimePicker = (TimePicker) findViewById(timePicker.getInt(null));
            Field m = rClass.getField("minute");
        } catch (IllegalAccessException | NoSuchFieldException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        ((LinearLayout) ((LinearLayout) mTimePicker.getChildAt(0)).getChildAt(4)).getChildAt(0).setVisibility(GONE);
    }

    @Override
    public void show() {
        super.show();
        getButton(BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View view) {
                if (mTimePicker.validateInput()) {
                    Calendar mcurrentTime = Calendar.getInstance();
                    int currentHour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
                    int currentMinute = mcurrentTime.get(Calendar.MINUTE);
                    CustomTimePickeerDialog.this.onClick(CustomTimePickeerDialog.this, BUTTON_POSITIVE);
                    // Clearing focus forces the dialog to commit any pending
                    // changes, e.g. typed text in a NumberPicker.
                    if (mTimePicker.getHour() < currentHour || (mTimePicker.getHour() == currentHour && mTimePicker.getMinute() < currentMinute)) {
                        Toast.makeText(getContext(),"You can't travel back in time!\nPlease select a later time",Toast.LENGTH_SHORT).show();
                    } else if (mTimePicker.getHour()*60 + mTimePicker.getMinute() < currentHour*60 + currentMinute + 5) {
                        Toast.makeText(getContext(),"Sorry! The earliest you can book is in 5 minutes time",Toast.LENGTH_SHORT).show();
                    }
                    else {
                        String amPm = "AM";
                        int selectedHour = mTimePicker.getHour();
                        if (selectedHour >= 12){
                            if (selectedHour != 12) {
                                selectedHour -= 12;
                            }
                            amPm = "PM";
                        }
                        String output = String.format(Locale.getDefault(), "%2d:%02d %s", selectedHour, mTimePicker.getMinute(), amPm);
                        editText.getBackground().mutate().setColorFilter(context.getResources().getColor(android.R.color.holo_green_light), PorterDuff.Mode.SRC_ATOP);
                        editText.setText(output);
                        distanceRelativeView.setVisibility(View.VISIBLE);
                        nextButton.setVisibility(VISIBLE);
                        mTimePicker.clearFocus();
                        dismiss();
                    }
                }
            }
        });
    }
}
