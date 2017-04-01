package com.sikayetvar.textmining.api.entity;

import javax.persistence.*;

@Entity
@Table(name = "fis_hashtag")
public class FisHashtag {
    private int id;
    private String category;
    private int bagId;
    private String term;
    private float score;
    private int termCount;

    public FisHashtag() {
    }

    public FisHashtag(int id, String category, int bagId, String term, int termCount, float score) {
        this.id = id;
        this.category = category;
        this.bagId = bagId;
        this.term = term;
        this.score = score;
        this.termCount = termCount;
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
    @Column(name = "category", nullable = false)
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Basic
    @Column(name = "bag_id", nullable = false)
    public int getBagId() {
        return bagId;
    }

    public void setBagId(int bagId) {
        this.bagId = bagId;
    }

    @Basic
    @Column(name = "term", nullable = false, length = 40)
    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    @Basic
    @Column(name = "score", nullable = false)
    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    @Basic
    @Column(name = "term_count", nullable = false)
    public int getTermCount() {
        return termCount;
    }

    public void setTermCount(int termCount) {
        this.termCount = termCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FisHashtag fisHashtag = (FisHashtag) o;

        if (id != fisHashtag.id) return false;
        if (bagId != fisHashtag.bagId) return false;
        if (Double.compare(fisHashtag.score, score) != 0) return false;
        if (term != null ? !term.equals(fisHashtag.term) : fisHashtag.term != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = id;
        result = 31 * result + bagId;
        result = 31 * result + (term != null ? term.hashCode() : 0);
        temp = Float.floatToIntBits(score);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return id + "," + category.replace(",", "\\,") + "," + bagId + "," + term + "," + termCount + "," + score;
    }
}
