package com.sikayetvar.textmining.api.scoring;

import com.sikayetvar.textmining.api.entity.Hashtag;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class QueryScoreModel {

    private List<Hashtag> hashtags;
    private QueryScoreFunction queryScoreFunction;

    public QueryScoreModel(List<Hashtag> hashtags, QueryScoreFunction queryScoreFunction) {
        this.hashtags = hashtags;
        this.queryScoreFunction = queryScoreFunction;
    }

    public void calculateScores() {
        List<Hashtag> newHashtags = new ArrayList<>();

        for (Hashtag hashtag : this.hashtags) {
            float score = queryScoreFunction.computeScore(hashtag.getTf(), hashtag.getIdf());
            // create new hashtag
            Hashtag newHashtag =
                    new Hashtag(hashtag.getId(), hashtag.getCategory(), hashtag.getTerm(), hashtag.getTermCount(), score, hashtag.getTf(), hashtag.getIdf());
            newHashtags.add(newHashtag);
        }

        this.hashtags = newHashtags.stream().sorted(Comparator.comparingDouble(Hashtag::getScore).reversed()).collect(Collectors.toList());
    }

    public List<Hashtag> getHashtags() {
        return hashtags;
    }
}
