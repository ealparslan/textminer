package com.sikayetvar.textmining.api;

import com.ecyrd.speed4j.StopWatch;
import com.google.common.collect.Lists;
import com.sikayetvar.textmining.api.datalayer.DataOperator;
import com.sikayetvar.textmining.api.datalayer.DataOperatorFactory;
import com.sikayetvar.textmining.api.entity.Complaint;
import com.sikayetvar.textmining.api.entity.ComplaintHashtag;
import com.sikayetvar.textmining.api.entity.Hashtag;
import com.sikayetvar.textmining.api.middle.ServiceOperator;
import com.sikayetvar.textmining.api.util.Configuration;
import com.sikayetvar.textmining.api.util.MemoryWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.sikayetvar.textmining.api.util.Configuration.NOTIFY_ROW_SIZE;

public class ComplaintHashtagger {
    private static final Logger logger = LoggerFactory.getLogger(ComplaintHashtagger.class);
    private AtomicInteger complaintId;

    private List<Complaint> complaints;

    public ComplaintHashtagger(List<Complaint> complaints) {
        this.complaints = complaints;
        this.complaintId = new AtomicInteger(0);
    }

    public void tag(int numberOfThreads) {
        int size = complaints.size() / numberOfThreads + 1;
        List<List<Complaint>> partitions = Lists.partition(complaints, size);

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        for (List<Complaint> partition : partitions) {
            executor.execute(() -> tagComplaints(partition));
        }

        executor.shutdown();
        try {
            while (!executor.awaitTermination(24L, TimeUnit.HOURS)) {
                System.out.println("Not yet. Still waiting for termination");
            }
        } catch (InterruptedException e) {
            logger.error("Error while tagging complaints", e);
            throw new RuntimeException(e);
        }
    }


    private void tagComplaints(List<Complaint> complaintsPartition) {
        List<ComplaintHashtag> partitionHashtags = new ArrayList<>();
        for (int i = 0; i < complaintsPartition.size(); i++) {
            Complaint complaint = complaintsPartition.get(i);
            ServiceOperator serviceOperator = new ServiceOperator();

            List<Hashtag> hashtags = serviceOperator.getHashtagsInMemory(complaint.getCategory(), complaint.getBody(), Configuration.JSON_HASHTAG_TOP_N);

            Collection<ComplaintHashtag> complaintHashtags = hashtags.stream()
                    .map(hashtag -> new ComplaintHashtag(complaintId.incrementAndGet(), complaint.getId(), hashtag.getTerm(), hashtag.getScore())).collect(Collectors.toList());

            partitionHashtags.addAll(complaintHashtags);

            if (i % NOTIFY_ROW_SIZE == 0)
                logger.info(String.format("Document tagging completed for : %1$d of %2$d (%%%3$.2f)", i, complaintsPartition.size(), (float) i / complaintsPartition.size() * 100f));
        }

        DataOperatorFactory.getDataOperator(Configuration.DATABASE).saveComplaintHashtags(partitionHashtags);
        logger.info(complaintsPartition.size() + " Complaint hashtags written.");
    }

    public static void main(String[] args) {
        try {
            StopWatch sw = new StopWatch();
            MemoryWatch mw = new MemoryWatch();

            logger.info("Starting complaint tagging...");
            logger.info(Configuration.dumpCurrentConfiguration());

            DataOperator dataOperator = DataOperatorFactory.getDataOperator(Configuration.DATABASE);

            logger.info(mw.stop("DataOperator").getReadableAmount());
            sw.start();

            List<Complaint> complaints = dataOperator.getComplaints();

            logger.info(mw.stop("getComplaints").getReadableAmount());
            sw.start();

            // delete previous model
            DataOperatorFactory.getDataOperator(Configuration.DATABASE).truncateComplaintHashtags();
            logger.info("Previous model deleted");

            ComplaintHashtagger complaintHashtagger = new ComplaintHashtagger(complaints);
            complaintHashtagger.tag(Configuration.NUMBER_OF_THREADS);

            logger.info(mw.stop("ComplaintHashtagger").getReadableAmount());
            logger.info(sw.toString());
            logger.info("Complaint tagging complete");

            System.exit(0);

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error in main", e);
            System.exit(-1);
        }
    }
}
