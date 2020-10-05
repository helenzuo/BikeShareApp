package com.example.mysecondapp.extensions;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.mysecondapp.ui.stationSearch.SearchFragment;
import com.example.mysecondapp.ui.booking.BookingFragment;
import com.example.mysecondapp.ui.profile.ProfileFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {

    private final Fragment[] childFragments;

    public ViewPagerAdapter(@NonNull FragmentManager fm, Lifecycle lifecycle) {
        super(fm, lifecycle);
        childFragments = new Fragment[]{
                new ProfileFragment(), //0
                new BookingFragment(), //1
                new SearchFragment() //2
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

    public Fragment getFragment(int position){
        return childFragments[position];
    }

}
