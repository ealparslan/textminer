package com.sikayetvar.textmining.poc;

import com.sikayetvar.textmining.api.nlp.ZemberekStemmer;
import com.sikayetvar.textmining.api.nlp.ZemberekStemmerBuilder;

import java.io.IOException;

/**
 * Created by deniz on 1/17/17.
 */
public class ServletSimulator {

    public static ZemberekStemmer stemmer = ZemberekStemmerBuilder.getInstance().getStemmer();

    public static void main(String[] args) {
        try {
            String responseString = stemmer.stemThenFilter("soyulması").getRoot();
            if(null == responseString)  System.out.print("NA");
            else System.out.print(responseString);
        } catch (Exception e) {
            System.out.print("NA");
        }
    }
}
