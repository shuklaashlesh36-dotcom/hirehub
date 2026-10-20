<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Create Account - HireHub</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
    <script src="${pageContext.request.contextPath}/js/theme.js"></script>
</head>
<body>
    <nav class="navbar">
        <a href="${pageContext.request.contextPath}/" class="brand">
            HireHub <span class="brand-badge">Portal</span>
        </a>
        <ul class="nav-links">
            <li>
                <button type="button" class="theme-toggle" aria-label="Toggle theme" title="Toggle Light/Dark Theme">
                    <span class="theme-toggle-icon" aria-hidden="true">&#9728;&#65039;</span>
                    <span class="theme-toggle-text">Theme</span>
                </button>
            </li>
            <li><a href="${pageContext.request.contextPath}/">Home</a></li>
            <li><a href="${pageContext.request.contextPath}/login">Sign In</a></li>
        </ul>
    </nav>

    <div class="container auth-container">
        <div class="glass-panel">
            <div class="header-panel">
                <h1>Join HireHub</h1>
                <p>Choose your account type to get started</p>
            </div>

            <% if (request.getAttribute("errorMessage") != null) { %>
                <div class="alert alert-error">
                    <span>&#9888;</span> <%= request.getAttribute("errorMessage") %>
                </div>
            <% } %>

            <% 
                String selectedRole = (String) request.getAttribute("selectedRole");
                if (selectedRole == null || selectedRole.isBlank()) {
                    selectedRole = "JOB_SEEKER";
                }
            %>

            <form action="${pageContext.request.contextPath}/register" method="post" id="registerForm">
                <label style="display:block; margin-bottom: 0.5rem; font-size: 0.875rem; color: var(--text-secondary); font-weight: 500;">
                    Select Account Type
                </label>
                <div class="role-selector">
                    <label class="role-card <%= "JOB_SEEKER".equals(selectedRole) ? "selected" : "" %>" id="roleCardSeeker">
                        <input type="radio" name="role" value="JOB_SEEKER" <%= "JOB_SEEKER".equals(selectedRole) ? "checked" : "" %> onchange="onRoleChange('JOB_SEEKER')">
                        <div class="role-title">Job Seeker</div>
                        <div class="role-desc">Looking for opportunities</div>
                    </label>

                    <label class="role-card <%= "EMPLOYER".equals(selectedRole) ? "selected" : "" %>" id="roleCardEmployer">
                        <input type="radio" name="role" value="EMPLOYER" <%= "EMPLOYER".equals(selectedRole) ? "checked" : "" %> onchange="onRoleChange('EMPLOYER')">
                        <div class="role-title">Employer</div>
                        <div class="role-desc">Hiring top talent</div>
                    </label>
                </div>

                <div class="form-group">
                    <label for="name">Full Name</label>
                    <input type="text" id="name" name="name" class="form-control" required
                           placeholder="e.g. Alex Johnson"
                           value="<%= request.getAttribute("name") != null ? request.getAttribute("name") : "" %>">
                </div>

                <div class="form-group">
                    <label for="email">Email Address</label>
                    <input type="email" id="email" name="email" class="form-control" required
                           placeholder="alex@example.com"
                           value="<%= request.getAttribute("email") != null ? request.getAttribute("email") : "" %>">
                </div>

                <div class="form-row">
                    <div class="form-group">
                        <label for="password">Password</label>
                        <input type="password" id="password" name="password" class="form-control" 
                               minlength="8" required placeholder="Minimum 8 characters">
                    </div>
                    <div class="form-group">
                        <label for="confirmPassword">Confirm Password</label>
                        <input type="password" id="confirmPassword" name="confirmPassword" class="form-control" 
                               minlength="8" required placeholder="Re-type password">
                    </div>
                </div>

                <!-- Employer Specific Fields -->
                <div id="employerFields" style="<%= "EMPLOYER".equals(selectedRole) ? "display:block;" : "display:none;" %>">
                    <div class="form-group">
                        <label for="companyName">Company Name *</label>
                        <input type="text" id="companyName" name="companyName" class="form-control"
                               placeholder="e.g. Acme Innovations"
                               value="<%= request.getAttribute("companyName") != null ? request.getAttribute("companyName") : "" %>">
                    </div>
                    <div class="form-group">
                        <label for="companyDescription">Company Description</label>
                        <textarea id="companyDescription" name="companyDescription" class="form-control" rows="2"
                                  placeholder="Brief overview of your company..."><%= request.getAttribute("companyDescription") != null ? request.getAttribute("companyDescription") : "" %></textarea>
                    </div>
                </div>

                <!-- Job Seeker Specific Fields -->
                <div id="seekerFields" style="<%= "JOB_SEEKER".equals(selectedRole) ? "display:block;" : "display:none;" %>">
                    <div class="form-group">
                        <label for="phone">Phone Number</label>
                        <input type="text" id="phone" name="phone" class="form-control"
                               placeholder="e.g. +91 9876543210"
                               value="<%= request.getAttribute("phone") != null ? request.getAttribute("phone") : "" %>">
                    </div>
                    <div class="form-group">
                        <label for="education">Highest Education</label>
                        <input type="text" id="education" name="education" class="form-control"
                               placeholder="e.g. B.Tech Computer Science"
                               value="<%= request.getAttribute("education") != null ? request.getAttribute("education") : "" %>">
                    </div>
                    <div class="form-group">
                        <label for="skills">Skills</label>
                        <input type="text" id="skills" name="skills" class="form-control"
                               placeholder="e.g. Java, MySQL, Servlets, HTML/CSS"
                               value="<%= request.getAttribute("skills") != null ? request.getAttribute("skills") : "" %>">
                    </div>
                </div>

                <div style="margin-top: 1.5rem;">
                    <button type="submit" class="btn btn-primary btn-block">Complete Registration</button>
                </div>
            </form>

            <div style="margin-top: 1.75rem; text-align: center; color: var(--text-secondary); font-size: 0.9rem;">
                Already have an account? 
                <a href="${pageContext.request.contextPath}/login" style="color: var(--accent-primary); text-decoration: none; font-weight: 600;">
                    Sign in here
                </a>
            </div>
        </div>
    </div>

    <footer class="footer">
        HireHub &copy; 2026. Online Job Portal.
    </footer>

    <script>
        function onRoleChange(role) {
            const seekerCard = document.getElementById('roleCardSeeker');
            const employerCard = document.getElementById('roleCardEmployer');
            const employerFields = document.getElementById('employerFields');
            const seekerFields = document.getElementById('seekerFields');
            const companyNameInput = document.getElementById('companyName');

            if (role === 'EMPLOYER') {
                employerCard.classList.add('selected');
                seekerCard.classList.remove('selected');
                employerFields.style.display = 'block';
                seekerFields.style.display = 'none';
                companyNameInput.setAttribute('required', 'required');
            } else {
                seekerCard.classList.add('selected');
                employerCard.classList.remove('selected');
                seekerFields.style.display = 'block';
                employerFields.style.display = 'none';
                companyNameInput.removeAttribute('required');
            }
        }
    </script>
</body>
</html>
