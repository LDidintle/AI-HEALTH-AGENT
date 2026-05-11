/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package za.ac.tut.web;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import za.ac.tut.util.PatientValidation;

/**
 *
 * @author Mcolisi Sithole
 */
public class SaveDataServlet extends HttpServlet {


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(true);
        
        String title = trimToDefault(request.getParameter("title"), "Patient");
        String name = trimToNull(request.getParameter("first_name"));
        String surname = trimToNull(request.getParameter("surname"));
        String email = trimToNull(request.getParameter("email"));
        String dob = trimToNull(request.getParameter("dob"));

        if (isBlank(name) || isBlank(surname) || isBlank(email) || isBlank(dob)) {
            reject(request, response, "Please enter your name, surname, email address, and date of birth.");
            return;
        }

        if (!email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            reject(request, response, "Please enter a valid email address.");
            return;
        }

        if (!PatientValidation.isValidDateOfBirth(dob)) {
            reject(request, response, "Date of birth must be a real past date.");
            return;
        }
	        
        session.setAttribute("title", title);
        session.setAttribute("name", name);
        session.setAttribute("surname", surname);
        session.setAttribute("email", email);
        session.setAttribute("dob", dob);
        
        
        RequestDispatcher disp = request.getRequestDispatcher("password.jsp");
        disp.forward(request, response);

    }

    private void reject(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        request.setAttribute("error", message);
        request.getRequestDispatcher("welcome.html").forward(request, response);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String trimToDefault(String value, String fallback) {
        String trimmed = trimToNull(value);
        return trimmed == null ? fallback : trimmed;
    }

}
