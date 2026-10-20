package com.hirehub.util;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility for session-bound CSRF token generation and validation.
 */
public final class CsrfUtil {

    public static final String CSRF_SESSION_KEY = "csrfToken";
    public static final String CSRF_PARAM_NAME = "csrfToken";
    private static final SecureRandom RANDOM = new SecureRandom();

    private CsrfUtil() {
    }

    /**
     * Retrieves or generates a cryptographically random CSRF token for the session.
     *
     * @param session the user's HttpSession
     * @return non-null CSRF token
     */
    public static String getToken(HttpSession session) {
        if (session == null) {
            return "";
        }
        synchronized (session) {
            String token = (String) session.getAttribute(CSRF_SESSION_KEY);
            if (token == null || token.isBlank()) {
                byte[] bytes = new byte[32];
                RANDOM.nextBytes(bytes);
                token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
                session.setAttribute(CSRF_SESSION_KEY, token);
            }
            return token;
        }
    }

    /**
     * Validates that the submitted token matches the one in the user's session.
     *
     * @param request HttpServletRequest
     * @return true if token matches, false otherwise
     */
    public static boolean isValid(HttpServletRequest request) {
        if (request == null) {
            return false;
        }
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }

        String sessionToken = (String) session.getAttribute(CSRF_SESSION_KEY);
        if (sessionToken == null || sessionToken.isBlank()) {
            return false;
        }

        String submittedToken = request.getParameter(CSRF_PARAM_NAME);
        if (submittedToken == null || submittedToken.isBlank()) {
            submittedToken = request.getHeader("X-CSRF-Token");
        }

        return sessionToken.equals(submittedToken);
    }
}
