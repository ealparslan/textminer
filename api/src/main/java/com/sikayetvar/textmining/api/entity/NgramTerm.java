package com.sikayetvar.textmining.api.entity;

import javax.persistence.*;

@Entity
@Table(name = "ngram_term")
public class NgramTerm {
    private int id;
    private String rawTerm;
    private String fixedTerm;
    private float score;

    public NgramTerm() {
    }

    public NgramTerm(String rawTerm, String fixedTerm) {
        this.rawTerm = rawTerm;
        this.fixedTerm = fixedTerm;
    }

    @Id
    @Column(name = "id", nullable = false)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "raw_term", nullable = false, length = 200)
    public String getRawTerm() {
        return rawTerm;
    }

    public void setRawTerm(String rawTerm) {
        this.rawTerm = rawTerm;
    }

    @Basic
    @Column(name = "fixed_term", nullable = false, length = 200)
    public String getFixedTerm() {
        return fixedTerm;
    }

    public void setFixedTerm(String fixedTerm) {
        this.fixedTerm = fixedTerm;
    }

    @Basic
    @Column(name = "score", nullable = false, precision = 0)
    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NgramTerm ngramTerm = (NgramTerm) o;

        if (id != ngramTerm.id) return false;
        if (Float.compare(ngramTerm.score, score) != 0) return false;
        if (rawTerm != null ? !rawTerm.equals(ngramTerm.rawTerm) : ngramTerm.rawTerm != null) return false;
        if (fixedTerm != null ? !fixedTerm.equals(ngramTerm.fixedTerm) : ngramTerm.fixedTerm != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (rawTerm != null ? rawTerm.hashCode() : 0);
        result = 31 * result + (fixedTerm != null ? fixedTerm.hashCode() : 0);
        result = 31 * result + (score != +0.0f ? Float.floatToIntBits(score) : 0);
        return result;
    }
}
