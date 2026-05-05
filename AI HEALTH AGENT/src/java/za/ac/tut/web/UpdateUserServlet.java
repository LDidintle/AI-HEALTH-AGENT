package za.ac.tut.web;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import za.ac.tut.util.Database;
import za.ac.tut.util.PatientValidation;

public class UpdateUserServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            int id = Integer.parseInt(request.getParameter("id"));
            String cellNumber = request.getParameter("cell_number");
            String emergencyNumber = request.getParameter("emergency_contact_number");
            boolean verified = "true".equals(request.getParameter("is_verified"));
            boolean hasCellNumber = PatientValidation.trimToNull(cellNumber) != null;
            boolean hasEmergencyNumber = PatientValidation.trimToNull(emergencyNumber) != null;

            if ((hasCellNumber && !PatientValidation.isValidSouthAfricanPhone(cellNumber))
                    || (hasEmergencyNumber && !PatientValidation.isValidSouthAfricanPhone(emergencyNumber))
                    || PatientValidation.samePhone(cellNumber, emergencyNumber)) {
                throw new ServletException("Phone numbers must be valid South African numbers and must not be the same.");
            }

            String sql = "UPDATE users SET title=?, first_name=?, surname=?, dob=?, gender=?, marital_status=?, "
                    + "email=?, cell_number=?, id_number=?, emergency_contact_name=?, emergency_contact_number=?, "
                    + "blood_group=?, known_allergies=?, chronic_conditions=?, address=?, is_verified=? WHERE id=?";

            try (Connection conn = Database.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, request.getParameter("title"));
                ps.setString(2, request.getParameter("first_name"));
                ps.setString(3, request.getParameter("surname"));
                ps.setString(4, request.getParameter("dob"));
                ps.setString(5, request.getParameter("gender"));
                ps.setString(6, request.getParameter("marital_status"));
                ps.setString(7, request.getParameter("email"));
                ps.setString(8, PatientValidation.normalizePhone(cellNumber));
                ps.setString(9, request.getParameter("id_number"));
                ps.setString(10, request.getParameter("emergency_contact_name"));
                ps.setString(11, PatientValidation.normalizePhone(emergencyNumber));
                ps.setString(12, request.getParameter("blood_group"));
                ps.setString(13, request.getParameter("known_allergies"));
                ps.setString(14, request.getParameter("chronic_conditions"));
                ps.setString(15, request.getParameter("address"));
                ps.setBoolean(16, verified);
                ps.setInt(17, id);

                ps.executeUpdate();
            }

            response.sendRedirect("ViewUsersServlet.do");

        } catch (Exception e) {
            throw new ServletException("Unable to update user details.", e);
        }
    }
}

