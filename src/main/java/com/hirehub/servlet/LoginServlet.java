package com.hirehub.servlet;

import com.hirehub.exception.AuthenticationException;
import com.hirehub.exception.ValidationException;
import com.hirehub.model.Role;
import com.hirehub.model.User;
import com.hirehub.service.AuthService;
import com.hirehub.service.AuthServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Handles user login and session establishment.
 */
public class LoginServlet extends HttpServlet {

    private final AuthService authService;

    public LoginServlet() {
        this(new AuthServiceImpl());
    }

    public LoginServlet(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null && session.getAttribute("currentUser") instanceof User user) {
            redirectByRole(req, resp, user.getRole());
            return;
        }

        if ("true".equals(req.getParameter("registered"))) {
            req.setAttribute("successMessage", "Account created successfully! Please sign in.");
        } else if ("true".equals(req.getParameter("logout"))) {
            req.setAttribute("infoMessage", "You have been logged out successfully.");
        } else if ("true".equals(req.getParameter("authRequired"))) {
            req.setAttribute("errorMessage", "Please log in to access this page.");
        }

        req.getRequestDispatcher("/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");

        String email = req.getParameter("email");
        String password = req.getParameter("password");

        req.setAttribute("email", email);

        try {
            User user = authService.login(email, password);

            // Prevent session fixation: invalidate old session if one exists
            HttpSession oldSession = req.getSession(false);
            if (oldSession != null) {
                oldSession.invalidate();
            }

            // Create fresh session
            HttpSession newSession = req.getSession(true);
            newSession.setAttribute("currentUser", user);
            newSession.setAttribute("userRole", user.getRole().name());

            redirectByRole(req, resp, user.getRole());
        } catch (AuthenticationException | ValidationException e) {
            req.setAttribute("errorMessage", e.getMessage());
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
        } catch (Exception e) {
            req.setAttribute("errorMessage", "An unexpected error occurred during login. Please try again.");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
        }
    }

    private void redirectByRole(HttpServletRequest req, HttpServletResponse resp, Role role) throws IOException {
        String target = switch (role) {
            case ADMIN -> "/admin/dashboard.jsp";
            case EMPLOYER -> "/employer/dashboard";
            case JOB_SEEKER -> "/seeker/dashboard.jsp";
        };
        resp.sendRedirect(req.getContextPath() + target);
    }
}
