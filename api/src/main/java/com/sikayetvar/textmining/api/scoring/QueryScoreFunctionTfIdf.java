package com.sikayetvar.textmining.api.scoring;

import com.sikayetvar.textmining.api.util.Configuration;

public class QueryScoreFunctionTfIdf implements QueryScoreFunction {

    @Override
    public float computeScore(float tf, float idf) {
        return tf * idf * Configuration.TFIDF_SCORE_NORMALIZATION_INDEX;
    }
}
