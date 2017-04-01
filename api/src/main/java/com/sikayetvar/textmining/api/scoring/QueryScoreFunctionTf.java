package com.sikayetvar.textmining.api.scoring;

public class QueryScoreFunctionTf implements QueryScoreFunction {

    @Override
    public float computeScore(float tf, float idf) {
        return tf;
    }
}
