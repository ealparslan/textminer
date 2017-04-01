package com.sikayetvar.textmining.api.tests;

import com.sikayetvar.textmining.api.datalayer.DataOperatorFactory;
import com.sikayetvar.textmining.api.entity.Complaint;
import com.sikayetvar.textmining.api.entity.Hashtag;
import com.sikayetvar.textmining.api.middle.ServiceOperator;
import com.sikayetvar.textmining.api.util.Configuration;

import java.util.List;
import java.util.Scanner;

public class ComplaintHastaggerTest {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in, "Cp857");
        ServiceOperator serviceOperator = new ServiceOperator();

        String complaintId = "";

        while (!complaintId.equals("0")) {
            System.out.print("Enter complaint id: ");
            complaintId = scanner.nextLine();

            List<Complaint> complaints = DataOperatorFactory.getDataOperator(Configuration.DATABASE).getComplaintsById(Integer.parseInt(complaintId));
            if (!complaints.isEmpty()) {
                Complaint complaint = complaints.get(0);
                System.out.printf("Complaint id: %s\nSubject: %s\n%s\n\n", complaintId, complaint.getSubject(), complaint.getContent());

                List<Hashtag> hashtags = serviceOperator.getHashtags(complaint.getCategory(), complaint.getBody(), 30);
                for (int i = 0; i < hashtags.size(); i++) {
                    Hashtag hashtag = hashtags.get(i);
                    System.out.printf("%3d %15s:%10.2f %10.2f %10.2f\n", i, hashtag.getTerm(), hashtag.getScore(), hashtag.getTf(), hashtag.getIdf());
                }
            }
        }
    }
}
