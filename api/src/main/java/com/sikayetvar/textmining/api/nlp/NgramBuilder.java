package com.sikayetvar.textmining.api.nlp;

import zemberek.core.turkish.PrimaryPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class NgramBuilder {
    public static List<String> buildNgrams(List<String> terms, int n) {
        ArrayList<String> ngrams = new ArrayList<>();

        for (int i = 0; i < terms.size() - n + 1; i++) {
            List<String> ngram = new ArrayList<>(n);
            for (int j = i; j < i + n; j++) {
                String suffix = terms.get(j);
                ngram.add(suffix);
            }
            // check if ngram is complete
            if (ngram.size() == n)
                ngrams.add(String.join(" ", ngram));
        }

        return ngrams;
    }

    public static List<String> buildNgramsOfParses(List<WordParse> wordParses, int n) {
        ArrayList<String> ngrams = new ArrayList<>();

        for (int i = 0; i < wordParses.size() - n + 1; i++) {
            List<String> ngram = new ArrayList<>(n);
            for (int j = i; j < i + n; j++) {
                WordParse suffix = wordParses.get(j);
                // check if words are consecutive
                if (j > i) {
                    WordParse previous = wordParses.get(j - 1);
                    if (suffix.getIndex() - previous.getIndex() != 1)
                        break;
                }
                ngram.add(suffix.getRoot());
            }
            // check if ngram is complete
            if (ngram.size() == n)
                ngrams.add(String.join(" ", ngram));
        }

        return ngrams;
    }

    public static List<List<WordParse>> build(List<WordParse> wordParses, int n) {
        List<List<WordParse>> ngrams = new ArrayList<>();

        for (int i = 0; i < wordParses.size() - n + 1; i++) {
            List<WordParse> ngram = new ArrayList<>(n);
            for (int j = i; j < i + n; j++) {
                WordParse suffix = wordParses.get(j);
                // check if words are consecutive
                if (j > i) {
                    WordParse previous = wordParses.get(j - 1);
                    if (suffix.getIndex() - previous.getIndex() != 1)
                        break;
                }
                ngram.add(suffix);
            }
            // check if ngram is complete
            if (ngram.size() == n)
                ngrams.add(ngram);
        }

        return ngrams;
    }

    /**
     * Builds all ngrams starting with n=1 to n= {@code maxN}. Ngram terms are concatenated with space char.
     * Only the terms given with the parameter ngramTerms are considered as ngrams, thus any other term
     * will not be included in the result list. If ngramTerms is null (not an empty set, just {@code null}) then
     * no ngram filtering will be applied, thus all ngrams with given terms are considered as ngrams and
     * they will be built.
     *
     * @param wordParses terms to be used to build ngrams
     * @param maxN       maximum n
     * @param ngramTerms terms to be considered as ngrams
     * @return All ngrams as a list
     */
    public static List<WordParse> buildAllNgrams(List<WordParse> wordParses, int maxN, Set<String> ngramTerms) {
        List<WordParse> ngramWordParses = new ArrayList<>();
        for (int n = 2; n <= maxN; n++) {
            List<List<WordParse>> ngrams = build(wordParses, n);
            for (List<WordParse> ngram : ngrams) {
                String ngramTermRoot = ngram
                        .stream()
                        .map(WordParse::getRoot)
                        .collect(Collectors.joining(" "))
                        .intern();
                // add ngram only if it is in the ngram list
                if (ngramTerms.contains(ngramTermRoot)) {
                    WordParse ngramTerm = new WordParse(ngramTermRoot, ngramTermRoot, PrimaryPos.Noun, ngram.get(0).getIndex());
                    ngramWordParses.add(ngramTerm);
                }
            }
        }
        return ngramWordParses;
    }

    /**
     * Builds all ngrams starting with n=1 to n= {@code maxN}. Ngram terms are concatenated with space char.
     *
     * @param wordParses terms to be used to build ngrams
     * @param maxN       maximum n
     * @return All ngrams as a list
     */
    public static List<WordParse> buildAllNgrams(List<WordParse> wordParses, int maxN) {
        List<WordParse> ngramWordParses = new ArrayList<>();
        for (int n = 2; n <= maxN; n++) {
            List<List<WordParse>> ngrams = build(wordParses, n);
            for (List<WordParse> ngram : ngrams) {
                String ngramTermRoot = ngram
                        .stream()
                        .map(WordParse::getRoot)
                        .collect(Collectors.joining(" "))
                        .intern();
                WordParse ngramTerm = new WordParse(ngramTermRoot, ngramTermRoot, PrimaryPos.Noun, ngram.get(0).getIndex());
                ngramWordParses.add(ngramTerm);
            }
        }
        return ngramWordParses;
    }
}
