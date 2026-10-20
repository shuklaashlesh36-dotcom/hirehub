<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.hirehub.model.User" %>
<%@ page import="com.hirehub.model.Admin" %>
<%
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/login?authRequired=true");
        return;
    }
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Admin Dashboard - HireHub</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <script src="${pageContext.request.contextPath}/js/theme.js"></script>
</head>
<body>
    <nav class="navbar">
        <a href="${pageContext.request.contextPath}/admin/dashboard.jsp" class="brand">
            HireHub <span class="brand-badge">Admin</span>
        </a>
        <div style="display: flex; align-items: center; gap: 1rem;">
            <button type="button" class="theme-toggle" aria-label="Toggle theme" title="Toggle Light/Dark Theme">
                <span class="theme-toggle-icon" aria-hidden="true">&#9728;&#65039;</span>
                <span class="theme-toggle-text">Theme</span>
            </button>
            <a href="${pageContext.request.contextPath}/profile" class="user-badge" style="text-decoration: none; display: flex; align-items: center; gap: 0.5rem;" title="View Profile">
                <img src="${pageContext.request.contextPath}/profile/photo" alt="Avatar" class="avatar-nav">
                <span><strong><%= currentUser.getName() %></strong> (<%= currentUser.getRole() %>)</span>
            </a>
            <a href="${pageContext.request.contextPath}/logout" class="btn btn-secondary" style="padding: 0.45rem 0.95rem; font-size: 0.85rem;">
                Sign Out
            </a>
        </div>
    </nav>

    <div class="container">
        <div class="glass-panel" style="margin-bottom: 2rem;">
            <h1 style="font-size: 1.85rem; font-weight: 800; margin-bottom: 0.5rem;">
                Welcome, <%= currentUser.getName() %>
            </h1>
            <p style="color: var(--text-secondary); font-size: 0.95rem;">
                System Dashboard: <code style="color: var(--accent-primary);"><%= currentUser.getDashboardType() %></code> &bull; Email: <%= currentUser.getEmail() %>
            </p>
        </div>

        <div class="dashboard-grid">
            <div class="stat-card">
                <span class="stat-label">System Role</span>
                <span class="stat-value" style="color: #818cf8;"><%= currentUser.getRole() %></span>
                <p style="color: var(--text-muted); font-size: 0.85rem;">Full administrative privileges</p>
            </div>
            <div class="stat-card">
                <span class="stat-label">Security Protocol</span>
                <span class="stat-value" style="color: #34d399;">PBKDF2</span>
                <p style="color: var(--text-muted); font-size: 0.85rem;">HMAC-SHA256 salted password encryption</p>
            </div>
            <div class="stat-card">
                <span class="stat-label">Account Status</span>
                <span class="stat-value" style="color: #38bdf8;">ACTIVE</span>
                <p style="color: var(--text-muted); font-size: 0.85rem;">Session verified & authorized</p>
            </div>
        </div>

        <div class="glass-panel" style="margin-top: 2rem;">
            <h2 style="font-size: 1.25rem; font-weight: 700; margin-bottom: 1rem;">Administrative Overview</h2>
            <p style="color: var(--text-secondary); margin-bottom: 1.25rem;">
                Phase 3 authentication has been successfully established with role authorization filters, session invalidation, and password hashing.
            </p>
            <div style="display: flex; gap: 1rem; flex-wrap: wrap;">
                <a href="${pageContext.request.contextPath}/" class="btn btn-secondary">Portal Home</a>
                <a href="${pageContext.request.contextPath}/logout" class="btn btn-danger">Log Out</a>
            </div>
        </div>
    </div>

    <footer class="footer">
        HireHub &copy; 2026. Online Job Portal.
    </footer>
</body>
</html>
