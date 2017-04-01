package com.sikayetvar.textmining.api.entity;

import javax.persistence.*;

@Entity
@Table(name = "complaint_hashtag")
public class ComplaintHashtag {
    private int id;
    private int complaintId;
    private String hashtag;
    private double score;

    public ComplaintHashtag() {
    }

    public ComplaintHashtag(int id, int complaintId, String hashtag, double score) {
        this.id = id;
        this.complaintId = complaintId;
        this.hashtag = hashtag;
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
    @Column(name = "complaint_id", nullable = false)
    public int getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(int complaintId) {
        this.complaintId = complaintId;
    }

    @Basic
    @Column(name = "hashtag", nullable = false, length = 200)
    public String getHashtag() {
        return hashtag;
    }

    public void setHashtag(String hashtag) {
        this.hashtag = hashtag;
    }

    @Basic
    @Column(name = "score", nullable = false)
    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ComplaintHashtag that = (ComplaintHashtag) o;

        if (id != that.id) return false;
        if (complaintId != that.complaintId) return false;
        if (hashtag != null ? !hashtag.equals(that.hashtag) : that.hashtag != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + complaintId;
        result = 31 * result + (hashtag != null ? hashtag.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return id + "," + complaintId + "," + hashtag + "," + score;
    }
}
