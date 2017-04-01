package com.sikayetvar.textmining.lucene;

import com.sikayetvar.textmining.api.nlp.WordParse;
import com.sikayetvar.textmining.api.nlp.ZemberekStemmer;
import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

import java.io.IOException;

public class ZemberekFilter extends TokenFilter {
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
    private int skippedPositions;

    private ZemberekStemmer stemmer;

    protected ZemberekFilter(TokenStream input, ZemberekStemmer stemmer) {
        super(input);
        this.stemmer = stemmer;
    }

    @Override
    public boolean incrementToken() throws IOException {
        skippedPositions = 0;
        while (input.incrementToken()) {
            final char termBuffer[] = termAtt.buffer();
            final int length = termAtt.length();
            String word = new String(termBuffer, 0, length);
            WordParse parse = stemmer.stemThenFilter(word);
            if (parse != null) {
                if (skippedPositions != 0) {
                    posIncrAtt.setPositionIncrement(posIncrAtt.getPositionIncrement() + skippedPositions);
                }
                String lemma = parse.getRoot();
                final char finalTerm[] = lemma.toCharArray();
                final int newLength = lemma.length();
                if (finalTerm != termBuffer)
                    termAtt.copyBuffer(finalTerm, 0, newLength);
                else
                    termAtt.setLength(newLength);

                return true;
            }

            skippedPositions += posIncrAtt.getPositionIncrement();
        }

        // reached EOS -- return false
        return false;
    }
}

