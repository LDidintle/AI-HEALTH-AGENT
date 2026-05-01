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

            <label for="cell_number">Personal Cell Number</label>
            <input type="text" id="cell_number" name="cell_number" value="${cell_number}">

            <label for="id_number">South African ID Number</label>
            <input type="number" id="id_number" name="id_number" value="${id_number}" inputmode="numeric">

            <label for="emergency_contact_name">Emergency Contact / Next of Kin Name</label>
            <input type="text" id="emergency_contact_name" name="emergency_contact_name" value="${emergency_contact_name}">

            <label for="emergency_contact_number">Emergency Contact Cell Number</label>
            <input type="text" id="emergency_contact_number" name="emergency_contact_number" value="${emergency_contact_number}">

            <label for="blood_group">Blood Group</label>
            <select id="blood_group" name="blood_group">
                <option value="${blood_group}">${blood_group}</option>
                <option>A+</option>
                <option>A-</option>
                <option>B+</option>
                <option>B-</option>
                <option>AB+</option>
                <option>AB-</option>
                <option>O+</option>
                <option>O-</option>
                <option>Unknown</option>
            </select>

            <label for="known_allergies">Known Allergies</label>
            <textarea id="known_allergies" name="known_allergies" rows="3">${known_allergies}</textarea>

            <label for="chronic_conditions">Chronic Conditions</label>
            <textarea id="chronic_conditions" name="chronic_conditions" rows="3">${chronic_conditions}</textarea>

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
