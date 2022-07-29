package de.hierlmeier.paxweb8test.mixWabAndWhiteboard;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

public abstract class CounterServletAbstract extends HttpServlet  {

    private void writeRespone(Integer counter, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        try (PrintWriter writer = response.getWriter()) {

            writer.println("{\"count\": "+ counter +"}");
        }
    }
    public final void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(true);
        Integer counter = (Integer) session.getAttribute("counter");
        if(counter == null) {
            counter = 0;
        }
        writeRespone(counter, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(true);
        Integer counter = (Integer) session.getAttribute("counter");
        if(counter == null) {
            counter = 1;
        } else {
            counter = counter + 1;
        }
        session.setAttribute("counter", counter);
        writeRespone(counter, response);
    }
}
