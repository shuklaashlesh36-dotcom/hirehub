package com.hirehub.servlet;

import com.hirehub.exception.ValidationException;
import com.hirehub.model.Application;
import com.hirehub.model.ApplicationStatus;
import com.hirehub.model.Job;
import com.hirehub.model.User;
import com.hirehub.service.EmployerService;
import com.hirehub.service.EmployerServiceImpl;
import com.hirehub.util.CsrfUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * Controller servlet for Employer Applicant Review:
 * - Listing applicants across all employer jobs or filtered by a specific job
 * - Updating applicant status (APPLIED, SHORTLISTED, HIRED, REJECTED) with server-side ownership checks
 */
public class EmployerApplicantServlet extends HttpServlet {

    private final EmployerService employerService;

    public EmployerApplicantServlet() {
        this(new EmployerServiceImpl());
    }

    public EmployerApplicantServlet(EmployerService employerService) {
        this.employerService = employerService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = getAuthenticatedEmployer(req, resp);
        if (user == null) {
            return;
        }

        String jobIdParam = req.getParameter("jobId");
        Long selectedJobId = null;
        if (jobIdParam != null && !jobIdParam.isBlank()) {
            try {
                selectedJobId = Long.parseLong(jobIdParam.trim());
            } catch (NumberFormatException ignored) {
            }
        }

        List<Job> employerJobs = employerService.getEmployerJobs(user.getId());
        List<Application> applications;

        try {
            applications = employerService.getEmployerApplications(user.getId(), selectedJobId);
        } catch (SecurityException e) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
            return;
        }

        req.setAttribute("jobs", employerJobs);
        req.setAttribute("applications", applications);
        req.setAttribute("selectedJobId", selectedJobId);
        req.setAttribute("csrfToken", CsrfUtil.getToken(req.getSession(true)));

        if ("true".equals(req.getParameter("statusUpdated"))) {
            req.setAttribute("successMessage", "Candidate application status updated successfully!");
        }

        req.getRequestDispatcher("/employer/applicants.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = getAuthenticatedEmployer(req, resp);
        if (user == null) {
            return;
        }

        if (!CsrfUtil.isValid(req)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid or expired session security token.");
            return;
        }

        String appIdStr = req.getParameter("applicationId");
        String statusStr = req.getParameter("status");
        String jobIdParam = req.getParameter("jobId");

        if (appIdStr == null || appIdStr.isBlank() || statusStr == null || statusStr.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/employer/applications?error=Missing+required+parameters");
            return;
        }

        try {
            Long applicationId = Long.parseLong(appIdStr.trim());
            ApplicationStatus status = ApplicationStatus.valueOf(statusStr.trim().toUpperCase());

            employerService.updateApplicationStatus(user.getId(), applicationId, status);

            String redirectUrl = req.getContextPath() + "/employer/applications?statusUpdated=true";
            if (jobIdParam != null && !jobIdParam.isBlank()) {
                redirectUrl += "&jobId=" + jobIdParam.trim();
            }
            resp.sendRedirect(redirectUrl);

        } catch (IllegalArgumentException e) {
            resp.sendRedirect(req.getContextPath() + "/employer/applications?error=Invalid+status+value");
        } catch (SecurityException e) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (ValidationException e) {
            resp.sendRedirect(req.getContextPath() + "/employer/applications?error=" + e.getMessage());
        }
    }

    private User getAuthenticatedEmployer(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login?authRequired=true");
            return null;
        }
        return (User) session.getAttribute("currentUser");
    }
}
