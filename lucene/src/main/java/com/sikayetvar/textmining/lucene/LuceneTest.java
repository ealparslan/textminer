package com.sikayetvar.textmining.lucene;

import com.ecyrd.speed4j.StopWatch;
import com.sikayetvar.textmining.api.nlp.ZemberekStemmer;
import com.sikayetvar.textmining.api.nlp.ZemberekStemmerBuilder;
import com.sikayetvar.textmining.api.util.Configuration;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.nio.file.Paths;

/**
 * Created by John on 16.11.2016.
 */
public class LuceneTest {
    private static final Logger logger = LogManager.getLogger("XML_ROLLING_FILE_APPENDER");

    public static void main(String[] args) {
        try {
            logger.info(Configuration.dumpCurrentConfiguration());

            ZemberekStemmer stemmer = ZemberekStemmerBuilder.getInstance().getStemmer();

            Analyzer analyzer = new ZemberekAnalyzer(stemmer);
            // Directory index = new RAMDirectory();
            Directory index = FSDirectory.open(Paths.get("data"));
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            IndexWriter w = new IndexWriter(index, config);

            StopWatch sw = new StopWatch();
            logger.info("Creating index");
            /**
             * Create a field with term vector enabled
             */
            FieldType indexFieldType = new FieldType();
            indexFieldType.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
            indexFieldType.setStored(true);
            indexFieldType.setStoreTermVectors(true);
            indexFieldType.setTokenized(true);
            indexFieldType.setStoreTermVectorOffsets(true);

            Document doc = new Document();
            doc.add(new StoredField("id", 123));
            doc.add(new StringField("category", "555", Field.Store.YES));
            doc.add(new StringField("subject", "Dry ve ??stanbul ??zinsiz ??ekilen ??yelik ??creti!", Field.Store.YES));
            doc.add(new Field("content", "Kart??mdan hafta ??yelik ??creti ??ekim i??in onay vermedi??imi ve ??yeli??iminde iptal edilmesini istememe ra??men 14/03/2014 tarihinde 5 taksit olarak 100 tl tutar??nda ??ekim yap??lm????. M????teri ??ikayeti ve mail ile ula??t??????m Dry ??stanbul yetkilileri taraf??m?? arayarak ??cretin iade edilece??ini belirttiler (17/03/2014)\n" +
                    "\n" +
                    "Belirtilen tarihin ??zerinde 1 ay ge??mi?? olmas??na ra??men iade edilen tutar bulunmuyor ve taksitlendirme ekstremde g??r??nmeye devam ediyor. Tutar??n iadesini talep ediyorum.", indexFieldType));
            w.addDocument(doc);

            logger.info(sw.stop("Create Index").toString());
            logger.info("Indexing complete");
            w.close();

           /* String querystr = args.length > 0 ? args[0] : "telefon";

            // the "title" arg specifies the default field to use
            // when no field is explicitly specified in the query.
            Query q = new QueryParser("content", analyzer).parse(querystr);

            // 3. search
            int hitsPerPage = 10;
            IndexReader reader = DirectoryReader.open(index);
            IndexSearcher searcher = new IndexSearcher(reader);
            TopDocs docs = searcher.search(q, hitsPerPage);
            ScoreDoc[] hits = docs.scoreDocs;

            // 4. display results
            System.out.println("Found " + hits.length + " hits.");
            for (int i = 0; i < hits.length; ++i) {
                int docId = hits[i].doc;
                Document d = searcher.doc(docId);
                System.out.println((i + 1) + ". " + d.get("subject") + "\n" + d.get("content") + "\n");
                System.out.println("==============================================================");
            }

            // reader can only be closed when there
            // is no need to access the documents any more.
            reader.close();*/
        } catch (Throwable e) {
            e.printStackTrace();
            logger.error(e.getMessage(), e);
        } finally {
            System.exit(0);
        }
    }
}
