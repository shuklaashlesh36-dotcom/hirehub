<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.hirehub.model.User" %>
<%@ page import="com.hirehub.model.Employer" %>
<%
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/login?authRequired=true");
        return;
    }
    Employer employer = (currentUser instanceof Employer) ? (Employer) currentUser : null;

    String csrfToken = (String) request.getAttribute("csrfToken");
    String errorMessage = (String) request.getAttribute("errorMessage");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Post a New Job - HireHub</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <script src="${pageContext.request.contextPath}/js/theme.js"></script>
</head>
<body>
    <nav class="navbar">
        <a href="${pageContext.request.contextPath}/employer/dashboard" class="brand">
            HireHub <span class="brand-badge" style="background: linear-gradient(135deg, #10b981 0%, #06b6d4 100%);">Employer</span>
        </a>
        <ul class="nav-links">
            <li><a href="${pageContext.request.contextPath}/employer/dashboard">Dashboard</a></li>
            <li><a href="${pageContext.request.contextPath}/employer/jobs">Manage Jobs</a></li>
            <li><a href="${pageContext.request.contextPath}/employer/post-job" style="color: var(--text-primary); font-weight: 600;">Post a Job</a></li>
            <li><a href="${pageContext.request.contextPath}/employer/applications">Applicants</a></li>
        </ul>
        <div style="display: flex; align-items: center; gap: 1rem;">
            <button type="button" class="theme-toggle" aria-label="Toggle theme" title="Toggle Light/Dark Theme">
                <span class="theme-toggle-icon" aria-hidden="true">&#9728;&#65039;</span>
                <span class="theme-toggle-text">Theme</span>
            </button>
            <a href="${pageContext.request.contextPath}/profile" class="user-badge" style="text-decoration: none; display: flex; align-items: center; gap: 0.5rem;" title="View Profile">
                <img src="${pageContext.request.contextPath}/profile/photo" alt="Avatar" class="avatar-nav">
                <span><strong><%= currentUser.getName() %></strong> (<%= employer != null && employer.getCompanyName() != null ? employer.getCompanyName() : "Employer" %>)</span>
            </a>
            <a href="${pageContext.request.contextPath}/logout" class="btn btn-secondary" style="padding: 0.45rem 0.95rem; font-size: 0.85rem;">
                Sign Out
            </a>
        </div>
    </nav>

    <div class="container" style="max-width: 800px;">
        <div class="glass-panel">
            <div style="margin-bottom: 1.75rem; border-bottom: 1px solid var(--border-color); padding-bottom: 1rem;">
                <h1 style="font-size: 1.75rem; font-weight: 800; margin-bottom: 0.25rem;">Post a New Career Opportunity</h1>
                <p style="color: var(--text-secondary); font-size: 0.92rem;">
                    Company: <strong style="color: var(--accent-primary);"><%= employer != null && employer.getCompanyName() != null ? employer.getCompanyName() : "Employer Company" %></strong>
                </p>
            </div>

            <% if (errorMessage != null) { %>
                <div class="alert alert-error" style="margin-bottom: 1.5rem;">
                    <%= errorMessage %>
                </div>
            <% } %>

            <form action="${pageContext.request.contextPath}/employer/jobs" method="post">
                <input type="hidden" name="action" value="create">
                <input type="hidden" name="csrfToken" value="<%= csrfToken %>">

                <div class="form-group">
                    <label for="title">Job Title <span style="color: #f87171;">*</span></label>
                    <input type="text" id="title" name="title" class="form-control" 
                           placeholder="e.g. Senior Full-Stack Java Engineer" maxlength="150" required autofocus>
                </div>

                <div class="form-row" style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
                    <div class="form-group">
                        <label for="jobType">Employment Type</label>
                        <select id="jobType" name="jobType" class="form-control">
                            <option value="Full-time" selected>Full-time</option>
                            <option value="Part-time">Part-time</option>
                            <option value="Contract">Contract</option>
                            <option value="Internship">Internship</option>
                            <option value="Remote">Remote</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label for="location">Location</label>
                        <input type="text" id="location" name="location" class="form-control" 
                               placeholder="e.g. Bangalore, KA / Remote" maxlength="100">
                    </div>
                </div>

                <div class="form-group">
                    <label for="salary">Compensation / Salary Range</label>
                    <input type="text" id="salary" name="salary" class="form-control" 
                           placeholder="e.g. $90,000 - $120,000 / ₹12 - ₹18 LPA" maxlength="100">
                </div>

                <div class="form-group">
                    <label for="skills">Key Skills & Technologies (comma separated)</label>
                    <input type="text" id="skills" name="skills" class="form-control" 
                           placeholder="e.g. Java, Spring Boot, MySQL, REST APIs, Microservices" maxlength="500">
                </div>

                <div class="form-group">
                    <label for="description">Job Description & Responsibilities <span style="color: #f87171;">*</span></label>
                    <textarea id="description" name="description" class="form-control" rows="8" 
                              placeholder="Describe role responsibilities, qualifications, requirements, and benefits..." required></textarea>
                </div>

                <div style="display: flex; gap: 1rem; justify-content: flex-end; margin-top: 2rem;">
                    <a href="${pageContext.request.contextPath}/employer/jobs" class="btn btn-secondary">
                        Cancel
                    </a>
                    <button type="submit" class="btn btn-primary" style="padding: 0.75rem 2rem;">
                        Publish Job Listing
                    </button>
                </div>
            </form>
        </div>
    </div>

    <footer class="footer">
        HireHub &copy; 2026. Online Job Portal.
    </footer>
</body>
</html>
