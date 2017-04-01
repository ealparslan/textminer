package com.sikayetvar.textmining.api.util;

import com.google.common.base.CaseFormat;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

public class Configuration {
    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);

    /* PARAMETERS AND THEIR DEFAULT VALUES. ALL VALUES ARE OVERRIDDEN BY PROPERTIES FILE */
    /**
     * DEBUG flag, for debugging purposes only.
     * It must be false in production environments.
     */
    public static boolean DEBUG = true;

    /**
     * When DEBUG flag is set to true, this many records are read from database and
     * model is built with this sample.
     */
    public static int DEBUG_COMPLAINT_SAMPLE_SIZE = 1000;

    /**
     * When to insert log record while saving entities to database
     */
    public static int NOTIFY_ROW_SIZE = 10000;

    /**
     * Batch size while saving entities to database. Used for bulk inserts
     */
    public static int BATCH_SIZE = 30;

    /**
     * Hashtag item size to return to hashtag json servlet.
     */
    public static int JSON_HASHTAG_TOP_N = 10;

    /**
     * Due to SVNW-888 we need to normalize TfIdf scores by multiplying that constant
     */
    public static int TFIDF_SCORE_NORMALIZATION_INDEX = 1000;

    /**
     * Ngram size to be created
     */
    public static int MAXIMUM_NGRAM_N = 3;

    /**
     * Maximum term count to be included in FIS model
     */
    public static int MAXIMUM_FIS_TERM_COUNT = 0;

    /**
     * Minimum support value for FIS Builder
     */
    public static float MINIMUM_SUPPORT = 0.1f;

    /**
     * Minimum occurrence count of a term to be considered as "ngram". Below this threshold a term will never be
     * considered as an ngram.
     */
    public static int MINIMUM_NGRAM_FREQUENCY = 10;

    /**
     * For FIS Builder, this many items are considered in the basket. Setting this value greater than
     * 10 may result significant performance degrade.
     */
    public static int BASKET_TOP_N = 10;

    /**
     * For parallel processing. Default 1
     */
    public static int NUMBER_OF_THREADS = 4;

    /**
     * Maximum term length. Terms having longer than this length will be discarded from the model.
     */
    public static int MAXIMUM_TERM_LENGTH = 40;

    /**
     * Database to be used for data access. Default to "MYSQL"
     */
    public static String DATABASE = "MYSQL";

    /**
     * Database connection URL to connect to database
     */
    public static String DATABASE_CONNECTION_URL = "jdbc:mysql://127.0.0.1:3306/textmining?characterEncoding=UTF-8&amp;useSSL=false&amp;rewriteBatchedStatements=true";
//    public static String DATABASE_CONNECTION_URL = "jdbc:mysql://curiosity.sikayetvar.com:3307/textmining?characterEncoding=UTF-8&amp;useSSL=false&amp;rewriteBatchedStatements=true";

    /**
     * Database username to connect to database
     */
    public static String DATABASE_USERNAME = "textminer";

    /**
     * Database password to connect to database
     */
    public static String DATABASE_PASSWORD = "3BY98DJnmtfpZgzmusT3";

    public static String FIS_HASHTAG_FILE_NAME = "fis_hashtag_" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".csv";

    public static String HASHTAG_FILE_NAME = "hashtag_" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".csv";

    public static String NGRAM_CANDIDATE_FILE_NAME = "nram_candidate_" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".csv";

    public static String COMPLAINT_HASHTAG_FILE_NAME = "complaint_hashtag_" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".csv";

    public static String COMPLAINT_STEM_FILE_NAME = "complaint_stem_" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".csv";

    public static String ELASTIC_HOST = "luna.sikayetvar.com";

    public static int ELASTIC_PORT = 9300;


    static {
        Configurations configs = new Configurations();
        try {
            File configFile = new File("api.properties");
            if (configFile.exists()) {
                org.apache.commons.configuration2.Configuration config = configs.properties(configFile);

                DEBUG = config.getBoolean("api.debug", DEBUG);
                DEBUG_COMPLAINT_SAMPLE_SIZE = config.getInt("api.debugComplaintSampleSize", DEBUG_COMPLAINT_SAMPLE_SIZE);
                NOTIFY_ROW_SIZE = config.getInt("api.notifyRowSize", NOTIFY_ROW_SIZE);
                BATCH_SIZE = config.getInt("api.batchSize", BATCH_SIZE);
                JSON_HASHTAG_TOP_N = config.getInt("api.jsonHashtagTopN", JSON_HASHTAG_TOP_N);
                TFIDF_SCORE_NORMALIZATION_INDEX = config.getInt("api.tfidfNormalizationScore", TFIDF_SCORE_NORMALIZATION_INDEX);
                MAXIMUM_NGRAM_N = config.getInt("api.maximumNgramN", MAXIMUM_NGRAM_N);
                MAXIMUM_FIS_TERM_COUNT = config.getInt("api.maximumFisTermCount", MAXIMUM_FIS_TERM_COUNT);
                MINIMUM_SUPPORT = config.getFloat("api.minimumSupport", MINIMUM_SUPPORT);
                MINIMUM_NGRAM_FREQUENCY = config.getInt("api.minimumNgramFrequency", MINIMUM_NGRAM_FREQUENCY);
                BASKET_TOP_N = config.getInt("api.basketTopN", BASKET_TOP_N);
                NUMBER_OF_THREADS = config.getInt("api.numberOfThreads", NUMBER_OF_THREADS);
                MAXIMUM_TERM_LENGTH = config.getInt("api.maximumTermLength", MAXIMUM_TERM_LENGTH);
                DATABASE = config.getString("api.database", DATABASE);
                DATABASE_USERNAME = config.getString("api.databaseConnectionURL", DATABASE_CONNECTION_URL);
                DATABASE_USERNAME = config.getString("api.databaseUsername", DATABASE_USERNAME);
                DATABASE_PASSWORD = config.getString("api.databasePassword", DATABASE_PASSWORD);
                FIS_HASHTAG_FILE_NAME = config.getString("api.fisHashtagFileName", FIS_HASHTAG_FILE_NAME);
                HASHTAG_FILE_NAME = config.getString("api.hashtagFileName", HASHTAG_FILE_NAME);
                NGRAM_CANDIDATE_FILE_NAME = config.getString("api.ngramCandidateFileName", NGRAM_CANDIDATE_FILE_NAME);
                COMPLAINT_HASHTAG_FILE_NAME = config.getString("api.complaintHashtagFileName", COMPLAINT_HASHTAG_FILE_NAME);
                COMPLAINT_STEM_FILE_NAME = config.getString("api.complaintStemFileName", COMPLAINT_STEM_FILE_NAME);
                ELASTIC_HOST = config.getString("api.elasticHost", ELASTIC_HOST);
                ELASTIC_PORT = config.getInt("api.elasticPort", ELASTIC_PORT);


            } else {
                logger.warn("Config file <" + configFile.getName() + "> not found");
                logger.warn("Falling back to default hardcoded values.");
            }

        } catch (ConfigurationException cex) {
            logger.warn("Error loading configuration", cex);
            logger.warn("Falling back to default hardcoded values.");
        }
    }

    public static String dumpCurrentConfiguration() {
        return "Active configuration\r\n" + Arrays.stream(Configuration.class.getFields()).map(field -> {
            String fieldName = CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, field.getName());
            try {
                return fieldName + " = " + (fieldName.toLowerCase().contains("password") ? "***********" : field.get(null));
            } catch (IllegalAccessException e) {
                return fieldName + " = N/A";
            }
        }).collect(Collectors.joining("\r\n"));
    }
}
