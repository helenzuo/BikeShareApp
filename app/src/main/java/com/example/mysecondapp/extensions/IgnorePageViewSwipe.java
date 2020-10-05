package com.example.mysecondapp.extensions;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

import com.example.mysecondapp.MainActivity;

public class IgnorePageViewSwipe implements View.OnTouchListener {

    private Context context;

    public IgnorePageViewSwipe(Context context){
        this.context = context;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN){
            ((MainActivity)context).viewPager.setUserInputEnabled(false);
        } else if (event.getAction() == MotionEvent.ACTION_UP){
            ((MainActivity)context).viewPager.setUserInputEnabled(true);
        }
        return false;
    }
}
