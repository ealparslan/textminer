package com.sikayetvar.textmining.poc;

import com.medallia.word2vec.Word2VecModel;
import com.medallia.word2vec.neuralnetwork.NeuralNetworkType;
import com.medallia.word2vec.util.Format;
import com.sikayetvar.textmining.api.entity.SimpleDocument;
import com.sikayetvar.textmining.api.nlp.ZemberekStemmer;
import info.bliki.wiki.dump.WikiXMLParser;
import info.bliki.wiki.filter.PlainTextConverter;
import info.bliki.wiki.model.WikiModel;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class WikiParser {
    private static final Logger logger = LogManager.getLogger(WikiParser.class);
    public static final String MODEL_OUTPUT_FILE_PATH = "out/trwiki.model";

    public static void main(String[] args) {
        try {
            if (args.length < 1) {
                System.out.println("Enter xml dump");
                System.exit(-1);
            }
            String xmlDumpFile = args[0];

            // ZemberekStemmer stemmer = ZemberekStemmerBuilder.getInstance().getStemmer();
            ZemberekStemmer stemmer = null;
            DocumentList documentList = new DocumentList();

            Thread parserThread = new Thread(() -> {
                try {
                    WikiModel wikiModel = new WikiModel("https://tr.wikipedia.org/wiki/${image}", "https://tr.wikipedia.org/wiki/${title}");
                    WikiXMLParser parser = new WikiXMLParser(new File(xmlDumpFile), (article, siteinfo) -> {
                        try {
                            String plainText = wikiModel.render(new PlainTextConverter(), article.getText());
                            DocumentParser documentParser = new DocumentParser(new SimpleDocument(plainText), stemmer);
                            documentParser.parse();
                            for (List<String> sentences : documentParser.getSentences()) {
                                documentList.addDocument(sentences);
                            }
                        } catch (Exception e) {
                            logger.warn("Error [" + e.getMessage() + "] in parsing pages: " + article.getTitle());
                        }
                    });
                    parser.parse();
                    logger.info("Parsing completed.");
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error(" Exception in parser", e);
                } finally {
                    documentList.finishProcessing();
                }
            }, "parserThread");

            parserThread.start();
            logger.info("Parser started...");

            Thread modelThread = new Thread(() -> {
                try {
                    Word2VecModel model = Word2VecModel.trainer()
                            .setMinVocabFrequency(5)
                            .useNumThreads(8)
                            .setWindowSize(8)
                            .type(NeuralNetworkType.CBOW)
                            .setLayerSize(400)
                            .useNegativeSamples(25)
                            .setDownSamplingRate(1e-4)
                            .setNumIterations(5)
                            .setListener((stage, progress) -> System.out.println(String.format("%s is %.2f%% complete", Format.formatEnum(stage), progress * 100)))
                            .train(documentList);

                    logger.info("Model building completed.");

                    // Writes model to a thrift file
                    // FileUtils.writeStringToFile(new File(MODEL_OUTPUT_FILE_PATH), ThriftUtils.serializeJson(model.toThrift()), Charsets.UTF_8);
                    OutputStream outputStream = Files.newOutputStream(Paths.get(MODEL_OUTPUT_FILE_PATH));
                    model.toBinFile(outputStream);
                    outputStream.close();
                    logger.info("Model file written to file:" + MODEL_OUTPUT_FILE_PATH);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error("Error in model", e);
                }
            }, "modelThread");

            modelThread.start();
            logger.info("Model started...");

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error in main", e);
        }
    }
}
