package com.sikayetvar.textmining.api.datalayer;

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.sikayetvar.textmining.api.entity.*;
import com.sikayetvar.textmining.api.entity.Dictionary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class CassandraDataOperator implements DataOperator {
    private static final Logger logger = LoggerFactory.getLogger(CassandraDataOperator.class);

    private static CassandraDataOperator ourInstance = new CassandraDataOperator();
    private final PreparedStatement termUpdateStatement;
    private final PreparedStatement bagUpdateStatement;
    private final PreparedStatement bagSelectStatement;

    public static CassandraDataOperator getInstance() {
        return ourInstance;
    }

    Cluster cluster;
    Session session;

    private CassandraDataOperator() {
        try {
            cluster = Cluster.builder()
                    .addContactPoint("192.168.1.105")
                    .build();
            session = cluster.connect();

            termUpdateStatement = session.prepare("update sikayetvar.term set bag_id=bag_id+? where category=? and name=?");
            bagUpdateStatement = session.prepare("update sikayetvar.bag set score=?, terms=terms+? where id=?");
            bagSelectStatement = session.prepare("select score,terms from sikayetvar.bag where id in (?)");
        } catch (Throwable e) {
            logger.error("Error while initializing CassandraDataOperator", e);
            throw e;
        }
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
        List<Suggestion> suggestions = new ArrayList<>();

        Statement statement = QueryBuilder
                .select("bag_id")
                .from("sikayetvar", "term")
                .where(QueryBuilder.eq("category", category))
                .and(QueryBuilder.in("name", terms));

        ResultSet rows = session.execute(statement);
        Set<Integer> bagIdIntersection = null;

        for (Row row : rows) {
            Set<Integer> bagIdSet = row.getSet("bag_id", Integer.class);
            if (bagIdIntersection == null)
                bagIdIntersection = bagIdSet;
            else
                bagIdIntersection = bagIdIntersection.stream().filter(bagIdSet::contains).collect(Collectors.toSet());
        }

        if (bagIdIntersection == null)
            return suggestions;

        statement = QueryBuilder
                .select("score", "terms")
                .from("sikayetvar", "bag")
                .where(QueryBuilder.in("id", new ArrayList<>(bagIdIntersection)));

        rows = session.execute(statement);

        for (Row row : rows) {
            Set<String> termSet = row.getSet("terms", String.class);
            // remove query terms
            termSet.removeAll(terms);
            if (termSet.size() == 1) {
                float score = row.getFloat("score");
                suggestions.add(new Suggestion(termSet.iterator().next(), score));
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
        /*BatchStatement batchStatement = new BatchStatement(BatchStatement.Type.LOGGED);*/
        for (FisHashtag fisHashtag : fises) {
            Set<Integer> bagSet = new HashSet<>();
            bagSet.add(fisHashtag.getBagId());
            //batchStatement.add(termUpdateStatement.bind(bagSet, fisHashtag.getCategory(), fisHashtag.getTerm()));

            session.executeAsync(termUpdateStatement.bind(bagSet, fisHashtag.getCategory(), fisHashtag.getTerm()));

            Set<String> termSet = new HashSet<>();
            termSet.add(fisHashtag.getTerm());
            //batchStatement.add(bagUpdateStatement.bind(fisHashtag.getScore(), termSet, fisHashtag.getId()));

            session.executeAsync(bagUpdateStatement.bind(fisHashtag.getScore(), termSet, fisHashtag.getBagId()));

            /*// check if batch limit reached
            if (batchStatement.size() > 100) {
                session.executeAsync(batchStatement);
                batchStatement = new BatchStatement(BatchStatement.Type.LOGGED);
            }*/
        }
       /* if (batchStatement.size() > 0)
            session.executeAsync(batchStatement);*/
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
        if (cluster != null) cluster.close();
    }
}
