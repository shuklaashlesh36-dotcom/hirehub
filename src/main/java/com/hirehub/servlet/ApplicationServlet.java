package com.hirehub.servlet;

import com.hirehub.model.Application;
import com.hirehub.model.User;
import com.hirehub.service.ApplicationService;
import com.hirehub.service.ApplicationServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

/**
 * Servlet handling candidate applications view (My Applications).
 * Guarded under /seeker/* route requiring authenticated JOB_SEEKER session.
 */
public class ApplicationServlet extends HttpServlet {

    private final ApplicationService applicationService;

    public ApplicationServlet() {
        this(new ApplicationServiceImpl());
    }

    public ApplicationServlet(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("currentUser") == null) {
            resp.sendRedirect(req.getContextPath() + "/login?authRequired=true");
            return;
        }

        User user = (User) session.getAttribute("currentUser");
        List<Application> applications = applicationService.getCandidateApplications(user.getId());

        req.setAttribute("applications", applications);

        String appliedParam = req.getParameter("applied");
        if ("true".equals(appliedParam)) {
            req.setAttribute("successMessage", "Application submitted successfully! Track its status below.");
        }

        req.getRequestDispatcher("/seeker/applications.jsp").forward(req, resp);
    }
}
