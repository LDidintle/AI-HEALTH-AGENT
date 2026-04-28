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

                String sql = "SELECT id, first_name, surname, title, gender, cell_number "
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
                                + "\"cellNumber\":" + JsonUtil.quote(rs.getString("cell_number"))
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

    private void writeJson(HttpServletResponse response, String json) throws IOException {
        try (PrintWriter out = response.getWriter()) {
            out.write(json);
        }
    }
}
