package com.sikayetvar.textmining.api.scoring;

public interface QueryScoreFunction {
    float computeScore(float tf, float idf);
}
