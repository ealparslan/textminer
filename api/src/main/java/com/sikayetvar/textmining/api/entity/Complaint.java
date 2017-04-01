package com.sikayetvar.textmining.api.entity;

import javax.persistence.*;
import java.util.Locale;

/**
 * Created by John on 16.11.2016.
 */
@Entity
@Table(name = "complaint")
public class Complaint implements Document {
    private static final Locale TR = new Locale("tr");

    protected int id;
    protected String subject;
    protected String content;
    protected String category;

    @Id
    @Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "subject")
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    @Basic
    @Column(name = "content")
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    @Basic
    @Column(name = "category")
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    @Transient
    public String getBody() {
        return (subject + " . " + content).toLowerCase(TR);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Complaint complaint = (Complaint) o;

        if (id != complaint.id) return false;
        if (subject != null ? !subject.equals(complaint.subject) : complaint.subject != null) return false;
        if (content != null ? !content.equals(complaint.content) : complaint.content != null) return false;
        if (category != null ? !category.equals(complaint.category) : complaint.category != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (subject != null ? subject.hashCode() : 0);
        result = 31 * result + (content != null ? content.hashCode() : 0);
        result = 31 * result + (category != null ? category.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return String.format("Id:%d Category:%s Subject:%s", getId(), getCategory(), getSubject());
    }
}
