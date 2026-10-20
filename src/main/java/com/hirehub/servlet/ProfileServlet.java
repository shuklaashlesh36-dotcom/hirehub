package com.hirehub.servlet;

import com.hirehub.exception.ValidationException;
import com.hirehub.model.Employer;
import com.hirehub.model.JobSeeker;
import com.hirehub.model.Role;
import com.hirehub.model.User;
import com.hirehub.service.ProfileService;
import com.hirehub.service.ProfileServiceImpl;
import com.hirehub.util.CsrfUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Servlet handling user profile display and updates for candidates and employers.
 * Accessible under /profile and /profile/edit.
 */
public class ProfileServlet extends HttpServlet {

    private final ProfileService profileService;

    public ProfileServlet() {
        this(new ProfileServiceImpl());
    }

    public ProfileServlet(ProfileService profileService) {
        this.profileService = profileService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User sessionUser = getAuthenticatedUser(req, resp);
        if (sessionUser == null) {
            return;
        }

        // Always reload latest user record from database
        Optional<User> freshUserOpt = profileService.getUserProfile(sessionUser.getId());
        if (freshUserOpt.isEmpty()) {
            resp.sendRedirect(req.getContextPath() + "/login?error=" + URLEncoder.encode("User record not found.", StandardCharsets.UTF_8));
            return;
        }
        User user = freshUserOpt.get();
        req.getSession().setAttribute("currentUser", user);
        req.setAttribute("profileUser", user);
        req.setAttribute("csrfToken", CsrfUtil.getToken(req.getSession()));

        String servletPath = req.getServletPath();
        String pathInfo = req.getPathInfo();

        if ("/edit".equalsIgnoreCase(pathInfo) || (servletPath != null && servletPath.endsWith("/edit"))) {
            req.getRequestDispatcher("/edit-profile.jsp").forward(req, resp);
        } else {
            req.getRequestDispatcher("/profile.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User sessionUser = getAuthenticatedUser(req, resp);
        if (sessionUser == null) {
            return;
        }

        if (!CsrfUtil.isValid(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or missing CSRF token.");
            return;
        }

        // Disallow cross-user ID tampering: always use authenticated session user's ID
        String reqUserIdStr = req.getParameter("userId");
        if (reqUserIdStr != null && !reqUserIdStr.isBlank()) {
            try {
                long reqUserId = Long.parseLong(reqUserIdStr.trim());
                if (reqUserId != sessionUser.getId() && sessionUser.getRole() != Role.ADMIN) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, "You cannot modify another user's profile.");
                    return;
                }
            } catch (NumberFormatException ignored) {
            }
        }

        Long userId = sessionUser.getId();

        try {
            User updatedUser;
            if (sessionUser.getRole() == Role.JOB_SEEKER) {
                String name = req.getParameter("name");
                String phone = req.getParameter("phone");
                String education = req.getParameter("education");
                String skills = req.getParameter("skills");
                String headline = req.getParameter("headline");

                updatedUser = profileService.updateCandidateProfile(userId, name, phone, education, skills, headline);
            } else if (sessionUser.getRole() == Role.EMPLOYER) {
                String name = req.getParameter("name");
                String companyName = req.getParameter("companyName");
                String companyDescription = req.getParameter("companyDescription");
                String phone = req.getParameter("phone");
                String website = req.getParameter("website");
                String location = req.getParameter("location");

                updatedUser = profileService.updateEmployerProfile(userId, name, companyName, companyDescription,
                        phone, website, location);
            } else {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Profile editing is only supported for candidates and employers.");
                return;
            }

            req.getSession().setAttribute("currentUser", updatedUser);
            resp.sendRedirect(req.getContextPath() + "/profile?success="
                    + URLEncoder.encode("Profile updated successfully.", StandardCharsets.UTF_8));

        } catch (ValidationException e) {
            req.setAttribute("errorMessage", e.getMessage());
            req.setAttribute("profileUser", sessionUser);
            req.setAttribute("csrfToken", CsrfUtil.getToken(req.getSession()));
            req.getRequestDispatcher("/edit-profile.jsp").forward(req, resp);
        }
    }

    private User getAuthenticatedUser(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login?authRequired=true");
            return null;
        }
        return (User) session.getAttribute("currentUser");
    }
}
