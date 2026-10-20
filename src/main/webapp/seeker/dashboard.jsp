<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.hirehub.model.User" %>
<%@ page import="com.hirehub.model.JobSeeker" %>
<%
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/login?authRequired=true");
        return;
    }
    JobSeeker seeker = (currentUser instanceof JobSeeker) ? (JobSeeker) currentUser : null;
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Candidate Dashboard - HireHub</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <script src="${pageContext.request.contextPath}/js/theme.js"></script>
</head>
<body>
    <nav class="navbar">
        <a href="${pageContext.request.contextPath}/seeker/dashboard.jsp" class="brand">
            HireHub <span class="brand-badge" style="background: linear-gradient(135deg, #3b82f6 0%, #8b5cf6 100%);">Candidate</span>
        </a>
        <div style="display: flex; align-items: center; gap: 0.75rem; flex-wrap: wrap;">
            <button type="button" class="theme-toggle" aria-label="Toggle theme" title="Toggle Light/Dark Theme">
                <span class="theme-toggle-icon" aria-hidden="true">&#9728;&#65039;</span>
                <span class="theme-toggle-text">Theme</span>
            </button>
            <a href="${pageContext.request.contextPath}/seeker/jobs" class="btn btn-secondary" style="padding: 0.45rem 0.85rem; font-size: 0.85rem;">
                &#128269; Browse Jobs
            </a>
            <a href="${pageContext.request.contextPath}/seeker/applications" class="btn btn-secondary" style="padding: 0.45rem 0.85rem; font-size: 0.85rem;">
                &#128196; My Applications
            </a>
            <a href="${pageContext.request.contextPath}/seeker/resume" class="btn btn-secondary" style="padding: 0.45rem 0.85rem; font-size: 0.85rem;">
                &#128193; Resume
            </a>
            <a href="${pageContext.request.contextPath}/profile" class="user-badge" style="text-decoration: none; display: flex; align-items: center; gap: 0.5rem;" title="View Profile">
                <img src="${pageContext.request.contextPath}/profile/photo" alt="Avatar" class="avatar-nav">
                <span><strong><%= currentUser.getName() %></strong></span>
            </a>
            <a href="${pageContext.request.contextPath}/logout" class="btn btn-secondary" style="padding: 0.45rem 0.85rem; font-size: 0.85rem;">
                Sign Out
            </a>
        </div>
    </nav>

    <div class="container">
        <div class="glass-panel" style="margin-bottom: 2rem;">
            <div style="display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 1.25rem;">
                <div style="display: flex; align-items: center; gap: 1.25rem;">
                    <a href="${pageContext.request.contextPath}/profile" title="Change Photo">
                        <img src="${pageContext.request.contextPath}/profile/photo" alt="<%= currentUser.getName() %>" class="avatar-lg" style="width: 80px; height: 80px;">
                    </a>
                    <div>
                        <h1 style="font-size: 1.85rem; font-weight: 800; margin-bottom: 0.35rem;">
                            Welcome, <%= currentUser.getName() %>
                        </h1>
                        <p style="color: var(--text-secondary); font-size: 0.95rem;">
                            Candidate Profile &bull; <%= currentUser.getEmail() %>
                            <% if (seeker != null && seeker.getHeadline() != null && !seeker.getHeadline().isBlank()) { %>
                                &bull; <span style="color: var(--accent-primary); font-style: italic;"><%= seeker.getHeadline() %></span>
                            <% } %>
                        </p>
                    </div>
                </div>
                <div style="display: flex; gap: 0.75rem;">
                    <a href="${pageContext.request.contextPath}/profile" class="btn btn-secondary" style="padding: 0.45rem 0.95rem; font-size: 0.85rem;">
                        View Profile
                    </a>
                    <a href="${pageContext.request.contextPath}/profile/edit" class="btn btn-primary" style="padding: 0.45rem 0.95rem; font-size: 0.85rem;">
                        &#9998; Edit Profile
                    </a>
                </div>
            </div>
        </div>

        <div class="dashboard-grid">
            <div class="stat-card">
                <span class="stat-label">Phone</span>
                <span class="stat-value" style="font-size: 1.25rem; color: #60a5fa;">
                    <%= (seeker != null && seeker.getPhone() != null && !seeker.getPhone().isBlank()) ? seeker.getPhone() : "Not provided" %>
                </span>
                <p style="color: var(--text-muted); font-size: 0.85rem;">Contact phone</p>
            </div>
            <div class="stat-card">
                <span class="stat-label">Education</span>
                <span class="stat-value" style="font-size: 1.25rem; color: #a78bfa;">
                    <%= (seeker != null && seeker.getEducation() != null && !seeker.getEducation().isBlank()) ? seeker.getEducation() : "Not provided" %>
                </span>
                <p style="color: var(--text-muted); font-size: 0.85rem;">Academic credential</p>
            </div>
            <div class="stat-card">
                <span class="stat-label">Skills</span>
                <span class="stat-value" style="font-size: 1.25rem; color: #34d399;">
                    <%= (seeker != null && seeker.getSkills() != null && !seeker.getSkills().isBlank()) ? seeker.getSkills() : "Not specified" %>
                </span>
                <p style="color: var(--text-muted); font-size: 0.85rem;">Candidate competencies</p>
            </div>
        </div>

        <div class="glass-panel" style="margin-top: 2rem;">
            <h2 style="font-size: 1.35rem; font-weight: 700; margin-bottom: 0.5rem;">Candidate Workspace</h2>
            <p style="color: var(--text-secondary); font-size: 0.95rem; margin-bottom: 1.5rem;">
                Manage your job search activities, applications, and attached resume.
            </p>

            <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(240px, 1fr)); gap: 1rem; margin-bottom: 1.5rem;">
                <div style="background: var(--bg-card-subtle); border: 1px solid var(--border-color); border-radius: var(--radius-md); padding: 1.25rem;">
                    <div style="font-size: 1.75rem; margin-bottom: 0.5rem;">&#128269;</div>
                    <h3 style="font-size: 1.1rem; font-weight: 700; margin-bottom: 0.25rem;">Browse Openings</h3>
                    <p style="font-size: 0.85rem; color: var(--text-secondary); margin-bottom: 1rem;">
                        Search verified jobs with keyword, location, and salary filters.
                    </p>
                    <a href="${pageContext.request.contextPath}/seeker/jobs" class="btn btn-primary" style="font-size: 0.85rem; padding: 0.45rem 1rem;">
                        Explore Jobs &rarr;
                    </a>
                </div>

                <div style="background: var(--bg-card-subtle); border: 1px solid var(--border-color); border-radius: var(--radius-md); padding: 1.25rem;">
                    <div style="font-size: 1.75rem; margin-bottom: 0.5rem;">&#128196;</div>
                    <h3 style="font-size: 1.1rem; font-weight: 700; margin-bottom: 0.25rem;">My Applications</h3>
                    <p style="font-size: 0.85rem; color: var(--text-secondary); margin-bottom: 1rem;">
                        Track review statuses and interview stages for submitted jobs.
                    </p>
                    <a href="${pageContext.request.contextPath}/seeker/applications" class="btn btn-secondary" style="font-size: 0.85rem; padding: 0.45rem 1rem;">
                        View Status &rarr;
                    </a>
                </div>

                <div style="background: var(--bg-card-subtle); border: 1px solid var(--border-color); border-radius: var(--radius-md); padding: 1.25rem;">
                    <div style="font-size: 1.75rem; margin-bottom: 0.5rem;">&#128193;</div>
                    <h3 style="font-size: 1.1rem; font-weight: 700; margin-bottom: 0.25rem;">Resume Management</h3>
                    <p style="font-size: 0.85rem; color: var(--text-secondary); margin-bottom: 1rem;">
                        Upload or replace your PDF resume for 1-click applications.
                    </p>
                    <a href="${pageContext.request.contextPath}/seeker/resume" class="btn btn-secondary" style="font-size: 0.85rem; padding: 0.45rem 1rem;">
                        Manage CV &rarr;
                    </a>
                </div>
            </div>

            <div style="display: flex; gap: 1rem; flex-wrap: wrap; border-top: 1px solid var(--border-color); padding-top: 1rem;">
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
