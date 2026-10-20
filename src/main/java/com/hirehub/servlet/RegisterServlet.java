
package com.hirehub.servlet;

import com.hirehub.exception.ValidationException;
import com.hirehub.model.Employer;
import com.hirehub.model.JobSeeker;
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

public class RegisterServlet extends HttpServlet {

    private final AuthService authService;

    public RegisterServlet() {
        this(new AuthServiceImpl());
    }

    public RegisterServlet(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doGet(HttpServletRequest req,
                         HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);

        if (session != null
                && session.getAttribute("currentUser") instanceof User user) {
            redirectByRole(req, resp, user.getRole());
            return;
        }

        req.getRequestDispatcher("/register.jsp")
                .forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req,
                          HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");
        String roleStr = req.getParameter("role");

        // Retain entered values if registration fails
        req.setAttribute("name", name);
        req.setAttribute("email", email);
        req.setAttribute("selectedRole", roleStr);

        if (password == null || !password.equals(confirmPassword)) {
            req.setAttribute("errorMessage", "Passwords do not match.");
            req.getRequestDispatcher("/register.jsp")
                    .forward(req, resp);
            return;
        }

        Role role;

        try {
            role = Role.valueOf(roleStr);
        } catch (Exception e) {
            req.setAttribute(
                    "errorMessage",
                    "Please select a valid role (Employer or Job Seeker)."
            );
            req.getRequestDispatcher("/register.jsp")
                    .forward(req, resp);
            return;
        }

        if (role == Role.ADMIN) {
            req.setAttribute(
                    "errorMessage",
                    "Administrator registration is not permitted."
            );
            req.getRequestDispatcher("/register.jsp")
                    .forward(req, resp);
            return;
        }

        User user;

        if (role == Role.EMPLOYER) {
            String companyName = req.getParameter("companyName");
            String companyDescription =
                    req.getParameter("companyDescription");

            req.setAttribute("companyName", companyName);
            req.setAttribute("companyDescription", companyDescription);

            user = new Employer(
                    name,
                    email,
                    password,
                    companyName,
                    companyDescription
            );

        } else {
            String phone = req.getParameter("phone");
            String skills = req.getParameter("skills");
            String education = req.getParameter("education");

            req.setAttribute("phone", phone);
            req.setAttribute("skills", skills);
            req.setAttribute("education", education);

            user = new JobSeeker(
                    name,
                    email,
                    password,
                    phone,
                    skills,
                    education
            );
        }

        try {
            authService.register(user);

            resp.sendRedirect(
                    req.getContextPath() + "/login?registered=true"
            );

        } catch (ValidationException e) {

            req.setAttribute("errorMessage", e.getMessage());

            req.getRequestDispatcher("/register.jsp")
                    .forward(req, resp);

        } catch (Exception e) {

            // Print the actual error and stack trace in IntelliJ console
            e.printStackTrace();

            req.setAttribute(
                    "errorMessage",
                    "Registration failed: " + e.getMessage()
            );

            req.getRequestDispatcher("/register.jsp")
                    .forward(req, resp);
        }
    }

    private void redirectByRole(HttpServletRequest req,
                                HttpServletResponse resp,
                                Role role)
            throws IOException {

        String target;

        switch (role) {
            case ADMIN:
                target = "/admin/dashboard.jsp";
                break;

            case EMPLOYER:
                target = "/employer/dashboard.jsp";
                break;

            case JOB_SEEKER:
                target = "/seeker/dashboard.jsp";
                break;

            default:
                throw new IllegalStateException(
                        "Unknown user role: " + role
                );
        }

        resp.sendRedirect(req.getContextPath() + target);
    }
}
