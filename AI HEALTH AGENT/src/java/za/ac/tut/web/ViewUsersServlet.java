package za.ac.tut.web;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import za.ac.tut.model.User;
import za.ac.tut.util.Database;

public class ViewUsersServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        List<User> userList = new ArrayList<>();

        try {
            try (Connection conn = Database.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT * FROM users ORDER BY id DESC");
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
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

                    userList.add(user);
                }
            }

            // send list to JSP
            HttpSession session = request.getSession();
            session.setAttribute("users", userList);

            response.sendRedirect("list_of_users.jsp");

        } catch (Exception e) {
            throw new ServletException("Unable to load users.", e);
        }
    }
}
