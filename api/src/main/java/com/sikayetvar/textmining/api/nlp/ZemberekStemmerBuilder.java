package com.sikayetvar.textmining.api.nlp;

import com.sikayetvar.textmining.api.datalayer.DataOperator;
import com.sikayetvar.textmining.api.datalayer.DataOperatorFactory;
import com.sikayetvar.textmining.api.entity.Dictionary;
import com.sikayetvar.textmining.api.entity.Preference;
import com.sikayetvar.textmining.api.entity.Stopword;
import com.sikayetvar.textmining.api.middle.HttpGenerator;
import com.sikayetvar.textmining.api.util.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class ZemberekStemmerBuilder {
    private static final Logger logger = LoggerFactory.getLogger(HttpGenerator.class);

    private static final ZemberekStemmerBuilder ourInstance = new ZemberekStemmerBuilder();

    public static ZemberekStemmerBuilder getInstance() {
        return ourInstance;
    }

    private ZemberekStemmer stemmer;

    private ZemberekStemmerBuilder() {
        load();
    }

    private boolean load(){
        boolean success = false;
        try {
            DataOperator dataOperator = DataOperatorFactory.getDataOperator(Configuration.DATABASE);
            List<Stopword> stopwords = dataOperator.getStopwords();
            List<Dictionary> dictionary = dataOperator.getDictionary();
            List<Preference> preferences = dataOperator.getPreferences();
            stemmer = new ZemberekStemmer(stopwords, dictionary, preferences);
            success = true;
        } catch (IOException e) {
            logger.error("Error in building ZemberekStemmer", e);
        }
        return success;
    }

    public boolean reload(){
        return load();
    }

    public ZemberekStemmer getStemmer() {
        return stemmer;
    }
}
