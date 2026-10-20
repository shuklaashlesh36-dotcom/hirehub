<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.hirehub.model.User" %>
<%@ page import="com.hirehub.model.Employer" %>
<%@ page import="com.hirehub.model.Job" %>
<%@ page import="com.hirehub.model.Application" %>
<%@ page import="com.hirehub.model.ApplicationStatus" %>
<%@ page import="java.util.List" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/login?authRequired=true");
        return;
    }
    Employer employer = (currentUser instanceof Employer) ? (Employer) currentUser : null;

    List<Job> jobs = (List<Job>) request.getAttribute("jobs");
    List<Application> applications = (List<Application>) request.getAttribute("applications");
    Long selectedJobId = (Long) request.getAttribute("selectedJobId");
    String csrfToken = (String) request.getAttribute("csrfToken");
    String successMessage = (String) request.getAttribute("successMessage");
    String errorMessage = request.getParameter("error");

    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Applicant Review - HireHub</title>
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
            <li><a href="${pageContext.request.contextPath}/employer/post-job">Post a Job</a></li>
            <li><a href="${pageContext.request.contextPath}/employer/applications" style="color: var(--text-primary); font-weight: 600;">Applicants</a></li>
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
        <!-- Header & Filter Bar -->
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.5rem; flex-wrap: wrap; gap: 1rem;">
            <div>
                <h1 style="font-size: 1.85rem; font-weight: 800; margin-bottom: 0.25rem;">Candidate Applications</h1>
                <p style="color: var(--text-secondary); font-size: 0.95rem;">Review resumes and update candidate hiring stages</p>
            </div>

            <!-- Job Filter Dropdown -->
            <form action="${pageContext.request.contextPath}/employer/applications" method="get" style="display: flex; align-items: center; gap: 0.75rem;">
                <label for="jobFilter" style="color: var(--text-secondary); font-size: 0.9rem; font-weight: 600;">Filter by Job:</label>
                <select id="jobFilter" name="jobId" class="form-control" style="width: auto; padding: 0.45rem 1rem;" onchange="this.form.submit()">
                    <option value="">All Job Postings</option>
                    <% if (jobs != null) {
                        for (Job j : jobs) { %>
                            <option value="<%= j.getId() %>" <%= (selectedJobId != null && selectedJobId.equals(j.getId())) ? "selected" : "" %>>
                                <%= j.getTitle() %> (<%= j.getStatus() %>)
                            </option>
                    <%  }
                    } %>
                </select>
            </form>
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

        <!-- Applicants Table -->
        <div class="glass-panel">
            <% if (applications == null || applications.isEmpty()) { %>
                <div style="text-align: center; padding: 3rem 1rem; color: var(--text-muted);">
                    <div style="font-size: 3rem; margin-bottom: 1rem;">&#128101;</div>
                    <h3 style="color: var(--text-secondary); font-size: 1.2rem; margin-bottom: 0.5rem;">No Applicants Found</h3>
                    <p style="margin-bottom: 1rem;">
                        <%= selectedJobId != null ? "No candidate applications have been received for this specific job." : "No applications have been submitted across your job postings yet." %>
                    </p>
                    <% if (selectedJobId != null) { %>
                        <a href="${pageContext.request.contextPath}/employer/applications" class="btn btn-secondary btn-sm">
                            View All Postings
                        </a>
                    <% } %>
                </div>
            <% } else { %>
                <div class="data-table-wrapper">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Candidate Info</th>
                                <th>Applied Position</th>
                                <th>Date Applied</th>
                                <th>Resume</th>
                                <th>Hiring Status</th>
                                <th style="text-align: right;">Update Decision</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (Application app : applications) {
                                String statusClass = "badge-applied";
                                if (app.getStatus() != null) {
                                    switch (app.getStatus()) {
                                        case SHORTLISTED: statusClass = "badge-shortlisted"; break;
                                        case HIRED: statusClass = "badge-hired"; break;
                                        case REJECTED: statusClass = "badge-rejected"; break;
                                        default: statusClass = "badge-applied"; break;
                                    }
                                }
                            %>
                                <tr>
                                    <td>
                                        <div style="display: flex; align-items: center; gap: 0.75rem;">
                                            <img src="${pageContext.request.contextPath}/profile/photo?userId=<%= app.getSeekerId() %>" alt="Candidate" class="avatar-nav" style="width: 36px; height: 36px;">
                                            <div>
                                                <div style="font-weight: 700; color: var(--text-primary); font-size: 0.98rem;">
                                                    <%= app.getSeekerName() != null ? app.getSeekerName() : "Candidate #" + app.getSeekerId() %>
                                                </div>
                                                <div style="color: var(--text-secondary); font-size: 0.85rem;">
                                                    &#9993; <%= app.getSeekerEmail() != null ? app.getSeekerEmail() : "No email" %>
                                                </div>
                                                <% if (app.getSeekerPhone() != null && !app.getSeekerPhone().isBlank()) { %>
                                                    <div style="color: var(--text-muted); font-size: 0.8rem;">
                                                        &#9742; <%= app.getSeekerPhone() %>
                                                    </div>
                                                <% } %>
                                            </div>
                                        </div>
                                    </td>
                                    <td>
                                        <div style="font-weight: 600; color: var(--text-primary);">
                                            <%= app.getJobTitle() %>
                                        </div>
                                        <% if (app.getJobLocation() != null) { %>
                                            <div style="color: var(--text-muted); font-size: 0.8rem;">
                                                <%= app.getJobLocation() %>
                                            </div>
                                        <% } %>
                                    </td>
                                    <td style="color: var(--text-secondary); font-size: 0.88rem;">
                                        <%= app.getAppliedAt() != null ? dateFormat.format(app.getAppliedAt()) : "-" %>
                                    </td>
                                    <td>
                                        <% if (app.getResumeFileName() != null) { %>
                                            <a href="${pageContext.request.contextPath}/employer/resume/download?applicationId=<%= app.getId() %>" 
                                               target="_blank" class="btn btn-secondary btn-sm" style="display: inline-flex; align-items: center; gap: 0.35rem;" title="View candidate PDF resume">
                                                <span>&#128196;</span> <%= app.getResumeFileName() %>
                                            </a>
                                        <% } else { %>
                                            <span style="color: var(--text-muted); font-size: 0.82rem;">No resume attached</span>
                                        <% } %>
                                    </td>
                                    <td>
                                        <span class="badge <%= statusClass %>"><%= app.getStatus() %></span>
                                    </td>
                                    <td style="text-align: right;">
                                        <form action="${pageContext.request.contextPath}/employer/applications/status" method="post" style="display: inline-flex; gap: 0.4rem; align-items: center;">
                                            <input type="hidden" name="applicationId" value="<%= app.getId() %>">
                                            <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                                            <% if (selectedJobId != null) { %>
                                                <input type="hidden" name="jobId" value="<%= selectedJobId %>">
                                            <% } %>
                                            <select name="status" class="form-control" style="width: auto; padding: 0.3rem 0.6rem; font-size: 0.82rem;">
                                                <option value="APPLIED" <%= app.getStatus() == ApplicationStatus.APPLIED ? "selected" : "" %>>Applied</option>
                                                <option value="SHORTLISTED" <%= app.getStatus() == ApplicationStatus.SHORTLISTED ? "selected" : "" %>>Shortlist</option>
                                                <option value="HIRED" <%= app.getStatus() == ApplicationStatus.HIRED ? "selected" : "" %>>Hire</option>
                                                <option value="REJECTED" <%= app.getStatus() == ApplicationStatus.REJECTED ? "selected" : "" %>>Reject</option>
                                            </select>
                                            <button type="submit" class="btn btn-secondary btn-sm" style="padding: 0.35rem 0.65rem;">
                                                Save
                                            </button>
                                        </form>
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
