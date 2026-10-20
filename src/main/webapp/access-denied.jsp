<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<%@ page import="com.hirehub.model.User" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Access Denied (403) - HireHub</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <script src="${pageContext.request.contextPath}/js/theme.js"></script>
</head>
<body>
    <nav class="navbar">
        <a href="${pageContext.request.contextPath}/" class="brand">
            HireHub <span class="brand-badge">Portal</span>
        </a>
        <ul class="nav-links">
            <li>
                <button type="button" class="theme-toggle" aria-label="Toggle theme" title="Toggle Light/Dark Theme">
                    <span class="theme-toggle-icon" aria-hidden="true">&#9728;&#65039;</span>
                    <span class="theme-toggle-text">Theme</span>
                </button>
            </li>
            <li><a href="${pageContext.request.contextPath}/">Home</a></li>
            <li><a href="${pageContext.request.contextPath}/logout">Sign Out</a></li>
        </ul>
    </nav>

    <div class="container auth-container" style="text-align: center;">
        <div class="glass-panel">
            <div style="font-size: 3.5rem; margin-bottom: 0.5rem; color: #f87171;">
                &#128274;
            </div>
            <h1 style="font-size: 1.85rem; font-weight: 800; margin-bottom: 0.75rem;">Access Denied</h1>
            <p style="color: var(--text-secondary); margin-bottom: 1.5rem; font-size: 0.95rem;">
                You do not have the required permissions or role to view this page.
            </p>

            <% 
                User currentUser = (User) session.getAttribute("currentUser");
                if (currentUser != null) {
                    String dashUrl = "/seeker/dashboard.jsp";
                    if (currentUser.getRole() == com.hirehub.model.Role.ADMIN) {
                        dashUrl = "/admin/dashboard.jsp";
                    } else if (currentUser.getRole() == com.hirehub.model.Role.EMPLOYER) {
                        dashUrl = "/employer/dashboard";
                    }
            %>
                <div style="margin-bottom: 1.5rem; padding: 0.75rem; background: var(--bg-card-subtle); border: 1px solid var(--border-color); border-radius: 8px;">
                    Logged in as: <strong><%= currentUser.getName() %></strong> (<%= currentUser.getRole() %>)
                </div>
                <div style="display: flex; gap: 0.75rem; justify-content: center;">
                    <a href="${pageContext.request.contextPath}<%= dashUrl %>" class="btn btn-primary">
                        Return to My Dashboard
                    </a>
                    <a href="${pageContext.request.contextPath}/logout" class="btn btn-secondary">
                        Switch Account
                    </a>
                </div>
            <% } else { %>
                <a href="${pageContext.request.contextPath}/login" class="btn btn-primary">
                    Sign In to HireHub
                </a>
            <% } %>
        </div>
    </div>

    <footer class="footer">
        HireHub &copy; 2026. Online Job Portal.
    </footer>
</body>
</html>
