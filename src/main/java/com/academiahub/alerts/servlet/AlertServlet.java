package com.academiahub.alerts.servlet;

import com.academiahub.alerts.dao.AlertDao;
import com.academiahub.alerts.model.Alert;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.List;

public class AlertServlet extends HttpServlet {
    private AlertDao alertDao = new AlertDao();

    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    try {
        List<Alert> alerts = alertDao.listAll();  
        req.setAttribute("alerts", alerts);
        RequestDispatcher rd = req.getRequestDispatcher("alerts.jsp");
        rd.forward(req, resp);
    } catch (Exception e) {
        throw new ServletException(e);
    }
}

}
