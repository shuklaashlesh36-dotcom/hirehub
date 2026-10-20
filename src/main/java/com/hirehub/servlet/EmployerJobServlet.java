package com.hirehub.servlet;

import com.hirehub.exception.ValidationException;
import com.hirehub.model.Job;
import com.hirehub.model.JobStatus;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller servlet for Employer Job Management:
 * - Posting new jobs
 * - Viewing employer's own jobs
 * - Editing jobs
 * - Closing and reopening jobs
 */
public class EmployerJobServlet extends HttpServlet {

    private final EmployerService employerService;

    public EmployerJobServlet() {
        this(new EmployerServiceImpl());
    }

    public EmployerJobServlet(EmployerService employerService) {
        this.employerService = employerService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = getAuthenticatedEmployer(req, resp);
        if (user == null) {
            return;
        }

        String servletPath = req.getServletPath();
        String action = req.getParameter("action");

        if ("/employer/post-job".equals(servletPath) || "new".equalsIgnoreCase(action)) {
            req.setAttribute("csrfToken", CsrfUtil.getToken(req.getSession(true)));
            req.getRequestDispatcher("/employer/post-job.jsp").forward(req, resp);
            return;
        }

        if ("edit".equalsIgnoreCase(action)) {
            String jobIdStr = req.getParameter("id");
            if (jobIdStr == null || jobIdStr.isBlank()) {
                resp.sendRedirect(req.getContextPath() + "/employer/jobs");
                return;
            }
            try {
                Long jobId = Long.parseLong(jobIdStr.trim());
                Optional<Job> jobOpt = employerService.getEmployerJob(user.getId(), jobId);
                if (jobOpt.isEmpty()) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Job not found or access denied.");
                    return;
                }
                req.setAttribute("job", jobOpt.get());
                req.setAttribute("csrfToken", CsrfUtil.getToken(req.getSession(true)));
                req.getRequestDispatcher("/employer/edit-job.jsp").forward(req, resp);
                return;
            } catch (NumberFormatException e) {
                resp.sendRedirect(req.getContextPath() + "/employer/jobs");
                return;
            }
        }

        // Default: list employer jobs
        List<Job> jobs = employerService.getEmployerJobs(user.getId());
        Map<Long, Integer> applicantCounts = new HashMap<>();
        for (Job j : jobs) {
            applicantCounts.put(j.getId(), employerService.getJobApplicantCount(j.getId()));
        }

        req.setAttribute("jobs", jobs);
        req.setAttribute("applicantCounts", applicantCounts);
        req.setAttribute("csrfToken", CsrfUtil.getToken(req.getSession(true)));

        if ("true".equals(req.getParameter("created"))) {
            req.setAttribute("successMessage", "Job posting created successfully!");
        } else if ("true".equals(req.getParameter("updated"))) {
            req.setAttribute("successMessage", "Job posting updated successfully!");
        } else if ("true".equals(req.getParameter("statusUpdated"))) {
            req.setAttribute("successMessage", "Job status updated successfully!");
        }

        req.getRequestDispatcher("/employer/jobs.jsp").forward(req, resp);
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

        String action = req.getParameter("action");
        if (action == null || action.isBlank()) {
            resp.sendRedirect(req.getContextPath() + "/employer/jobs");
            return;
        }

        try {
            switch (action.toLowerCase()) {
                case "create" -> handleCreateJob(req, resp, user.getId());
                case "edit" -> handleEditJob(req, resp, user.getId());
                case "status", "toggle" -> handleToggleStatus(req, resp, user.getId());
                default -> resp.sendRedirect(req.getContextPath() + "/employer/jobs");
            }
        } catch (SecurityException e) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, e.getMessage());
        } catch (ValidationException e) {
            req.setAttribute("errorMessage", e.getMessage());
            req.setAttribute("csrfToken", CsrfUtil.getToken(req.getSession(true)));
            if ("edit".equalsIgnoreCase(action)) {
                String jobIdStr = req.getParameter("jobId");
                if (jobIdStr != null) {
                    try {
                        Long jid = Long.parseLong(jobIdStr);
                        employerService.getEmployerJob(user.getId(), jid).ifPresent(j -> req.setAttribute("job", j));
                    } catch (NumberFormatException ignored) {
                    }
                }
                req.getRequestDispatcher("/employer/edit-job.jsp").forward(req, resp);
            } else {
                req.getRequestDispatcher("/employer/post-job.jsp").forward(req, resp);
            }
        }
    }

    private void handleCreateJob(HttpServletRequest req, HttpServletResponse resp, Long employerId)
            throws IOException {
        String title = req.getParameter("title");
        String description = req.getParameter("description");
        String skills = req.getParameter("skills");
        String location = req.getParameter("location");
        String salary = req.getParameter("salary");
        String jobType = req.getParameter("jobType");

        Job job = new Job();
        job.setTitle(title);
        job.setDescription(description);
        job.setSkills(skills);
        job.setLocation(location);
        job.setSalary(salary);
        job.setJobType(jobType);
        job.setStatus(JobStatus.APPROVED);

        employerService.createJob(employerId, job);
        resp.sendRedirect(req.getContextPath() + "/employer/jobs?created=true");
    }

    private void handleEditJob(HttpServletRequest req, HttpServletResponse resp, Long employerId)
            throws IOException {
        String jobIdStr = req.getParameter("jobId");
        if (jobIdStr == null || jobIdStr.isBlank()) {
            throw new ValidationException("Job ID is missing.");
        }
        Long jobId = Long.parseLong(jobIdStr.trim());

        String title = req.getParameter("title");
        String description = req.getParameter("description");
        String skills = req.getParameter("skills");
        String location = req.getParameter("location");
        String salary = req.getParameter("salary");
        String statusStr = req.getParameter("status");

        JobStatus status = null;
        if (statusStr != null && !statusStr.isBlank()) {
            try {
                status = JobStatus.valueOf(statusStr.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new ValidationException("Invalid job status selected.");
            }
        }

        Job job = new Job();
        job.setId(jobId);
        job.setTitle(title);
        job.setDescription(description);
        job.setSkills(skills);
        job.setLocation(location);
        job.setSalary(salary);
        job.setStatus(status);

        employerService.updateJob(employerId, job);
        resp.sendRedirect(req.getContextPath() + "/employer/jobs?updated=true");
    }

    private void handleToggleStatus(HttpServletRequest req, HttpServletResponse resp, Long employerId)
            throws IOException {
        String jobIdStr = req.getParameter("jobId");
        String statusStr = req.getParameter("status");
        if (jobIdStr == null || jobIdStr.isBlank() || statusStr == null || statusStr.isBlank()) {
            throw new ValidationException("Job ID and target status are required.");
        }

        Long jobId = Long.parseLong(jobIdStr.trim());
        JobStatus status = JobStatus.valueOf(statusStr.trim().toUpperCase());

        employerService.updateJobStatus(employerId, jobId, status);
        resp.sendRedirect(req.getContextPath() + "/employer/jobs?statusUpdated=true");
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
