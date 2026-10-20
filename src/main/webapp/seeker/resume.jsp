<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.hirehub.model.User" %>
<%@ page import="com.hirehub.model.Resume" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%
    User currentUser = (User) session.getAttribute("currentUser");
    if (currentUser == null) {
        response.sendRedirect(request.getContextPath() + "/login?authRequired=true");
        return;
    }

    Resume resume = (Resume) request.getAttribute("resume");
    String csrfToken = (String) request.getAttribute("csrfToken");
    String errorMessage = (String) request.getAttribute("errorMessage");
    String successMessage = (String) request.getAttribute("successMessage");

    SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Resume Management - HireHub</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <script src="${pageContext.request.contextPath}/js/theme.js"></script>
    <style>
        .upload-dropzone {
            border: 2px dashed rgba(99, 102, 241, 0.4);
            background: var(--bg-dropzone);
            border-radius: var(--radius-lg);
            padding: 2.5rem 1.5rem;
            text-align: center;
            transition: border-color 0.2s ease, background 0.2s ease;
            cursor: pointer;
            margin-bottom: 1.5rem;
        }
        .upload-dropzone:hover {
            border-color: var(--accent-primary);
            background: rgba(99, 102, 241, 0.08);
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
            <a href="${pageContext.request.contextPath}/seeker/applications" class="btn btn-secondary" style="padding: 0.45rem 0.85rem; font-size: 0.85rem;">
                &#128196; My Applications
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

    <div class="container" style="max-width: 860px; margin-top: 2rem;">

        <!-- Header -->
        <div style="margin-bottom: 2rem;">
            <h1 style="font-size: 2rem; font-weight: 800; margin-bottom: 0.5rem; color: var(--text-primary);">
                Resume Management
            </h1>
            <p style="color: var(--text-secondary); font-size: 1rem;">
                Keep your CV up to date. Your uploaded PDF resume is automatically attached when you apply for jobs.
            </p>
        </div>

        <!-- Feedback Alerts -->
        <% if (errorMessage != null && !errorMessage.isBlank()) { %>
            <div class="alert alert-error" style="margin-bottom: 1.5rem;">
                &#9888; <%= errorMessage %>
            </div>
        <% } %>

        <% if (successMessage != null && !successMessage.isBlank()) { %>
            <div class="alert alert-success" style="margin-bottom: 1.5rem;">
                &#10003; <%= successMessage %>
            </div>
        <% } %>

        <!-- Current Resume Card -->
        <% if (resume != null) { %>
            <div class="glass-panel" style="padding: 2rem; margin-bottom: 2rem; border-color: rgba(16, 185, 129, 0.35);">
                <div style="display: flex; justify-content: space-between; align-items: flex-start; flex-wrap: wrap; gap: 1rem;">
                    <div>
                        <span style="font-size: 0.8rem; text-transform: uppercase; letter-spacing: 0.05em; color: var(--success-text); font-weight: 700; display: inline-flex; align-items: center; gap: 0.35rem; margin-bottom: 0.5rem;">
                            &#10004; Active Resume On File
                        </span>
                        <h2 style="font-size: 1.35rem; font-weight: 700; color: var(--text-primary); margin-bottom: 0.35rem; display: flex; align-items: center; gap: 0.5rem;">
                            <span>&#128196;</span> <%= resume.getFileName() %>
                        </h2>
                        <p style="color: var(--text-secondary); font-size: 0.9rem;">
                            Uploaded: <%= (resume.getUploadedAt() != null) ? dateFormat.format(resume.getUploadedAt()) : "Recently" %>
                        </p>
                    </div>
                    <div style="display: flex; gap: 0.75rem;">
                        <a href="${pageContext.request.contextPath}/seeker/resume/download" target="_blank" class="btn btn-primary" style="padding: 0.55rem 1.25rem;">
                            &#128065; View / Download PDF
                        </a>
                    </div>
                </div>
            </div>
        <% } else { %>
            <div class="glass-panel" style="padding: 2rem; margin-bottom: 2rem; border-color: rgba(245, 158, 11, 0.3);">
                <div style="display: flex; align-items: center; gap: 1rem;">
                    <div style="font-size: 2.25rem;">&#128228;</div>
                    <div>
                        <h3 style="font-size: 1.15rem; font-weight: 700; margin-bottom: 0.25rem; color: var(--warning-text);">No Resume Uploaded Yet</h3>
                        <p style="color: var(--text-secondary); font-size: 0.9rem;">
                            Upload your PDF resume below. Having a resume ready ensures verified employers can review your qualifications immediately.
                        </p>
                    </div>
                </div>
            </div>
        <% } %>

        <!-- Upload Form -->
        <div class="glass-panel" style="padding: 2rem;">
            <h2 style="font-size: 1.25rem; font-weight: 700; margin-bottom: 0.5rem;">
                <%= (resume != null) ? "Replace Resume" : "Upload Resume (PDF)" %>
            </h2>
            <p style="color: var(--text-secondary); font-size: 0.9rem; margin-bottom: 1.5rem;">
                Accepted format: <strong>.pdf</strong> only &bull; Maximum file size: <strong>5 MB</strong>
            </p>

            <form action="${pageContext.request.contextPath}/seeker/resume" method="post" enctype="multipart/form-data">
                <input type="hidden" name="csrfToken" value="<%= csrfToken %>">

                <div class="upload-dropzone" onclick="document.getElementById('fileInput').click()">
                    <div style="font-size: 2.5rem; margin-bottom: 0.75rem;">&#128193;</div>
                    <p style="font-weight: 600; font-size: 1.05rem; margin-bottom: 0.25rem;">
                        Click to select your PDF resume
                    </p>
                    <p style="color: var(--text-muted); font-size: 0.85rem;" id="fileNameDisplay">
                        No file chosen yet
                    </p>
                    <input type="file" id="fileInput" name="resumeFile" accept="application/pdf,.pdf" 
                           style="display: none;" onchange="updateFileName(this)">
                </div>

                <div style="display: flex; justify-content: flex-end; gap: 1rem;">
                    <a href="${pageContext.request.contextPath}/seeker/dashboard.jsp" class="btn btn-secondary">
                        Back to Dashboard
                    </a>
                    <button type="submit" class="btn btn-primary" style="padding: 0.65rem 1.75rem;">
                        &#128229; <%= (resume != null) ? "Upload & Replace Resume" : "Upload Resume" %>
                    </button>
                </div>
            </form>
        </div>

    </div>

    <footer class="footer" style="margin-top: 3rem;">
        HireHub &copy; 2026. Online Job Portal.
    </footer>

    <script>
        function updateFileName(input) {
            const display = document.getElementById('fileNameDisplay');
            if (input.files && input.files.length > 0) {
                const file = input.files[0];
                if (!file.name.toLowerCase().endsWith('.pdf')) {
                    alert('Please select a valid PDF file.');
                    input.value = '';
                    display.textContent = 'No file chosen yet';
                    return;
                }
                if (file.size > 5 * 1024 * 1024) {
                    alert('File exceeds the 5 MB limit (' + (file.size / 1024 / 1024).toFixed(2) + ' MB). Please choose a smaller file.');
                    input.value = '';
                    display.textContent = 'No file chosen yet';
                    return;
                }
                display.textContent = 'Selected: ' + file.name + ' (' + (file.size / 1024).toFixed(1) + ' KB)';
                display.style.color = '#34d399';
            } else {
                display.textContent = 'No file chosen yet';
                display.style.color = 'var(--text-muted)';
            }
        }
    </script>
</body>
</html>
