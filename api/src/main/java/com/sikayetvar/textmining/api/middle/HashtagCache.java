package com.sikayetvar.textmining.api.middle;

import com.sikayetvar.textmining.api.datalayer.DataOperatorFactory;
import com.sikayetvar.textmining.api.entity.Hashtag;
import com.sikayetvar.textmining.api.util.Configuration;

import java.util.*;
import java.util.stream.Collectors;

public class HashtagCache {
    private static HashtagCache ourInstance = new HashtagCache();

    /**
     * Category - term - score
     */
    private final Map<String, Map<String, Float>> hashtags;

    public static HashtagCache getInstance() {
        return ourInstance;
    }

    private HashtagCache() {

        hashtags = DataOperatorFactory.getDataOperator(Configuration.DATABASE).getHashtags();

    }

    public List<Hashtag> getHashtags(String category, Set<String> terms, int topN) {
        List<Hashtag> result = new ArrayList<>();
        Map<String, Float> categoryMap = hashtags.get(category);
        if (categoryMap == null)
            return result;

        for (String term : terms) {
            Float score = categoryMap.get(term);
            if (score != null)
                result.add(new Hashtag(category, term, score));
        }

        List<Hashtag> topNHashtags = result.stream().sorted(Comparator.comparingDouble(Hashtag::getScore).reversed()).limit(topN).collect(Collectors.toList());
        if (topNHashtags.size() < 4)
            topNHashtags = DataOperatorFactory.getDataOperator(Configuration.DATABASE).getHashtags(null, terms, topN);
        return topNHashtags;
    }
}
