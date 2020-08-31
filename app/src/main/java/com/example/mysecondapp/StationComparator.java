package com.example.mysecondapp;

import java.util.Comparator;

public class StationComparator implements Comparator<Station> {

    int sort;

    public StationComparator(int type){
        sort = type;
    }

    @Override
    public int compare(Station o1, Station o2) {
        switch (sort){
            case 0:
                return o1.getName().compareToIgnoreCase(o2.getName());
            case 1:
                return -o1.getName().compareToIgnoreCase(o2.getName());
            case 2:
                return (int) -(o2.getDistanceFloat() - o1.getDistanceFloat());
            case 3:
                return (int) (o2.getFillLevel() * o2.getCapacity() - o1.getFillLevel() * o1.getCapacity());
            default:
                return (int) -(o2.getFillLevel() * o2.getCapacity() - o1.getFillLevel() * o1.getCapacity());
        }
    }
}
