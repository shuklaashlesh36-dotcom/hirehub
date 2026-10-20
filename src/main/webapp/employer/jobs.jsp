<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.hirehub.model.User" %>
<%@ page import="com.hirehub.model.Employer" %>
<%@ page import="com.hirehub.model.Job" %>
<%@ page import="com.hirehub.model.JobStatus" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/login?authRequired=true");
        return;
    }
    Employer employer = (currentUser instanceof Employer) ? (Employer) currentUser : null;

    List<Job> jobs = (List<Job>) request.getAttribute("jobs");
    Map<Long, Integer> applicantCounts = (Map<Long, Integer>) request.getAttribute("applicantCounts");
    String csrfToken = (String) request.getAttribute("csrfToken");
    String successMessage = (String) request.getAttribute("successMessage");
    String errorMessage = (String) request.getAttribute("errorMessage");

    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Manage Jobs - HireHub</title>
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

    <div class="container">
        <!-- Header -->
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; flex-wrap: wrap; gap: 1rem;">
            <div>
                <h1 style="font-size: 1.85rem; font-weight: 800; margin-bottom: 0.25rem;">Job Management</h1>
                <p style="color: var(--text-secondary); font-size: 0.95rem;">Manage, update, close, and review postings published by <%= employer != null ? employer.getCompanyName() : "your company" %></p>
            </div>
            <a href="${pageContext.request.contextPath}/employer/post-job" class="btn btn-primary" style="display: inline-flex; align-items: center; gap: 0.5rem;">
                <span>&#43;</span> Post New Job
            </a>
        </div>

        <% if (successMessage != null) { %>
            <div class="alert alert-success" style="margin-bottom: 1.5rem;">
                <%= successMessage %>
            </div>
        <% } %>

        <% if (errorMessage != null) { %>
            <div class="alert alert-error" style="margin-bottom: 1.5rem;">
                <%= errorMessage %>
            </div>
        <% } %>

        <!-- Jobs Listing -->
        <div class="glass-panel">
            <% if (jobs == null || jobs.isEmpty()) { %>
                <div style="text-align: center; padding: 3rem 1rem; color: var(--text-muted);">
                    <div style="font-size: 3rem; margin-bottom: 1rem;">&#128188;</div>
                    <h3 style="color: var(--text-secondary); font-size: 1.2rem; margin-bottom: 0.5rem;">No Job Postings Found</h3>
                    <p style="margin-bottom: 1.5rem;">You have not published any job listings yet.</p>
                    <a href="${pageContext.request.contextPath}/employer/post-job" class="btn btn-primary">
                        Post Your First Job Now
                    </a>
                </div>
            <% } else { %>
                <div class="data-table-wrapper">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Job Title</th>
                                <th>Location & Salary</th>
                                <th>Status</th>
                                <th>Applicants</th>
                                <th>Date Posted</th>
                                <th style="text-align: right;">Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (Job job : jobs) {
                                int appCount = (applicantCounts != null && applicantCounts.containsKey(job.getId())) ? applicantCounts.get(job.getId()) : 0;
                                String statusClass = "badge-pending";
                                if (job.getStatus() != null) {
                                    switch (job.getStatus()) {
                                        case APPROVED: statusClass = "badge-approved"; break;
                                        case CLOSED: statusClass = "badge-closed"; break;
                                        case REJECTED: statusClass = "badge-rejected"; break;
                                        default: statusClass = "badge-pending"; break;
                                    }
                                }
                            %>
                                <tr>
                                    <td>
                                        <div style="font-weight: 700; font-size: 1rem; color: var(--text-primary); margin-bottom: 0.2rem;">
                                            <%= job.getTitle() %>
                                        </div>
                                        <% if (job.getSkills() != null && !job.getSkills().isBlank()) { %>
                                            <div style="color: var(--text-muted); font-size: 0.8rem;">
                                                Skills: <%= job.getSkills() %>
                                            </div>
                                        <% } %>
                                    </td>
                                    <td>
                                        <div style="color: var(--text-secondary); font-size: 0.88rem;">
                                            <%= job.getLocation() != null && !job.getLocation().isBlank() ? job.getLocation() : "Remote / Flexible" %>
                                        </div>
                                        <% if (job.getSalary() != null && !job.getSalary().isBlank()) { %>
                                            <div style="color: #34d399; font-size: 0.82rem; font-weight: 600;">
                                                <%= job.getSalary() %>
                                            </div>
                                        <% } %>
                                    </td>
                                    <td>
                                        <span class="badge <%= statusClass %>"><%= job.getStatus() %></span>
                                    </td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/employer/applications?jobId=<%= job.getId() %>" 
                                           style="text-decoration: none;" title="View applicants for this job">
                                            <span class="badge" style="background: rgba(99, 102, 241, 0.15); color: #818cf8; border: 1px solid rgba(99, 102, 241, 0.3);">
                                                &#128101; <%= appCount %> <%= appCount == 1 ? "Applicant" : "Applicants" %>
                                            </span>
                                        </a>
                                    </td>
                                    <td style="color: var(--text-secondary); font-size: 0.85rem;">
                                        <%= job.getCreatedAt() != null ? dateFormat.format(job.getCreatedAt()) : "-" %>
                                    </td>
                                    <td style="text-align: right;">
                                        <div style="display: inline-flex; gap: 0.5rem; align-items: center;">
                                            <!-- Edit Action -->
                                            <a href="${pageContext.request.contextPath}/employer/jobs?action=edit&id=<%= job.getId() %>" class="btn btn-secondary btn-sm">
                                                Edit
                                            </a>

                                            <!-- Close or Reopen Form -->
                                            <% if (job.getStatus() == JobStatus.APPROVED) { %>
                                                <form action="${pageContext.request.contextPath}/employer/jobs" method="post" style="display: inline;">
                                                    <input type="hidden" name="action" value="status">
                                                    <input type="hidden" name="jobId" value="<%= job.getId() %>">
                                                    <input type="hidden" name="status" value="CLOSED">
                                                    <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                                                    <button type="submit" class="btn btn-warning btn-sm" onclick="return confirm('Are you sure you want to close this job listing? Candidates will no longer be able to apply.');">
                                                        Close
                                                    </button>
                                                </form>
                                            <% } else if (job.getStatus() == JobStatus.CLOSED) { %>
                                                <form action="${pageContext.request.contextPath}/employer/jobs" method="post" style="display: inline;">
                                                    <input type="hidden" name="action" value="status">
                                                    <input type="hidden" name="jobId" value="<%= job.getId() %>">
                                                    <input type="hidden" name="status" value="APPROVED">
                                                    <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                                                    <button type="submit" class="btn btn-success btn-sm">
                                                        Reopen
                                                    </button>
                                                </form>
                                            <% } %>

                                            <!-- Review Applicants Button -->
                                            <a href="${pageContext.request.contextPath}/employer/applications?jobId=<%= job.getId() %>" class="btn btn-primary btn-sm">
                                                Applicants
                                            </a>
                                        </div>
                                    </td>
                                </tr>
                            <% } %>
                        </tbody>
                    </table>
                </div>
            <% } %>
        </div>
    </div>

    <footer class="footer">
        HireHub &copy; 2026. Online Job Portal.
    </footer>
</body>
</html>
