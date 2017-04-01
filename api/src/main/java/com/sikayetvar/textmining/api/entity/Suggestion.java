package com.sikayetvar.textmining.api.entity;

/**
 * Created by John on 11.11.2016.
 */
public class Suggestion implements java.io.Serializable {
    private String term;
    private float score;

    public Suggestion(String term, float score) {
        this.term = term;
        this.score = score;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return term + ":" + score;
    }
}
