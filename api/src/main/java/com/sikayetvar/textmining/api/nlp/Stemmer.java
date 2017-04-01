package com.sikayetvar.textmining.api.nlp;

import java.util.Collection;
import java.util.List;

public interface Stemmer {
    /**
     * Stems the word given. Parses morphologically and gets the most proper root.
     * @param word The word to be stemmed
     * @return most proper root or null if any root can not be found.
     */
    WordParse stem(String word);

    /**
     * Stems the sentence given and gets the most proper parses as a list
     * @param sentence The sentence to be stemmed
     * @return most proper roots or empty set if any root can not be found
     */
    List<WordParse> stemSentence(String sentence);

    /**
     * Filters stop words and some irrelevant words (punctuations, adverbs, ...)
     *
     * @param wordParse parse to be filtered
     * @return parse itself if not filtered (e.g. non stop word), null otherwise (if filtered)
     */
    WordParse filter(WordParse wordParse);

    WordParse filterEmpty(WordParse wordParse);

    WordParse filterPonctuation(WordParse wordParse);

    /**
     * Filters stop words and some irrelevant words (punctuations, adverbs, ...) from given parse list
     *
     * @param wordParses parse list to be filtered
     * @return list of unfiltered parses
     */
    List<WordParse> filter(Collection<WordParse> wordParses);

    List<WordParse> filterPonctuation(Collection<WordParse> wordParses);


    /**
     * Stem word and then filter it. Equals to stem() then filter()
     *
     * @param word word to be stemmed
     * @return parse if stemmed word is not filtered, null otherwise
     */
    WordParse stemThenFilter(String word);
    WordParse stemThenFilterPonctuation(String word);

        /**
         * Stem sentence and then filter generated parses
         *
         * @param sentence sentence to be stemmed
         * @return list of unfiltered parses
         */
    List<WordParse> stemSentenceThenFilter(String sentence);

    List<WordParse> stemSentenceThenFilterPonctuation(String sentence);

    List<String> makeUndefinedCompoundNoun(String root);

    String normalize(String str);
}
