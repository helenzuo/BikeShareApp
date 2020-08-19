package com.example.mysecondapp;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.adapter.FragmentViewHolder;

import com.example.mysecondapp.ui.dashboard.DashboardFragment;
import com.example.mysecondapp.ui.home.HomeFragment;
import com.example.mysecondapp.ui.notifications.NotificationsFragment;

import java.util.List;

public class ViewPagerAdapter extends FragmentStateAdapter {

    private final Fragment[] childFragments;

    public ViewPagerAdapter(@NonNull FragmentManager fm, Lifecycle lifecycle) {
        super(fm, lifecycle);
        childFragments = new Fragment[]{
                new NotificationsFragment(), //0
                new HomeFragment(), //1
                new DashboardFragment() //2
        };
    }


    @Override
    public int getItemCount() {
        return childFragments.length; //3 items
    }


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return childFragments[position];
    }


}
