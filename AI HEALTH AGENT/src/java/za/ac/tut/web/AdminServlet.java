/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.ac.tut.web;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Mcolisi Sithole
 */
//@WebServlet("/AdminServlet.do")
public class AdminServlet extends HttpServlet {
    private static final String STAFF_USER = trimToNull(System.getenv("SMARTHEALTH_STAFF_USER"));
    private static final String STAFF_PASS = trimToNull(System.getenv("SMARTHEALTH_STAFF_PASSWORD"));

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = valueOrDefault(request.getParameter("username"), "");
        String password = valueOrDefault(request.getParameter("password"), "");

        if (STAFF_USER != null && STAFF_PASS != null
                && STAFF_USER.equals(username) && STAFF_PASS.equals(password)) {
            HttpSession session = request.getSession();
            session.setAttribute("admin", "true");

            response.sendRedirect("admin_dashboard.jsp");
        } else {
            response.sendRedirect("error.jsp");
        }
        
    }

    private static String valueOrDefault(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }

    private static String trimToNull(String value) {
        String trimmed = valueOrDefault(value, "");
        return trimmed.isEmpty() ? null : trimmed;
    }

}
