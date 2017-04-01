package com.sikayetvar.textmining.api.entity;

import javax.persistence.Table;

/**
 * Created by John on 16.11.2016.
 */
@javax.persistence.Entity
@Table(name = "stopword")
public class Stopword {
    private int id;

    @javax.persistence.Id
    @javax.persistence.Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private String name;

    @javax.persistence.Basic
    @javax.persistence.Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Stopword stopword = (Stopword) o;

        if (id != stopword.id) return false;
        if (name != null ? !name.equals(stopword.name) : stopword.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }
}
