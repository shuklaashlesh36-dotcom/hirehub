package com.hirehub.servlet;

import com.hirehub.model.Job;
import com.hirehub.model.Resume;
import com.hirehub.model.User;
import com.hirehub.service.ApplicationService;
import com.hirehub.service.ApplicationServiceImpl;
import com.hirehub.service.JobService;
import com.hirehub.service.JobServiceImpl;
import com.hirehub.service.ResumeService;
import com.hirehub.service.ResumeServiceImpl;
import com.hirehub.util.CsrfUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Servlet handling candidate job browsing, keyword searching, location filtering,
 * and preparing candidate state (applied jobs, resume status) for job applications.
 * Protected under /seeker/* route requiring authenticated JOB_SEEKER session.
 */
public class JobServlet extends HttpServlet {

    private final JobService jobService;
    private final ApplicationService applicationService;
    private final ResumeService resumeService;

    public JobServlet() {
        this(new JobServiceImpl(), new ApplicationServiceImpl(), new ResumeServiceImpl());
    }

    public JobServlet(JobService jobService, ApplicationService applicationService, ResumeService resumeService) {
        this.jobService = jobService;
        this.applicationService = applicationService;
        this.resumeService = resumeService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        req.setCharacterEncoding("UTF-8");

        HttpSession session = req.getSession(false);
        Long seekerId = null;
        if (session != null && session.getAttribute("currentUser") instanceof User user) {
            seekerId = user.getId();
        }

        String keyword = req.getParameter("keyword");
        String location = req.getParameter("location");
        String status = req.getParameter("status");
        String jobIdStr = req.getParameter("jobId");
        String errorParam = req.getParameter("error");

        if (errorParam != null && !errorParam.isBlank()) {
            req.setAttribute("errorMessage", errorParam.trim());
        }

        // Default status to APPROVED for candidate search if not explicitly specified
        if (status == null || status.isBlank()) {
            status = "APPROVED";
        }

        List<Job> jobs = jobService.searchJobs(keyword, location, status);
        List<String> locations = jobService.getLocations();

        // Candidate-specific state
        Set<Long> appliedJobIds = (seekerId != null) ? applicationService.getAppliedJobIds(seekerId) : Set.of();
        Optional<Resume> resumeOpt = (seekerId != null) ? resumeService.getCandidateResume(seekerId) : Optional.empty();

        // If a specific job was selected to view details
        if (jobIdStr != null && !jobIdStr.isBlank()) {
            try {
                Long jobId = Long.parseLong(jobIdStr.trim());
                jobService.getJobById(jobId).ifPresent(job -> req.setAttribute("selectedJob", job));
            } catch (NumberFormatException ignored) {
                // Ignore invalid ID format
            }
        }

        req.setAttribute("jobs", jobs);
        req.setAttribute("locations", locations);
        req.setAttribute("appliedJobIds", appliedJobIds);
        req.setAttribute("candidateResume", resumeOpt.orElse(null));
        req.setAttribute("csrfToken", CsrfUtil.getToken(req.getSession(true)));
        req.setAttribute("keyword", keyword != null ? keyword.trim() : "");
        req.setAttribute("selectedLocation", location != null ? location.trim() : "");
        req.setAttribute("selectedStatus", status.trim());

        req.getRequestDispatcher("/seeker/jobs.jsp").forward(req, resp);
    }
}
