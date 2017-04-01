package com.sikayetvar.textmining.api.entity;

import javax.persistence.*;

@Entity
@Table(name = "end_hashtag_max")
public class EndHashtagMax {
    private int termId;
    private int id;
    private float score;

    public EndHashtagMax() {
    }

    public EndHashtagMax(int id, int termId, float score) {
        this.termId = termId;
        this.id = id;
        this.score = score;
    }

    @Basic
    @Column(name = "termId", nullable = false)
    public int getTerm() {
        return termId;
    }

    public void setTerm(int termId) {
        this.termId = termId;
    }

    @Id
    @Column(name = "id", nullable = false)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

}
