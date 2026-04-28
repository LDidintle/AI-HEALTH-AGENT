package za.ac.tut.web;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import za.ac.tut.model.User;
import za.ac.tut.util.Database;

public class ReadUserServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("read_user.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String email = request.getParameter("email");

        if (email == null || email.trim().isEmpty()) {
            request.setAttribute("errorMessage", "Please enter an email address.");
            request.getRequestDispatcher("read_user.jsp").forward(request, response);
            return;
        }

        String sql = "SELECT id, title, first_name, surname, dob, gender, marital_status, "
                + "email, cell_number, address FROM users WHERE email = ?";

        try {
            try (Connection conn = Database.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setString(1, email.trim());

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        User user = new User();
                        user.setId(rs.getInt("id"));
                        user.setTitle(rs.getString("title"));
                        user.setFirstName(rs.getString("first_name"));
                        user.setSurname(rs.getString("surname"));
                        user.setDob(rs.getDate("dob"));
                        user.setGender(rs.getString("gender"));
                        user.setMaritalStatus(rs.getString("marital_status"));
                        user.setEmail(rs.getString("email"));
                        user.setCellNumber(rs.getString("cell_number"));
                        user.setAddress(rs.getString("address"));

                        request.setAttribute("user", user);
                        request.getRequestDispatcher("read_user_result.jsp").forward(request, response);
                        return;
                    }
                }
            }

            request.setAttribute("errorMessage", "No user was found with that email address.");
            request.setAttribute("searchedEmail", email.trim());
            request.getRequestDispatcher("read_user.jsp").forward(request, response);
        } catch (Exception e) {
            throw new ServletException("Unable to read user details.", e);
        }
    }
}
