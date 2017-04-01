package com.sikayetvar.textmining.api.entity;

import javax.persistence.*;

@Entity
@Table(name = "end_hashtag_scores")
public class EndHashtag {
    private int id;
    private String category;
    private int termId;
    private int termCount;
    private float score;

    public EndHashtag() {
    }

    public EndHashtag(String category, int termId, float score) {
        this(0, category, termId, 0, score);
    }

    public EndHashtag(int id, String category, int termId, int termCount, float score) {
        this.id = id;
        this.category = category;
        this.termId = termId;
        this.termCount = termCount;
        this.score = score;
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
    @Column(name = "termId", nullable = false)
    public int getTermId() {
        return termId;
    }

    public void setTermId(int termId) {
        this.termId = termId;
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


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EndHashtag hashtag = (EndHashtag) o;

        return termId != 0 && termId == hashtag.termId;
    }

}
