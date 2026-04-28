package za.ac.tut.web;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import za.ac.tut.util.Database;

//@WebServlet("/SavePasswordServlet.do")
public class SavePasswordServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");
         try (Connection conn = Database.getConnection()) {

            if (conn != null && !conn.isClosed()) {
                response.getWriter().println("<h2 style='color:green;'>Database connected successfully.</h2>");
            } else {
                response.getWriter().println("<h2 style='color:red;'>Connection failed.</h2>");
            }

        } catch (SQLException e) {
            e.printStackTrace();

            response.getWriter().println("<h2 style='color:red;'>Database connection failed.</h2>");
            response.getWriter().println("<pre>" + e.getMessage() + "</pre>");
        }
    }
}
