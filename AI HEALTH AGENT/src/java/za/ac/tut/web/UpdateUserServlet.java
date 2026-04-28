package za.ac.tut.web;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import za.ac.tut.util.Database;

public class UpdateUserServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            int id = Integer.parseInt(request.getParameter("id"));

            String sql = "UPDATE users SET title=?, first_name=?, surname=?, dob=?, gender=?, marital_status=?, email=?, cell_number=?, address=? WHERE id=?";

            try (Connection conn = Database.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, request.getParameter("title"));
                ps.setString(2, request.getParameter("first_name"));
                ps.setString(3, request.getParameter("surname"));
                ps.setString(4, request.getParameter("dob"));
                ps.setString(5, request.getParameter("gender"));
                ps.setString(6, request.getParameter("marital_status"));
                ps.setString(7, request.getParameter("email"));
                ps.setString(8, request.getParameter("cell_number"));
                ps.setString(9, request.getParameter("address"));
                ps.setInt(10, id);

                ps.executeUpdate();
            }

            response.sendRedirect("ViewUsersServlet.do");

        } catch (Exception e) {
            throw new ServletException("Unable to update user details.", e);
        }
    }
}

