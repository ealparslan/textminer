package com.sikayetvar.textmining.api.nlp;

import zemberek.core.turkish.PrimaryPos;

public class WordParse {
    private final String word;
    private final String root;
    private final PrimaryPos pos;
    private long index;

    public WordParse(String word, String root, PrimaryPos pos, long index) {
        this.word = word;
        this.root = root;
        this.pos = pos;
        this.index = index;
    }

    public WordParse(String word, String root, PrimaryPos pos) {
        this(word, root, pos, 0);
    }

    public String getWord() {
        return word;
    }

    public String getRoot() {
        return root;
    }

    public PrimaryPos getPos() {
        return pos;
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    @Override
    public String toString() {
        return word + " => (" + root + ") (" + pos + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        WordParse wordParse = (WordParse) o;

        if (root.equals(wordParse.root)) return true;

        return false;
    }

    @Override
    public int hashCode() {
        return root.hashCode();
    }
}
