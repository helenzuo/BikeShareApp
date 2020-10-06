package com.example.BikesShare.station;

import java.util.Comparator;

// Comparator for the stations to sort the Station ArrayLists according to the switch case below
public class StationComparator implements Comparator<Station> {

    int sort;

    public StationComparator(int type){
        sort = type;
    }

    @Override
    public int compare(Station o1, Station o2) {
        switch (sort){
            case 0:  // a -> z
                return o1.getName().compareToIgnoreCase(o2.getName());
            case 1:  // z -> a
                return -o1.getName().compareToIgnoreCase(o2.getName());
            case 2: // distance
                return (int) -(o2.getDistanceFloat() - o1.getDistanceFloat());
            case 3: // many bikes to less bikes
                return (int) -(o2.getFillLevel() * o2.getCapacity() - o1.getFillLevel() * o1.getCapacity());
            default:  // less bikes to many bikes
                return (int) (o2.getFillLevel() * o2.getCapacity() - o1.getFillLevel() * o1.getCapacity());
        }
    }
}
