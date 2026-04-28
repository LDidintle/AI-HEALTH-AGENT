<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Edit Patient</title>
    <link rel="stylesheet" href="page-ui.css">
</head>
<body class="app-page">
    <main class="shell">
        <p class="eyebrow">Doctor / Staff workspace</p>
        <h1>Edit Patient</h1>
        <p class="lead">Update the saved patient account details.</p>

        <form action="UpdateUserServlet.do" method="post">
            <input type="hidden" name="id" value="${id}">

            <label for="title">Title</label>
            <input type="text" id="title" name="title" value="${title}">

            <label for="first_name">First Name</label>
            <input type="text" id="first_name" name="first_name" value="${first_name}">

            <label for="surname">Surname</label>
            <input type="text" id="surname" name="surname" value="${surname}">

            <label for="dob">DOB</label>
            <input type="date" id="dob" name="dob" value="${dob}">

            <label for="gender">Gender</label>
            <input type="text" id="gender" name="gender" value="${gender}">

            <label for="marital_status">Marital Status</label>
            <input type="text" id="marital_status" name="marital_status" value="${marital_status}">

            <label for="email">Email</label>
            <input type="email" id="email" name="email" value="${email}">

            <label for="cell_number">Cell Number</label>
            <input type="text" id="cell_number" name="cell_number" value="${cell_number}">

            <label for="address">Address</label>
            <input type="text" id="address" name="address" value="${address}">

            <div class="actions">
                <button class="btn primary" type="submit">Update Patient</button>
                <a class="btn secondary" href="admin_dashboard.jsp">Dashboard</a>
            </div>
        </form>
    </main>
</body>
</html>
