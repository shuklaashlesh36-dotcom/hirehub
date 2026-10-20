package com.hirehub.servlet;

import com.hirehub.exception.ValidationException;
import com.hirehub.model.Role;
import com.hirehub.model.User;
import com.hirehub.service.ProfileService;
import com.hirehub.service.ProfileServiceImpl;
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
 * Servlet handling profile photo streaming, secure upload, and removal.
 * Mapped to /profile/photo, /profile/photo/upload, and /profile/photo/remove.
 */
@MultipartConfig(
        maxFileSize = 2 * 1024 * 1024,      // 2 MB
        maxRequestSize = 3 * 1024 * 1024    // 3 MB
)
public class ProfilePhotoServlet extends HttpServlet {

    private static final String DEFAULT_AVATAR_SVG =
            "<svg xmlns=\"http://www.w3.org/2000/svg\" viewBox=\"0 0 120 120\" width=\"120\" height=\"120\">"
                    + "<defs>"
                    + "<linearGradient id=\"avGrad\" x1=\"0%\" y1=\"0%\" x2=\"100%\" y2=\"100%\">"
                    + "<stop offset=\"0%\" stop-color=\"#4f46e5\"/>"
                    + "<stop offset=\"100%\" stop-color=\"#06b6d4\"/>"
                    + "</linearGradient>"
                    + "</defs>"
                    + "<circle cx=\"60\" cy=\"60\" r=\"58\" fill=\"url(#avGrad)\"/>"
                    + "<circle cx=\"60\" cy=\"48\" r=\"22\" fill=\"#ffffff\" opacity=\"0.9\"/>"
                    + "<path d=\"M26 102 C 26 80, 42 74, 60 74 C 78 74, 94 80, 94 102 Z\" fill=\"#ffffff\" opacity=\"0.9\"/>"
                    + "</svg>";

    private final ProfileService profileService;

    public ProfilePhotoServlet() {
        this(new ProfileServiceImpl());
    }

    public ProfilePhotoServlet(ProfileService profileService) {
        this.profileService = profileService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        User sessionUser = getAuthenticatedUser(req, resp);
        if (sessionUser == null) {
            return;
        }

        Long targetUserId = sessionUser.getId();
        String userIdParam = req.getParameter("userId");
        if (userIdParam != null && !userIdParam.isBlank()) {
            try {
                targetUserId = Long.parseLong(userIdParam.trim());
            } catch (NumberFormatException e) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid user ID parameter.");
                return;
            }
        }

        // Authorization check: verify requester is permitted to view target user's photo
        if (!profileService.canViewProfilePhoto(sessionUser, targetUserId)) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "You are not authorized to view this user's profile photo.");
            return;
        }

        Optional<User> targetUserOpt = profileService.getUserProfile(targetUserId);
        if (targetUserOpt.isEmpty() || targetUserOpt.get().getProfilePhoto() == null) {
            serveDefaultAvatar(resp);
            return;
        }

        String photoFileName = targetUserOpt.get().getProfilePhoto();
        try {
            File photoFile = profileService.getProfilePhotoFile(photoFileName);
            String contentType = determineContentType(photoFileName);
            resp.setContentType(contentType);
            resp.setContentLengthLong(photoFile.length());
            resp.setHeader("Cache-Control", "private, max-age=3600");

            try (InputStream in = new FileInputStream(photoFile);
                 OutputStream out = resp.getOutputStream()) {
                in.transferTo(out);
            }
        } catch (Exception e) {
            // Fall back cleanly to default avatar if file is missing or corrupted
            serveDefaultAvatar(resp);
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

        // Cross-user ID security check: never allow uploading or removing for another user
        String reqUserIdStr = req.getParameter("userId");
        if (reqUserIdStr != null && !reqUserIdStr.isBlank()) {
            try {
                long reqUserId = Long.parseLong(reqUserIdStr.trim());
                if (reqUserId != sessionUser.getId() && sessionUser.getRole() != Role.ADMIN) {
                    resp.sendError(HttpServletResponse.SC_FORBIDDEN, "You cannot alter another user's profile photo.");
                    return;
                }
            } catch (NumberFormatException ignored) {
            }
        }

        String servletPath = req.getServletPath();
        String pathInfo = req.getPathInfo();
        String requestUri = req.getRequestURI();

        boolean isRemove = (pathInfo != null && pathInfo.contains("remove"))
                || (servletPath != null && servletPath.contains("remove"))
                || (requestUri != null && requestUri.contains("/photo/remove"));

        if (isRemove) {
            handleRemove(req, resp, sessionUser);
        } else {
            handleUpload(req, resp, sessionUser);
        }
    }

    private void handleUpload(HttpServletRequest req, HttpServletResponse resp, User sessionUser)
            throws ServletException, IOException {
        try {
            Part photoPart = req.getPart("photo");
            if (photoPart == null || photoPart.getSize() <= 0) {
                resp.sendRedirect(req.getContextPath() + "/profile?error="
                        + URLEncoder.encode("Please select an image file to upload.", StandardCharsets.UTF_8));
                return;
            }

            String filename = photoPart.getSubmittedFileName();
            String contentType = photoPart.getContentType();
            long size = photoPart.getSize();

            String savedName = profileService.uploadProfilePhoto(sessionUser.getId(), filename, contentType,
                    photoPart.getInputStream(), size);

            sessionUser.setProfilePhoto(savedName);
            req.getSession().setAttribute("currentUser", sessionUser);

            resp.sendRedirect(req.getContextPath() + "/profile?photoSuccess="
                    + URLEncoder.encode("Profile photo uploaded successfully.", StandardCharsets.UTF_8));

        } catch (ValidationException e) {
            resp.sendRedirect(req.getContextPath() + "/profile?error="
                    + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/profile?error="
                    + URLEncoder.encode("Failed to process photo upload: " + e.getMessage(), StandardCharsets.UTF_8));
        }
    }

    private void handleRemove(HttpServletRequest req, HttpServletResponse resp, User sessionUser)
            throws IOException {
        try {
            profileService.removeProfilePhoto(sessionUser.getId());
            sessionUser.setProfilePhoto(null);
            req.getSession().setAttribute("currentUser", sessionUser);

            resp.sendRedirect(req.getContextPath() + "/profile?photoSuccess="
                    + URLEncoder.encode("Profile photo removed successfully. Default avatar restored.", StandardCharsets.UTF_8));
        } catch (Exception e) {
            resp.sendRedirect(req.getContextPath() + "/profile?error="
                    + URLEncoder.encode("Failed to remove profile photo: " + e.getMessage(), StandardCharsets.UTF_8));
        }
    }

    private void serveDefaultAvatar(HttpServletResponse resp) throws IOException {
        byte[] bytes = DEFAULT_AVATAR_SVG.getBytes(StandardCharsets.UTF_8);
        resp.setContentType("image/svg+xml");
        resp.setContentLength(bytes.length);
        resp.setHeader("Cache-Control", "public, max-age=86400");
        try (OutputStream out = resp.getOutputStream()) {
            out.write(bytes);
        }
    }

    private String determineContentType(String fileName) {
        String lower = fileName.toLowerCase();
        if (lower.endsWith(".png")) {
            return "image/png";
        } else if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        return "image/jpeg";
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
