<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%!
    private String escapeHtml(Object value) {
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
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Action Needed</title>
    <link rel="stylesheet" href="page-ui.css">
</head>
<body class="app-page">
    <main class="shell">
        <p class="eyebrow">Action needed</p>
        <h1>Something needs attention</h1>
        <p class="lead"><%= request.getAttribute("error") == null
                ? "We could not complete that action. Please check the details and try again."
                : escapeHtml(request.getAttribute("error")) %></p>

        <div class="actions">
            <a class="btn primary" href="admin_sign.html">Staff Login</a>
            <a class="btn secondary" href="index.html">Home</a>
        </div>
    </main>
</body>
</html>
