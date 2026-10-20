package com.hirehub.servlet;

import com.hirehub.exception.ValidationException;
import com.hirehub.model.Application;
import com.hirehub.model.Resume;
import com.hirehub.model.User;
import com.hirehub.service.EmployerService;
import com.hirehub.service.EmployerServiceImpl;
import com.hirehub.service.ResumeService;
import com.hirehub.service.ResumeServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Controller servlet for securely streaming candidate resumes to authorized employers.
 * Strictly verifies that the requested applicant applied to a job owned by the requesting employer.
 */
public class EmployerResumeServlet extends HttpServlet {

    private final EmployerService employerService;
    private final ResumeService resumeService;

    public EmployerResumeServlet() {
        this(new EmployerServiceImpl(), new ResumeServiceImpl());
    }

    public EmployerResumeServlet(EmployerService employerService, ResumeService resumeService) {
        this.employerService = employerService;
        this.resumeService = resumeService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = getAuthenticatedEmployer(req, resp);
        if (user == null) {
            return;
        }

        String appIdStr = req.getParameter("applicationId");
        if (appIdStr == null || appIdStr.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Application ID is required.");
            return;
        }

        Long applicationId;
        try {
            applicationId = Long.parseLong(appIdStr.trim());
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Application ID.");
            return;
        }

        // Ownership enforcement: Verify application belongs to a job owned by this employer
        Optional<Application> appOpt = employerService.getEmployerApplication(user.getId(), applicationId);
        if (appOpt.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Access denied: This application does not belong to your company's jobs.");
            return;
        }

        Application application = appOpt.get();
        Optional<Resume> resumeOpt;
        if (application.getResumeId() != null) {
            resumeOpt = resumeService.getResumeById(application.getResumeId());
        } else {
            resumeOpt = resumeService.getCandidateResume(application.getSeekerId());
        }

        if (resumeOpt.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No resume file is associated with this application.");
            return;
        }

        Resume resume = resumeOpt.get();
        File file;
        try {
            file = resumeService.getResumeFile(resume);
        } catch (ValidationException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
            return;
        }

        String encodedName = URLEncoder.encode(resume.getFileName(), StandardCharsets.UTF_8).replace("+", "%20");
        resp.setContentType("application/pdf");
        resp.setHeader("Content-Disposition", "inline; filename=\"" + encodedName + "\"");
        resp.setHeader("X-Content-Type-Options", "nosniff");
        resp.setContentLengthLong(file.length());

        try (InputStream in = new FileInputStream(file);
             OutputStream out = resp.getOutputStream()) {
            in.transferTo(out);
            out.flush();
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
