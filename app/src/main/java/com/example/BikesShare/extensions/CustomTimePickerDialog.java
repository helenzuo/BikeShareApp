package com.example.BikesShare.extensions;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.example.BikesShare.R;
import com.example.BikesShare.ui.booking.BookingFragment;

import java.lang.reflect.Field;
import java.util.Calendar;
import java.util.Locale;

import static android.view.View.GONE;

// Customised timepickerdialog that will verify the time selected is valid (ie: in the future)
// for the booking pages
public class CustomTimePickerDialog extends TimePickerDialog {
    TimePicker mTimePicker;
    BookingFragment bookingFragment;
    Context context;
    public CustomTimePickerDialog(Context context, int themeResId, OnTimeSetListener listener, int hourOfDay, int minute, boolean is24HourView, BookingFragment bookingFragment) {
        super(context, themeResId, listener, hourOfDay, minute, is24HourView);
        this.bookingFragment = bookingFragment;
        this.context = context;
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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            ((LinearLayout) ((LinearLayout) mTimePicker.getChildAt(0)).getChildAt(4)).getChildAt(0).setVisibility(GONE); // hide keyboard on timepicker pop up
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
                    CustomTimePickerDialog.this.onClick(CustomTimePickerDialog.this, BUTTON_POSITIVE);
                    // Clearing focus forces the dialog to commit any pending
                    // changes, e.g. typed text in a NumberPicker.
                    if (mTimePicker.getHour() < currentHour || (mTimePicker.getHour() == currentHour && mTimePicker.getMinute() < currentMinute)) {
                        Toast.makeText(getContext(),"You can't travel back in time!\nPlease select a later time",Toast.LENGTH_SHORT).show();
                    } else if (mTimePicker.getHour()*60 + mTimePicker.getMinute() < currentHour*60 + currentMinute + 1) {
                        Toast.makeText(getContext(),"Sorry! The earliest you can book is in 1 minute's time",Toast.LENGTH_SHORT).show();
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
                        bookingFragment.timeEditText.setBackgroundTintList(ColorStateList.valueOf(context.getResources().getColor(R.color.edited)));
                        bookingFragment.timeEditText.setTextColor(context.getResources().getColor(R.color.almostBlack));
                        bookingFragment.timeEditText.setText(output);
                        bookingFragment.updateParentView();
                        mTimePicker.clearFocus();
                        dismiss();
                    }
                }
            }
        });
    }
}
