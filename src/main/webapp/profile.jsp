<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.hirehub.model.User" %>
<%@ page import="com.hirehub.model.JobSeeker" %>
<%@ page import="com.hirehub.model.Employer" %>
<%@ page import="com.hirehub.model.Role" %>
<%@ page import="com.hirehub.util.CsrfUtil" %>
<%
    User currentUser = (User) request.getAttribute("profileUser");
    if (currentUser == null) {
        currentUser = (User) session.getAttribute("currentUser");
    }
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/login?authRequired=true");
        return;
    }

    boolean isSeeker = (currentUser.getRole() == Role.JOB_SEEKER);
    boolean isEmployer = (currentUser.getRole() == Role.EMPLOYER);
    JobSeeker seeker = isSeeker ? (JobSeeker) currentUser : null;
    Employer employer = isEmployer ? (Employer) currentUser : null;

    String csrfToken = (String) request.getAttribute("csrfToken");
    if (csrfToken == null) {
        csrfToken = CsrfUtil.getToken(session);
    }

    String dashboardUrl = isEmployer ? (request.getContextPath() + "/employer/dashboard")
            : (request.getContextPath() + "/seeker/dashboard.jsp");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title><%= currentUser.getName() %> - Profile - HireHub</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <script src="${pageContext.request.contextPath}/js/theme.js"></script>
</head>
<body>
    <nav class="navbar">
        <a href="<%= dashboardUrl %>" class="brand">
            HireHub <span class="brand-badge"><%= isEmployer ? "Employer" : "Candidate" %></span>
        </a>
        <div style="display: flex; align-items: center; gap: 0.75rem; flex-wrap: wrap;">
            <button type="button" class="theme-toggle" aria-label="Toggle theme" title="Toggle Light/Dark Theme">
                <span class="theme-toggle-icon" aria-hidden="true">&#9728;&#65039;</span>
                <span class="theme-toggle-text">Theme</span>
            </button>
            <a href="<%= dashboardUrl %>" class="btn btn-secondary" style="padding: 0.45rem 0.85rem; font-size: 0.85rem;">
                &larr; Dashboard
            </a>
            <% if (isSeeker) { %>
                <a href="${pageContext.request.contextPath}/seeker/jobs" class="btn btn-secondary" style="padding: 0.45rem 0.85rem; font-size: 0.85rem;">
                    Browse Jobs
                </a>
                <a href="${pageContext.request.contextPath}/seeker/applications" class="btn btn-secondary" style="padding: 0.45rem 0.85rem; font-size: 0.85rem;">
                    My Applications
                </a>
            <% } else if (isEmployer) { %>
                <a href="${pageContext.request.contextPath}/employer/jobs" class="btn btn-secondary" style="padding: 0.45rem 0.85rem; font-size: 0.85rem;">
                    Manage Jobs
                </a>
                <a href="${pageContext.request.contextPath}/employer/applications" class="btn btn-secondary" style="padding: 0.45rem 0.85rem; font-size: 0.85rem;">
                    Applicants
                </a>
            <% } %>
            <div class="user-badge" style="display: flex; align-items: center; gap: 0.5rem;">
                <img src="${pageContext.request.contextPath}/profile/photo" alt="Avatar" class="avatar-nav">
                <span><strong><%= currentUser.getName() %></strong></span>
            </div>
            <a href="${pageContext.request.contextPath}/logout" class="btn btn-secondary" style="padding: 0.45rem 0.85rem; font-size: 0.85rem;">
                Sign Out
            </a>
        </div>
    </nav>

    <div class="container" style="max-width: 860px;">
        <% if (request.getParameter("success") != null) { %>
            <div class="alert alert-success">
                <span>&#10003;</span> <%= request.getParameter("success") %>
            </div>
        <% } %>
        <% if (request.getParameter("photoSuccess") != null) { %>
            <div class="alert alert-success">
                <span>&#10003;</span> <%= request.getParameter("photoSuccess") %>
            </div>
        <% } %>
        <% if (request.getParameter("error") != null) { %>
            <div class="alert alert-error">
                <span>&#9888;</span> <%= request.getParameter("error") %>
            </div>
        <% } %>

        <div class="glass-panel" style="margin-bottom: 2rem;">
            <div style="display: flex; align-items: center; justify-content: space-between; flex-wrap: wrap; gap: 1.5rem; margin-bottom: 1.5rem;">
                <div style="display: flex; align-items: center; gap: 1.5rem; flex-wrap: wrap;">
                    <img src="${pageContext.request.contextPath}/profile/photo" alt="<%= currentUser.getName() %>" class="avatar-lg" id="profileAvatarImg">
                    <div>
                        <h1 style="font-size: 1.85rem; font-weight: 800; margin-bottom: 0.35rem;">
                            <%= currentUser.getName() %>
                        </h1>
                        <div style="display: flex; align-items: center; gap: 0.5rem; flex-wrap: wrap;">
                            <span class="badge <%= isEmployer ? "badge-approved" : "badge-hired" %>">
                                <%= currentUser.getRole() %>
                            </span>
                            <span style="color: var(--text-secondary); font-size: 0.9rem;">
                                <%= currentUser.getEmail() %>
                            </span>
                        </div>
                        <% if (isSeeker && seeker != null && seeker.getHeadline() != null && !seeker.getHeadline().isBlank()) { %>
                            <p style="color: var(--accent-primary); font-size: 0.95rem; margin-top: 0.5rem; font-style: italic;">
                                "<%= seeker.getHeadline() %>"
                            </p>
                        <% } %>
                        <% if (isEmployer && employer != null && employer.getCompanyName() != null && !employer.getCompanyName().isBlank()) { %>
                            <p style="color: var(--color-success); font-size: 0.95rem; margin-top: 0.5rem; font-weight: 600;">
                                <%= employer.getCompanyName() %>
                            </p>
                        <% } %>
                    </div>
                </div>

                <div style="display: flex; gap: 0.75rem; flex-wrap: wrap;">
                    <a href="${pageContext.request.contextPath}/profile/edit" class="btn btn-primary" id="editProfileBtn">
                        &#9998; Edit Profile
                    </a>
                    <% if (currentUser.getProfilePhoto() != null && !currentUser.getProfilePhoto().isBlank()) { %>
                        <form method="POST" action="${pageContext.request.contextPath}/profile/photo/remove" style="display: inline;" onsubmit="return confirm('Remove your profile photo and revert to default avatar?');">
                            <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                            <button type="submit" class="btn btn-danger" style="padding: 0.75rem 1rem;" title="Remove Photo">
                                &#128465; Remove DP
                            </button>
                        </form>
                    <% } %>
                </div>
            </div>

            <div style="border-top: 1px solid var(--border-color); padding-top: 1.5rem; margin-top: 1rem;">
                <h3 style="font-size: 1.15rem; font-weight: 700; margin-bottom: 1.25rem;">
                    <%= isEmployer ? "Company & Account Details" : "Candidate Profile Details" %>
                </h3>

                <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(280px, 1fr)); gap: 1.25rem;">
                    <div>
                        <div class="profile-field-label">Full Name</div>
                        <div class="profile-field-value"><%= currentUser.getName() %></div>
                    </div>

                    <div>
                        <div class="profile-field-label">Email Address (Login Identity)</div>
                        <div class="profile-field-value">
                            <%= currentUser.getEmail() %>
                            <span style="font-size: 0.75rem; color: var(--text-muted); margin-left: 0.35rem;">(Read-only)</span>
                        </div>
                    </div>

                    <% if (isSeeker) { %>
                        <div>
                            <div class="profile-field-label">Phone Number</div>
                            <div class="profile-field-value">
                                <%= (seeker != null && seeker.getPhone() != null && !seeker.getPhone().isBlank()) ? seeker.getPhone() : "<span style='color: var(--text-muted);'>Not specified</span>" %>
                            </div>
                        </div>

                        <div>
                            <div class="profile-field-label">Highest Education</div>
                            <div class="profile-field-value">
                                <%= (seeker != null && seeker.getEducation() != null && !seeker.getEducation().isBlank()) ? seeker.getEducation() : "<span style='color: var(--text-muted);'>Not specified</span>" %>
                            </div>
                        </div>

                        <div style="grid-column: 1 / -1;">
                            <div class="profile-field-label">Skills & Expertise</div>
                            <div class="profile-field-value">
                                <%= (seeker != null && seeker.getSkills() != null && !seeker.getSkills().isBlank()) ? seeker.getSkills() : "<span style='color: var(--text-muted);'>Not specified</span>" %>
                            </div>
                        </div>

                        <div style="grid-column: 1 / -1;">
                            <div class="profile-field-label">Professional Headline / Bio</div>
                            <div class="profile-field-value">
                                <%= (seeker != null && seeker.getHeadline() != null && !seeker.getHeadline().isBlank()) ? seeker.getHeadline() : "<span style='color: var(--text-muted);'>Not specified</span>" %>
                            </div>
                        </div>
                    <% } else if (isEmployer) { %>
                        <div>
                            <div class="profile-field-label">Company Name</div>
                            <div class="profile-field-value">
                                <%= (employer != null && employer.getCompanyName() != null && !employer.getCompanyName().isBlank()) ? employer.getCompanyName() : "<span style='color: var(--text-muted);'>Not specified</span>" %>
                            </div>
                        </div>

                        <div>
                            <div class="profile-field-label">Contact Phone</div>
                            <div class="profile-field-value">
                                <%= (employer != null && employer.getPhone() != null && !employer.getPhone().isBlank()) ? employer.getPhone() : "<span style='color: var(--text-muted);'>Not specified</span>" %>
                            </div>
                        </div>

                        <div>
                            <div class="profile-field-label">Company Website</div>
                            <div class="profile-field-value">
                                <% if (employer != null && employer.getWebsite() != null && !employer.getWebsite().isBlank()) { %>
                                    <a href="<%= employer.getWebsite() %>" target="_blank" rel="noopener noreferrer" style="color: var(--accent-primary); text-decoration: underline;">
                                        <%= employer.getWebsite() %>
                                    </a>
                                <% } else { %>
                                    <span style="color: var(--text-muted);">Not specified</span>
                                <% } %>
                            </div>
                        </div>

                        <div>
                            <div class="profile-field-label">Company Location / HQ</div>
                            <div class="profile-field-value">
                                <%= (employer != null && employer.getLocation() != null && !employer.getLocation().isBlank()) ? employer.getLocation() : "<span style='color: var(--text-muted);'>Not specified</span>" %>
                            </div>
                        </div>

                        <div style="grid-column: 1 / -1;">
                            <div class="profile-field-label">Company Description</div>
                            <div class="profile-field-value">
                                <%= (employer != null && employer.getCompanyDescription() != null && !employer.getCompanyDescription().isBlank()) ? employer.getCompanyDescription() : "<span style='color: var(--text-muted);'>Not specified</span>" %>
                            </div>
                        </div>
                    <% } %>
                </div>
            </div>

            <div style="display: flex; justify-content: space-between; align-items: center; margin-top: 2rem; border-top: 1px solid var(--border-color); padding-top: 1.25rem;">
                <a href="<%= dashboardUrl %>" class="btn btn-secondary">
                    &larr; Back to Dashboard
                </a>
                <a href="${pageContext.request.contextPath}/profile/edit" class="btn btn-primary">
                    Edit Profile Details &rarr;
                </a>
            </div>
        </div>
    </div>

    <footer class="footer">
        HireHub &copy; 2026. Online Job Portal.
    </footer>
</body>
</html>
