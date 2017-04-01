package com.sikayetvar.textmining.api.tests;

import com.ecyrd.speed4j.StopWatch;
import com.google.common.net.UrlEscapers;
import com.sikayetvar.textmining.api.datalayer.DataOperatorFactory;
import com.sikayetvar.textmining.api.entity.Complaint;
import com.sikayetvar.textmining.api.util.Configuration;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;

public class RestApiTest {

    public static final int SAMPLE_SIZE = 1000;

    public static void main(String[] args) {
        try {
            List<Complaint> complaints = DataOperatorFactory.getDataOperator(Configuration.DATABASE).getComplaints();

            Collections.shuffle(complaints);

            complaints = complaints.subList(0, SAMPLE_SIZE);

            // DB method
            StopWatch stopWatch = new StopWatch();
            for (int i = 0; i < SAMPLE_SIZE; i++) {
                post("http://localhost:8080/GetHashtags", complaints.get(i));
            }
            stopWatch.stop();
            System.out.println("DB Method: " + stopWatch.toString(SAMPLE_SIZE));

            /*// IM method
            stopWatch = new StopWatch();
            for (int i = 0; i < SAMPLE_SIZE; i++) {
                post("http://localhost:8080/GetHashtagsIM", complaints.get(i).getBody());
            }
            stopWatch.stop();
            System.out.println("IM Method: " + stopWatch.toString(SAMPLE_SIZE));*/

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            System.exit(0);
        }
    }

    public static String post(String urlString, Complaint complaint) throws IOException {
        URL obj = new URL(urlString);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        con.setRequestMethod("POST");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        String params = "companyID=" + UrlEscapers.urlFormParameterEscaper().escape(complaint.getCategory()) + "&content=" + UrlEscapers.urlFormParameterEscaper().escape(complaint.getBody());
        wr.writeBytes(params);
        wr.flush();
        wr.close();

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), "UTF-8"));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        System.out.println(complaint + " : " + response);

        return response.toString();
    }
}
