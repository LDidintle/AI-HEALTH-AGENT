<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Doctor / Staff Login</title>
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="page-ui.css">
</head>
<body class="app-page">
    <main class="shell">
        <p class="eyebrow">Doctor / Staff access</p>
        <h1>Sign in again</h1>
        <p class="lead">The staff username or password was not accepted.</p>

        <form action="AdminServlet.do" method="POST">
            <label for="username">Staff username</label>
            <input type="text" id="username" name="username" required>

            <label for="password">Staff password</label>
            <input type="password" id="password" name="password" required>

            <div class="message error">
                <%= request.getAttribute("usernameError") != null ? request.getAttribute("usernameError") : "Please enter the correct staff credentials." %>
            </div>

            <div class="actions">
                <button class="btn primary" type="submit">Log in</button>
                <a class="btn secondary" href="admin_sign.html">Back</a>
                <a class="btn secondary" href="index.html">Home</a>
            </div>
        </form>
    </main>
</body>
</html>
