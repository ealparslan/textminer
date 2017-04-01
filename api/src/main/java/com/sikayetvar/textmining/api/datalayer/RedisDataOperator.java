package com.sikayetvar.textmining.api.datalayer;

import com.sikayetvar.textmining.api.entity.*;
import com.sikayetvar.textmining.api.entity.Dictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.*;
import java.util.stream.Collectors;

public class RedisDataOperator implements DataOperator {
    private static final Logger logger = LoggerFactory.getLogger(RedisDataOperator.class);

    private static RedisDataOperator instance = null;

    private static JedisPool jedisPool = new JedisPool(new JedisPoolConfig(), "localhost");

    protected RedisDataOperator() {
    }

    public static RedisDataOperator getInstance() {
        if (instance == null) {
            instance = new RedisDataOperator();
        }
        return instance;
    }

    @Override
    public List<Category> getCategories() {
        return null;
    }

    @Override
    public List<Complaint> getComplaints(String category, int topNRows) {
        return null;
    }

    @Override
    public List<Complaint> getComplaintsById(Integer... complaintIds) {
        return null;
    }

    @Override
    public List<Hashtag> getHashtags(String category, Set<String> terms, int topN) {
        return null;
    }

    public List<EndHashtag> getEndHashtags(String category, Set<String> terms, int topN)  {
        return null;
    }

        @Override
    public Map<String, Map<String, Float>> getHashtags() {
        return null;
    }

    @Override
    public Map<String, Integer> getCorpus() {
        return null;
    }

    @Override
    public List<Suggestion> getSuggestions(String category, String term, int count) {
        return null;
    }

    @Override
    public List<Suggestion> getFisSuggestions(String category, String term, int resultLimit) {
        List<String> terms = Arrays.asList(term.split("\\s+"));
        String[] termsKey = terms.stream().map(s -> "termBag:" + category + ":" + s).toArray(String[]::new);
        List<Suggestion> suggestions = new ArrayList<>();

        try (Jedis jedis = jedisPool.getResource()) {
            Set<String> bagIds = jedis.sinter(termsKey);

            for (String bagId : bagIds) {
                Set<String> termsInBag = jedis.smembers("bagTerm:" + bagId);
                termsInBag.removeAll(terms);
                if (termsInBag.size() == 1) {
                    float score = Float.parseFloat(jedis.get("bagScore:" + bagId));
                    String suggestionTerm = termsInBag.iterator().next();
                    suggestions.add(new Suggestion(suggestionTerm, score));
                }
            }
        }

        return suggestions.stream().sorted(Comparator.comparingDouble(Suggestion::getScore).reversed()).limit(resultLimit).collect(Collectors.toList());
    }

    @Override
    public List<Complaint> getComplaints() {
        return null;
    }

    @Override
    public List<NgramTerm> getNgramTerms() {
        return null;
    }

    @Override
    public List<Stopword> getStopwords() {
        return null;
    }

    @Override
    public List<Dictionary> getDictionary() {
        return null;
    }

    @Override
    public List<Preference> getPreferences() {
        return null;
    }

    @Override
    public void saveHashtags(Collection<Hashtag> hashtags) {

    }

    @Override
    public void saveHashtagsOnDB(Collection<Hashtag> hashtags) {

    }

    @Override
    public void saveFisHashtags(Collection<FisHashtag> fises) {
        try (Jedis jedis = jedisPool.getResource()) {
            for (FisHashtag fisHashtag : fises) {
                String bagId = Integer.toString(fisHashtag.getBagId());
                jedis.sadd("termBag:" + fisHashtag.getCategory() + ":" + fisHashtag.getTerm(), bagId);
                jedis.set("bagScore:" + bagId, Float.toString(fisHashtag.getScore()), "NX");
                jedis.sadd("bagTerm:" + bagId, fisHashtag.getTerm());
            }
        }
    }

    @Override
    public void saveNgramCandidates(Collection<Hashtag> hashtags) {

    }

    @Override
    public void saveComplaintHashtags(Collection<ComplaintHashtag> complaintHashtags) {

    }

    @Override
    public void saveComplaintStems(Collection<ComplaintStems> complaintStemss) {

    }

    @Override
    public void truncateHashtags() {

    }

    @Override
    public void truncateFisHashtags() {

    }

    @Override
    public void truncateNgramCandidates() {

    }

    @Override
    public void truncateComplaintHashtags() {

    }

    @Override
    public void truncateComplaintStems() {

    }

    @Override
    public void destroy() {
        jedisPool.destroy();
    }
}
