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
import za.ac.tut.util.DeviceCapabilityService;
import za.ac.tut.util.JsonUtil;

public class MobileDeviceCapabilitiesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        Integer userId = resolveUserId(request);
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(response, "{\"success\":false,\"message\":\"Sign in before reading device capabilities.\"}");
            return;
        }

        try (Connection conn = Database.getConnection()) {
            String source = latestSource(conn, userId);
            writeJson(response, "{"
                    + "\"success\":true,"
                    + "\"source\":" + JsonUtil.quote(source) + ","
                    + "\"capabilities\":" + DeviceCapabilityService.forSource(source).toJson()
                    + "}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJson(response, "{\"success\":false,\"message\":\"Unable to read device capabilities.\"}");
        }
    }

    private String latestSource(Connection conn, int userId) throws Exception {
        String sql = "SELECT source FROM health_sync_sections WHERE user_id = ? "
                + "ORDER BY window_end DESC, section_id DESC " + limitOne(conn);
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("source") : null;
            }
        }
    }

    private String limitOne(Connection conn) throws Exception {
        String productName = conn.getMetaData().getDatabaseProductName();
        return productName != null && productName.toLowerCase().contains("derby")
                ? "FETCH FIRST 1 ROW ONLY" : "LIMIT 1";
    }

    private Integer resolveUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return null;
        }
        return Integer.valueOf(String.valueOf(session.getAttribute("userId")));
    }

    private void writeJson(HttpServletResponse response, String json) throws IOException {
        try (PrintWriter out = response.getWriter()) {
            out.write(json);
        }
    }
}
