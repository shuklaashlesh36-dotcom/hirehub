package com.hirehub.filter;

import com.hirehub.model.Role;
import com.hirehub.model.User;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Enforces role-based access control for /admin/*, /employer/*, and /seeker/* routes.
 */
public class RoleAuthorizationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        HttpSession session = httpRequest.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("currentUser") : null;

        if (user == null) {
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login?authRequired=true");
            return;
        }

        String contextPath = httpRequest.getContextPath();
        String uri = httpRequest.getRequestURI();
        String relativePath = uri.startsWith(contextPath) ? uri.substring(contextPath.length()) : uri;

        Role role = user.getRole();
        boolean authorized = true;

        if (relativePath.startsWith("/admin") && role != Role.ADMIN) {
            authorized = false;
        } else if (relativePath.startsWith("/employer") && role != Role.EMPLOYER) {
            authorized = false;
        } else if (relativePath.startsWith("/seeker") && role != Role.JOB_SEEKER) {
            authorized = false;
        }

        if (authorized) {
            chain.doFilter(request, response);
        } else {
            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
            httpRequest.setAttribute("deniedRole", role);
            httpRequest.setAttribute("attemptedPath", relativePath);
            httpRequest.getRequestDispatcher("/access-denied.jsp").forward(request, response);
        }
    }

    @Override
    public void destroy() {
    }
}
