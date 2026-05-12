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
import za.ac.tut.util.AuthUtil;

/**
 *
 * @author Mcolisi Sithole
 */
//@WebServlet("/AdminServlet.do")
public class AdminServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = valueOrDefault(request.getParameter("username"), "");
        String password = valueOrDefault(request.getParameter("password"), "");
        String staffUser = config("SMARTHEALTH_STAFF_USER");
        String staffPass = config("SMARTHEALTH_STAFF_PASSWORD");

        if (staffUser != null && staffPass != null
                && staffUser.equals(username) && staffPass.equals(password)) {
            HttpSession session = request.getSession();
            AuthUtil.markAdmin(session);

            response.sendRedirect(request.getContextPath() + "/admin_dashboard.jsp");
        } else {
            response.sendRedirect(request.getContextPath() + "/admin?error=invalid");
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

    private static String config(String name) {
        String property = trimToNull(System.getProperty(name));
        return property != null ? property : trimToNull(System.getenv(name));
    }

}
