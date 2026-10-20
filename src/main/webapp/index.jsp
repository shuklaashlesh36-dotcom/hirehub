
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.hirehub.model.User" %>
<%@ page import="com.hirehub.model.Role" %>
<%
    User currentUser = (User) session.getAttribute("currentUser");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>HireHub – Online Job Portal</title>
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
        <% if (currentUser != null) {
            String dashUrl;

            if (currentUser.getRole() == Role.ADMIN) {
                dashUrl = "/admin/dashboard.jsp";
            } else if (currentUser.getRole() == Role.EMPLOYER) {
                dashUrl = "/employer/dashboard";
            } else {
                dashUrl = "/seeker/dashboard.jsp";
            }
        %>
        <% if (currentUser.getRole() == Role.JOB_SEEKER) { %>
        <li>
            <a href="${pageContext.request.contextPath}/seeker/jobs">Browse Jobs</a>
        </li>
        <li>
            <a href="${pageContext.request.contextPath}/seeker/applications">My Applications</a>
        </li>
        <% } else if (currentUser.getRole() == Role.EMPLOYER) { %>
        <li>
            <a href="${pageContext.request.contextPath}/employer/jobs">Manage Jobs</a>
        </li>
        <li>
            <a href="${pageContext.request.contextPath}/employer/post-job">Post a Job</a>
        </li>
        <li>
            <a href="${pageContext.request.contextPath}/employer/applications">Applicants</a>
        </li>
        <% } %>
        <li>
            <a href="${pageContext.request.contextPath}<%= dashUrl %>"
               style="color: var(--accent-primary); font-weight: 600;">
                My Dashboard (<%= currentUser.getRole() %>)
            </a>
        </li>
        <li>
            <a href="${pageContext.request.contextPath}/logout"
               class="btn btn-secondary"
               style="padding: 0.35rem 0.85rem;">
                Sign Out
            </a>
        </li>
        <% } else { %>
        <li>
            <a href="${pageContext.request.contextPath}/login">Sign In</a>
        </li>
        <li>
            <a href="${pageContext.request.contextPath}/register"
               class="btn btn-primary"
               style="padding: 0.45rem 1rem;">
                Create Account
            </a>
        </li>
        <% } %>
    </ul>
</nav>

<div class="container"
     style="max-width: 860px; margin-top: 4rem; text-align: center;">
    <div class="glass-panel" style="padding: 3.5rem 2.5rem;">

        <div class="brand-badge"
             style="display: inline-block; margin-bottom: 1.25rem;
                        font-size: 0.85rem; padding: 0.35rem 0.85rem;">
            HireHub &bull; Online Job Portal
        </div>

        <h1 style="font-size: 2.75rem; font-weight: 800;
                       margin-bottom: 1.25rem; letter-spacing: -0.03em;
                       line-height: 1.2;">
            Connecting Talent with Opportunities
        </h1>

        <p style="color: var(--text-secondary); font-size: 1.15rem;
                      max-width: 640px; margin: 0 auto 2.5rem;">
            A comprehensive online job portal engineered with core Java,
            JDBC, and secure role-based session authentication.
        </p>

        <div style="display: flex; gap: 1rem; justify-content: center;
                        flex-wrap: wrap;">

            <% if (currentUser != null) {
                String dashUrl;

                if (currentUser.getRole() == Role.ADMIN) {
                    dashUrl = "/admin/dashboard.jsp";
                } else if (currentUser.getRole() == Role.EMPLOYER) {
                    dashUrl = "/employer/dashboard.jsp";
                } else {
                    dashUrl = "/seeker/dashboard.jsp";
                }
            %>
            <a href="${pageContext.request.contextPath}<%= dashUrl %>"
               class="btn btn-primary"
               style="font-size: 1.05rem; padding: 0.85rem 2rem;">
                Go to My Dashboard &rarr;
            </a>

            <a href="${pageContext.request.contextPath}/logout"
               class="btn btn-secondary"
               style="font-size: 1.05rem; padding: 0.85rem 1.75rem;">
                Sign Out
            </a>
            <% } else { %>
            <a href="${pageContext.request.contextPath}/register"
               class="btn btn-primary"
               style="font-size: 1.05rem; padding: 0.85rem 2rem;">
                Get Started (Register)
            </a>

            <a href="${pageContext.request.contextPath}/login"
               class="btn btn-secondary"
               style="font-size: 1.05rem; padding: 0.85rem 1.75rem;">
                Sign In
            </a>
            <% } %>
        </div>

        <div style="margin-top: 3.5rem; display: grid;
                        grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
                        gap: 1.25rem; text-align: left;">

            <div style="padding: 1.25rem; background: var(--bg-card-subtle);
                            border: 1px solid var(--border-color);
                            border-radius: var(--radius-md);">
                <div style="font-size: 1.35rem; margin-bottom: 0.5rem;">
                    &#128101;
                </div>
                <div style="font-weight: 700; margin-bottom: 0.25rem;">
                    Role-Based Dashboards
                </div>
                <div style="color: var(--text-secondary); font-size: 0.85rem;">
                    Separate workspaces for Job Seekers, Employers, and Admins.
                </div>
            </div>

            <div style="padding: 1.25rem; background: var(--bg-card-subtle);
                            border: 1px solid var(--border-color);
                            border-radius: var(--radius-md);">
                <div style="font-size: 1.35rem; margin-bottom: 0.5rem;">
                    &#128272;
                </div>
                <div style="font-weight: 700; margin-bottom: 0.25rem;">
                    PBKDF2 Salted Encryption
                </div>
                <div style="color: var(--text-secondary); font-size: 0.85rem;">
                    Secure password hashing with HMAC-SHA256 and unique salts.
                </div>
            </div>

            <div style="padding: 1.25rem; background: var(--bg-card-subtle);
                            border: 1px solid var(--border-color);
                            border-radius: var(--radius-md);">
                <div style="font-size: 1.35rem; margin-bottom: 0.5rem;">
                    &#128737;
                </div>
                <div style="font-weight: 700; margin-bottom: 0.25rem;">
                    Route Authorization
                </div>
                <div style="color: var(--text-secondary); font-size: 0.85rem;">
                    Authentication and role filters safeguarding protected paths.
                </div>
            </div>
        </div>
    </div>
</div>

<footer class="footer">
    HireHub &copy; 2026. Online Job Portal.
</footer>
</body>
</html>
