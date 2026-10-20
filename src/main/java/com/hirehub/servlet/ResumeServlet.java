package com.hirehub.servlet;

import com.hirehub.exception.ValidationException;
import com.hirehub.model.Resume;
import com.hirehub.model.User;
import com.hirehub.service.ResumeService;
import com.hirehub.service.ResumeServiceImpl;
import com.hirehub.util.CsrfUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Servlet handling candidate resume management (uploading, viewing, replacing, and streaming PDF).
 * Guarded under /seeker/* route requiring authenticated JOB_SEEKER session.
 */
@MultipartConfig(
        maxFileSize = 5 * 1024 * 1024,      // 5 MB
        maxRequestSize = 6 * 1024 * 1024    // 6 MB
)
public class ResumeServlet extends HttpServlet {

    private final ResumeService resumeService;

    public ResumeServlet() {
        this(new ResumeServiceImpl());
    }

    public ResumeServlet(ResumeService resumeService) {
        this.resumeService = resumeService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = getAuthenticatedSeeker(req, resp);
        if (user == null) {
            return;
        }

        String pathInfo = req.getPathInfo();
        String servletPath = req.getServletPath();
        String requestUri = req.getRequestURI();
        if ("/download".equalsIgnoreCase(pathInfo)
                || (servletPath != null && servletPath.endsWith("/download"))
                || (requestUri != null && requestUri.endsWith("/download"))) {
            handleDownload(req, resp, user.getId());
            return;
        }

        Optional<Resume> resumeOpt = resumeService.getCandidateResume(user.getId());
        req.setAttribute("resume", resumeOpt.orElse(null));
        req.setAttribute("csrfToken", CsrfUtil.getToken(req.getSession(true)));

        String uploadedParam = req.getParameter("uploaded");
        if ("true".equals(uploadedParam)) {
            req.setAttribute("successMessage", "Your resume has been uploaded successfully!");
        }

        req.getRequestDispatcher("/seeker/resume.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User user = getAuthenticatedSeeker(req, resp);
        if (user == null) {
            return;
        }

        if (!CsrfUtil.isValid(req)) {
            req.setAttribute("errorMessage", "Security validation failed. Please refresh the page and try again.");
            Optional<Resume> resumeOpt = resumeService.getCandidateResume(user.getId());
            req.setAttribute("resume", resumeOpt.orElse(null));
            req.setAttribute("csrfToken", CsrfUtil.getToken(req.getSession(true)));
            req.getRequestDispatcher("/seeker/resume.jsp").forward(req, resp);
            return;
        }

        try {
            Part filePart = req.getPart("resumeFile");
            if (filePart == null || filePart.getSize() == 0) {
                throw new ValidationException("Please select a PDF file to upload.");
            }

            String submittedName = filePart.getSubmittedFileName();
            if (submittedName == null || submittedName.isBlank()) {
                throw new ValidationException("Invalid file name.");
            }

            try (InputStream is = filePart.getInputStream()) {
                resumeService.uploadResume(user.getId(), submittedName, is, filePart.getSize());
            }

            resp.sendRedirect(req.getContextPath() + "/seeker/resume?uploaded=true");

        } catch (ValidationException e) {
            req.setAttribute("errorMessage", e.getMessage());
            Optional<Resume> resumeOpt = resumeService.getCandidateResume(user.getId());
            req.setAttribute("resume", resumeOpt.orElse(null));
            req.setAttribute("csrfToken", CsrfUtil.getToken(req.getSession(true)));
            req.getRequestDispatcher("/seeker/resume.jsp").forward(req, resp);
        } catch (Exception e) {
            req.setAttribute("errorMessage", "Upload failed: " + e.getMessage());
            Optional<Resume> resumeOpt = resumeService.getCandidateResume(user.getId());
            req.setAttribute("resume", resumeOpt.orElse(null));
            req.setAttribute("csrfToken", CsrfUtil.getToken(req.getSession(true)));
            req.getRequestDispatcher("/seeker/resume.jsp").forward(req, resp);
        }
    }

    private void handleDownload(HttpServletRequest req, HttpServletResponse resp, Long seekerId)
            throws IOException {
        Optional<Resume> resumeOpt = resumeService.getCandidateResume(seekerId);
        if (resumeOpt.isEmpty()) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No resume found for candidate.");
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

    private User getAuthenticatedSeeker(HttpServletRequest req, HttpServletResponse resp)
            throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login?authRequired=true");
            return null;
        }
        return (User) session.getAttribute("currentUser");
    }
}
