package com.sikayetvar.textmining.poc;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.primitives.Doubles;
import com.medallia.word2vec.Searcher;
import com.medallia.word2vec.Word2VecModel;
import com.medallia.word2vec.neuralnetwork.NeuralNetworkType;
import com.medallia.word2vec.thrift.Word2VecModelThrift;
import com.medallia.word2vec.util.Common;
import com.medallia.word2vec.util.Format;
import com.medallia.word2vec.util.Strings;
import com.medallia.word2vec.util.ThriftUtils;
import com.sikayetvar.textmining.api.HashtagBuilder;
import com.sikayetvar.textmining.api.datalayer.DataOperatorFactory;
import com.sikayetvar.textmining.api.entity.Document;
import com.sikayetvar.textmining.api.nlp.ZemberekStemmer;
import com.sikayetvar.textmining.api.nlp.ZemberekStemmerBuilder;
import com.sikayetvar.textmining.api.util.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.thrift.TException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Word2vec {
    private static final Logger logger = LogManager.getLogger(HashtagBuilder.class);
    public static final String MODEL_OUTPUT_FILE_PATH = "out/sikayetvar.model";

    public static void main(String[] args) {
        try {
            Word2vec word2vec = new Word2vec();
            Word2VecModel model = null;
            // check if model exists
            try {
                model = word2vec.loadModel();
            } catch (Throwable e) {
                logger.warn("Existing model could not found.", e);
            }
            if (model == null)
                model = word2vec.buildModel();

            word2vec.analogy(model.forSearch());
            System.exit(0);

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error in main", e);
        } finally {
            System.exit(-1);
        }
    }

    public Word2VecModel buildModel() throws IOException, TException, InterruptedException, Searcher.UnknownWordException {
        logger.info("Loading complaints...");
        List<?> complaints = DataOperatorFactory.getDataOperator(Configuration.DATABASE).getComplaints();
        logger.info("Loading completed. Total complaints: " + complaints.size());
        ZemberekStemmer stemmer = ZemberekStemmerBuilder.getInstance().getStemmer();
        logger.info("Starting parser...");
        DocumentParser documentParser = new DocumentParser((List<Document>) complaints, stemmer);
        documentParser.parse();
        logger.info("Parsing completed.");

        Word2VecModel model = Word2VecModel.trainer()
                .setMinVocabFrequency(5)
                .useNumThreads(8)
                .setWindowSize(8)
                .type(NeuralNetworkType.CBOW)
                .setLayerSize(200)
                .useNegativeSamples(25)
                .setDownSamplingRate(1e-4)
                .setNumIterations(5)
                .setListener((stage, progress) -> System.out.println(String.format("%s is %.2f%% complete", Format.formatEnum(stage), progress * 100)))
                .train(documentParser.getSentences());

        // Writes model to a thrift file
        logger.info("Writing output to file");
        // FileUtils.writeStringToFile(new File(MODEL_OUTPUT_FILE_PATH), ThriftUtils.serializeJson(model.toThrift()), Charsets.UTF_8);
        model.toBinFile(Files.newOutputStream(Paths.get(MODEL_OUTPUT_FILE_PATH)));
        return model;
    }

    /**
     * Loads a model and allows user to find similar words
     */
    public Word2VecModel loadModel() throws IOException, TException, Searcher.UnknownWordException {
        final Word2VecModel model;
        logger.info("Loading model");
        String json = new String(Files.readAllBytes(Paths.get(MODEL_OUTPUT_FILE_PATH)), StandardCharsets.UTF_8);
        model = Word2VecModel.fromThrift(ThriftUtils.deserializeJson(new Word2VecModelThrift(), json));
        // model = Word2VecModel.fromBinFile(new File(MODEL_OUTPUT_FILE_PATH));
        // model = Word2VecBuilder.fromBinFile(new File(MODEL_OUTPUT_FILE_PATH));
        return model;
    }

    /**
     * Example using Skip-Gram model
     */
    public void skipGram() throws IOException, TException, InterruptedException, Searcher.UnknownWordException {
        List<String> read = Common.readToList(new File("out/sents.cleaned.word2vec.txt"));
        List<List<String>> partitioned = Lists.transform(read, new Function<String, List<String>>() {
            @Override
            public List<String> apply(String input) {
                return Arrays.asList(input.split(" "));
            }
        });

        Word2VecModel model = Word2VecModel.trainer()
                .setMinVocabFrequency(100)
                .useNumThreads(8)
                .setWindowSize(7)
                .type(NeuralNetworkType.SKIP_GRAM)
                .useHierarchicalSoftmax()
                .setLayerSize(300)
                .useNegativeSamples(0)
                .setDownSamplingRate(1e-3)
                .setNumIterations(5)
                .setListener((stage, progress) -> System.out.println(String.format("%s is %.2f%% complete", Format.formatEnum(stage), progress * 100)))
                .train(partitioned);

        logger.info("Writing output to file");
        FileUtils.writeStringToFile(new File("out/300layer.8threads.5iter.model"), ThriftUtils.serializeJson(model.toThrift()));

        interact(model.forSearch());
    }

    private void interact(Searcher searcher) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                System.out.print("Enter word or sentence (EXIT to break): ");
                String word = br.readLine();
                if (word.equals("EXIT")) {
                    break;
                }
                try {
                    List<Searcher.Match> matches = searcher.getMatches(word, 20);
                    System.out.println(Strings.joinObjects("\n", matches));
                } catch (Searcher.UnknownWordException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void analogy(Searcher searcher) throws Searcher.UnknownWordException, IOException {
        ZemberekStemmer stemmer = ZemberekStemmerBuilder.getInstance().getStemmer();

        try (Scanner scanner = new Scanner(System.in, "Cp857")) {
            while (true) {
                try {
                    System.out.print("Enter formula (e.g. v1-v2+v3) (EXIT to break): ");
                    String formula = scanner.nextLine();
                    if (formula.equals("EXIT")) {
                        break;
                    }
                    // stem formula
                    formula = VectorOperator.stemFormula(stemmer, formula);
                    System.out.println("Formula translated into: " + formula);
                    ImmutableList<Double> vectors = VectorOperator.calculate(formula, searcher);
                    List<Searcher.Match> matches = searcher.getMatches(Doubles.toArray(vectors), 20);
                    System.out.println(Strings.joinObjects("\n", matches));
                } catch (Exception e) {
                    logger.error("Error", e);
                }
            }
        }
    }
}