package com.chromeos.playtool.common.model;

public class Pairs implements Comparable<Pairs> {

    public Integer first;
    public Pair second;

    public Pairs(Integer first, Pair second) {
        this.first = first;
        this.second = second;
    }

    @Override
    public int compareTo(Pairs pair) {
        if (first.compareTo(pair.first) == 0) {
            if (second.first.compareTo(pair.second.first) == 0)
                return second.second.compareTo(pair.second.second);
            return second.first.compareTo(pair.second.first);
        }
        return first.compareTo(pair.first);
    }
}
