package com.sikayetvar.textmining.poc;

import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;

public class DoubleVectors {
    public static ImmutableList<Double> add(ImmutableList<Double> v1, ImmutableList<Double> v2) {
        List<Double> diff = new ArrayList<>(v1.size());
        for (int i = 0; i < v1.size(); i++) {
            diff.add(v1.get(i) + v2.get(i));
        }

        return ImmutableList.copyOf(diff);
    }

    public static ImmutableList<Double> subtract(ImmutableList<Double> v1, ImmutableList<Double> v2) {
        List<Double> diff = new ArrayList<>(v1.size());
        for (int i = 0; i < v1.size(); i++) {
            diff.add(v1.get(i) - v2.get(i));
        }

        return ImmutableList.copyOf(diff);
    }
}
