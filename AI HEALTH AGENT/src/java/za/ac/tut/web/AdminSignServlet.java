package za.ac.tut.web;

import java.io.IOException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import za.ac.tut.util.AuthUtil;

public class AdminSignServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        boolean isAdmin = AuthUtil.isAdmin(session);

        if (isAdmin) {
            response.sendRedirect(request.getContextPath() + "/admin_dashboard.jsp");
            return;
        }

        RequestDispatcher dispatcher = request.getRequestDispatcher("/WEB-INF/pages/admin_sign.jsp");
        dispatcher.forward(request, response);
    }
}
