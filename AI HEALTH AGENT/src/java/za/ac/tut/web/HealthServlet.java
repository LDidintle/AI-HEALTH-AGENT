package za.ac.tut.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import za.ac.tut.util.Database;
import za.ac.tut.util.JsonUtil;

public class HealthServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json;charset=UTF-8");
        boolean databaseOk = false;
        try (Connection ignored = Database.getConnection()) {
            databaseOk = true;
        } catch (Exception ignored) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }

        try (PrintWriter out = response.getWriter()) {
            out.write("{"
                    + "\"success\":" + databaseOk + ","
                    + "\"status\":" + JsonUtil.quote(databaseOk ? "UP" : "DEGRADED") + ","
                    + "\"database\":" + JsonUtil.quote(databaseOk ? "UP" : "DOWN") + ","
                    + "\"app\":" + JsonUtil.quote("SmartHealth") + ","
                    + "\"version\":" + JsonUtil.quote(setting("SMARTHEALTH_APP_VERSION", "local"))
                    + "}");
        }
    }

    private String setting(String name, String fallback) {
        String property = System.getProperty(name);
        if (property != null && !property.trim().isEmpty()) {
            return property.trim();
        }
        String env = System.getenv(name);
        return env == null || env.trim().isEmpty() ? fallback : env.trim();
    }
}
