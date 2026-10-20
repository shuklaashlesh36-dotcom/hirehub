package com.hirehub.servlet;

import com.hirehub.model.Employer;
import com.hirehub.model.EmployerDashboardStats;
import com.hirehub.model.User;
import com.hirehub.service.EmployerService;
import com.hirehub.service.EmployerServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Controller servlet for the Employer Dashboard.
 * Protected under /employer/* by AuthenticationFilter and RoleAuthorizationFilter.
 */
public class EmployerDashboardServlet extends HttpServlet {

    private final EmployerService employerService;

    public EmployerDashboardServlet() {
        this(new EmployerServiceImpl());
    }

    public EmployerDashboardServlet(EmployerService employerService) {
        this.employerService = employerService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("currentUser") : null;
        if (user == null) {
            resp.sendRedirect(req.getContextPath() + "/login?authRequired=true");
            return;
        }

        EmployerDashboardStats stats = employerService.getDashboardStats(user.getId());
        req.setAttribute("stats", stats);
        req.setAttribute("employer", user instanceof Employer ? (Employer) user : null);

        req.getRequestDispatcher("/employer/dashboard.jsp").forward(req, resp);
    }
}
