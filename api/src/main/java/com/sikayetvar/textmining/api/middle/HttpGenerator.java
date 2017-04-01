package com.sikayetvar.textmining.api.middle;

import com.sikayetvar.textmining.api.entity.EndHashtag;
import com.sikayetvar.textmining.api.entity.Hashtag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.sikayetvar.textmining.api.util.Configuration.JSON_HASHTAG_TOP_N;

/**
 * Created by John on 11.11.2016.
 */
public class HttpGenerator {
    private static final Logger logger = LoggerFactory.getLogger(HttpGenerator.class);
    ServiceOperator serviceOperator;
    public HttpGenerator(ServiceOperator operator){
        serviceOperator = operator;
    }

    public String getEndHashtagJson(String categoryId, String content) {
        String json = "[]";

        try {

            List<EndHashtag> hashtags = serviceOperator.getEndHashtags(categoryId, content, JSON_HASHTAG_TOP_N);
            if (hashtags == null)
                return json;

            return "[" + hashtags.stream().map((hashtag) -> "\"" + hashtag.getTermId() + "\"").collect(Collectors.joining(",")) + "]";
        } catch (Exception e) {
            logger.error("Error in getEndHashtagJson", e);
        }

        return json;
    }

    public String getHashtagJsonInMemory(String category, String content) {
        try {
            ServiceOperator serviceOperator = new ServiceOperator();

            List<Hashtag> hashtags = serviceOperator.getHashtagsInMemory(category, content, JSON_HASHTAG_TOP_N);

            return "[" + hashtags.stream().map((hashtag) -> "\"" + hashtag.getTerm() + "\"").collect(Collectors.joining(",")) + "]";
        } catch (Exception e) {
            logger.error("Error in getHashtagJsonInMemory", e);
            throw new RuntimeException(e);
        }
    }


}
