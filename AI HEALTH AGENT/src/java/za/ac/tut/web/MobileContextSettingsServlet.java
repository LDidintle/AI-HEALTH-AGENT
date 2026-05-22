package za.ac.tut.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import za.ac.tut.util.Database;
import za.ac.tut.util.JsonUtil;
import za.ac.tut.util.PatientContextSettingsService;

public class MobileContextSettingsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        Integer userId = resolveUserId(request);
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(response, "{\"success\":false,\"message\":\"No active user session.\"}");
            return;
        }

        try (Connection conn = Database.getConnection()) {
            PatientContextSettingsService.ContextSettings settings =
                    PatientContextSettingsService.load(conn, userId);
            writeJson(response, "{"
                    + "\"success\":true,"
                    + "\"sleepStart\":" + JsonUtil.quote(settings.getSleepStart()) + ","
                    + "\"sleepEnd\":" + JsonUtil.quote(settings.getSleepEnd())
                    + "}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJson(response, "{\"success\":false,\"message\":\"Unable to load context settings.\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");

        Integer userId = resolveUserId(request);
        if (userId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            writeJson(response, "{\"success\":false,\"message\":\"No active user session.\"}");
            return;
        }

        try (Connection conn = Database.getConnection()) {
            PatientContextSettingsService.save(
                    conn,
                    userId,
                    request.getParameter("sleepStart"),
                    request.getParameter("sleepEnd")
            );
            writeJson(response, "{\"success\":true,\"message\":\"Context settings saved.\"}");
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            writeJson(response, "{\"success\":false,\"message\":\"Sleep times must use HH:mm.\"}");
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            writeJson(response, "{\"success\":false,\"message\":\"Unable to save context settings.\"}");
        }
    }

    private Integer resolveUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return null;
        }
        try {
            return Integer.valueOf(String.valueOf(session.getAttribute("userId")));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private void writeJson(HttpServletResponse response, String json) throws IOException {
        try (PrintWriter out = response.getWriter()) {
            out.write(json);
        }
    }
}
