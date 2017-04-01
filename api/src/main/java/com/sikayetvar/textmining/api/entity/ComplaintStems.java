package com.sikayetvar.textmining.api.entity;

import javax.persistence.*;

@Entity
@Table(name = "complaint_stems")
public class ComplaintStems {
    private int id;
    private int complaintId;
    private String stems;

    public ComplaintStems() {
    }

    public ComplaintStems(int id, int complaintId, String stems) {
        this.id = id;
        this.complaintId = complaintId;
        this.stems = stems;
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
    @Column(name = "stems", nullable = false, length = 200)
    public String getStems() {
        return stems;
    }

    public void setStems(String stems) {
        this.stems = stems;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ComplaintStems that = (ComplaintStems) o;

        if (id != that.id) return false;
        if (complaintId != that.complaintId) return false;
        if (stems != null ? !stems.equals(that.stems) : that.stems != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + complaintId;
        result = 31 * result + (stems != null ? stems.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return id + "," + complaintId + "," + stems.replace(',','.') ;
    }
}
