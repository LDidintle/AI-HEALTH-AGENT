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
        String address = request.getParameter("address");
        String latitude = request.getParameter("location_latitude");
        String longitude = request.getParameter("location_longitude");

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
        session.setAttribute("cell_no", cell_no);
        session.setAttribute("address", address);
        
        
        RequestDispatcher disp = request.getRequestDispatcher("password.jsp");
        disp.forward(request, response);

    }


}
