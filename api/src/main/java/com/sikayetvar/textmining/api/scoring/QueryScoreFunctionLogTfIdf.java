package com.sikayetvar.textmining.api.scoring;

public class QueryScoreFunctionLogTfIdf implements QueryScoreFunction {

    @Override
    public float computeScore(float tf, float idf) {
        return (float) (Math.log(tf + 1) * idf);
    }
}
