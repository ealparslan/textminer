package com.sikayetvar.textmining.lucene;

import com.sikayetvar.textmining.api.nlp.ZemberekStemmer;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.miscellaneous.SetKeywordMarkerFilter;
import org.apache.lucene.analysis.snowball.SnowballFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tr.ApostropheFilter;
import org.apache.lucene.analysis.tr.TurkishLowerCaseFilter;

import java.io.IOException;
import java.io.Reader;

public final class ZemberekAnalyzer extends Analyzer {
    private ZemberekStemmer stemmer;

    public ZemberekAnalyzer(ZemberekStemmer stemmer) {
        this.stemmer = stemmer;
    }

    /**
     * Creates a {@link org.apache.lucene.analysis.Analyzer.TokenStreamComponents} which tokenizes all the text in the provided {@link Reader}.
     *
     * @return A {@link org.apache.lucene.analysis.Analyzer.TokenStreamComponents} built from an {@link StandardTokenizer} filtered with {@link StandardFilter},
     * {@link TurkishLowerCaseFilter}, {@link StopFilter}, {@link SetKeywordMarkerFilter} if a stem exclusion set is provided and {@link SnowballFilter}.
     */
    @Override
    protected TokenStreamComponents createComponents(String fieldName) {
        final Tokenizer source = new StandardTokenizer();
        TokenStream result = new StandardFilter(source);
        result = new ApostropheFilter(result);
        result = new TurkishLowerCaseFilter(result);
        result = new ZemberekFilter(result, stemmer);
        return new TokenStreamComponents(source, result);
    }

    @Override
    protected TokenStream normalize(String fieldName, TokenStream in) {
        TokenStream result = new StandardFilter(in);
        result = new TurkishLowerCaseFilter(result);
        return result;
    }

    @Override
    protected Reader initReader(String fieldName, Reader reader) {
        try {
            return new ZemberekNormalizer(stemmer, reader);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
