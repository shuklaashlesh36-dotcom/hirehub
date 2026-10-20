<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Sign In - HireHub</title>
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
            <li><a href="${pageContext.request.contextPath}/register">Create Account</a></li>
        </ul>
    </nav>

    <div class="container auth-container">
        <div class="glass-panel">
            <div class="header-panel">
                <h1>Welcome Back</h1>
                <p>Sign in to access your HireHub dashboard</p>
            </div>

            <% if (request.getAttribute("errorMessage") != null) { %>
                <div class="alert alert-error">
                    <span>&#9888;</span> <%= request.getAttribute("errorMessage") %>
                </div>
            <% } %>

            <% if (request.getAttribute("successMessage") != null) { %>
                <div class="alert alert-success">
                    <span>&#10004;</span> <%= request.getAttribute("successMessage") %>
                </div>
            <% } %>

            <% if (request.getAttribute("infoMessage") != null) { %>
                <div class="alert alert-info">
                    <span>&#8505;</span> <%= request.getAttribute("infoMessage") %>
                </div>
            <% } %>

            <form action="${pageContext.request.contextPath}/login" method="post" autocomplete="on">
                <div class="form-group">
                    <label for="email">Email Address</label>
                    <input type="email" id="email" name="email" class="form-control" 
                           placeholder="name@example.com" required autofocus
                           value="<%= request.getAttribute("email") != null ? request.getAttribute("email") : "" %>">
                </div>

                <div class="form-group">
                    <label for="password">Password</label>
                    <input type="password" id="password" name="password" class="form-control" 
                           placeholder="Enter your password" required>
                </div>

                <div style="margin-top: 1.5rem;">
                    <button type="submit" class="btn btn-primary btn-block">Sign In</button>
                </div>
            </form>

            <div style="margin-top: 1.75rem; text-align: center; color: var(--text-secondary); font-size: 0.9rem;">
                Don't have an account yet? 
                <a href="${pageContext.request.contextPath}/register" style="color: var(--accent-primary); text-decoration: none; font-weight: 600;">
                    Register here
                </a>
            </div>
        </div>
    </div>

    <footer class="footer">
        HireHub &copy; 2026. Online Job Portal.
    </footer>
</body>
</html>
