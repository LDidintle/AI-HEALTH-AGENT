package za.ac.tut.web;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import za.ac.tut.util.Database;
import za.ac.tut.util.PatientAccountProcedureService;

public class DeleteUserServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            int id = parseId(request.getParameter("id"));
            if (id <= 0) {
                throw new ServletException("A valid patient ID is required before deleting.");
            }

            Connection conn = null;
            try {
                conn = Database.getConnection();
                conn.setAutoCommit(false);
                PatientAccountProcedureService.deletePatientAccount(conn, id);
                conn.commit();
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

    private int parseId(String value) {
        if (value == null || value.trim().isEmpty()) {
            return -1;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}
