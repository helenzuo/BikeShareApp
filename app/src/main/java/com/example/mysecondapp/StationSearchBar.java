package com.example.mysecondapp;

import android.content.Context;
import android.content.res.ColorStateList;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;

public class StationSearchBar extends androidx.appcompat.widget.AppCompatEditText implements TextWatcher, View.OnFocusChangeListener, View.OnTouchListener {

    ListView stationList;
    Button sortButton;
    Context context;
    int height;

    public StationSearchBar(Context context) {
        super(context);
        this.context = context;
        this.setCompoundDrawables(null,null,getResources().getDrawable(R.drawable.edit_text_clear),null);
        this.setSoundEffectsEnabled(true);
        this.setBackground(getResources().getDrawable(R.drawable.edit_text_focus));
        this.setSingleLine();
        this.setOnFocusChangeListener(this);
        this.addTextChangedListener(this);
    }

    public StationSearchBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        this.setCompoundDrawables(null,null,getResources().getDrawable(R.drawable.edit_text_clear),null);
        this.setSoundEffectsEnabled(true);
        this.setBackground(getResources().getDrawable(R.drawable.edit_text_focus));
        this.setSingleLine();
        this.setOnFocusChangeListener(this);
        this.addTextChangedListener(this);
    }

    public StationSearchBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        this.stationList = stationList;
        this.setCompoundDrawables(null,null,getResources().getDrawable(R.drawable.edit_text_clear),null);
        this.setSoundEffectsEnabled(true);
        this.setBackground(getResources().getDrawable(R.drawable.edit_text_focus));
        this.setSingleLine();
        this.setOnFocusChangeListener(this);
        this.addTextChangedListener(this);
    }

    public void setExternalViews(ListView stationList, Button sortButton){
        this.stationList = stationList;
        this.sortButton = sortButton;
        height = this.getHeight();
    }


    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    private void updateDrawable(){
        if (this.getText().toString().length() > 0) {
            ResourcesCompat.getDrawable(getResources(), R.drawable.edit_text_clear, context.getTheme()).setTint(getResources().getColor(R.color.almostBlack));
            this.setCompoundDrawablesWithIntrinsicBounds(null, null, ResourcesCompat.getDrawable(getResources(), R.drawable.edit_text_clear, context.getTheme()), null);
            this.sortButton.setVisibility(GONE);
            this.setOnTouchListener(this);
        } else {
            ResourcesCompat.getDrawable(getResources(), R.drawable.edit_text_clear, context.getTheme()).setTint(getResources().getColor(R.color.transparent));
            this.setCompoundDrawablesWithIntrinsicBounds(null, null, ResourcesCompat.getDrawable(getResources(), R.drawable.edit_text_clear, context.getTheme()), null);
            this.sortButton.setVisibility(VISIBLE);
            this.setOnTouchListener(null);
        }
    }

    public void afterTextChanged(Editable s) {
       ((MyAdapter)stationList.getAdapter()).getFilter().filter(s.toString().toLowerCase().trim());
        updateDrawable();
    }

    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus()){
            this.setVisibility(View.VISIBLE);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) this.getLayoutParams();
            params.height = (int) (height * (6f/5f));
            this.setLayoutParams(params);
            this.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.editTextFocus)));
            this.setHintTextColor(getResources().getColor(R.color.darkGray));
            this.setTextColor(getResources().getColor(R.color.almostBlack));
            updateDrawable();
        } else {
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) this.getLayoutParams();
            params.height = (int) (height);
            this.setLayoutParams(params);
            this.setHintTextColor(getResources().getColor(R.color.editTextHintColor));
            ResourcesCompat.getDrawable(getResources(), R.drawable.edit_text_clear, context.getTheme()).setTint(getResources().getColor(R.color.transparent));
            this.setCompoundDrawablesWithIntrinsicBounds(null, null, ResourcesCompat.getDrawable(getResources(), R.drawable.edit_text_clear, context.getTheme()), null);
            this.setBackgroundTintList(ColorStateList.valueOf(getResources().getColor(R.color.editTextNoFocus)));
            this.setTextColor(getResources().getColor(R.color.offWhite));
            this.setOnTouchListener(null);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final int DRAWABLE_RIGHT = 2;
        if (this.getCompoundDrawables()[DRAWABLE_RIGHT] != null && event.getAction() == MotionEvent.ACTION_DOWN) {
            if (event.getRawX() >= (this.getRight() - (this.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())*2)) {
                this.playSoundEffect(SoundEffectConstants.CLICK);
                this.setText("");
                ResourcesCompat.getDrawable(getResources(), R.drawable.edit_text_clear, context.getTheme()).setTint(getResources().getColor(R.color.transparent));
                this.setCompoundDrawablesWithIntrinsicBounds(null, null, ResourcesCompat.getDrawable(getResources(), R.drawable.edit_text_clear, context.getTheme()), null);
                this.sortButton.setVisibility(VISIBLE);
                this.setOnTouchListener(null);
                return true;
            }
        }
        return false;
    }

}
