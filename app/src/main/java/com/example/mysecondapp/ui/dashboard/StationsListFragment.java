package com.example.mysecondapp.ui.dashboard;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import com.example.mysecondapp.MainActivity;
import com.example.mysecondapp.MyAdapter;
import com.example.mysecondapp.R;
import com.example.mysecondapp.Station;

import java.util.ArrayList;

public class StationsListFragment extends Fragment implements AdapterView.OnItemClickListener {
    private boolean stationClicked = false;
    private OnChildFragmentInteractionListener mParentListener;
    private ListView stationListView;
    private View root;
    private MainActivity main;
    private MyAdapter adapter;
    private SearchView searchView;
    private ViewGroup container;
    private int height;
    private int itemHeight;
    private View view;
    private ArrayList<Station> stations;


    public interface OnChildFragmentInteractionListener {
        void messageFromChildToParent(String message);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        this.container = container;
        root = inflater.inflate(R.layout.fragment_station_list, container, false);
        main = (MainActivity)getActivity();
        setHasOptionsMenu(true);
        stations = main.getStations();
        return root;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        stationListView = root.findViewById(R.id.stationListView);
        TextView empty=(TextView)root.findViewById(R.id.empty);
        stationListView.setEmptyView(empty);
        adapter= new MyAdapter(getActivity(), R.layout.station_list_design, stations, main.getFavouriteStations(), main);
        stationListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        View item = adapter.getView(0, null, stationListView);
        item.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),

                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        itemHeight = item.getMeasuredHeight() + stationListView.getDividerHeight();
        stationListView.setOnItemClickListener(this);
        stationListView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                InputMethodManager imm = (InputMethodManager)view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                return false;
            }
        });
        updateContainerHeight(adapter.getCount());
    }


    public void collapseSearchView(){
        InputMethodManager imm = (InputMethodManager)view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        stationListView.requestFocus();
    }

    private void updateContainerHeight(int count){
        if(count> 5){
            height = (int) (5.5 * itemHeight);
        } else {
            height = (int) (count * itemHeight);
        }
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) container.getLayoutParams();
        params.height = height;
        container.setLayoutParams(params);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // check if parent Fragment implements listener
        if (getParentFragment() instanceof StationsListFragment.OnChildFragmentInteractionListener) {
            mParentListener = (OnChildFragmentInteractionListener) getParentFragment();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        stationClicked = true;
        selectStation(((Station) adapter.getItem(position)).getName());
        searchView.setQuery(((Station) adapter.getItem(position)).getName(), false);
    }

    public void selectStation(String stationName){
        mParentListener.messageFromChildToParent(stationName);
        collapseSearchView();
        updateContainerHeight(0);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // TODO Add your menu entries here
        menu.findItem(R.id.editProfile).setVisible(false);
//        menu.findItem(R.id.saveProfileSettings).setVisible(false);
        MenuItem searchItem = menu.findItem(R.id.search);

        searchView = (SearchView) searchItem.getActionView();
        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    stationClicked = false;
                    adapter.getFilter().filter(searchView.getQuery(), new Filter.FilterListener() {
                        public void onFilterComplete(int count) {
                            updateContainerHeight(adapter.getCount());
                        }
                    });

                } else {
                    updateContainerHeight(0);
                }
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
            @Override
            public boolean onQueryTextSubmit(String query) {
                for (Station station : stations){
                    if (station.getName().toLowerCase().equals(query.toLowerCase().trim())){
                        selectStation(station.getName());
                        break;
                    }
                }
                searchView.setQuery(query, false);
                return false;
            }
            @Override
            public boolean onQueryTextChange(final String newText) {
                if (!stationClicked) {
                    String mNewText = newText.trim();
                    adapter.getFilter().filter(mNewText, new Filter.FilterListener() {
                        public void onFilterComplete(int count) {
                            if (adapter.getCount() > 0) {
                                updateContainerHeight(adapter.getCount());
                            } else {
                                updateContainerHeight(1);
                            }
                        }
                    });
                }
                return false;
            }
        });
        searchView.setQueryHint("Search for Station...");
        updateContainerHeight(0);
        searchView.clearFocus();
    }

}