<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isErrorPage="true" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Error - HireHub</title>
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
        </ul>
    </nav>

    <div class="container auth-container" style="text-align: center;">
        <div class="glass-panel">
            <div style="font-size: 3.5rem; margin-bottom: 0.5rem; color: #f87171;">
                &#9888;
            </div>
            <h1 style="font-size: 1.85rem; font-weight: 800; margin-bottom: 0.75rem;">Something went wrong</h1>
            <p style="color: var(--text-secondary); margin-bottom: 1.5rem; font-size: 0.95rem;">
                An unexpected error occurred while processing your request. Please return to the homepage or try again later.
            </p>
            <div style="display: flex; gap: 0.75rem; justify-content: center;">
                <a href="${pageContext.request.contextPath}/" class="btn btn-primary">
                    Return to Home
                </a>
            </div>
        </div>
    </div>

    <footer class="footer">
        HireHub &copy; 2026. Online Job Portal.
    </footer>
</body>
</html>
