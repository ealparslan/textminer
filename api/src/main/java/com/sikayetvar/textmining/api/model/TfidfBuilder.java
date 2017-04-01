package com.sikayetvar.textmining.api.model;

import com.sikayetvar.textmining.api.entity.Hashtag;
import com.sikayetvar.textmining.api.scoring.ScoreFunction;
import com.sikayetvar.textmining.api.util.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class TfidfBuilder {
    private static final Logger logger = LoggerFactory.getLogger(TfidfBuilder.class);

    private Map<String, Map<String, Integer>> termFrequencies;
    private ScoreFunction scoreFunction;
    private Map<String, Map<String, Hashtag>> hashtagMap;
    private int numberOfThreads;
    private AtomicInteger hashtagId;
    private List<Hashtag> hashtags;
    private AtomicInteger processedCategoryCount;

    public TfidfBuilder(Map<String, Map<String, Integer>> termFrequencies, ScoreFunction scoreFunction, int numberOfThreads) {
        this.termFrequencies = termFrequencies;
        this.scoreFunction = scoreFunction;
        this.numberOfThreads = numberOfThreads;
        this.hashtagMap = new ConcurrentHashMap<>();
    }

    public void build() {
        hashtagMap.clear();
        this.hashtagId = new AtomicInteger();
        this.processedCategoryCount = new AtomicInteger();

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        for (Map.Entry<String, Map<String, Integer>> categoryTermFrequencies : termFrequencies.entrySet()) {
            String category = categoryTermFrequencies.getKey();
            Map<String, Integer> value = categoryTermFrequencies.getValue();
            executor.execute(() -> {
                processTermFrequencies(category, value);
                int index = processedCategoryCount.incrementAndGet();
                int size = termFrequencies.size();
                logger.info(String.format("Scores calculated for category [%s]:%d of %d (%%%.2f)", category, index, size, (float) index / size * 100f));
            });
        }

        executor.shutdown();
        try {
            while (!executor.awaitTermination(24L, TimeUnit.HOURS)) {
                System.out.println("Not yet. Still waiting for termination");
            }
        } catch (InterruptedException e) {
            logger.error("Error while generating TFIDF model", e);
            throw new RuntimeException(e);
        }

        hashtags = hashtagMap.values().stream().flatMap(stringHashtagMap -> stringHashtagMap.values().stream().map(hashtag -> hashtag)).collect(Collectors.toList());
    }

    public Map<String, Map<String, Hashtag>> getHashtagMap() {
        return hashtagMap;
    }

    public List<Hashtag> getHashtags() {
        return hashtags;
    }

    private void processTermFrequencies(String category, Map<String, Integer> termFrequencies) {
        // only first part is written concurrently, thus inner map can be ordinary HashMap
        Map<String, Hashtag> categoryMap = new HashMap<>();
        hashtagMap.put(category, categoryMap);
        for (String word : termFrequencies.keySet()) {
            // check for length
            if (word.length() > Configuration.MAXIMUM_TERM_LENGTH)
                continue;
            float tf = scoreFunction.computeTf(word, category);
            float idf = scoreFunction.computeIdf(word);
            float score = scoreFunction.computeScore(tf, idf);
            Hashtag hashtag = new Hashtag(hashtagId.incrementAndGet(), category, word, word.split("\\s+").length, score, tf, idf);
            categoryMap.put(hashtag.getTerm(), hashtag);
        }
    }
}
