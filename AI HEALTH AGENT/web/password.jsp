<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Password Setup</title>
    <link rel="stylesheet" href="auth.css">
</head>
<body>
    <main class="form-container">
        <h2>Set Your Password</h2>
        <form action="TestServlet.do" method="post" onsubmit="return validatePassword()">
            <label for="password">Password:</label>
            <input type="password" id="password" name="password" required>

            <ul class="requirements">
                <li>At least 8 characters</li>
                <li>At least 2 numbers</li>
                <li>At least 1 uppercase letter</li>
                <li>At least 1 special character</li>
            </ul>

            <label for="confirmPassword">Confirm Password:</label>
            <input type="password" id="confirmPassword" name="confirmPassword" required>

            <div id="errorMsg" class="error"><%= request.getAttribute("error") != null ? request.getAttribute("error") : "" %></div>
            <button type="submit">Submit</button>
        </form>
    </main>
    <script src="password.js"></script>
</body>
</html>
