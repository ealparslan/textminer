package com.sikayetvar.textmining.lucene;

import com.sikayetvar.textmining.api.nlp.Stemmer;

import java.io.IOException;
import java.io.Reader;

public class ZemberekNormalizer extends Reader {
    public static final int BLOCK_SIZE = 1024;
    private int pos = 0, size = 0;
    private String s = null;
    private Stemmer stemmer;

    public ZemberekNormalizer(Stemmer stemmer, String s) {
        this.stemmer = stemmer;
        this.s = s;
        this.size = s.length();
        this.pos = 0;
    }

    public ZemberekNormalizer(Stemmer stemmer, Reader reader) throws IOException {
        this.stemmer = stemmer;
        char[] block = new char[BLOCK_SIZE];
        StringBuilder buffer = new StringBuilder();
        int numCharsRead;
        while ((numCharsRead = reader.read(block, 0, block.length)) != -1) {
            buffer.append(block, 0, numCharsRead);
        }
        reader.close();
        this.s = normalize(buffer.toString());
        this.size = s.length();
        this.pos = 0;
    }


    @Override
    public int read() {
        if (pos < size) {
            return s.charAt(pos++);
        } else {
            s = null;
            return -1;
        }
    }

    @Override
    public int read(char[] c, int off, int len) {
        if (pos < size) {
            len = Math.min(len, size - pos);
            s.getChars(pos, pos + len, c, off);
            pos += len;
            return len;
        } else {
            s = null;
            return -1;
        }
    }

    @Override
    public void close() {
        pos = size; // this prevents NPE when reading after close!
        s = null;
    }

    public String normalize(String str) {
        return stemmer.normalize(str);
    }
}
