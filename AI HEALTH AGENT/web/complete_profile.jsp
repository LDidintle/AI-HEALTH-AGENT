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
    <title>Complete Profile</title>
    <link rel="stylesheet" href="page-ui.css">
</head>
<body class="app-page">
    <main class="shell">
        <p class="eyebrow">Patient profile</p>
        <h1>Complete Your Information</h1>
        <p class="lead">Your account is ready. Add the details staff may need for safer monitoring, or continue and fill them in later.</p>

        <% if (request.getAttribute("error") != null) { %>
            <p class="message error"><%= value(request.getAttribute("error")) %></p>
        <% } %>

        <form action="CompleteProfileServlet.do" method="post">
            <label>Account</label>
            <input type="text" value="<%= value(request.getAttribute("first_name")) %> <%= value(request.getAttribute("surname")) %> - <%= value(request.getAttribute("email")) %>" disabled>

            <label for="dob">Date of Birth</label>
            <input type="date" id="dob" name="dob" value="<%= value(request.getAttribute("dob")) %>">

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

            <label for="cell_number">Personal Cell Number</label>
            <input type="text" id="cell_number" name="cell_number" value="<%= value(request.getAttribute("cell_number")) %>" placeholder="0712345678">

            <label for="id_number">South African ID Number</label>
            <input type="text" id="id_number" name="id_number" value="<%= value(request.getAttribute("id_number")) %>" inputmode="numeric" maxlength="13">

            <label for="emergency_contact_name">Emergency Contact / Next of Kin Name</label>
            <input type="text" id="emergency_contact_name" name="emergency_contact_name" value="<%= value(request.getAttribute("emergency_contact_name")) %>">

            <label for="emergency_contact_number">Emergency Contact Cell Number</label>
            <input type="text" id="emergency_contact_number" name="emergency_contact_number" value="<%= value(request.getAttribute("emergency_contact_number")) %>" placeholder="0821234567">

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
            <textarea id="known_allergies" name="known_allergies" rows="3"><%= value(request.getAttribute("known_allergies")) %></textarea>

            <label for="chronic_conditions">Chronic Conditions</label>
            <textarea id="chronic_conditions" name="chronic_conditions" rows="3"><%= value(request.getAttribute("chronic_conditions")) %></textarea>

            <label for="address">Address</label>
            <input type="text" id="address" name="address" value="<%= value(request.getAttribute("address")) %>">

            <div class="actions">
                <button class="btn primary" type="submit">Save and Continue</button>
                <a class="btn secondary" href="CompleteProfileServlet.do?skip=true">Fill In Later</a>
            </div>
        </form>
    </main>
</body>
</html>
