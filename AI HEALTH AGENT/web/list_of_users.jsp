<%@page import="java.util.List"%>
<%@page import="za.ac.tut.model.User"%>
<%@page import="za.ac.tut.util.CsrfUtil"%>
<%
    List<User> users = (List<User>) session.getAttribute("users");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Patient Directory</title>
    <link rel="stylesheet" href="page-ui.css">
</head>
<body class="app-page">
    <main class="shell wide">
        <p class="eyebrow">Doctor / Staff workspace</p>
        <h1>Patient Directory</h1>
        <p class="lead">Search, edit, and manage registered patient accounts.</p>

        <div class="actions">
            <a class="btn primary" href="admin_dashboard.jsp">Dashboard</a>
            <a class="btn secondary" href="SignOutServlet.do">Logout</a>
        </div>

        <div class="toolbar">
            <div>
                <label for="userSearch">Search patients</label>
                <input type="search" id="userSearch" placeholder="Search by name, surname, email, phone, ID, or address">
            </div>
            <button class="btn secondary" type="button" id="clearSearch">Clear</button>
        </div>

        <div class="result-count" id="resultCount"></div>

        <div class="table-wrap">
            <table class="data-table" id="usersTable">
                <tr>
                    <th>ID</th>
                    <th>Title</th>
                    <th>Name</th>
                    <th>Surname</th>
                    <th>DOB</th>
                    <th>Gender</th>
                    <th>Email</th>
                    <th>Verified</th>
                    <th>Cell</th>
                    <th>ID Number</th>
                    <th>Emergency Contact</th>
                    <th>Address</th>
                    <th>Actions</th>
                </tr>

                <%
                    if (users != null && !users.isEmpty()) {
                        for (User u : users) {
                %>
                <tr data-user-row>
                    <td><%= u.getId() %></td>
                    <td><%= u.getTitle() %></td>
                    <td><%= u.getFirstName() %></td>
                    <td><%= u.getSurname() %></td>
                    <td><%= u.getDob() %></td>
                    <td><%= u.getGender() %></td>
                    <td><%= u.getEmail() %></td>
                    <td><span class="status-pill <%= u.isVerified() ? "verified" : "pending" %>"><%= u.isVerified() ? "Verified" : "Pending" %></span></td>
                    <td><%= u.getCellNumber() %></td>
                    <td><%= u.getIdNumber() != null ? u.getIdNumber() : "" %></td>
                    <td><%= u.getEmergencyContactName() != null ? u.getEmergencyContactName() : "" %><br><%= u.getEmergencyContactNumber() != null ? u.getEmergencyContactNumber() : "" %></td>
                    <td><%= u.getAddress() %></td>
                    <td>
                        <div class="actions">
                            <a class="btn primary" href="EditUserServlet.do?id=<%= u.getId() %>">Edit</a>
                            <form action="DeleteUserServlet.do" method="post">
                                <input type="hidden" name="<%= CsrfUtil.PARAMETER %>" value="<%= CsrfUtil.token(request) %>">
                                <input type="hidden" name="id" value="<%= u.getId() %>">
                                <button class="btn secondary" type="submit" onclick="return confirm('Delete this user?');">Delete</button>
                            </form>
                        </div>
                    </td>
                </tr>
                <%
                        }
                    } else {
                %>
                <tr>
                    <td colspan="13" class="empty">No patients found</td>
                </tr>
                <%
                    }
                %>
                <tr id="noSearchResults" class="hidden">
                    <td colspan="13" class="empty">No matching patients found</td>
                </tr>
            </table>
        </div>
    </main>

    <script>
        const searchInput = document.getElementById('userSearch');
        const clearSearch = document.getElementById('clearSearch');
        const resultCount = document.getElementById('resultCount');
        const patientRows = Array.from(document.querySelectorAll('[data-user-row]'));
        const noSearchResults = document.getElementById('noSearchResults');

        function updatePatientSearch() {
            const query = searchInput.value.trim().toLowerCase();
            let visible = 0;

            patientRows.forEach(row => {
                const match = row.textContent.toLowerCase().includes(query);
                row.classList.toggle('hidden', !match);
                if (match) visible++;
            });

            if (noSearchResults) noSearchResults.classList.toggle('hidden', !(visible === 0 && patientRows.length > 0));
            resultCount.textContent = visible + ' of ' + patientRows.length + ' patients shown';
        }

        searchInput.addEventListener('input', updatePatientSearch);
        clearSearch.addEventListener('click', () => {
            searchInput.value = '';
            searchInput.focus();
            updatePatientSearch();
        });
        updatePatientSearch();
    </script>
</body>
</html>
