<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.hirehub.model.User" %>
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

    @SuppressWarnings("unchecked")
    List<Application> applications = (List<Application>) request.getAttribute("applications");
    String successMessage = (String) request.getAttribute("successMessage");

    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>My Applications - HireHub</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <script src="${pageContext.request.contextPath}/js/theme.js"></script>
    <style>
        .app-card {
            background: var(--bg-card);
            border: 1px solid var(--border-color);
            border-radius: var(--radius-md);
            padding: 1.5rem;
            margin-bottom: 1.25rem;
            display: flex;
            justify-content: space-between;
            align-items: center;
            flex-wrap: wrap;
            gap: 1.25rem;
            transition: transform 0.2s ease, border-color 0.2s ease, box-shadow 0.2s ease;
            box-shadow: var(--shadow-card);
        }
        .app-card:hover {
            transform: translateY(-2px);
            border-color: var(--border-glow);
            box-shadow: var(--shadow-hover);
        }
        .status-badge {
            display: inline-flex;
            align-items: center;
            gap: 0.35rem;
            padding: 0.35rem 0.85rem;
            border-radius: 9999px;
            font-size: 0.8rem;
            font-weight: 700;
            text-transform: uppercase;
            letter-spacing: 0.05em;
        }
        .status-applied {
            background: var(--info-bg);
            color: var(--info-text);
            border: 1px solid var(--info-border);
        }
        .status-shortlisted {
            background: var(--warning-bg);
            color: var(--warning-text);
            border: 1px solid var(--warning-border);
        }
        .status-hired {
            background: var(--success-bg);
            color: var(--success-text);
            border: 1px solid var(--success-border);
        }
        .status-rejected {
            background: var(--error-bg);
            color: var(--error-text);
            border: 1px solid var(--error-border);
        }
    </style>
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
            <a href="${pageContext.request.contextPath}/seeker/dashboard.jsp" class="btn btn-secondary" style="padding: 0.45rem 0.85rem; font-size: 0.85rem;">
                &#128100; Dashboard
            </a>
            <a href="${pageContext.request.contextPath}/seeker/jobs" class="btn btn-secondary" style="padding: 0.45rem 0.85rem; font-size: 0.85rem;">
                &#128269; Browse Jobs
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

    <div class="container" style="max-width: 950px; margin-top: 2rem;">

        <!-- Header -->
        <div style="margin-bottom: 2rem;">
            <h1 style="font-size: 2rem; font-weight: 800; margin-bottom: 0.5rem; color: var(--text-primary);">
                My Job Applications
            </h1>
            <p style="color: var(--text-secondary); font-size: 1rem;">
                Track the progress of your submitted applications and interview workflows in real-time.
            </p>
        </div>

        <!-- Success Alert -->
        <% if (successMessage != null && !successMessage.isBlank()) { %>
            <div class="alert alert-success" style="margin-bottom: 1.5rem;">
                &#10003; <%= successMessage %>
            </div>
        <% } %>

        <!-- Summary Bar -->
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 1.25rem;">
            <div style="font-size: 0.95rem; color: var(--text-secondary);">
                Total Applications: <strong><%= (applications != null) ? applications.size() : 0 %></strong>
            </div>
            <a href="${pageContext.request.contextPath}/seeker/jobs" class="btn btn-primary" style="padding: 0.5rem 1.15rem; font-size: 0.85rem;">
                + Apply for More Jobs
            </a>
        </div>

        <!-- Applications List -->
        <% if (applications == null || applications.isEmpty()) { %>
            <div class="glass-panel" style="text-align: center; padding: 4rem 2rem;">
                <div style="font-size: 3rem; margin-bottom: 1rem;">&#128196;</div>
                <h3 style="font-size: 1.35rem; font-weight: 700; margin-bottom: 0.5rem;">No Applications Yet</h3>
                <p style="color: var(--text-secondary); max-width: 480px; margin: 0 auto 1.5rem auto;">
                    You have not submitted any job applications yet. Browse verified employer job openings and submit your first application.
                </p>
                <a href="${pageContext.request.contextPath}/seeker/jobs" class="btn btn-primary">
                    &#128269; Explore Available Jobs
                </a>
            </div>
        <% } else { %>
            <div style="display: flex; flex-direction: column;">
                <% for (Application app : applications) { 
                    String statusClass = "status-applied";
                    String statusIcon = "&#9203;";
                    if (app.getStatus() == ApplicationStatus.SHORTLISTED) {
                        statusClass = "status-shortlisted";
                        statusIcon = "&#11088;";
                    } else if (app.getStatus() == ApplicationStatus.HIRED) {
                        statusClass = "status-hired";
                        statusIcon = "&#127881;";
                    } else if (app.getStatus() == ApplicationStatus.REJECTED) {
                        statusClass = "status-rejected";
                        statusIcon = "&#10060;";
                    }
                %>
                    <div class="app-card">
                        <div>
                            <h2 style="font-size: 1.25rem; font-weight: 700; color: var(--text-primary); margin-bottom: 0.35rem;">
                                <%= app.getJobTitle() %>
                            </h2>
                            <div style="color: var(--accent-primary); font-weight: 600; font-size: 0.95rem; margin-bottom: 0.4rem; display: flex; align-items: center; gap: 0.4rem;">
                                <span>&#127970;</span> <%= app.getCompanyName() %>
                                <% if (app.getJobLocation() != null && !app.getJobLocation().isBlank()) { %>
                                    &bull; <span style="color: var(--text-secondary); font-weight: 400;">&#128205; <%= app.getJobLocation() %></span>
                                <% } %>
                            </div>
                            <div style="font-size: 0.85rem; color: var(--text-muted); display: flex; gap: 1rem; flex-wrap: wrap;">
                                <span>Applied: <%= (app.getAppliedAt() != null) ? dateFormat.format(app.getAppliedAt()) : "Recently" %></span>
                                <% if (app.getResumeFileName() != null && !app.getResumeFileName().isBlank()) { %>
                                    <span>&#128196; Resume: <em><%= app.getResumeFileName() %></em></span>
                                <% } else { %>
                                    <span style="color: #fbbf24;">(No resume was attached)</span>
                                <% } %>
                            </div>
                        </div>

                        <div style="display: flex; flex-direction: column; align-items: flex-end; gap: 0.5rem;">
                            <span class="status-badge <%= statusClass %>">
                                <span><%= statusIcon %></span> <%= app.getStatus() %>
                            </span>
                        </div>
                    </div>
                <% } %>
            </div>
        <% } %>

    </div>

    <footer class="footer" style="margin-top: 3rem;">
        HireHub &copy; 2026. Online Job Portal.
    </footer>
</body>
</html>
