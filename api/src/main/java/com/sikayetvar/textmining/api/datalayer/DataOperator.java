package com.sikayetvar.textmining.api.datalayer;

import com.sikayetvar.textmining.api.entity.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DataOperator {
    List<Category> getCategories();

    List<Complaint> getComplaints(String category, int topNRows);

    List<Complaint> getComplaintsById(Integer... complaintIds);

    List<Hashtag> getHashtags(String category, Set<String> terms, int topN);

    List<EndHashtag> getEndHashtags(String category, Set<String> terms, int topN);

    Map<String, Map<String, Float>> getHashtags();

    Map<String , Integer> getCorpus();

    List<Suggestion> getSuggestions(String category, String term, int count);

    List<Suggestion> getFisSuggestions(String category, String term, int resultLimit);

    List<Complaint> getComplaints();

    List<NgramTerm> getNgramTerms();

    List<Stopword> getStopwords();

    List<Dictionary> getDictionary();

    List<Preference> getPreferences();

    void saveHashtags(Collection<Hashtag> hashtags);

    void saveHashtagsOnDB(Collection<Hashtag> hashtags);

    void saveFisHashtags(Collection<FisHashtag> fises);

    void saveNgramCandidates(Collection<Hashtag> hashtags);

    void saveComplaintHashtags(Collection<ComplaintHashtag> complaintHashtags);

   void saveComplaintStems(Collection<ComplaintStems> complaintStemss);


    void truncateHashtags();

    void truncateFisHashtags();

    void truncateNgramCandidates();

    void truncateComplaintHashtags();

    void truncateComplaintStems();

    void destroy();
}
