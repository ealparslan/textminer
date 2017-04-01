package com.sikayetvar.textmining.api.scoring;

import com.sikayetvar.textmining.api.util.Configuration;

import java.util.HashMap;
import java.util.Map;

public class ScoreFunctionTfIdf implements ScoreFunction {
    public static final float MIN_IDF = 0.001f;

    private Map<String, Map<String, Integer>> termFrequencies;
    private Map<String, Integer> termCategoryOccurrences;

    @Override
    public synchronized void init(Map<String, Map<String, Integer>> termFrequencies) {
        this.termFrequencies = termFrequencies;
        this.termCategoryOccurrences = new HashMap<>();
        for (Map.Entry<String, Map<String, Integer>> categoryEntry : termFrequencies.entrySet()) {
            for (String term : categoryEntry.getValue().keySet()) {
                termCategoryOccurrences.put(term, termCategoryOccurrences.getOrDefault(term, 0) + 1);
            }
        }
    }

    @Override
    public float computeTf(String term, String category) {
        if (termFrequencies == null)
            throw new IllegalStateException("Function not initialized. Call init() method first.");
        return termFrequencies.get(category).getOrDefault(term, 0);
    }

    @Override
    public float computeIdf(String term) {
        if (termFrequencies == null)
            throw new IllegalStateException("Function not initialized. Call init() method first.");

        Float idf = (float) Math.log((float) termFrequencies.size() / (float) (termCategoryOccurrences.get(term) + 1));
        // avoid zero
        idf = Math.max(idf, MIN_IDF);
        return idf;
    }

    @Override
    public float computeScore(String term, String category) {
        if (termFrequencies == null)
            throw new IllegalStateException("Function not initialized. Call init() method first.");
        return computeTf(term, category) * computeIdf(term) * Configuration.TFIDF_SCORE_NORMALIZATION_INDEX;
    }

    @Override
    public float computeScore(float tf, float idf) {
        return tf * idf * Configuration.TFIDF_SCORE_NORMALIZATION_INDEX;
    }
}
