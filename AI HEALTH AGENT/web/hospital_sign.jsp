<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    boolean showRegister = request.getAttribute("registerError") != null;
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Hospital Portal Login</title>
    <link rel="stylesheet" href="auth.css">
</head>
<body>
    <main class="form-container">
        <h2>Hospital Portal</h2>
        <p class="lead">Hospitals can register a service area, receive emergency-alert patients for that area, and review read-only summaries.</p>
        <% if (request.getAttribute("message") != null) { %>
            <div class="message success"><%= request.getAttribute("message") %></div>
        <% } %>
        <% if (request.getAttribute("error") != null) { %>
            <div class="error"><%= request.getAttribute("error") %></div>
        <% } %>

        <div class="portal-actions" role="tablist" aria-label="Hospital portal actions">
            <button class="portal-action <%= showRegister ? "" : "active" %>" type="button" data-target="loginPanel">Login as Hospital</button>
            <button class="portal-action <%= showRegister ? "active" : "" %>" type="button" data-target="registerPanel">Register Hospital</button>
        </div>

        <section id="loginPanel" class="portal-panel <%= showRegister ? "hidden" : "" %>">
            <h2>Hospital Login</h2>
            <form action="HospitalLoginServlet.do" method="post">
                <label for="username">Hospital Email or Username:</label>
                <input type="text" id="username" name="username" required>

                <label for="password">Hospital Password:</label>
                <input type="password" id="password" name="password" required>

                <button type="submit">Login</button>
            </form>
        </section>

        <section id="registerPanel" class="portal-panel <%= showRegister ? "" : "hidden" %>">
            <h2>Register Hospital</h2>
            <p class="lead">Use a clear service area such as Pretoria, Soshanguve, Soweto, or Polokwane. Patient emergency alerts are matched against the patient's saved address.</p>
            <% if (request.getAttribute("registerError") != null) { %>
                <div class="error"><%= request.getAttribute("registerError") %></div>
            <% } %>
            <form action="HospitalRegisterServlet.do" method="post">
                <label for="name">Hospital Name:</label>
                <input type="text" id="name" name="name" required>

                <label for="email">Hospital Email:</label>
                <input type="email" id="email" name="email" required>

                <label for="phone">Phone Number:</label>
                <input type="tel" id="phone" name="phone">

                <label for="serviceArea">Service Area:</label>
                <input type="text" id="serviceArea" name="serviceArea" placeholder="Example: Pretoria" required>

                <label for="address">Hospital Address:</label>
                <input type="text" id="address" name="address">

                <label for="registerPassword">Password:</label>
                <input type="password" id="registerPassword" name="password" minlength="8" required>

                <button type="submit">Register Hospital</button>
            </form>
        </section>

        <a class="secondary-link" href="index.html">Back to home</a>
    </main>

    <script>
        const actionButtons = Array.from(document.querySelectorAll('.portal-action'));
        const panels = Array.from(document.querySelectorAll('.portal-panel'));

        actionButtons.forEach(button => {
            button.addEventListener('click', () => {
                actionButtons.forEach(item => item.classList.remove('active'));
                panels.forEach(panel => panel.classList.add('hidden'));

                button.classList.add('active');
                document.getElementById(button.dataset.target).classList.remove('hidden');
            });
        });
    </script>
</body>
</html>
