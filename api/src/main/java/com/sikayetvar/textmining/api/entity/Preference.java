package com.sikayetvar.textmining.api.entity;

import javax.persistence.Table;

/**
 * Created by John on 16.11.2016.
 */
@javax.persistence.Entity
@Table(name = "preference")
public class Preference {
    private int id;

    @javax.persistence.Id
    @javax.persistence.Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Preference that = (Preference) o;

        if (id != that.id) return false;
        if (root != null ? !root.equals(that.root) : that.root != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (root != null ? root.hashCode() : 0);
        return result;
    }
}
