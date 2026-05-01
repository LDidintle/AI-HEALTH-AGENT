package za.ac.tut.web;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import za.ac.tut.util.Database;

public class EditUserServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idParam = request.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) {
            response.sendRedirect("ViewUsersServlet.do");
            return;
        }

        try {
            int id = Integer.parseInt(idParam);

            String sql = "SELECT * FROM users WHERE id = ?";
            try (Connection conn = Database.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, id);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        request.setAttribute("id", rs.getInt("id"));
                        request.setAttribute("title", rs.getString("title"));
                        request.setAttribute("first_name", rs.getString("first_name"));
                        request.setAttribute("surname", rs.getString("surname"));
                        request.setAttribute("dob", rs.getDate("dob"));
                        request.setAttribute("gender", rs.getString("gender"));
                        request.setAttribute("marital_status", rs.getString("marital_status"));
                        request.setAttribute("email", rs.getString("email"));
                        request.setAttribute("cell_number", rs.getString("cell_number"));
                        request.setAttribute("id_number", rs.getString("id_number"));
                        request.setAttribute("emergency_contact_name", rs.getString("emergency_contact_name"));
                        request.setAttribute("emergency_contact_number", rs.getString("emergency_contact_number"));
                        request.setAttribute("blood_group", rs.getString("blood_group"));
                        request.setAttribute("known_allergies", rs.getString("known_allergies"));
                        request.setAttribute("chronic_conditions", rs.getString("chronic_conditions"));
                        request.setAttribute("address", rs.getString("address"));
                        request.getRequestDispatcher("edit_user.jsp").forward(request, response);
                        return;
                    }
                }
            }

            response.sendRedirect("ViewUsersServlet.do");
        } catch (Exception e) {
            throw new ServletException("Unable to load user details for editing.", e);
        }
    }
}
