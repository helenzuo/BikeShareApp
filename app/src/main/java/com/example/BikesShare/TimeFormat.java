package com.example.BikesShare;

import java.util.Calendar;
import java.util.Locale;

// Class for converting time for int in minutes to string and vice versa
public class TimeFormat {
    // convert time in minutes to String format (AM/PM)
    public String timeInString(int minutes){
        int hours = minutes/60;
        minutes = minutes % 60;
        if (hours > 12 && hours != 12) {
            hours -= 12;
            return String.format(Locale.getDefault(), "%d:%02d %s", hours, minutes, "PM" );
        }
        return String.format(Locale.getDefault(), "%d:%02d %s", hours, minutes, "AM" );
    }
    // convert current time + some minutes in minutes to string format (AM/PM)
    public String currentTimeInString(int plusMinutes){
        Calendar now = Calendar.getInstance();
        int minutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
        minutes += plusMinutes;
        int hours = minutes/60;
        minutes = minutes % 60;
        if (hours > 12 && hours != 12) {
            hours -= 12;
            return String.format(Locale.getDefault(), "%d:%02d%s", hours, minutes, "PM" );
        }
        return String.format(Locale.getDefault(), "%d:%02d%s", hours, minutes, "AM" );
    }
    // convert string format (AM/PM) time to time in minutes (int)
    public int timeInInt(String s) {
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
}
