package com.sikayetvar.textmining.api.scoring;

public class QueryScoreFunctionIdf implements QueryScoreFunction {

    @Override
    public float computeScore(float tf, float idf) {
        return idf;
    }
}
