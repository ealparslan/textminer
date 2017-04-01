package com.sikayetvar.textmining.api;

import com.ecyrd.speed4j.StopWatch;
import com.google.common.collect.Lists;
import com.sikayetvar.textmining.api.datalayer.DataOperator;
import com.sikayetvar.textmining.api.datalayer.DataOperatorFactory;
import com.sikayetvar.textmining.api.entity.Category;
import com.sikayetvar.textmining.api.entity.Document;
import com.sikayetvar.textmining.api.entity.Hashtag;
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

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import static com.sikayetvar.textmining.api.util.Configuration.NOTIFY_ROW_SIZE;

public class NgramCandidateGenerator {
    private static final Logger logger = LoggerFactory.getLogger(NgramCandidateGenerator.class);
    private static final long WORD_INDEX_PARTITION_SIZE = 10000L;

    private final List<Document> documents;
    private final Map<String, Set<String>> categoryExcludedWords;
    private ScoreFunction scoreFunction;
    private final Stemmer stemmer;
    private AtomicLong completedDocuments;

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

    public NgramCandidateGenerator(List<Document> documents, List<Category> categories, ScoreFunction scoreFunction, Stemmer stemmer) {
        this.documents = documents;
        this.stemmer = stemmer;
        this.categoryExcludedWords = new HashMap<>();
        this.scoreFunction = scoreFunction;
        termFrequencies = new ConcurrentHashMap<>();
        termPositions = new ConcurrentHashMap<>();
        for (Category category : categories) {
            Set<String> excludedWords = stemmer.stemSentenceThenFilter(category.getName()).stream().map(WordParse::getRoot).collect(Collectors.toSet());
            categoryExcludedWords.put(Integer.toString(category.getId()), excludedWords);
        }
    }

    public void build() {
        termFrequencies.clear();
        termPositions.clear();
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
            stemNgrams(categoryTermPositions, Configuration.MINIMUM_NGRAM_FREQUENCY);
        }
        logger.info("Term frequency stemming completed.");

        // build term frequencies
        buildTermFrequencies();

        // memory tweak, since we have built term frequencies,
        // we do not need term positions any more
        termPositions.clear();

        // memory tweak, we do not need documents anymore
        documents.clear();
        logger.info("Documents cleared from memory since FIS hashtags are not to be generated.");

        // init score function
        scoreFunction.init(termFrequencies);
        logger.info(String.format("ScoreFunction [%s] initialized.", scoreFunction.getClass().getSimpleName()));

        // build hashtags with TFIDF score model
        TfidfBuilder tfidfBuilder = buildTfidfHashtags();

        // delete previous model
        DataOperatorFactory.getDataOperator(Configuration.DATABASE).truncateNgramCandidates();
        logger.info("Previous model deleted");

        saveNgramCandidates(tfidfBuilder.getHashtags());
        // memory tweak, we do not need tfidf hashtags anymore
        tfidfBuilder.getHashtags().clear();
        logger.info("TFIDF hashtags cleared from memory");
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
            Map<String, Integer> categoryTermFrequencies = termPositionSet.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, o -> o.getValue().size()));
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
            List<WordParse> ngramWordParses = NgramBuilder.buildAllNgrams(wordParses, Configuration.MAXIMUM_NGRAM_N);

            fixWordParseIndexes(ngramWordParses, wordIndexStartOffset);

            addToTermPositions(document.getCategory(), ngramWordParses);

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

    void stemNgrams(Map<String, Set<Long>> categoryTermPositions, int minimumFrequency) {
        for (Iterator<Map.Entry<String, Set<Long>>> iterator = categoryTermPositions.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<String, Set<Long>> termFrequency = iterator.next();
            String term = termFrequency.getKey();
            List<String> termSplits = Arrays.asList(term.split("\\s+"));
            Set<Long> positions = termFrequency.getValue();
            if (termSplits.size() > 1 && positions.size() < minimumFrequency) {
                iterator.remove();
                continue;
            }
            for (int termCount = termSplits.size() - 1; termCount > 1; termCount--) {
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

    private void saveNgramCandidates(List<Hashtag> tfidfHashtags) {
        int size = tfidfHashtags.size() / Configuration.NUMBER_OF_THREADS + 1;
        List<List<Hashtag>> partitions = Lists.partition(tfidfHashtags, size);

        ExecutorService executor = Executors.newFixedThreadPool(Configuration.NUMBER_OF_THREADS);

        for (List<Hashtag> partition : partitions) {
            executor.execute(() -> DataOperatorFactory.getDataOperator(Configuration.DATABASE).saveNgramCandidates(partition));
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

            ScoreFunction scoreFunction = new ScoreFunctionTfIdf();

            Stemmer stemmer = ZemberekStemmerBuilder.getInstance().getStemmer();

            @SuppressWarnings({"unchecked", "deprecation"})
            NgramCandidateGenerator hashtagBuilder = new NgramCandidateGenerator((List<Document>) complaints, categories, scoreFunction, stemmer);
            hashtagBuilder.build();

            dataOperator.destroy();

            logger.info(mw.stop("HashtagBuilder").getReadableAmount());
            logger.info(sw.toString());
            logger.info("Model building complete");

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error in main", e);
            System.exit(-1);
        }
    }
}

