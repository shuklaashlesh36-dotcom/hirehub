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

    String errorMessage = (String) request.getAttribute("errorMessage");
    String dashboardUrl = isEmployer ? (request.getContextPath() + "/employer/dashboard")
            : (request.getContextPath() + "/seeker/dashboard.jsp");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Edit Profile - HireHub</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <script src="${pageContext.request.contextPath}/js/theme.js"></script>
    <style>
        .photo-upload-container {
            display: flex;
            align-items: center;
            gap: 1.5rem;
            flex-wrap: wrap;
            padding: 1.5rem;
            background: var(--bg-card-subtle);
            border: 1px dashed var(--border-color);
            border-radius: var(--radius-md);
            margin-bottom: 2rem;
        }
        .preview-avatar {
            width: 100px;
            height: 100px;
            border-radius: 50%;
            object-fit: cover;
            border: 3px solid rgba(99, 102, 241, 0.4);
            background: var(--bg-input);
        }
    </style>
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
            <a href="${pageContext.request.contextPath}/profile" class="btn btn-secondary" style="padding: 0.45rem 0.85rem; font-size: 0.85rem;">
                &larr; View Profile
            </a>
            <div class="user-badge" style="display: flex; align-items: center; gap: 0.5rem;">
                <img src="${pageContext.request.contextPath}/profile/photo" alt="Avatar" class="avatar-nav">
                <span><strong><%= currentUser.getName() %></strong></span>
            </div>
            <a href="${pageContext.request.contextPath}/logout" class="btn btn-secondary" style="padding: 0.45rem 0.85rem; font-size: 0.85rem;">
                Sign Out
            </a>
        </div>
    </nav>

    <div class="container" style="max-width: 800px;">
        <% if (errorMessage != null && !errorMessage.isBlank()) { %>
            <div class="alert alert-error">
                <span>&#9888;</span> <%= errorMessage %>
            </div>
        <% } %>

        <div class="glass-panel" style="margin-bottom: 2rem;">
            <div style="margin-bottom: 1.5rem;">
                <h1 style="font-size: 1.75rem; font-weight: 800; margin-bottom: 0.35rem;">Edit Profile</h1>
                <p style="color: var(--text-secondary); font-size: 0.95rem;">
                    Update your account details and profile display photo (DP).
                </p>
            </div>

            <!-- Profile Photo Upload Box with Instant Preview -->
            <div class="photo-upload-container">
                <div>
                    <img id="avatarPreview" src="${pageContext.request.contextPath}/profile/photo" alt="Current Photo" class="preview-avatar">
                </div>
                <div style="flex: 1; min-width: 240px;">
                    <h3 style="font-size: 1.05rem; font-weight: 700; margin-bottom: 0.35rem;">Profile Photo (DP)</h3>
                    <p style="font-size: 0.85rem; color: var(--text-muted); margin-bottom: 0.85rem;">
                        Upload a JPEG, PNG, or WebP image. Maximum size: 2 MB.
                    </p>

                    <form id="photoUploadForm" method="POST" action="${pageContext.request.contextPath}/profile/photo/upload" enctype="multipart/form-data" style="display: flex; gap: 0.5rem; flex-wrap: wrap; align-items: center;">
                        <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                        <input type="file" id="photoInput" name="photo" accept="image/jpeg,image/png,image/webp" style="display: none;" onchange="previewSelectedImage(this)">
                        <button type="button" class="btn btn-secondary" style="padding: 0.45rem 0.95rem; font-size: 0.85rem;" onclick="document.getElementById('photoInput').click()">
                            &#128247; Choose Photo
                        </button>
                        <button type="submit" id="savePhotoBtn" class="btn btn-primary" style="display: none; padding: 0.45rem 0.95rem; font-size: 0.85rem;">
                            Upload & Save Photo
                        </button>
                    </form>

                    <% if (currentUser.getProfilePhoto() != null && !currentUser.getProfilePhoto().isBlank()) { %>
                        <form method="POST" action="${pageContext.request.contextPath}/profile/photo/remove" style="margin-top: 0.5rem;" onsubmit="return confirm('Remove your profile photo?');">
                            <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                            <button type="submit" class="btn btn-danger" style="padding: 0.35rem 0.75rem; font-size: 0.8rem;">
                                &#128465; Remove Photo (Reset to Default)
                            </button>
                        </form>
                    <% } %>
                    <span id="photoFileName" style="display: block; font-size: 0.8rem; color: var(--accent-primary); margin-top: 0.35rem;"></span>
                </div>
            </div>

            <!-- Profile Details Form -->
            <form method="POST" action="${pageContext.request.contextPath}/profile/edit">
                <input type="hidden" name="csrfToken" value="<%= csrfToken %>">
                <input type="hidden" name="userId" value="<%= currentUser.getId() %>">

                <div class="form-row">
                    <div class="form-group">
                        <label for="name">
                            <%= isEmployer ? "Contact Person Name *" : "Full Name *" %>
                        </label>
                        <input type="text" id="name" name="name" class="form-control"
                               value="<%= currentUser.getName() != null ? currentUser.getName() : "" %>" required maxlength="100">
                    </div>

                    <div class="form-group">
                        <label for="email">Email Address (Read-only)</label>
                        <input type="email" id="email" class="form-control"
                               value="<%= currentUser.getEmail() %>" disabled style="opacity: 0.7; cursor: not-allowed;">
                        <small style="color: var(--text-muted); font-size: 0.75rem;">Login email cannot be changed from profile.</small>
                    </div>
                </div>

                <% if (isSeeker) { %>
                    <div class="form-row">
                        <div class="form-group">
                            <label for="phone">Contact Phone</label>
                            <input type="text" id="phone" name="phone" class="form-control"
                                   value="<%= (seeker != null && seeker.getPhone() != null) ? seeker.getPhone() : "" %>"
                                   placeholder="e.g. +1 555-0192" maxlength="20">
                        </div>

                        <div class="form-group">
                            <label for="education">Highest Education</label>
                            <input type="text" id="education" name="education" class="form-control"
                                   value="<%= (seeker != null && seeker.getEducation() != null) ? seeker.getEducation() : "" %>"
                                   placeholder="e.g. B.Tech Computer Science, MS, etc." maxlength="100">
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="headline">Professional Headline / Bio</label>
                        <input type="text" id="headline" name="headline" class="form-control"
                               value="<%= (seeker != null && seeker.getHeadline() != null) ? seeker.getHeadline() : "" %>"
                               placeholder="e.g. Senior Java Full-Stack Engineer with 5+ years experience" maxlength="200">
                    </div>

                    <div class="form-group">
                        <label for="skills">Skills & Core Competencies</label>
                        <input type="text" id="skills" name="skills" class="form-control"
                               value="<%= (seeker != null && seeker.getSkills() != null) ? seeker.getSkills() : "" %>"
                               placeholder="e.g. Java, Spring, MySQL, React, REST APIs" maxlength="255">
                        <small style="color: var(--text-muted); font-size: 0.75rem;">Comma-separated list of skills.</small>
                    </div>

                <% } else if (isEmployer) { %>
                    <div class="form-row">
                        <div class="form-group">
                            <label for="companyName">Company Name *</label>
                            <input type="text" id="companyName" name="companyName" class="form-control"
                                   value="<%= (employer != null && employer.getCompanyName() != null) ? employer.getCompanyName() : "" %>"
                                   required maxlength="100">
                        </div>

                        <div class="form-group">
                            <label for="phone">Contact Phone</label>
                            <input type="text" id="phone" name="phone" class="form-control"
                                   value="<%= (employer != null && employer.getPhone() != null) ? employer.getPhone() : "" %>"
                                   placeholder="e.g. +1 555-0199" maxlength="20">
                        </div>
                    </div>

                    <div class="form-row">
                        <div class="form-group">
                            <label for="website">Company Website</label>
                            <input type="url" id="website" name="website" class="form-control"
                                   value="<%= (employer != null && employer.getWebsite() != null) ? employer.getWebsite() : "" %>"
                                   placeholder="https://example.com" maxlength="150">
                        </div>

                        <div class="form-group">
                            <label for="location">Company Location / Headquarters</label>
                            <input type="text" id="location" name="location" class="form-control"
                                   value="<%= (employer != null && employer.getLocation() != null) ? employer.getLocation() : "" %>"
                                   placeholder="e.g. San Francisco, CA / Remote" maxlength="100">
                        </div>
                    </div>

                    <div class="form-group">
                        <label for="companyDescription">Company Description</label>
                        <textarea id="companyDescription" name="companyDescription" class="form-control" rows="4"
                                  placeholder="Describe your organization, mission, and work environment..." maxlength="1000"><%= (employer != null && employer.getCompanyDescription() != null) ? employer.getCompanyDescription() : "" %></textarea>
                    </div>
                <% } %>

                <div style="display: flex; gap: 1rem; justify-content: flex-end; margin-top: 1.5rem; border-top: 1px solid var(--border-color); padding-top: 1.25rem;">
                    <a href="${pageContext.request.contextPath}/profile" class="btn btn-secondary">
                        Cancel
                    </a>
                    <button type="submit" class="btn btn-primary" id="saveProfileBtn">
                        Save Changes
                    </button>
                </div>
            </form>
        </div>
    </div>

    <script>
        function previewSelectedImage(input) {
            if (input.files && input.files[0]) {
                const file = input.files[0];
                const validTypes = ['image/jpeg', 'image/png', 'image/webp'];
                if (!validTypes.includes(file.type)) {
                    alert('Invalid file format! Please select a JPEG, PNG, or WebP image.');
                    input.value = '';
                    return;
                }
                if (file.size > 2 * 1024 * 1024) {
                    alert('Image file exceeds the 2 MB limit! Please select a smaller photo.');
                    input.value = '';
                    return;
                }

                const reader = new FileReader();
                reader.onload = function(e) {
                    document.getElementById('avatarPreview').src = e.target.result;
                    document.getElementById('savePhotoBtn').style.display = 'inline-flex';
                    document.getElementById('photoFileName').textContent = 'Selected: ' + file.name + ' (' + Math.round(file.size / 1024) + ' KB)';
                };
                reader.readAsDataURL(file);
            }
        }
    </script>

    <footer class="footer">
        HireHub &copy; 2026. Online Job Portal.
    </footer>
</body>
</html>
