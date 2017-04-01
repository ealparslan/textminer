package com.sikayetvar.textmining.api.middle;

import com.sikayetvar.textmining.api.datalayer.DataOperatorFactory;
import com.sikayetvar.textmining.api.entity.EndHashtag;
import com.sikayetvar.textmining.api.entity.Hashtag;
import com.sikayetvar.textmining.api.entity.NgramTerm;
import com.sikayetvar.textmining.api.nlp.NgramBuilder;
import com.sikayetvar.textmining.api.nlp.WordParse;
import com.sikayetvar.textmining.api.nlp.ZemberekStemmer;
import com.sikayetvar.textmining.api.nlp.ZemberekStemmerBuilder;
import com.sikayetvar.textmining.api.util.Configuration;
import com.sikayetvar.textmining.api.util.Utils;

import java.util.*;
import java.util.stream.Collectors;

public class ServiceOperator {

    private static final Map<String, String> ngramTerms =
            DataOperatorFactory.getDataOperator(Configuration.DATABASE).getNgramTerms().stream()
                    .collect(Collectors.toMap(NgramTerm::getRawTerm, NgramTerm::getFixedTerm));

    ZemberekStemmer stemmer = ZemberekStemmerBuilder.getInstance().getStemmer();

    public boolean reload(){
        if(ZemberekStemmerBuilder.getInstance().reload() && CorpusCache.getInstance().reload()) {
            stemmer = ZemberekStemmerBuilder.getInstance().getStemmer();
            return true;
        }
        else return false;
    }

    public List<Hashtag> getHashtags(String categoryId, String content, int topN) {
        Set<String> terms = buildTerms(content);

        return DataOperatorFactory.getDataOperator(Configuration.DATABASE).getHashtags(categoryId, terms, topN);
    }
    public String getStemOnlyHashtags(String categoryId, String content, int topN) {
        List<Hashtag> hashtags = getHashtags(categoryId,content,topN);

        return hashtags.stream().map(hashtag -> hashtag.getTerm()).collect(Collectors.joining(" "));
    }
    public List<EndHashtag> getEndHashtags(String categoryId, String content, int topN) {
        Set<String> terms = buildTerms(content);

        return DataOperatorFactory.getDataOperator(Configuration.DATABASE).getEndHashtags(categoryId, terms, topN);
    }

    public String getStem(String word) {
        StringBuilder stems = new StringBuilder();
        WordParse wordParse = stemmer.stemThenFilterPonctuation(word);
        return (wordParse != null) ? wordParse.getRoot() : null;
    }

    public String getStems(String content) {
        return stemmer.stemSentenceThenFilterPonctuation(content).stream().map(wordParse -> wordParse.getRoot()).collect(Collectors.joining(" "));
    }

    public List<Hashtag> getHashtagsInMemory(String categoryId, String content, int topN) {
        Set<String> terms = buildTerms(content);

        return HashtagCache.getInstance().getHashtags(categoryId, terms, topN);
    }

    private Set<String> buildTerms(String content) {
        List<WordParse> wordParses = stemmer.stemSentenceThenFilter(content);
        List<WordParse> ngramWordParses = NgramBuilder.buildAllNgrams(wordParses, Configuration.MAXIMUM_NGRAM_N, ngramTerms.keySet());

        wordParses.addAll(ngramWordParses);
        Map<String, Set<Long>> termPositions = new HashMap<>();

        for (WordParse wordParse : wordParses) {
            String term = wordParse.getRoot();
            Utils.putIfAbsent(termPositions, term, new HashSet<>()).add(wordParse.getIndex());
        }

        stemNgrams(termPositions);

        return termPositions.entrySet().stream()
                .filter(stringSetEntry -> stringSetEntry.getValue().size() > 0)
                .map((stringSetEntry1) -> ngramTerms.getOrDefault(stringSetEntry1.getKey(), stringSetEntry1.getKey()))
                .collect(Collectors.toSet());
    }

    void stemNgrams(Map<String, Set<Long>> categoryTermPositions) {
        for (Iterator<Map.Entry<String, Set<Long>>> iterator = categoryTermPositions.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, Set<Long>> termFrequency = iterator.next();
            String term = termFrequency.getKey();
            List<String> termSplits = Arrays.asList(term.split("\\s+"));
            Set<Long> positions = termFrequency.getValue();
            for (int termCount = termSplits.size() - 1; termCount > 0; termCount--) {
                List<String> ngrams = NgramBuilder.buildNgrams(termSplits, termCount);
                for (int i = 0; i < ngrams.size(); i++) {
                    String subTerm = ngrams.get(i);
                    int termOffset = i;
                    Set<Long> termPositions = positions.stream().map(integer -> integer + termOffset).collect(Collectors.toSet());
                    Set<Long> subTermPositions = categoryTermPositions.get(subTerm);
                    if (subTermPositions != null)
                        subTermPositions.removeAll(termPositions);
                }
            }
        }
    }
}
