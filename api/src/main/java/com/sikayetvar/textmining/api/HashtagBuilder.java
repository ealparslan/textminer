package com.sikayetvar.textmining.api;

import com.ecyrd.speed4j.StopWatch;
import com.google.common.collect.Lists;
import com.sikayetvar.textmining.api.datalayer.DataOperator;
import com.sikayetvar.textmining.api.datalayer.DataOperatorFactory;
import com.sikayetvar.textmining.api.entity.Category;
import com.sikayetvar.textmining.api.entity.Document;
import com.sikayetvar.textmining.api.entity.Hashtag;
import com.sikayetvar.textmining.api.entity.NgramTerm;
import com.sikayetvar.textmining.api.model.FisBuilder;
import com.sikayetvar.textmining.api.model.TfidfBuilder;
import com.sikayetvar.textmining.api.nlp.NgramBuilder;
import com.sikayetvar.textmining.api.nlp.Stemmer;
import com.sikayetvar.textmining.api.nlp.WordParse;
import com.sikayetvar.textmining.api.nlp.ZemberekStemmerBuilder;
import com.sikayetvar.textmining.api.scoring.ScoreFunction;
import com.sikayetvar.textmining.api.scoring.ScoreFunctionTfIdf;
import com.sikayetvar.textmining.api.util.Configuration;
import com.sikayetvar.textmining.api.util.MemoryWatch;
import com.sikayetvar.textmining.api.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.sikayetvar.textmining.api.util.Configuration.*;

public class HashtagBuilder {
    private static final Logger logger = LoggerFactory.getLogger(HashtagBuilder.class);
    public static final long WORD_INDEX_PARTITION_SIZE = 10000L;

    private final List<Document> documents;
    private final Map<String, Set<String>> categoryExcludedWords;
    private ScoreFunction scoreFunction;
    private final Stemmer stemmer;
    private AtomicLong completedDocuments;
    private AtomicInteger fisId;
    private AtomicInteger bagId;

    /**
     * category - term - frequency
     * <p>In which category, which term occurs how many times
     */
    private final Map<String, Map<String, Integer>> termFrequencies;

    /**
     * category - term - position set
     * <p>In which category, which term occurs in which index
     */
    private final Map<String, Map<String, Set<Long>>> termPositions;
    private Map<String, String> ngramTerms;

    public HashtagBuilder(List<Document> documents, List<Category> categories, List<NgramTerm> ngramTerms, ScoreFunction scoreFunction, Stemmer stemmer) {
        this.documents = documents;
        this.stemmer = stemmer;
        this.categoryExcludedWords = new HashMap<>();
        this.scoreFunction = scoreFunction;
        termFrequencies = new ConcurrentHashMap<>();
        termPositions = new ConcurrentHashMap<>();
        for (Category category : categories) {
            Set<String> excludedWords = null;
            try {
                excludedWords = stemmer.stemSentenceThenFilter(category.getName()).stream().map(WordParse::getRoot).collect(Collectors.toSet());
            } catch (Exception e) {
                logger.error(category.getName() + "\n" + e.getMessage());
            }
            categoryExcludedWords.put(Integer.toString(category.getId()), excludedWords);
        }
        this.ngramTerms = new HashMap<>();
        for (NgramTerm ngramTerm : ngramTerms) {
            this.ngramTerms.put(ngramTerm.getRawTerm(), ngramTerm.getFixedTerm());
        }
    }

    public void build() {
        termFrequencies.clear();
        termPositions.clear();
        bagId = new AtomicInteger();
        completedDocuments = new AtomicLong(0);

        int size = documents.size() / Configuration.NUMBER_OF_THREADS + 1;
        List<List<Document>> partitions = Lists.partition(documents, size);

        ExecutorService executor = Executors.newFixedThreadPool(Configuration.NUMBER_OF_THREADS);

        for (List<Document> partition : partitions) {
            executor.execute(() -> parseDocuments(partition));
        }
        executor.shutdown();
        try {
            while (!executor.awaitTermination(24L, TimeUnit.HOURS)) {
                System.out.println("Not yet. Still waiting for termination");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        logger.info("Parsing completed.");
        logger.info("Total documents: " + documents.size());

        // stem term frequencies
        for (Map<String, Set<Long>> categoryTermPositions : termPositions.values()) {
            stemNgrams(categoryTermPositions);
        }
        logger.info("Term frequency stemming completed.");

        // build term frequencies
        buildTermFrequencies();

        // memory tweak, since we have built term frequencies,
        // we do not need term positions any more
        termPositions.clear();

        // if FIS terms are not to be generated
        if (Configuration.MAXIMUM_FIS_TERM_COUNT < 1) {
            // memory tweak, we do not need documents anymore
            documents.clear();
            logger.info("Documents cleared from memory since FIS hashtags are not to be generated.");
        }

        // init score function
        scoreFunction.init(termFrequencies);
        logger.info(String.format("ScoreFunction [%s] initialized.", scoreFunction.getClass().getSimpleName()));

        // build hashtags with TFIDF score model
        TfidfBuilder tfidfBuilder = buildTfidfHashtags();

        // delete previous model
        DataOperatorFactory.getDataOperator(Configuration.DATABASE).truncateHashtags();
        DataOperatorFactory.getDataOperator(Configuration.DATABASE).truncateFisHashtags();
        logger.info("Previous model deleted");

        // fix FIS hashtag ids
        fisId = new AtomicInteger(tfidfBuilder.getHashtags().size() + 1);

        saveTfidfHashtags(tfidfBuilder.getHashtags());
        // memory tweak, we do not need tfidf hashtags anymore
        tfidfBuilder.getHashtags().clear();
        logger.info("TFIDF hashtags cleared from memory");

        // build hashtags with FIS score model
        if (Configuration.MAXIMUM_FIS_TERM_COUNT > 0) {
            // Also partition complaints according to their categories
            Map<String, List<Document>> categoryDocuments = new HashMap<>();
            for (Document document : documents) {
                List<Document> partition = Utils.putIfAbsent(categoryDocuments, document.getCategory(), new ArrayList<>());
                partition.add(document);
            }

            executor = Executors.newFixedThreadPool(Configuration.NUMBER_OF_THREADS);

            for (Map.Entry<String, List<Document>> partitionMap : categoryDocuments.entrySet()) {
                List<Document> partition = partitionMap.getValue();
                String category = partitionMap.getKey();
                executor.execute(() -> buildFisHashtags(category, partition, tfidfBuilder.getHashtagMap()));
            }
            executor.shutdown();
            try {
                while (!executor.awaitTermination(24L, TimeUnit.HOURS)) {
                    System.out.println("Not yet. Still waiting for termination");
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }

            logger.info("FIS building completed. " + fisId.get() + " hashtags generated.");
        }
    }

    private void buildFisHashtags(String category, List<Document> documents, Map<String, Map<String, Hashtag>> hashtagMap) {
        FisBuilder fisBuilder = new FisBuilder(category, documents, hashtagMap, MINIMUM_SUPPORT, BASKET_TOP_N, fisId, bagId);
        fisBuilder.build();
        DataOperatorFactory.getDataOperator(Configuration.DATABASE).saveFisHashtags(fisBuilder.getFisHashtags());
        long totalCompleted = completedDocuments.get();
        logger.info(String.format("FIS building completed for category [%s]: %d of %d (%%%.2f)", category, totalCompleted, this.documents.size(), (float) totalCompleted / (this.documents.size() == 0 ? 1 : this.documents.size()) * 100f));
    }

    private TfidfBuilder buildTfidfHashtags() {
        TfidfBuilder tfidfBuilder = new TfidfBuilder(termFrequencies, scoreFunction, Configuration.NUMBER_OF_THREADS);
        tfidfBuilder.build();
        logger.info("TFIDF building completed. " + tfidfBuilder.getHashtags().size() + " hashtags generated.");
        return tfidfBuilder;
    }

    private void buildTermFrequencies() {
        for (Map.Entry<String, Map<String, Set<Long>>> categoryTermPositions : termPositions.entrySet()) {
            String category = categoryTermPositions.getKey();
            Map<String, Set<Long>> termPositionSet = categoryTermPositions.getValue();
            Map<String, Integer> categoryTermFrequencies = new HashMap<>();
            for (Map.Entry<String, Set<Long>> positionEntry : termPositionSet.entrySet()) {
                String term = positionEntry.getKey();
                Set<Long> positions = positionEntry.getValue();
                String[] termSplits = term.split("\\s+");
                // check if term is an ngram
                if (termSplits.length > 1) {
                    term = ngramTerms.get(term);
                }
                // for duplicate terms
                Integer frequency = categoryTermFrequencies.getOrDefault(term, 0);
                categoryTermFrequencies.put(term, frequency + positions.size());
            }
            this.termFrequencies.put(category, categoryTermFrequencies);
        }
    }

    private void parseDocuments(List<Document> documents) {
        for (int i = 0; i < documents.size(); i++) {
            // increment document count
            // also get word position offset for this document
            long wordIndexStartOffset = completedDocuments.incrementAndGet() * WORD_INDEX_PARTITION_SIZE;

            Document document = documents.get(i);
            String sentence = document.getBody();

            Set<String> excludedWords = categoryExcludedWords.get(document.getCategory());
            List<WordParse> wordParses = stemmer.stemSentenceThenFilter(sentence);
            if (excludedWords != null)
                wordParses = wordParses
                        .stream()
                        .filter(word -> !excludedWords.contains(word.getRoot()))
                        .collect(Collectors.toList());

            // build ngrams
            List<WordParse> ngramWordParses = NgramBuilder.buildAllNgrams(wordParses, Configuration.MAXIMUM_NGRAM_N, ngramTerms.keySet());
            wordParses.addAll(ngramWordParses);

            fixWordParseIndexes(wordParses, wordIndexStartOffset);

            addToTermPositions(document.getCategory(), wordParses);

            if (i % NOTIFY_ROW_SIZE == 0)
                logger.info(String.format("Document parsing completed for : %1$d of %2$d (%%%3$.2f)", i, documents.size(), (float) i / documents.size() * 100f));
        }
    }

    private void fixWordParseIndexes(List<WordParse> wordParses, long offset) {
        wordParses.forEach(wordParse -> wordParse.setIndex(wordParse.getIndex() + offset));
    }

    private void addToTermPositions(String category, List<WordParse> wordParses) {
        Map<String, Set<Long>> categoryTermPositions = Utils.putIfAbsent(termPositions, category, new ConcurrentHashMap<>());
        for (WordParse wordParse : wordParses) {
            String term = wordParse.getRoot();
            Utils.putIfAbsent(categoryTermPositions, term, new HashSet<>()).add(wordParse.getIndex());
        }
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

    private void saveTfidfHashtags(List<Hashtag> tfidfHashtags) {
        int size = tfidfHashtags.size() / Configuration.NUMBER_OF_THREADS + 1;
        List<List<Hashtag>> partitions = Lists.partition(tfidfHashtags, size);

        ExecutorService executor = Executors.newFixedThreadPool(Configuration.NUMBER_OF_THREADS);

        for (List<Hashtag> partition : partitions) {
            executor.execute(() -> DataOperatorFactory.getDataOperator(Configuration.DATABASE).saveHashtags(partition));
        }
        executor.shutdown();
        try {
            while (!executor.awaitTermination(24L, TimeUnit.HOURS)) {
                System.out.println("Not yet. Still waiting for termination");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        logger.info("TFIDF Hashtags saving completed.");
    }

    public static void main(String[] args) {
        try {
            StopWatch sw = new StopWatch();
            MemoryWatch mw = new MemoryWatch();

            logger.info("Starting hashtag building...");
            logger.info(Configuration.dumpCurrentConfiguration());

            DataOperator dataOperator = DataOperatorFactory.getDataOperator(Configuration.DATABASE);

            logger.info(mw.stop("DataOperator").getReadableAmount());
            sw.start();

            List<?> complaints = dataOperator.getComplaints();

            logger.info(mw.stop("getComplaints").getReadableAmount());
            sw.start();

            List<Category> categories = dataOperator.getCategories();
            List<NgramTerm> ngramTerms = dataOperator.getNgramTerms();
            logger.info(String.format("%d Ngram terms loaded.", ngramTerms.size()));

            ScoreFunction scoreFunction = new ScoreFunctionTfIdf();

            Stemmer stemmer = ZemberekStemmerBuilder.getInstance().getStemmer();

            @SuppressWarnings({"unchecked", "deprecation"})
            HashtagBuilder hashtagBuilder = new HashtagBuilder((List<Document>) complaints, categories, ngramTerms, scoreFunction, stemmer);
            hashtagBuilder.build();

            logger.info(mw.stop("HashtagBuilder").getReadableAmount());
            logger.info(sw.toString());
            logger.info("Model building complete");

            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error in main", e);
            System.exit(-1);
        }
    }
}

