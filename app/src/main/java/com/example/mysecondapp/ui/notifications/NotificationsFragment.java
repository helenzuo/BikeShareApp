package com.example.mysecondapp.ui.notifications;

import android.animation.LayoutTransition;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.mysecondapp.FlipListener;
import com.example.mysecondapp.MainActivity;
import com.example.mysecondapp.MyAdapter;
import com.example.mysecondapp.OnSwipeTouchListener;
import com.example.mysecondapp.R;

public class NotificationsFragment extends Fragment {

    private MainActivity main;
    private EditText firstNameDisplay;
    private EditText surnameDisplay;
    private EditText emailDisplay;
    private RadioButton femaleDisplay;
    private RadioButton maleDisplay;

    boolean isBackVisible = false;
    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        final View root = inflater.inflate(R.layout.fragment_profile, container, false);
        main = (MainActivity)getActivity();
        ImageButton button = root.findViewById(R.id.editProfileButton);
        Button save = root.findViewById(R.id.saveButton);
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RelativeLayout imgFront = root.findViewById(R.id.frontCard);
                RelativeLayout imgBack = root.findViewById(R.id.backCard);

                ValueAnimator mFlipAnimator = ValueAnimator.ofFloat(0f, 1f);
                mFlipAnimator.addUpdateListener(new FlipListener(imgFront, imgBack));
                mFlipAnimator.setDuration(700);
                mFlipAnimator.reverse();
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RelativeLayout imgFront = root.findViewById(R.id.frontCard);
                RelativeLayout imgBack = root.findViewById(R.id.backCard);

                ValueAnimator mFlipAnimator = ValueAnimator.ofFloat(0f, 1f);
                mFlipAnimator.addUpdateListener(new FlipListener(imgFront, imgBack));
                mFlipAnimator.setDuration(700);
                mFlipAnimator.start();
            }
        });

        ListView tripListView = root.findViewById(R.id.tripListView);
        final MyAdapter stationListAdapter = new MyAdapter(getActivity(), R.layout.station_list_card_design);
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
                return gestureDetector.onTouchEvent(event);
            }
        });
        return root;
    }


}