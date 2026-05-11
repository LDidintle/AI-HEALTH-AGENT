<%@page contentType="text/html" pageEncoding="UTF-8"%>
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
        <form action="HospitalLoginServlet.do" method="post">
            <label for="username">Hospital Username:</label>
            <input type="text" id="username" name="username" required>

            <label for="password">Hospital Password:</label>
            <input type="password" id="password" name="password" required>

            <button type="submit">Login</button>
            <a class="secondary-link" href="index.html">Back to home</a>
        </form>

        <hr>

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
    </main>
</body>
</html>
