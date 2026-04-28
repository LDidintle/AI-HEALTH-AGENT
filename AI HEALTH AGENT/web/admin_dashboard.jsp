<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Doctor / Staff Dashboard</title>
    <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@400;500;600;700&display=swap" rel="stylesheet">
    <link rel="stylesheet" href="page-ui.css">
</head>
<body class="app-page">
    <main class="shell wide">
        <p class="eyebrow">Doctor / Staff workspace</p>
        <h1>Doctor / Staff Dashboard</h1>
        <p class="lead">Manage registered users and inspect patient account details from one place.</p>

        <section class="admin-grid">
            <a class="admin-tile" href="ViewUsersServlet.do">
                <strong>View patients</strong>
                <span>Open the full patient list with edit and delete actions.</span>
            </a>
            <a class="admin-tile" href="read_user.jsp">
                <strong>Find patient</strong>
                <span>Search one patient by email and view their details.</span>
            </a>
            <a class="admin-tile" href="index.html">
                <strong>Logout</strong>
                <span>Return to the SmartHealth home page.</span>
            </a>
        </section>
    </main>
</body>
</html>
