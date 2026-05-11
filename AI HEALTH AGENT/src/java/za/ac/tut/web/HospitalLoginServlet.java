package za.ac.tut.web;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import za.ac.tut.model.PasswordUtils;
import za.ac.tut.util.Database;

public class HospitalLoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = valueOrDefault(request.getParameter("username"), "");
        String password = valueOrDefault(request.getParameter("password"), "");

        if (loginRegisteredHospital(request, response, username, password)) {
            return;
        }

        String hospitalUser = config("SMARTHEALTH_HOSPITAL_USER");
        String hospitalPass = config("SMARTHEALTH_HOSPITAL_PASSWORD");

        if (hospitalUser != null && hospitalPass != null
                && hospitalUser.equals(username) && hospitalPass.equals(password)) {
            HttpSession session = request.getSession();
            session.setAttribute("hospital", "true");
            session.setAttribute("hospitalLegacy", "true");
            response.sendRedirect("HospitalPatientsServlet.do");
            return;
        }

        request.setAttribute("error", "Hospital username or password was not accepted.");
        request.getRequestDispatcher("hospital_sign.jsp").forward(request, response);
    }

    private boolean loginRegisteredHospital(HttpServletRequest request, HttpServletResponse response,
            String username, String password) throws IOException, ServletException {
        String sql = "SELECT h.hospital_id, h.name, h.service_area, ha.password_hash "
                + "FROM hospitals h JOIN hospital_auth ha ON h.hospital_id = ha.hospital_id "
                + "WHERE h.email = ? AND h.active = TRUE";

        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }

                String expectedHash = rs.getString("password_hash");
                if (!PasswordUtils.hashPassword(password).equals(expectedHash)) {
                    return false;
                }

                HttpSession session = request.getSession();
                session.setAttribute("hospital", "true");
                session.setAttribute("hospitalId", rs.getInt("hospital_id"));
                session.setAttribute("hospitalName", rs.getString("name"));
                session.setAttribute("hospitalServiceArea", rs.getString("service_area"));
                response.sendRedirect("HospitalPatientsServlet.do");
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private static String valueOrDefault(String value, String fallback) {
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        return value.trim();
    }

    private static String config(String name) {
        String property = valueOrDefault(System.getProperty(name), "");
        if (!property.isEmpty()) {
            return property;
        }
        String env = valueOrDefault(System.getenv(name), "");
        return env.isEmpty() ? null : env;
    }
}
