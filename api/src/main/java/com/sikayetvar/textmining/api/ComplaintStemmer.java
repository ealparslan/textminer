package com.sikayetvar.textmining.api;

import com.ecyrd.speed4j.StopWatch;
import com.google.common.collect.Lists;
import com.sikayetvar.textmining.api.datalayer.DataOperator;
import com.sikayetvar.textmining.api.datalayer.DataOperatorFactory;
import com.sikayetvar.textmining.api.entity.Complaint;
import com.sikayetvar.textmining.api.entity.ComplaintStems;
import com.sikayetvar.textmining.api.middle.ServiceOperator;
import com.sikayetvar.textmining.api.util.Configuration;
import com.sikayetvar.textmining.api.util.MemoryWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.sikayetvar.textmining.api.util.Configuration.NOTIFY_ROW_SIZE;

/**
 * Created by deniz on 2/14/17.
 */
public class ComplaintStemmer {

    private static final Logger logger = LoggerFactory.getLogger(ComplaintStemmer.class);
    private AtomicInteger complaintId;

    private List<Complaint> complaints;

    public ComplaintStemmer(List<Complaint> complaints) {
        this.complaints = complaints;
        this.complaintId = new AtomicInteger(0);
    }

    public void stem(int numberOfThreads) {
        int size = complaints.size() / numberOfThreads + 1;
        List<List<Complaint>> partitions = Lists.partition(complaints, size);

        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);

        for (List<Complaint> partition : partitions) {
            executor.execute(() -> stemComplaints(partition));
        }

        executor.shutdown();
        try {
            while (!executor.awaitTermination(24L, TimeUnit.HOURS)) {
                System.out.println("Not yet. Still waiting for termination");
            }
        } catch (InterruptedException e) {
            logger.error("Error while stemming complaints", e);
            throw new RuntimeException(e);
        }
    }

    private void stemComplaints(List<Complaint> complaintsPartition) {
        List<ComplaintStems> partitionStemss = new ArrayList<>();
        for (int i = 0; i < complaintsPartition.size(); i++) {
            Complaint complaint = complaintsPartition.get(i);
            ServiceOperator serviceOperator = new ServiceOperator();

            String stems = serviceOperator.getStems(complaint.getBody());

            partitionStemss.add(new ComplaintStems(complaintId.incrementAndGet(),complaint.getId(),stems));

            if (i % NOTIFY_ROW_SIZE == 0)
                logger.info(String.format("Document stemming completed for : %1$d of %2$d (%%%3$.2f)", i, complaintsPartition.size(), (float) i / complaintsPartition.size() * 100f));
        }

        DataOperatorFactory.getDataOperator(Configuration.DATABASE).saveComplaintStems(partitionStemss);
        logger.info(complaintsPartition.size() + " Complaint stems written.");
    }



    public static void main(String[] args) {
        try {
            StopWatch sw = new StopWatch();
            MemoryWatch mw = new MemoryWatch();

            logger.info("Starting complaint stemming...");
            logger.info(Configuration.dumpCurrentConfiguration());

            DataOperator dataOperator = DataOperatorFactory.getDataOperator(Configuration.DATABASE);

            logger.info(mw.stop("DataOperator").getReadableAmount());
            sw.start();

            List<Complaint> complaints = dataOperator.getComplaints();

            logger.info(mw.stop("getComplaints").getReadableAmount());
            sw.start();

            // delete previous model
            DataOperatorFactory.getDataOperator(Configuration.DATABASE).truncateComplaintStems();
            logger.info("Previous model deleted");

            ComplaintStemmer complaintStemmer = new ComplaintStemmer(complaints);
            complaintStemmer.stem(Configuration.NUMBER_OF_THREADS);

            logger.info(mw.stop("ComplaintStemmer").getReadableAmount());
            logger.info(sw.toString());
            logger.info("Complaint stemming complete");

            System.exit(0);

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error in main", e);
            System.exit(-1);
        }
    }


}
