<%@page import="za.ac.tut.util.CsrfUtil"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%!
    private String value(Object value) {
        if (value == null) {
            return "";
        }
        return value.toString()
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String selected(Object current, String option) {
        return option.equals(value(current)) ? "selected" : "";
    }
%>
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
            <input type="hidden" name="<%= CsrfUtil.PARAMETER %>" value="<%= CsrfUtil.token(request) %>">
            <input type="hidden" name="id" value="${id}">

            <label for="title">Title</label>
            <select id="title" name="title">
                <option value="">Not saved</option>
                <option <%= selected(request.getAttribute("title"), "Mr") %>>Mr</option>
                <option <%= selected(request.getAttribute("title"), "Ms") %>>Ms</option>
                <option <%= selected(request.getAttribute("title"), "Mrs") %>>Mrs</option>
                <option <%= selected(request.getAttribute("title"), "Dr") %>>Dr</option>
                <option <%= selected(request.getAttribute("title"), "Patient") %>>Patient</option>
            </select>

            <label for="first_name">First Name</label>
            <input type="text" id="first_name" name="first_name" value="${first_name}">

            <label for="surname">Surname</label>
            <input type="text" id="surname" name="surname" value="${surname}">

            <label for="dob">DOB</label>
            <input type="date" id="dob" name="dob" value="${dob}">

            <label for="gender">Gender</label>
            <select id="gender" name="gender">
                <option value="">Prefer not to say yet</option>
                <option <%= selected(request.getAttribute("gender"), "Female") %>>Female</option>
                <option <%= selected(request.getAttribute("gender"), "Male") %>>Male</option>
                <option <%= selected(request.getAttribute("gender"), "Other") %>>Other</option>
                <option <%= selected(request.getAttribute("gender"), "Prefer not to say") %>>Prefer not to say</option>
            </select>

            <label for="marital_status">Marital Status</label>
            <select id="marital_status" name="marital_status">
                <option value="">Not saved yet</option>
                <option <%= selected(request.getAttribute("marital_status"), "Single") %>>Single</option>
                <option <%= selected(request.getAttribute("marital_status"), "Married") %>>Married</option>
                <option <%= selected(request.getAttribute("marital_status"), "Divorced") %>>Divorced</option>
                <option <%= selected(request.getAttribute("marital_status"), "Widowed") %>>Widowed</option>
            </select>

            <label for="email">Email</label>
            <input type="email" id="email" name="email" value="${email}">

            <label class="checkbox-field" for="is_verified">
                <input type="checkbox" id="is_verified" name="is_verified" value="true" <%= Boolean.TRUE.equals(request.getAttribute("is_verified")) ? "checked" : "" %>>
                Verified patient account
            </label>

            <label for="cell_number">Personal Cell Number</label>
            <input type="text" id="cell_number" name="cell_number" value="${cell_number}">

            <label for="id_number">South African ID Number</label>
            <input type="text" id="id_number" name="id_number" value="${id_number}" inputmode="numeric" maxlength="13">

            <label for="emergency_contact_name">Emergency Contact / Next of Kin Name</label>
            <input type="text" id="emergency_contact_name" name="emergency_contact_name" value="${emergency_contact_name}">

            <label for="emergency_contact_number">Emergency Contact Cell Number</label>
            <input type="text" id="emergency_contact_number" name="emergency_contact_number" value="${emergency_contact_number}">

            <label for="blood_group">Blood Group</label>
            <select id="blood_group" name="blood_group">
                <option value="">Not sure yet</option>
                <option <%= selected(request.getAttribute("blood_group"), "A+") %>>A+</option>
                <option <%= selected(request.getAttribute("blood_group"), "A-") %>>A-</option>
                <option <%= selected(request.getAttribute("blood_group"), "B+") %>>B+</option>
                <option <%= selected(request.getAttribute("blood_group"), "B-") %>>B-</option>
                <option <%= selected(request.getAttribute("blood_group"), "AB+") %>>AB+</option>
                <option <%= selected(request.getAttribute("blood_group"), "AB-") %>>AB-</option>
                <option <%= selected(request.getAttribute("blood_group"), "O+") %>>O+</option>
                <option <%= selected(request.getAttribute("blood_group"), "O-") %>>O-</option>
                <option <%= selected(request.getAttribute("blood_group"), "Unknown") %>>Unknown</option>
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
    <script src="patient-date.js"></script>
    <script>
        setLatestAllowedDateOfBirth(document.getElementById('dob'));
    </script>
</body>
</html>
