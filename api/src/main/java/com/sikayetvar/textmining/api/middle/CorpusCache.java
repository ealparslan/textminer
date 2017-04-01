package com.sikayetvar.textmining.api.middle;

import com.sikayetvar.textmining.api.datalayer.DataOperatorFactory;
import com.sikayetvar.textmining.api.util.Configuration;

import java.util.Map;

/**
 * Created by deniz on 2/6/17.
 */
public class CorpusCache {

    private static CorpusCache ourInstance = new CorpusCache();

    private Map<String,Integer> corpus;

    public static CorpusCache getInstance() {
        if(ourInstance == null) {
            ourInstance = new CorpusCache();
        }
        return ourInstance;
    }

    private CorpusCache() {
        load();
    }

    private boolean load(){

        boolean success = false;
        try {
            corpus = DataOperatorFactory.getDataOperator(Configuration.DATABASE).getCorpus();
            success = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return success;
    }

    public boolean reload(){
        return load();
    }

    public Map<String,Integer> getCorpus(){
        return corpus;
    }

}
