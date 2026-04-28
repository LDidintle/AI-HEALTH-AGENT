<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>User Login</title>
    <link rel="stylesheet" href="auth.css">
</head>
<body>
    <main class="form-container">
        <h2>User Login</h2>
        <form action="UserConfirmServlet.do" method="post">
            <label for="username">User Email:</label>
            <input type="email" id="username" name="username" required>

            <div class="error"><%= request.getAttribute("usernameError") != null ? request.getAttribute("usernameError") : "" %></div>

            <label for="password">Password:</label>
            <input type="password" id="password" name="password" required>

            <div class="error"><%= request.getAttribute("passwordError") != null ? request.getAttribute("passwordError") : "" %></div>
            <button type="submit">Login</button>
            <a class="secondary-link" href="index.html">Back to home</a>
        </form>
    </main>
</body>
</html>
