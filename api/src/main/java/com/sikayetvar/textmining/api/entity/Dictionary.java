package com.sikayetvar.textmining.api.entity;

import javax.persistence.Table;

/**
 * Created by John on 16.11.2016.
 */
@javax.persistence.Entity
@Table(name = "dictionary")
public class Dictionary {
    private int id;

    @javax.persistence.Id
    @javax.persistence.Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private String lemma;

    @javax.persistence.Basic
    @javax.persistence.Column(name = "lemma")
    public String getLemma() {
        return lemma;
    }

    public void setLemma(String lemma) {
        this.lemma = lemma;
    }

    private String root;

    @javax.persistence.Basic
    @javax.persistence.Column(name = "root")
    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    private String pos;

    @javax.persistence.Basic
    @javax.persistence.Column(name = "pos")
    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    private String pronunciation;

    @javax.persistence.Basic
    @javax.persistence.Column(name = "pronunciation")
    public String getPronunciation() {
        return pronunciation;
    }

    public void setPronunciation(String pronunciation) {
        this.pronunciation = pronunciation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Dictionary that = (Dictionary) o;

        if (id != that.id) return false;
        if (lemma != null ? !lemma.equals(that.lemma) : that.lemma != null) return false;
        if (root != null ? !root.equals(that.root) : that.root != null) return false;
        if (pos != null ? !pos.equals(that.pos) : that.pos != null) return false;
        if (pronunciation != null ? !pronunciation.equals(that.pronunciation) : that.pronunciation != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (lemma != null ? lemma.hashCode() : 0);
        result = 31 * result + (root != null ? root.hashCode() : 0);
        result = 31 * result + (pos != null ? pos.hashCode() : 0);
        result = 31 * result + (pronunciation != null ? pronunciation.hashCode() : 0);
        return result;
    }
}
