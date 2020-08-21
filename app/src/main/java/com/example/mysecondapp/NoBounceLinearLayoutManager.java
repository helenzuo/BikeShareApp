package com.example.mysecondapp;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import travel.ithaka.android.horizontalpickerlib.PickerLayoutManager;

public class NoBounceLinearLayoutManager extends PickerLayoutManager {

    public NoBounceLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }

    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView, RecyclerView.State state, final int position) {
        LinearSmoothScroller linearSmoothScroller = new LinearSmoothScroller(recyclerView.getContext()) {
            @Override
            protected int getHorizontalSnapPreference() {
                return position > findFirstVisibleItemPosition() ? SNAP_TO_START : SNAP_TO_END;
            }
        };
        linearSmoothScroller.setTargetPosition(position);
        startSmoothScroll(linearSmoothScroller);
    }




}