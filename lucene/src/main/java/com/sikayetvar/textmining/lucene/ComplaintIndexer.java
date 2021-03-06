package com.sikayetvar.textmining.lucene;

import com.ecyrd.speed4j.StopWatch;
import com.sikayetvar.textmining.api.datalayer.DataOperatorFactory;
import com.sikayetvar.textmining.api.entity.Complaint;
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
import java.util.List;

/**
 * Created by John on 16.11.2016.
 */
public class ComplaintIndexer {
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
            logger.info("Getting complaints from database");
            List<Complaint> complaints = DataOperatorFactory.getDataOperator(Configuration.DATABASE).getComplaints();
            logger.info(sw.stop("getComplaints").toString());
            logger.info("Complaint getting finished. Total complaints to be indexed:" + complaints.size());

            sw.start();
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

            for (int i = 0; i < complaints.size(); i++) {
                Complaint complaint = complaints.get(i);
                Document doc = new Document();
                doc.add(new StoredField("id", complaint.getId()));
                doc.add(new StringField("category", complaint.getCategory(), Field.Store.YES));
                doc.add(new StringField("subject", complaint.getSubject(), Field.Store.YES));
                doc.add(new Field("content", complaint.getContent(), indexFieldType));
                w.addDocument(doc);
                if (i % Configuration.NOTIFY_ROW_SIZE == 0)
                    logger.info(String.format("%1$d complaints indexed of %2$d (%%%3$.2f)", i, complaints.size(), (float) i / complaints.size() * 100f));
            }

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
