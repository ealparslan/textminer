package com.sikayetvar.textmining.web;

import com.sikayetvar.textmining.api.middle.ServiceOperator;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import static org.elasticsearch.common.xcontent.XContentFactory.jsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sikayetvar.textmining.api.util.Configuration;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import static com.sikayetvar.textmining.api.util.Configuration.JSON_HASHTAG_TOP_N;

/**
 * Created by deniz on 2/17/17.
 * This service gets a complaint string and finds stems.
 * Then it stashes thE complaint string into the elasticsearch complaint_stems index
 */

@WebServlet(name = "IndexComplaint", value = "/IndexComplaint")
public class IndexComplaint extends HttpServlet{

    TransportClient client;
    private static final Logger logger = LoggerFactory.getLogger(IndexComplaint.class);


    @Override
    public void init() throws ServletException {
        try {
            client = new PreBuiltTransportClient(Settings.EMPTY).addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(Configuration.ELASTIC_HOST),Configuration.ELASTIC_PORT));
        } catch (UnknownHostException e) {
            logger.error("IndexComplaint: Could not create transport client to elastic node! /n" + e.getMessage());
        }
    }

    @Override
    public void destroy() {
        client.close();
    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        generateResponse(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        generateResponse(request, response);
    }

    protected void generateResponse(HttpServletRequest request, HttpServletResponse response) throws IOException{
        ServiceOperator serviceOperator = new ServiceOperator();
        RestStatus status = null;
        String returnVal = "";

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        request.setCharacterEncoding("UTF-8");

        String reload = request.getParameter("reload");
        if (null != reload){
            if (serviceOperator.reload())
                response.getWriter().write("Service Reloaded");
            else
                response.getWriter().write("Problem occured in reload!");
            return;
        }

        String content = request.getParameter("content");
        String complaint_id = request.getParameter("complaint_id");
        String company_id = request.getParameter("company_id");
        int publish_time = 0;
        try {
            publish_time = Integer.valueOf(request.getParameter("publish_time"));
        } catch (NumberFormatException e) {
            logger.error("IndexComplaint: Publish time conversion error! /n" + e.getMessage());
            response.getWriter().write("An error occured!");
            return;
        }
        if (content == null || complaint_id == null || company_id == null || publish_time == 0){
            response.getWriter().write("Content OR complaint_id OR company_id OR publish_time empty!");
            return;
        }

        String json = "";
        try {
            json = jsonBuilder().startObject().field("complaint_id", complaint_id).field("company_id", company_id).field("publish_time", publish_time).field("hashtags", serviceOperator.getStemOnlyHashtags(company_id,content,JSON_HASHTAG_TOP_N)).field("nonhashtags", serviceOperator.getStems(content)).endObject().string();
            IndexResponse ixresponse = client.prepareIndex("complaint_stems", "logs").setSource(json).get();
            status = ixresponse.status();
        } catch (IOException e) {
            logger.error("IndexComplaint Complaint_id: " + complaint_id + "\n" + e.getMessage());
            response.getWriter().write("An error occured!");
            return;
        }

        if(null != status) returnVal = status.name();

        try {
            response.getWriter().write(returnVal);
        } catch (IOException e) {
            logger.error("IndexComplaint: Could not write response! /n" + e.getMessage());
            response.getWriter().write("An error occured!");
        }

    }
}


