package com.hirehub.servlet;

import com.hirehub.exception.ValidationException;
import com.hirehub.model.User;
import com.hirehub.service.ApplicationService;
import com.hirehub.service.ApplicationServiceImpl;
import com.hirehub.util.CsrfUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * Servlet handling candidate job application submissions.
 * Requires authenticated candidate session and CSRF verification.
 */
public class ApplyJobServlet extends HttpServlet {

    private final ApplicationService applicationService;

    public ApplyJobServlet() {
        this(new ApplicationServiceImpl());
    }

    public ApplyJobServlet(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // Direct GET not allowed for state-changing apply action; redirect to job search
        resp.sendRedirect(req.getContextPath() + "/seeker/jobs");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login?authRequired=true");
            return;
        }

        User user = (User) session.getAttribute("currentUser");

        if (!CsrfUtil.isValid(req)) {
            resp.sendRedirect(req.getContextPath() + "/seeker/jobs?error="
                    + URLEncoder.encode("Security validation failed (CSRF token missing or invalid). Please try again.", StandardCharsets.UTF_8));
            return;
        }

        String jobIdStr = req.getParameter("jobId");
        if (jobIdStr == null || jobIdStr.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/seeker/jobs?error="
                    + URLEncoder.encode("Invalid job selection.", StandardCharsets.UTF_8));
            return;
        }

        try {
            Long jobId = Long.parseLong(jobIdStr.trim());
            applicationService.applyForJob(user.getId(), jobId);

            resp.sendRedirect(req.getContextPath() + "/seeker/applications?applied=true");

        } catch (NumberFormatException e) {
            resp.sendRedirect(req.getContextPath() + "/seeker/jobs?error="
                    + URLEncoder.encode("Invalid job ID format.", StandardCharsets.UTF_8));
        } catch (ValidationException e) {
            resp.sendRedirect(req.getContextPath() + "/seeker/jobs?error="
                    + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/seeker/jobs?error="
                    + URLEncoder.encode("Failed to submit application: " + e.getMessage(), StandardCharsets.UTF_8));
        }
    }
}
