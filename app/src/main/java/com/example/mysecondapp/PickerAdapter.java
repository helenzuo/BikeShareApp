package com.example.mysecondapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SnapHelper;

import java.util.List;

// Created by adityagohad on 06/06/17.

public class PickerAdapter extends RecyclerView.Adapter<PickerAdapter.TextVH> {

    private Context context;
    private List<String> dataList;
    private RecyclerView recyclerView;
    private SnapHelper snapHelper;
    private NoBounceLinearLayoutManager layoutManager;

    public PickerAdapter(Context context, List<String> dataList, RecyclerView recyclerView, SnapHelper snapHelper, NoBounceLinearLayoutManager layoutManager) {
        this.context = context;
        this.dataList = dataList;
        this.recyclerView = recyclerView;
        this.snapHelper = snapHelper;
        this.layoutManager = layoutManager;
    }

    @Override
    public TextVH onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        LayoutInflater inflater = LayoutInflater.from(context);
        view = inflater.inflate(R.layout.picker_item_layout, parent, false);

        return new PickerAdapter.TextVH(view);
    }

    @Override
    public void onBindViewHolder(TextVH holder, final int position) {
        TextVH textVH = holder;
        textVH.pickerTxt.setText(dataList.get(position));
        textVH.pickerTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (recyclerView != null) {
                    View view =  snapHelper.findSnapView(layoutManager);
                    if (recyclerView.getChildAdapterPosition(view) != position) {
                        recyclerView.smoothScrollToPosition(position);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataList.size();
    }


    class TextVH extends RecyclerView.ViewHolder {
        TextView pickerTxt;

        public TextVH(View itemView) {
            super(itemView);
            pickerTxt = (TextView) itemView.findViewById(R.id.picker_item);
        }
    }
}