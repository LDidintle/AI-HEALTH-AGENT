package za.ac.tut.web;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class HospitalLoginServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = valueOrDefault(request.getParameter("username"), "");
        String password = valueOrDefault(request.getParameter("password"), "");
        String hospitalUser = config("SMARTHEALTH_HOSPITAL_USER");
        String hospitalPass = config("SMARTHEALTH_HOSPITAL_PASSWORD");

        if (hospitalUser != null && hospitalPass != null
                && hospitalUser.equals(username) && hospitalPass.equals(password)) {
            HttpSession session = request.getSession();
            session.setAttribute("hospital", "true");
            response.sendRedirect("HospitalPatientsServlet.do");
            return;
        }

        request.setAttribute("error", "Hospital username or password was not accepted.");
        request.getRequestDispatcher("hospital_sign.jsp").forward(request, response);
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
