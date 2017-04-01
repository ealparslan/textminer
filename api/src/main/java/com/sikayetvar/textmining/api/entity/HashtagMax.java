package com.sikayetvar.textmining.api.entity;

import javax.persistence.*;

@Entity
@Table(name = "hashtag_max")
public class HashtagMax {
    private String term;
    private int id;
    private float score;

    public HashtagMax() {
    }

    public HashtagMax(int id, String term, float score) {
        this.term = term;
        this.id = id;
        this.score = score;
    }

    @Basic
    @Column(name = "term", nullable = false, length = 200)
    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
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

        HashtagMax that = (HashtagMax) o;

        if (id != that.id) return false;
        if (Float.compare(that.score, score) != 0) return false;
        if (term != null ? !term.equals(that.term) : that.term != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = term != null ? term.hashCode() : 0;
        result = 31 * result + id;
        result = 31 * result + (score != +0.0f ? Float.floatToIntBits(score) : 0);
        return result;
    }
}
