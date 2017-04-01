package com.sikayetvar.textmining.api.model;

import ca.pfv.spmf.algorithms.frequentpatterns.fin_prepost.PrePost;
import com.sikayetvar.textmining.api.entity.Document;
import com.sikayetvar.textmining.api.entity.FisHashtag;
import com.sikayetvar.textmining.api.entity.Hashtag;
import com.sikayetvar.textmining.api.nlp.WordParse;
import com.sikayetvar.textmining.api.nlp.ZemberekStemmer;
import com.sikayetvar.textmining.api.nlp.ZemberekStemmerBuilder;
import com.sikayetvar.textmining.api.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.sikayetvar.textmining.api.util.Configuration.MAXIMUM_FIS_TERM_COUNT;
import static com.sikayetvar.textmining.api.util.Configuration.NOTIFY_ROW_SIZE;

public class FisBuilder {
    private static final Logger logger = LoggerFactory.getLogger(FisBuilder.class);

    private final int topN;
    private final float minimumSupport;
    private final String category;
    private final List<Document> documents;

    private List<FisHashtag> fisHashtags;
    private final Map<String, Map<String, Hashtag>> hashtagMap;
    private ZemberekStemmer stemmer;
    private AtomicInteger fisId;
    private AtomicInteger bagId;

    public FisBuilder(String category, List<Document> documents, Map<String, Map<String, Hashtag>> hashtagMap, float minimumSupport, int topN, AtomicInteger fisId, AtomicInteger bagId) {
        this.category = category;
        this.documents = documents;
        this.hashtagMap = hashtagMap;
        this.minimumSupport = minimumSupport;
        this.topN = topN;
        this.fisId = fisId;
        this.bagId = bagId;
        this.fisHashtags = new ArrayList<>();
        stemmer = ZemberekStemmerBuilder.getInstance().getStemmer();
    }

    public void build() {
        fisHashtags.clear();

        final Map<Document, Map<String, Float>> filteredParses = filterTerms(documents, hashtagMap, topN);

        List<Set<String>> categoryItemsetList = buildItemsetLists(filteredParses);

        logger.debug(String.format("Generating FIS hashtags for %d documents...", categoryItemsetList.size()));
        generateCategoryHashtags(category, categoryItemsetList);
    }

    private void generateCategoryHashtags(String category, List<Set<String>> itemsetList) {
        PrePost prePost = new PrePost();
        FrequentItemsetData<String> frequentItemsetData = prePost.runAlgorithm(itemsetList, minimumSupport, MAXIMUM_FIS_TERM_COUNT);

        for (Set<String> itemset : frequentItemsetData.getSupportCountMap().keySet()) {
            if (itemset.size() == 1)
                continue;

            int bagId = this.bagId.incrementAndGet();
            float support = (float) frequentItemsetData.getSupport(itemset);
            for (String term : itemset) {
                FisHashtag fisHashtag = new FisHashtag(fisId.incrementAndGet(), category, bagId, term, itemset.size(), support);
                fisHashtags.add(fisHashtag);
            }
        }

        logger.debug("FIS generated. Generated data size: " + frequentItemsetData.getSupportCountMap().size());
    }

    private Map<Document, Map<String, Float>> filterTerms(List<Document> documents, Map<String, Map<String, Hashtag>> hashtagMap, int topN) {
        Map<Document, Map<String, Float>> filteredParses = new HashMap<>();

        for (int i = 0; i < documents.size(); i++) {
            Document document = documents.get(i);
            List<WordParse> wordParses = stemmer.stemSentenceThenFilter(document.getBody());
            Set<String> terms = wordParses.stream().map(WordParse::getRoot).collect(Collectors.toSet());
            Map<String, Float> basket = new HashMap<>();
            Map<String, Hashtag> categoryMap = hashtagMap.get(document.getCategory());
            for (String term : terms) {
                Hashtag hashtag = categoryMap.get(term);
                if (hashtag != null && hashtag.getTermCount() == 1) {
                    Float score = hashtag.getScore();
                    basket.put(term, score);
                }
            }
            basket = Utils.sortByValue(basket, topN);
            filteredParses.put(document, basket);

            if (i / NOTIFY_ROW_SIZE > 1 && i % NOTIFY_ROW_SIZE == 0)
                logger.info(String.format("Document parsing completed for category [%s]: %d of %d (%%%.2f)", category, i, documents.size(), (float) i / documents.size() * 100f));
        }

        logger.debug(String.format("Document parsing completed for total %d documents.", documents.size()));

        return filteredParses;
    }

    private List<Set<String>> buildItemsetLists(Map<Document, Map<String, Float>> filteredTerms) {
        List<Set<String>> itemsetList = new ArrayList<>();

        for (Map.Entry<Document, Map<String, Float>> entry : filteredTerms.entrySet()) {
            Map<String, Float> termScores = entry.getValue();
            Set<String> basket = new HashSet<>();

            for (String term : termScores.keySet()) {
                basket.add(term);
            }

            itemsetList.add(basket);
        }

        return itemsetList;
    }

    public List<FisHashtag> getFisHashtags() {
        return fisHashtags;
    }
}
