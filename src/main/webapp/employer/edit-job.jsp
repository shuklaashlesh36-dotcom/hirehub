<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.hirehub.model.User" %>
<%@ page import="com.hirehub.model.Employer" %>
<%@ page import="com.hirehub.model.Job" %>
<%@ page import="com.hirehub.model.JobStatus" %>
<%
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/login?authRequired=true");
        return;
    }
    Employer employer = (currentUser instanceof Employer) ? (Employer) currentUser : null;

    Job job = (Job) request.getAttribute("job");
    if (job == null) {
        response.sendRedirect(request.getContextPath() + "/employer/jobs");
        return;
    }

    String csrfToken = (String) request.getAttribute("csrfToken");
    String errorMessage = (String) request.getAttribute("errorMessage");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Edit Job #<%= job.getId() %> - HireHub</title>
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
            <li><a href="${pageContext.request.contextPath}/employer/jobs" style="color: var(--text-primary); font-weight: 600;">Manage Jobs</a></li>
            <li><a href="${pageContext.request.contextPath}/employer/post-job">Post a Job</a></li>
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
            <div style="margin-bottom: 1.75rem; border-bottom: 1px solid var(--border-color); padding-bottom: 1rem; display: flex; justify-content: space-between; align-items: center;">
                <div>
                    <h1 style="font-size: 1.75rem; font-weight: 800; margin-bottom: 0.25rem;">Edit Job Posting</h1>
                    <p style="color: var(--text-secondary); font-size: 0.92rem;">
                        Job ID: #<strong><%= job.getId() %></strong> &bull; Company: <strong style="color: #34d399;"><%= employer != null && employer.getCompanyName() != null ? employer.getCompanyName() : "Employer" %></strong>
                    </p>
                </div>
                <a href="${pageContext.request.contextPath}/employer/applications?jobId=<%= job.getId() %>" class="btn btn-secondary btn-sm">
                    View Applicants
                </a>
            </div>

            <% if (errorMessage != null) { %>
                <div class="alert alert-error" style="margin-bottom: 1.5rem;">
                    <%= errorMessage %>
                </div>
            <% } %>

            <form action="${pageContext.request.contextPath}/employer/jobs" method="post">
                <input type="hidden" name="action" value="edit">
                <input type="hidden" name="jobId" value="<%= job.getId() %>">
                <input type="hidden" name="csrfToken" value="<%= csrfToken %>">

                <div class="form-group">
                    <label for="title">Job Title <span style="color: #f87171;">*</span></label>
                    <input type="text" id="title" name="title" class="form-control" 
                           value="<%= job.getTitle() != null ? job.getTitle() : "" %>" maxlength="150" required autofocus>
                </div>

                <div class="form-row" style="display: grid; grid-template-columns: 1fr 1fr; gap: 1rem;">
                    <div class="form-group">
                        <label for="status">Posting Status</label>
                        <select id="status" name="status" class="form-control">
                            <option value="APPROVED" <%= job.getStatus() == JobStatus.APPROVED ? "selected" : "" %>>APPROVED (Open / Accepting Applications)</option>
                            <option value="CLOSED" <%= job.getStatus() == JobStatus.CLOSED ? "selected" : "" %>>CLOSED (Applications Closed)</option>
                            <option value="PENDING" <%= job.getStatus() == JobStatus.PENDING ? "selected" : "" %>>PENDING (Draft / Pending Review)</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label for="location">Location</label>
                        <input type="text" id="location" name="location" class="form-control" 
                               value="<%= job.getLocation() != null ? job.getLocation() : "" %>" maxlength="100">
                    </div>
                </div>

                <div class="form-group">
                    <label for="salary">Compensation / Salary Range</label>
                    <input type="text" id="salary" name="salary" class="form-control" 
                           value="<%= job.getSalary() != null ? job.getSalary() : "" %>" maxlength="100">
                </div>

                <div class="form-group">
                    <label for="skills">Key Skills & Technologies (comma separated)</label>
                    <input type="text" id="skills" name="skills" class="form-control" 
                           value="<%= job.getSkills() != null ? job.getSkills() : "" %>" maxlength="500">
                </div>

                <div class="form-group">
                    <label for="description">Job Description & Responsibilities <span style="color: #f87171;">*</span></label>
                    <textarea id="description" name="description" class="form-control" rows="8" required><%= job.getDescription() != null ? job.getDescription() : "" %></textarea>
                </div>

                <div style="display: flex; gap: 1rem; justify-content: flex-end; margin-top: 2rem;">
                    <a href="${pageContext.request.contextPath}/employer/jobs" class="btn btn-secondary">
                        Cancel
                    </a>
                    <button type="submit" class="btn btn-primary" style="padding: 0.75rem 2rem;">
                        Save Changes
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
