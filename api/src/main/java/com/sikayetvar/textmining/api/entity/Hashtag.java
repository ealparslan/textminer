package com.sikayetvar.textmining.api.entity;

import javax.persistence.*;

@Entity
@Table(name = "hashtag")
public class Hashtag {
    private int id;
    private String category;
    private String term;
    private int termCount;
    private float score;
    private float tf;
    private float idf;

    public Hashtag() {
    }

    public Hashtag(String term, float score) {
        this(0, null, term, term.split("\\s+").length, score, 0, 0);
    }

    public Hashtag(String category, String term, float score) {
        this(0, category, term, term.split("\\s+").length, score, 0, 0);
    }

    public Hashtag(int id, String category, String term, int termCount, float score, float tf, float idf) {
        this.id = id;
        this.category = category;
        this.term = term;
        this.termCount = termCount;
        this.score = score;
        this.tf = tf;
        this.idf = idf;
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
    @Column(name = "category", nullable = false, length = 100)
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Basic
    @Column(name = "term", nullable = false, length = 200)
    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    @Basic
    @Column(name = "term_count", nullable = false)
    public int getTermCount() {
        return termCount;
    }

    public void setTermCount(int termCount) {
        this.termCount = termCount;
    }

    @Basic
    @Column(name = "score", nullable = false, precision = 0)
    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    @Basic
    @Column(name = "tf", nullable = false, precision = 0)
    public float getTf() {
        return tf;
    }

    public void setTf(float tf) {
        this.tf = tf;
    }

    @Basic
    @Column(name = "idf", nullable = false, precision = 0)
    public float getIdf() {
        return idf;
    }

    public void setIdf(float idf) {
        this.idf = idf;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Hashtag hashtag = (Hashtag) o;

        return term != null && term.equals(hashtag.term);
    }

    @Override
    public int hashCode() {
        return term.hashCode();
    }

    @Override
    public String toString() {
        return id + "," + category.replace(",", "\\,") + "," + term + "," + termCount + "," + score + "," + tf + "," + idf;
    }
}
