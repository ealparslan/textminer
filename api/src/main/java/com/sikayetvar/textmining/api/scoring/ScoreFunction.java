package com.sikayetvar.textmining.api.scoring;

import java.util.Map;
import java.util.Set;

public interface ScoreFunction {
    void init(Map<String, Map<String, Integer>> termFrequencies);

    float computeTf(String term, String category);

    float computeIdf(String term);

    float computeScore(String term, String category);

    float computeScore(float tf, float idf);
}

