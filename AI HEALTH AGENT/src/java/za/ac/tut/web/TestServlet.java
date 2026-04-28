package za.ac.tut.web;

import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.*;
import za.ac.tut.model.PasswordUtils;
import za.ac.tut.util.Database;

public class TestServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html");
        HttpSession session = request.getSession(false);

        if (session == null) {
            response.getWriter().println("Session expired!");
            return;
        }

        try {
            String title = (String) session.getAttribute("title");
            String name = (String) session.getAttribute("name");
            String surname = (String) session.getAttribute("surname");
            String dobStr = (String) session.getAttribute("dob");
            String gender = (String) session.getAttribute("gender");
            String status = (String) session.getAttribute("status");
            String email = (String) session.getAttribute("email");
            String cell_no = (String) session.getAttribute("cell_no");
            String address = (String) session.getAttribute("address");

         
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            java.util.Date utilDate = sdf.parse(dobStr);
            java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

         
            String pass = request.getParameter("confirmPassword");
            PasswordUtils pu = new PasswordUtils();
            String hashedPass = pu.hashPassword(pass);

       
            String sqlUser = "INSERT INTO users (title, first_name, surname, dob, gender, marital_status, email, cell_number, address) "
                    + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            String sqlAuth = "INSERT INTO user_auth (user_id, password_hash) VALUES (?, ?)";

            
            try (
                Connection conn = Database.getConnection();
                PreparedStatement psUser = conn.prepareStatement(sqlUser, PreparedStatement.RETURN_GENERATED_KEYS);
                PreparedStatement psAuth = conn.prepareStatement(sqlAuth);
            ) {
                conn.setAutoCommit(false);

              
                psUser.setString(1, title);
                psUser.setString(2, name);
                psUser.setString(3, surname);
                psUser.setDate(4, sqlDate);
                psUser.setString(5, gender);
                psUser.setString(6, status);
                psUser.setString(7, email);
                psUser.setString(8, cell_no);
                psUser.setString(9, address);

                int rowsInserted = psUser.executeUpdate();
                if (rowsInserted == 0) throw new SQLException("Failed to insert user!");

               
                int userId = 0;
                try (ResultSet rs = psUser.getGeneratedKeys()) {
                    if (rs.next()) {
                        userId = rs.getInt(1);
                    } else {
                        throw new SQLException("Failed to get user ID!");
                    }
                }

          
                psAuth.setInt(1, userId);
                psAuth.setString(2, hashedPass);
                psAuth.executeUpdate();

            
                conn.commit();

                RequestDispatcher disp = request.getRequestDispatcher("index.html");
                disp.forward(request, response);
                
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().println("<h2>Error occurred!</h2>");
            response.getWriter().println("<pre>" + e.getMessage() + "</pre>");
        }
    }
}
