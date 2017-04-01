package com.sikayetvar.textmining.web;

import com.sikayetvar.textmining.api.middle.HttpGenerator;
import com.sikayetvar.textmining.api.middle.ServiceOperator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "GetHashtags", value = "/GetHashtags")
public class GetHashtags extends HttpServlet {

    private static final Logger logger = LoggerFactory.getLogger(GetHashtags.class);

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

        String content = request.getParameter("content");
        String categoryId = request.getParameter("companyID");
        if (content == null)
            response.getWriter().write("An error occured!");

        try {
            HttpGenerator httpGenerator = new HttpGenerator(serviceOperator);
            String responseString = httpGenerator.getEndHashtagJson(categoryId, content);
            response.getWriter().write((responseString != null) ? responseString : "An error occured!");
        } catch (IOException e) {
            logger.error("Parameters: " + content + " / " + categoryId + "\n" + e.getMessage());
            response.getWriter().write("An error occured!");
        }
    }
}
