package com.sikayetvar.textmining.api.datalayer;

import com.sikayetvar.textmining.api.entity.*;
import com.sikayetvar.textmining.api.entity.Dictionary;
import com.sikayetvar.textmining.api.middle.CorpusCache;
import com.sikayetvar.textmining.api.util.Configuration;
import com.sikayetvar.textmining.api.util.HibernateUtil;
import com.sikayetvar.textmining.api.util.Utils;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static com.sikayetvar.textmining.api.util.Configuration.DEBUG;
import static com.sikayetvar.textmining.api.util.Configuration.DEBUG_COMPLAINT_SAMPLE_SIZE;

public class MysqlDataOperator implements DataOperator {
    private static final Logger logger = LoggerFactory.getLogger(MysqlDataOperator.class);

    private static MysqlDataOperator instance = null;

    private ReentrantLock hashtagLock;
    private ReentrantLock fisHashtagLock;
    private ReentrantLock ngramCandidateLock;
    private ReentrantLock complaintHashtagLock;
    private ReentrantLock complaintStemLock;

    protected MysqlDataOperator() {
        hashtagLock = new ReentrantLock();
        fisHashtagLock = new ReentrantLock();
        ngramCandidateLock = new ReentrantLock();
        complaintHashtagLock = new ReentrantLock();
        complaintStemLock = new ReentrantLock();
    }

    public static MysqlDataOperator getInstance() {
        if (instance == null) {
            instance = new MysqlDataOperator();
        }
        return instance;
    }

    @Override
    public List<Category> getCategories() {
        Session session = HibernateUtil.INSTANCE.getSession();
        try {
            @SuppressWarnings({"deprecation", "unchecked"})
            List<Category> categories = session.createQuery("from Category order by name").list();
            return categories;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                session.close();
            } catch (Exception e) {
                logger.warn(e.getMessage());
            }
        }
        return null;
    }

    @Override
    public List<Complaint> getComplaints(String category, int topNRows) {
        Session session = HibernateUtil.INSTANCE.getSession();
        try {
            @SuppressWarnings({"deprecation", "unchecked"})
            List<Complaint> complaints = session.createQuery("from Complaint where company_id = " + category + " order by id").setMaxResults(topNRows).list();
            return complaints;

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                session.close();
            } catch (Exception e) {
                logger.warn(e.getMessage());
            }
        }

        return null;
    }

    @Override
    public List<Complaint> getComplaintsById(Integer... complaintIds) {
        Session session = HibernateUtil.INSTANCE.getSession();
        try {
            String hql = "from Complaint where id in (:ids) order by id";
            Query query = session.createQuery(hql);
            query.setParameterList("ids", complaintIds);
            @SuppressWarnings({"deprecation", "unchecked"})
            List<Complaint> complaints = query.list();
            return complaints;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                session.close();
            } catch (Exception e) {
                logger.warn(e.getMessage());
            }
        }

        return null;
    }

    @Override
    public List<Hashtag> getHashtags(String category, Set<String> terms, int topN) {
        if (terms == null || terms.isEmpty())
            return null;

        Session session = HibernateUtil.INSTANCE.getSession();
        try {
            String hql;
            if (category != null && !category.trim().isEmpty()) {
                hql = "from Hashtag h where h.category = :category and h.term in (:terms) order by h.score desc";
            } else {
                hql = "select new Hashtag('', h.term, h.score) from HashtagMax h where h.term in (:terms) order by h.score desc";
            }
            Query query = session.createQuery(hql);
            if (category != null && !category.trim().isEmpty())
                query.setParameter("category", category);
            query.setParameterList("terms", terms);
            @SuppressWarnings({"deprecation", "unchecked"})
            List<Hashtag> hashtags = query.setMaxResults(topN).list();
            return hashtags;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                session.close();
            } catch (Exception e) {
                logger.warn(e.getMessage());
            }
        }
        return null;
    }

    @Override
    public List<EndHashtag> getEndHashtags(String category, Set<String> terms, int topN) {
        if (terms == null || terms.isEmpty())
            return null;

        Map<String,Integer> corpus = CorpusCache.getInstance().getCorpus();

        Set<Integer> termIds = new HashSet<>();
        for ( String term:terms) {
            termIds.add(corpus.get(term));
        }

        Session session = HibernateUtil.INSTANCE.getSession();
        try {
            String hql;
            if (category != null && !category.trim().isEmpty()) {
                hql = "from EndHashtag h where h.category = :category and h.termId in (:termIds) order by h.score desc";
            } else {
                hql = "select new EndHashtag('', h.term, h.score) from EndHashtagMax h where h.termId in (:termIds) order by h.score desc";
            }
            Query query = session.createQuery(hql);
            if (category != null && !category.trim().isEmpty())
                query.setParameter("category", category);
            query.setParameterList("termIds", termIds);
            @SuppressWarnings({"deprecation", "unchecked"})
            List<EndHashtag> hashtags = query.setMaxResults(topN).list();
            return hashtags;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                session.close();
            } catch (Exception e) {
                logger.warn(e.getMessage());
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings({"deprecation", "unchecked"})
    public Map<String, Map<String, Float>> getHashtags() {
        Session session = HibernateUtil.INSTANCE.getSession();
        try {
            String hql = "from Hashtag h";
            Query query = session.createQuery(hql);

            List<Hashtag> hashtags;
          /*  if (DEBUG)
                hashtags = query.setMaxResults(DEBUG_COMPLAINT_SAMPLE_SIZE).list();
            else*/
            hashtags = query.list();
            Map<String, Map<String, Float>> hashtagMap = new HashMap<>();
            for (Hashtag hashtag : hashtags) {
                Map<String, Float> map = Utils.putIfAbsent(hashtagMap, hashtag.getCategory(), new HashMap<>());
                map.put(hashtag.getTerm(), hashtag.getScore());
            }
            return hashtagMap;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                session.close();
            } catch (Exception e) {
                logger.warn(e.getMessage());
            }
        }
        return null;
    }

    @Override
    public Map<String,Integer> getCorpus() {
        Session session = null;
        Map<String,Integer> corpus =  new HashMap<>();
        try {
            session = HibernateUtil.INSTANCE.getSession();

            List<Object[]> corpusList = session.createNativeQuery("select id, term from corpus;").getResultList();
            for (Object[] item:corpusList) {
                Integer id = (Integer) item[0];
                String term = (String) item[1];
                corpus.put(term, id); //duplicate entry impossible by db design
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (session != null)
                try {
                    session.close();
                } catch (Exception e) {
                    logger.warn(e.getMessage());
                }
        }
        return corpus;
    }

    @Override
    public List<Suggestion> getSuggestions(String category, String term, int count) {
        Session session = null;
        try {
            session = HibernateUtil.INSTANCE.getSession();

            String[] terms = term.split("\\s+");
            // get next terms only
            int n = terms.length + 1;

            String hql;
            hql = "from Hashtag where company_id = :category and termCount = :termCount and term like :term order by score desc";

            Query query = session.createQuery(hql);
            query.setParameter("category", category);
            query.setParameter("termCount", n);
            query.setParameter("term", term + "%");

            @SuppressWarnings({"deprecation", "unchecked"})
            List<Hashtag> hashtags = query.setMaxResults(count).list();

            return hashtags.stream().map(hashtag -> new Suggestion(hashtag.getTerm().split("\\s+")[n - 1], hashtag.getScore())).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (session != null)
                try {
                    session.close();
                } catch (Exception e) {
                    logger.warn(e.getMessage());
                }
        }

        return null;
    }

    @Override
    public List<Suggestion> getFisSuggestions(String category, String term, int resultLimit) {
        Session session = null;
        try {
            session = HibernateUtil.INSTANCE.getSession();

            String[] terms = term.split("\\s+");

            // full dynamic
            StringBuilder hqlSelect = new StringBuilder("SELECT new com.sikayetvar.textmining.api.entity.Suggestion(h");
            hqlSelect.append(terms.length + 1);
            hqlSelect.append(".term, h");
            hqlSelect.append(terms.length + 1);
            hqlSelect.append(".score) ");

            StringBuilder hqlFrom = new StringBuilder("FROM ");
            StringBuilder hqlInputs = new StringBuilder();
            StringBuilder hqlJoin = new StringBuilder();
            StringBuilder hqlEnd = new StringBuilder();


            for (int i = 1; i < terms.length + 2; i++) {
                if (i > 1)
                    hqlFrom.append(", ");
                hqlFrom.append("FisHashtag h");
                hqlFrom.append(i);

                if (i > 1)
                    hqlInputs.append(" AND ");
                if (i < terms.length + 1) {
                    hqlInputs.append("h");
                    hqlInputs.append(i);
                    hqlInputs.append(".term = :input");
                    hqlInputs.append(i);
                }

                if (i > 2)
                    hqlJoin.append(" AND ");
                if (i > 1) {
                    hqlJoin.append("h1.bagId = h");
                    hqlJoin.append(i);
                    hqlJoin.append(".bagId ");
                }
            }

            hqlEnd.append(" AND h");
            hqlEnd.append(terms.length + 1);
            hqlEnd.append(".term NOT IN (:inputs) AND h");
            hqlEnd.append(terms.length + 1);
            hqlEnd.append(".termCount = ");
            hqlEnd.append(terms.length + 1);
            hqlEnd.append(" AND h");
            hqlEnd.append(terms.length + 1);
            hqlEnd.append(".category = :category");
            hqlEnd.append(" ORDER BY h");
            hqlEnd.append(terms.length + 1);
            hqlEnd.append(".score DESC");

            String hql = String.valueOf(hqlSelect) +
                    hqlFrom +
                    " WHERE " +
                    hqlInputs +
                    hqlJoin +
                    hqlEnd;

            Query query = session.createQuery(hql);
            for (int i = 1; i < terms.length + 1; i++) {
                String input = terms[i - 1];
                query.setParameter("input" + i, input);
            }
            query.setParameterList("inputs", terms);
            query.setParameter("category", category);

            @SuppressWarnings({"deprecation", "unchecked"})
            List<Suggestion> suggestions = query.setMaxResults(resultLimit).list();
            return suggestions;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (session != null)
                try {
                    session.close();
                } catch (Exception e) {
                    logger.warn(e.getMessage());
                }
        }

        return null;
    }

    @Override
    @SuppressWarnings({"unchecked", "deprecation"})
    public List<Complaint> getComplaints() {
        Session session = HibernateUtil.INSTANCE.getSession();
        if (DEBUG)
        {
            logger.warn("DEBUGGING ACTIVE");
            return session.createQuery("from Complaint").setMaxResults(DEBUG_COMPLAINT_SAMPLE_SIZE).list();
        }
        else
        {
            logger.warn("DEBUGGING PASSIVE");
            return session.createQuery("from Complaint").list();
        }
    }

    @Override
    @SuppressWarnings({"unchecked", "deprecation"})
    public List<NgramTerm> getNgramTerms() {
        Session session = HibernateUtil.INSTANCE.getSession();
        return session.createQuery("from NgramTerm ").list();
    }

    @Override
    @SuppressWarnings({"unchecked", "deprecation"})
    public List<Stopword> getStopwords() {
        Session session = HibernateUtil.INSTANCE.getSession();
        session.clear();
        return session.createQuery("from Stopword").list();
    }

    @Override
    @SuppressWarnings({"unchecked", "deprecation"})
    public List<Dictionary> getDictionary() {
        Session session = HibernateUtil.INSTANCE.getSession();
        session.clear();
        return session.createQuery("from Dictionary").list();
    }

    @Override
    @SuppressWarnings({"unchecked", "deprecation"})
    public List<Preference> getPreferences() {
        Session session = HibernateUtil.INSTANCE.getSession();
        session.clear();
        return session.createQuery("from Preference").list();
    }

    @Override
    public void saveHashtags(Collection<Hashtag> hashtags) {
        hashtagLock.lock();
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                Configuration.HASHTAG_FILE_NAME, true), StandardCharsets.UTF_8))) {
            for (Hashtag hashtag : hashtags) {
                bw.write(hashtag.toString() + "\n");
            }
        } catch (IOException e) {
            logger.error("Error while saving hashtags.", e);
            throw new RuntimeException(e);
        } finally {
            hashtagLock.unlock();
        }
        logger.info(hashtags.size() + " Hashtags written.");
    }

    public void saveHashtagsOnDB(Collection<Hashtag> hashtags) {
        Session session = null;
        try {
            session = HibernateUtil.INSTANCE.getSession();
            session.beginTransaction();
            for (Hashtag hashtag : hashtags) {
                session.save(hashtag);
            }
            session.getTransaction().commit();

        } catch (Exception e) {
            logger.error("Error while inserting hashtags.", e);
            session.getTransaction().rollback();
            throw new RuntimeException(e);
        } finally {
            if (session != null)
                try {
                    session.close();
                } catch (Exception e) {
                    logger.warn(e.getMessage());
                }
        }
        logger.info(hashtags.size() + " Hashtags inserted.");
    }

    @Override
    public void saveFisHashtags(Collection<FisHashtag> fisHashtags) {
        fisHashtagLock.lock();
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                Configuration.FIS_HASHTAG_FILE_NAME, true), StandardCharsets.UTF_8))) {
            for (FisHashtag fisHashtag : fisHashtags) {
                bw.write(fisHashtag.toString() + "\n");
            }
        } catch (IOException e) {
            logger.error("Error while saving hashtags.", e);
            throw new RuntimeException(e);
        } finally {
            fisHashtagLock.unlock();
        }
        logger.info(fisHashtags.size() + " FIS hashtags written.");
    }

    @Override
    public void saveNgramCandidates(Collection<Hashtag> hashtags) {
        ngramCandidateLock.lock();
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                Configuration.NGRAM_CANDIDATE_FILE_NAME, true), StandardCharsets.UTF_8))) {
            for (Hashtag hashtag : hashtags) {
                bw.write(hashtag.toString() + "\n");
            }
        } catch (IOException e) {
            logger.error("Error while saving ngram candidates.", e);
            throw new RuntimeException(e);
        } finally {
            ngramCandidateLock.unlock();
        }
        logger.info(hashtags.size() + " Ngram Candidates written.");
    }

    @Override
    public void saveComplaintHashtags(Collection<ComplaintHashtag> complaintHashtags) {
        complaintHashtagLock.lock();
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                Configuration.COMPLAINT_HASHTAG_FILE_NAME, true), StandardCharsets.UTF_8))) {
            for (ComplaintHashtag complaintHashtag : complaintHashtags) {
                bw.write(complaintHashtag.toString() + "\n");
            }
        } catch (IOException e) {
            logger.error("Error while saving hashtags.", e);
            throw new RuntimeException(e);
        } finally {
            complaintHashtagLock.unlock();
        }
    }

    @Override
    public void saveComplaintStems(Collection<ComplaintStems> complaintStemss) {
        complaintStemLock.lock();
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(
                Configuration.COMPLAINT_STEM_FILE_NAME, true), StandardCharsets.UTF_8))) {
            for (ComplaintStems complaintStems : complaintStemss) {
                bw.write(complaintStems.toString() + "\n");
            }
        } catch (IOException e) {
            logger.error("Error while saving stems.", e);
            throw new RuntimeException(e);
        } finally {
            complaintStemLock.unlock();
        }
    }

    @Override
    public void truncateHashtags() {
        hashtagLock.lock();
        try {
            Files.delete(Paths.get(Configuration.HASHTAG_FILE_NAME));
        } catch (IOException e) {
            logger.warn("Error while deleting previous model : " + e.getMessage());
        } finally {
            hashtagLock.unlock();
        }
    }

    @Override
    public void truncateFisHashtags() {
        fisHashtagLock.lock();
        try {
            Files.delete(Paths.get(Configuration.FIS_HASHTAG_FILE_NAME));
        } catch (IOException e) {
            logger.warn("Error while deleting previous model : " + e.getMessage());
        } finally {
            fisHashtagLock.unlock();
        }
    }

    @Override
    public void truncateNgramCandidates() {
        ngramCandidateLock.lock();
        try {
            Files.delete(Paths.get(Configuration.NGRAM_CANDIDATE_FILE_NAME));
        } catch (IOException e) {
            logger.warn("Error while deleting previous model : " + e.getMessage());
        } finally {
            ngramCandidateLock.unlock();
        }
    }

    @Override
    public void truncateComplaintHashtags() {
        complaintHashtagLock.lock();
        try {
            Files.delete(Paths.get(Configuration.COMPLAINT_HASHTAG_FILE_NAME));
        } catch (IOException e) {
            logger.warn("Error while deleting previous model : " + e.getMessage());
        } finally {
            complaintHashtagLock.unlock();
        }
    }

    @Override
    public void truncateComplaintStems() {
        complaintHashtagLock.lock();
        try {
            Files.delete(Paths.get(Configuration.COMPLAINT_STEM_FILE_NAME));
        } catch (IOException e) {
            logger.warn("Error while deleting previous model : " + e.getMessage());
        } finally {
            complaintHashtagLock.unlock();
        }
    }

    @Override
    public void destroy() {
        HibernateUtil.destroy();
    }
}
