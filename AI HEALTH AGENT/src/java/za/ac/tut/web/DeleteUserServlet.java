package za.ac.tut.web;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import za.ac.tut.util.Database;

public class DeleteUserServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            int id = Integer.parseInt(request.getParameter("id"));

            Connection conn = null;
            try {
                conn = Database.getConnection();
                PreparedStatement deleteAuthPs = conn.prepareStatement("DELETE FROM user_auth WHERE user_id = ?");
                PreparedStatement deleteUserPs = conn.prepareStatement("DELETE FROM users WHERE id = ?");

                conn.setAutoCommit(false);

                deleteAuthPs.setInt(1, id);
                deleteAuthPs.executeUpdate();

                deleteUserPs.setInt(1, id);
                deleteUserPs.executeUpdate();

                conn.commit();
                deleteAuthPs.close();
                deleteUserPs.close();
                conn.close();
            } catch (Exception e) {
                if (conn != null) {
                    try {
                        conn.rollback();
                        conn.close();
                    } catch (SQLException rollbackException) {
                        throw new ServletException("Unable to rollback failed delete operation.", rollbackException);
                    }
                }
                throw e;
            }

            response.sendRedirect("ViewUsersServlet.do");

        } catch (Exception e) {
            throw new ServletException("Unable to delete user.", e);
        }
    }
}
