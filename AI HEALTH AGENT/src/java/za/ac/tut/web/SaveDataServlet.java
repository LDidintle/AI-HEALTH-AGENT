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
        
        String title = request.getParameter("title");
        String name = request.getParameter("first_name");
        String surname = request.getParameter("surname");
        String dob = request.getParameter("dob");
        String gender = request.getParameter("gender");
        String status = request.getParameter("status");
        String email = request.getParameter("email");
        String cell_no = request.getParameter("cell_number");
        String idNumber = request.getParameter("id_number");
        String emergencyContactName = request.getParameter("emergency_contact_name");
        String emergencyContactNumber = request.getParameter("emergency_contact_number");
        String bloodGroup = request.getParameter("blood_group");
        String knownAllergies = request.getParameter("known_allergies");
        String chronicConditions = request.getParameter("chronic_conditions");
        String address = request.getParameter("address");
        String latitude = request.getParameter("location_latitude");
        String longitude = request.getParameter("location_longitude");

        if (!PatientValidation.isValidIdNumber(idNumber)) {
            reject(request, response, "Please enter a valid 13 digit South African ID number.");
            return;
        }

        if (!PatientValidation.isValidSouthAfricanPhone(cell_no)) {
            reject(request, response, "Please enter a valid South African personal cell number.");
            return;
        }

        if (!PatientValidation.isValidSouthAfricanPhone(emergencyContactNumber)) {
            reject(request, response, "Please enter a valid South African emergency contact number.");
            return;
        }

        if (PatientValidation.samePhone(cell_no, emergencyContactNumber)) {
            reject(request, response, "Personal number and emergency contact number must not be the same.");
            return;
        }

        if (address != null && latitude != null && longitude != null
                && !latitude.trim().isEmpty() && !longitude.trim().isEmpty()
                && !address.contains("Device location:")) {
            address = address + " (Device location: " + latitude.trim() + ", " + longitude.trim() + ")";
        }
        
        session.setAttribute("title", title);
        session.setAttribute("name", name);
        session.setAttribute("surname", surname);
        session.setAttribute("dob", dob);
        session.setAttribute("gender", gender);
        session.setAttribute("status", status);
        session.setAttribute("email", email);
        session.setAttribute("cell_no", PatientValidation.normalizePhone(cell_no));
        session.setAttribute("id_number", idNumber.trim());
        session.setAttribute("emergency_contact_name", emergencyContactName);
        session.setAttribute("emergency_contact_number", PatientValidation.normalizePhone(emergencyContactNumber));
        session.setAttribute("blood_group", bloodGroup);
        session.setAttribute("known_allergies", knownAllergies);
        session.setAttribute("chronic_conditions", chronicConditions);
        session.setAttribute("address", address);
        
        
        RequestDispatcher disp = request.getRequestDispatcher("password.jsp");
        disp.forward(request, response);

    }

    private void reject(HttpServletRequest request, HttpServletResponse response, String message)
            throws ServletException, IOException {
        request.setAttribute("error", message);
        request.getRequestDispatcher("welcome.html").forward(request, response);
    }

}
