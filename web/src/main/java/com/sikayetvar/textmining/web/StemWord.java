package com.sikayetvar.textmining.web;

import com.sikayetvar.textmining.api.middle.ServiceOperator;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Created by deniz on 1/17/17.
 */

@WebServlet(name = "StemWord", value = "/StemWord")
public class StemWord extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(StemWord.class);

    ServiceOperator serviceOperator = new ServiceOperator();


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        generateResponse(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        generateResponse(request, response);
    }

    protected void generateResponse(HttpServletRequest request, HttpServletResponse response) throws IOException {
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

        String content = request.getParameter("word");

        if (content == null){
            response.getWriter().write("Content empty!");
        }

        try {
            String responseString = serviceOperator.getStem(content);
            if(null == responseString) response.getWriter().write("NA");
            else response.getWriter().write(responseString);
        } catch (Exception e) {
            logger.error("Error in StemWord.generateResponse. Content: " + content , e);
            response.getWriter().write("An error occured!");
        }
    }

}
