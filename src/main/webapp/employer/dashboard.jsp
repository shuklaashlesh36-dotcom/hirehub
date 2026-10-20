<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.hirehub.model.User" %>
<%@ page import="com.hirehub.model.Employer" %>
<%@ page import="com.hirehub.model.EmployerDashboardStats" %>
<%@ page import="com.hirehub.model.Application" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/login?authRequired=true");
        return;
    }
    Employer employer = (currentUser instanceof Employer) ? (Employer) currentUser : null;

    EmployerDashboardStats stats = (EmployerDashboardStats) request.getAttribute("stats");
    if (stats == null) {
        response.sendRedirect(request.getContextPath() + "/employer/dashboard");
        return;
    }

    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Employer Dashboard - HireHub</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <script src="${pageContext.request.contextPath}/js/theme.js"></script>
</head>
<body>
    <nav class="navbar">
        <a href="${pageContext.request.contextPath}/employer/dashboard" class="brand">
            HireHub <span class="brand-badge" style="background: linear-gradient(135deg, #10b981 0%, #06b6d4 100%);">Employer</span>
        </a>
        <ul class="nav-links">
            <li><a href="${pageContext.request.contextPath}/employer/dashboard" style="color: var(--text-primary); font-weight: 600;">Dashboard</a></li>
            <li><a href="${pageContext.request.contextPath}/employer/jobs">Manage Jobs</a></li>
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
        <!-- Welcome Banner -->
        <div class="glass-panel" style="margin-bottom: 2rem;">
            <div style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem;">
                <div style="display: flex; align-items: center; gap: 1.25rem;">
                    <a href="${pageContext.request.contextPath}/profile" title="Change Photo">
                        <img src="${pageContext.request.contextPath}/profile/photo" alt="<%= currentUser.getName() %>" class="avatar-lg" style="width: 80px; height: 80px;">
                    </a>
                    <div>
                        <h1 style="font-size: 1.85rem; font-weight: 800; margin-bottom: 0.35rem;">
                            <%= employer != null && employer.getCompanyName() != null ? employer.getCompanyName() : currentUser.getName() %>
                        </h1>
                        <p style="color: var(--text-secondary); font-size: 0.95rem;">
                            Representative: <strong><%= currentUser.getName() %></strong> &bull; <%= currentUser.getEmail() %>
                            <% if (employer != null && employer.getLocation() != null && !employer.getLocation().isBlank()) { %>
                                &bull; <span style="color: var(--accent-primary);"><%= employer.getLocation() %></span>
                            <% } %>
                        </p>
                    </div>
                </div>
                <div style="display: flex; gap: 0.75rem; flex-wrap: wrap;">
                    <a href="${pageContext.request.contextPath}/profile/edit" class="btn btn-secondary" style="padding: 0.5rem 0.95rem; font-size: 0.85rem;">
                        &#9998; Edit Profile
                    </a>
                    <a href="${pageContext.request.contextPath}/employer/post-job" class="btn btn-primary" style="display: inline-flex; align-items: center; gap: 0.5rem;">
                        <span>&#43;</span> Post New Job
                    </a>
                    <a href="${pageContext.request.contextPath}/employer/jobs" class="btn btn-secondary">
                        View All Jobs
                    </a>
                </div>
            </div>

            <% if (employer != null && employer.getCompanyDescription() != null && !employer.getCompanyDescription().isBlank()) { %>
                <div style="margin-top: 1.25rem; padding: 0.85rem 1.15rem; background: var(--bg-card-subtle); border-radius: 8px; color: var(--text-secondary); font-size: 0.9rem; border-left: 3px solid #10b981;">
                    <strong>About Company:</strong> <%= employer.getCompanyDescription() %>
                </div>
            <% } %>
        </div>

        <!-- Real-Time Database Metrics -->
        <div class="dashboard-grid">
            <div class="stat-card">
                <span class="stat-label">Total Jobs Posted</span>
                <span class="stat-value" style="color: #60a5fa;"><%= stats.getTotalJobs() %></span>
                <p style="color: var(--text-muted); font-size: 0.85rem;">Total postings created by your account</p>
            </div>
            <div class="stat-card">
                <span class="stat-label">Active / Open Jobs</span>
                <span class="stat-value" style="color: #34d399;"><%= stats.getActiveJobs() %></span>
                <p style="color: var(--text-muted); font-size: 0.85rem;">Jobs currently accepting candidate applications</p>
            </div>
            <div class="stat-card">
                <span class="stat-label">Total Applicants</span>
                <span class="stat-value" style="color: #a78bfa;"><%= stats.getTotalApplicants() %></span>
                <p style="color: var(--text-muted); font-size: 0.85rem;">Candidate submissions across all your listings</p>
            </div>
        </div>

        <!-- Quick Access Shortcuts -->
        <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 1.25rem; margin-top: 2rem;">
            <div class="glass-panel" style="padding: 1.25rem;">
                <h3 style="font-size: 1.1rem; margin-bottom: 0.5rem; color: var(--text-primary);">&#128221; Post a New Job</h3>
                <p style="color: var(--text-secondary); font-size: 0.88rem; margin-bottom: 1rem;">
                    Publish a new career opportunity with custom salary, skills, and job description.
                </p>
                <a href="${pageContext.request.contextPath}/employer/post-job" class="btn btn-primary btn-sm">Create Job Listing &rarr;</a>
            </div>
            <div class="glass-panel" style="padding: 1.25rem;">
                <h3 style="font-size: 1.1rem; margin-bottom: 0.5rem; color: var(--text-primary);">&#128188; Manage Postings</h3>
                <p style="color: var(--text-secondary); font-size: 0.88rem; margin-bottom: 1rem;">
                    Edit listings, close filled roles, or reopen past postings.
                </p>
                <a href="${pageContext.request.contextPath}/employer/jobs" class="btn btn-secondary btn-sm">Manage Jobs &rarr;</a>
            </div>
            <div class="glass-panel" style="padding: 1.25rem;">
                <h3 style="font-size: 1.1rem; margin-bottom: 0.5rem; color: var(--text-primary);">&#128101; Review Applicants</h3>
                <p style="color: var(--text-secondary); font-size: 0.88rem; margin-bottom: 1rem;">
                    Download verified PDF resumes, evaluate candidates, and update application statuses.
                </p>
                <a href="${pageContext.request.contextPath}/employer/applications" class="btn btn-secondary btn-sm">View Applicants &rarr;</a>
            </div>
        </div>

        <!-- Recent Applications Table -->
        <div class="glass-panel" style="margin-top: 2rem;">
            <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.25rem;">
                <div>
                    <h2 style="font-size: 1.35rem; font-weight: 700;">Recent Candidate Submissions</h2>
                    <p style="color: var(--text-secondary); font-size: 0.88rem;">Latest applications received for your posted jobs</p>
                </div>
                <a href="${pageContext.request.contextPath}/employer/applications" style="color: var(--accent-primary); text-decoration: none; font-size: 0.9rem; font-weight: 600;">
                    View All Applicants &rarr;
                </a>
            </div>

            <% if (stats.getRecentApplications().isEmpty()) { %>
                <div style="text-align: center; padding: 2.5rem 1rem; color: var(--text-muted);">
                    <div style="font-size: 2.5rem; margin-bottom: 0.75rem;">&#128196;</div>
                    <p style="font-size: 1rem; font-weight: 600; color: var(--text-secondary); margin-bottom: 0.25rem;">No Applications Received Yet</p>
                    <p style="font-size: 0.88rem;">When candidates apply for your active jobs, their submissions will appear here.</p>
                </div>
            <% } else { %>
                <div class="data-table-wrapper">
                    <table class="data-table">
                        <thead>
                            <tr>
                                <th>Candidate</th>
                                <th>Position</th>
                                <th>Date Applied</th>
                                <th>Status</th>
                                <th>Actions</th>
                            </tr>
                        </thead>
                        <tbody>
                            <% for (Application app : stats.getRecentApplications()) { %>
                                <tr>
                                    <td>
                                        <strong><%= app.getSeekerName() != null ? app.getSeekerName() : "Candidate #" + app.getSeekerId() %></strong>
                                        <div style="color: var(--text-muted); font-size: 0.8rem;"><%= app.getSeekerEmail() != null ? app.getSeekerEmail() : "" %></div>
                                    </td>
                                    <td>
                                        <span style="font-weight: 600; color: var(--text-primary);"><%= app.getJobTitle() %></span>
                                        <% if (app.getJobLocation() != null) { %>
                                            <span style="color: var(--text-muted); font-size: 0.8rem;"> &bull; <%= app.getJobLocation() %></span>
                                        <% } %>
                                    </td>
                                    <td style="color: var(--text-secondary);">
                                        <%= app.getAppliedAt() != null ? dateFormat.format(app.getAppliedAt()) : "Recently" %>
                                    </td>
                                    <td>
                                        <%
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
                                        <span class="badge <%= statusClass %>"><%= app.getStatus() %></span>
                                    </td>
                                    <td>
                                        <div style="display: flex; gap: 0.5rem;">
                                            <% if (app.getResumeFileName() != null) { %>
                                                <a href="${pageContext.request.contextPath}/employer/resume/download?applicationId=<%= app.getId() %>" target="_blank" class="btn btn-secondary btn-sm" title="Download Candidate Resume">
                                                    &#128196; Resume
                                                </a>
                                            <% } %>
                                            <a href="${pageContext.request.contextPath}/employer/applications?jobId=<%= app.getJobId() %>" class="btn btn-primary btn-sm">
                                                Review
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
