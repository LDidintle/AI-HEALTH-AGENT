package za.ac.tut.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import za.ac.tut.util.Database;
import za.ac.tut.util.JsonUtil;

public class MobileProfileServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(response, "{\"success\":false,\"message\":\"No active user session.\"}");
            return;
        }

        String email = String.valueOf(session.getAttribute("user"));

        try {
            try (Connection conn = Database.getConnection()) {

                String sql = "SELECT id, first_name, surname, title, gender, cell_number, is_verified "
                        + "FROM users WHERE email = ?";

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, email);

                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                            writeJson(response, "{\"success\":false,\"message\":\"User was not found.\"}");
                            return;
                        }

                        String json = "{"
                                + "\"success\":true,"
                                + "\"user\":{"
                                + "\"id\":" + rs.getInt("id") + ","
                                + "\"email\":" + JsonUtil.quote(email) + ","
                                + "\"title\":" + JsonUtil.quote(rs.getString("title")) + ","
                                + "\"firstName\":" + JsonUtil.quote(rs.getString("first_name")) + ","
                                + "\"surname\":" + JsonUtil.quote(rs.getString("surname")) + ","
                                + "\"gender\":" + JsonUtil.quote(rs.getString("gender")) + ","
                                + "\"cellNumber\":" + JsonUtil.quote(rs.getString("cell_number")) + ","
                                + "\"isVerified\":" + rs.getBoolean("is_verified")
                                + "}"
                                + "}";

                        writeJson(response, json);
                    }
                }
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJson(response, "{\"success\":false,\"message\":\"Unable to load profile.\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(response, "{\"success\":false,\"message\":\"No active user session.\"}");
            return;
        }

        String title = valueOrDefault(trimToNull(request.getParameter("title")), "Patient");
        String firstName = trimToNull(request.getParameter("firstName"));
        String surname = trimToNull(request.getParameter("surname"));
        String gender = valueOrDefault(trimToNull(request.getParameter("gender")), "Not specified");
        String cellNumber = valueOrDefault(trimToNull(request.getParameter("cellNumber")), "");

        if (firstName == null || surname == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(response, "{\"success\":false,\"message\":\"firstName and surname are required.\"}");
            return;
        }

        try {
            try (Connection conn = Database.getConnection()) {
                String sql = "UPDATE users SET title = ?, first_name = ?, surname = ?, gender = ?, cell_number = ? WHERE id = ?";

                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, title);
                    ps.setString(2, firstName);
                    ps.setString(3, surname);
                    ps.setString(4, gender);
                    ps.setString(5, cellNumber);
                    ps.setInt(6, Integer.parseInt(String.valueOf(session.getAttribute("userId"))));
                    ps.executeUpdate();
                }

                writeJson(response, "{\"success\":true,\"message\":\"Profile updated.\"}");
            }
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJson(response, "{\"success\":false,\"message\":\"Unable to update profile.\"}");
        }
    }

    private String valueOrDefault(String value, String fallback) {
        return value == null ? fallback : value;
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private void writeJson(HttpServletResponse response, String json) throws IOException {
        try (PrintWriter out = response.getWriter()) {
            out.write(json);
        }
    }
}
