package com.sikayetvar.textmining.poc;

import com.google.common.collect.ImmutableList;
import com.medallia.word2vec.Searcher;
import com.sikayetvar.textmining.api.nlp.WordParse;
import com.sikayetvar.textmining.api.nlp.ZemberekStemmer;

import java.util.LinkedList;

public class VectorOperator {
    public static ImmutableList<Double> calculate(String formula, Searcher searcher) throws Searcher.UnknownWordException {
        ImmutableList<Double> result;
        String[] elements = formula.split("((?<=[+-])|(?=[+-]))");
        // if only one term
        if (elements.length > 0) {
            String term1 = elements[0];
            result = searcher.getRawVector(term1);
            if (elements.length % 2 == 1) {
                for (int i = 1; i < elements.length; i += 2) {
                    String operand = elements[i];
                    String term2 = elements[i + 1];
                    ImmutableList<Double> term2vectors = searcher.getRawVector(term2);
                    if (operand.equals("+"))
                        result = DoubleVectors.add(result, term2vectors);
                    else if (operand.equals("-"))
                        result = DoubleVectors.subtract(result, term2vectors);
                    else
                        throw new IllegalArgumentException("Invalid operand in formula");
                }
                return result;
            } else
                throw new IllegalArgumentException("Invalid formula");
        } else
            throw new IllegalArgumentException("Invalid formula");
    }

    public static String stemFormula(ZemberekStemmer stemmer, String formula) {
        LinkedList<String> stemmed = new LinkedList<>();

        String[] elements = formula.split("((?<=[+-])|(?=[+-]))");
        for (int i = 0; i < elements.length; i++) {
            String element = elements[i];
            if (element.matches("[+-]")) {
                stemmed.add(element);
                continue;
            }
            WordParse wordParse = stemmer.stemThenFilter(element);
            if (wordParse == null) {
                if (i > 1)
                    stemmed.removeLast();
            } else
                stemmed.add(wordParse.getRoot());
        }

        return String.join("", stemmed);
    }
}