package com.hirehub.util;

import org.junit.jupiter.api.Test;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsrfUtilTest {

    @Test
    void getToken_generatesAndCachesTokenInSession() {
        MockHttpSession session = new MockHttpSession();
        String token1 = CsrfUtil.getToken(session);

        assertNotNull(token1);
        assertFalse(token1.isBlank());

        String token2 = CsrfUtil.getToken(session);
        assertTrue(token1.equals(token2), "Token should remain consistent for the same session.");
    }

    @Test
    void isValid_matchesSubmittedParamAgainstSession() {
        MockHttpSession session = new MockHttpSession();
        String token = CsrfUtil.getToken(session);

        MockHttpServletRequest validReq = new MockHttpServletRequest(session);
        validReq.setParameter(CsrfUtil.CSRF_PARAM_NAME, token);
        assertTrue(CsrfUtil.isValid(validReq));

        MockHttpServletRequest invalidReq = new MockHttpServletRequest(session);
        invalidReq.setParameter(CsrfUtil.CSRF_PARAM_NAME, "wrong-token-value");
        assertFalse(CsrfUtil.isValid(invalidReq));

        MockHttpServletRequest missingReq = new MockHttpServletRequest(session);
        assertFalse(CsrfUtil.isValid(missingReq));
    }

    private static class MockHttpSession implements HttpSession {
        private final Map<String, Object> attrs = new HashMap<>();

        @Override
        public Object getAttribute(String s) {
            return attrs.get(s);
        }

        @Override
        public void setAttribute(String s, Object o) {
            attrs.put(s, o);
        }

        @Override
        public void removeAttribute(String s) {
            attrs.remove(s);
        }

        // Unused interface methods
        @Override public long getCreationTime() { return 0; }
        @Override public String getId() { return "mock"; }
        @Override public long getLastAccessedTime() { return 0; }
        @Override public javax.servlet.ServletContext getServletContext() { return null; }
        @Override public void setMaxInactiveInterval(int i) {}
        @Override public int getMaxInactiveInterval() { return 0; }
        @Override public javax.servlet.http.HttpSessionContext getSessionContext() { return null; }
        @Override public Object getValue(String s) { return null; }
        @Override public java.util.Enumeration<String> getAttributeNames() { return null; }
        @Override public String[] getValueNames() { return new String[0]; }
        @Override public void putValue(String s, Object o) {}
        @Override public void removeValue(String s) {}
        @Override public void invalidate() { attrs.clear(); }
        @Override public boolean isNew() { return false; }
    }

    private static class MockHttpServletRequest implements HttpServletRequest {
        private final HttpSession session;
        private final Map<String, String> params = new HashMap<>();

        public MockHttpServletRequest(HttpSession session) {
            this.session = session;
        }

        public void setParameter(String k, String v) {
            params.put(k, v);
        }

        @Override
        public HttpSession getSession(boolean create) {
            return session;
        }

        @Override
        public String getParameter(String s) {
            return params.get(s);
        }

        @Override
        public String getHeader(String s) {
            return null;
        }

        // Unused interface stubs
        @Override public String getAuthType() { return null; }
        @Override public javax.servlet.http.Cookie[] getCookies() { return new Cookie[0]; }
        @Override public long getDateHeader(String s) { return 0; }
        @Override public java.util.Enumeration<String> getHeaders(String s) { return null; }
        @Override public java.util.Enumeration<String> getHeaderNames() { return null; }
        @Override public int getIntHeader(String s) { return 0; }
        @Override public String getMethod() { return "POST"; }
        @Override public String getPathInfo() { return null; }
        @Override public String getPathTranslated() { return null; }
        @Override public String getContextPath() { return "/HireHub"; }
        @Override public String getQueryString() { return null; }
        @Override public String getRemoteUser() { return null; }
        @Override public boolean isUserInRole(String s) { return false; }
        @Override public java.security.Principal getUserPrincipal() { return null; }
        @Override public String getRequestedSessionId() { return null; }
        @Override public String getRequestURI() { return null; }
        @Override public StringBuffer getRequestURL() { return null; }
        @Override public String getServletPath() { return null; }
        @Override public HttpSession getSession() { return session; }
        @Override public String changeSessionId() { return null; }
        @Override public boolean isRequestedSessionIdValid() { return true; }
        @Override public boolean isRequestedSessionIdFromCookie() { return true; }
        @Override public boolean isRequestedSessionIdFromURL() { return false; }
        @Override public boolean isRequestedSessionIdFromUrl() { return false; }
        @Override public boolean authenticate(javax.servlet.http.HttpServletResponse httpServletResponse) { return false; }
        @Override public void login(String s, String s1) {}
        @Override public void logout() {}
        @Override public java.util.Collection<javax.servlet.http.Part> getParts() { return null; }
        @Override public javax.servlet.http.Part getPart(String s) { return null; }
        @Override public <T extends javax.servlet.http.HttpUpgradeHandler> T upgrade(Class<T> aClass) { return null; }
        @Override public Object getAttribute(String s) { return null; }
        @Override public java.util.Enumeration<String> getAttributeNames() { return null; }
        @Override public String getCharacterEncoding() { return null; }
        @Override public void setCharacterEncoding(String s) {}
        @Override public int getContentLength() { return 0; }
        @Override public long getContentLengthLong() { return 0; }
        @Override public String getContentType() { return null; }
        @Override public javax.servlet.ServletInputStream getInputStream() { return null; }
        @Override public java.util.Enumeration<String> getParameterNames() { return null; }
        @Override public String[] getParameterValues(String s) { return new String[0]; }
        @Override public Map<String, String[]> getParameterMap() { return null; }
        @Override public String getProtocol() { return null; }
        @Override public String getScheme() { return null; }
        @Override public String getServerName() { return null; }
        @Override public int getServerPort() { return 0; }
        @Override public java.io.BufferedReader getReader() { return null; }
        @Override public String getRemoteAddr() { return null; }
        @Override public String getRemoteHost() { return null; }
        @Override public void setAttribute(String s, Object o) {}
        @Override public void removeAttribute(String s) {}
        @Override public java.util.Locale getLocale() { return null; }
        @Override public java.util.Enumeration<java.util.Locale> getLocales() { return null; }
        @Override public boolean isSecure() { return false; }
        @Override public javax.servlet.RequestDispatcher getRequestDispatcher(String s) { return null; }
        @Override public String getRealPath(String s) { return null; }
        @Override public int getRemotePort() { return 0; }
        @Override public String getLocalName() { return null; }
        @Override public String getLocalAddr() { return null; }
        @Override public int getLocalPort() { return 0; }
        @Override public javax.servlet.ServletContext getServletContext() { return null; }
        @Override public javax.servlet.AsyncContext startAsync() { return null; }
        @Override public javax.servlet.AsyncContext startAsync(javax.servlet.ServletRequest servletRequest, javax.servlet.ServletResponse servletResponse) { return null; }
        @Override public boolean isAsyncStarted() { return false; }
        @Override public boolean isAsyncSupported() { return false; }
        @Override public javax.servlet.AsyncContext getAsyncContext() { return null; }
        @Override public javax.servlet.DispatcherType getDispatcherType() { return null; }
    }
}
