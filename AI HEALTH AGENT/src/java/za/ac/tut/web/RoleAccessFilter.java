package za.ac.tut.web;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import za.ac.tut.util.AuthUtil;
import za.ac.tut.util.RoleAccessPolicy;

public class RoleAccessFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No startup configuration is required.
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        String path = RoleAccessPolicy.normalize(httpRequest.getRequestURI()
                .substring(httpRequest.getContextPath().length()));

        if (RoleAccessPolicy.isPublic(path)) {
            chain.doFilter(request, response);
            return;
        }

        HttpSession session = httpRequest.getSession(false);
        String role = AuthUtil.currentRole(session);
        if (RoleAccessPolicy.isAllowed(role, path, httpRequest.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        if (path.startsWith("/api/mobile/")) {
            httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.getWriter().write("{\"success\":false,\"message\":\"Sign in before using this feature.\"}");
            return;
        }

        if (AuthUtil.ROLE_HOSPITAL.equals(role)) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/HospitalPatientsServlet.do");
            return;
        }
        if (AuthUtil.ROLE_PATIENT.equals(role)) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/healthApp.html");
            return;
        }
        if (RoleAccessPolicy.isAdminArea(path)) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/admin");
            return;
        }
        if (RoleAccessPolicy.isHospitalArea(path)) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/hospital_sign.jsp");
            return;
        }
        httpResponse.sendRedirect(httpRequest.getContextPath() + "/user_sign.html");
    }

    @Override
    public void destroy() {
        // Nothing to release.
    }
}
