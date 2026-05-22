package za.ac.tut.web;

import java.io.IOException;
import java.net.URI;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import za.ac.tut.util.CsrfUtil;

public class CsrfFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No startup configuration is required.
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        if (isSafeMethod(httpRequest) || isApiRequest(httpRequest)
                || CsrfUtil.hasValidToken(httpRequest) || isSameOrigin(httpRequest)) {
            chain.doFilter(request, response);
            return;
        }

        httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid CSRF token.");
    }

    private boolean isSafeMethod(HttpServletRequest request) {
        String method = request.getMethod();
        return "GET".equalsIgnoreCase(method) || "HEAD".equalsIgnoreCase(method) || "OPTIONS".equalsIgnoreCase(method);
    }

    private boolean isApiRequest(HttpServletRequest request) {
        return request.getRequestURI() != null && request.getRequestURI().contains("/api/mobile/");
    }

    private boolean isSameOrigin(HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        if (origin != null && !origin.trim().isEmpty()) {
            return sameHost(origin, request);
        }

        String referer = request.getHeader("Referer");
        return referer != null && sameHost(referer, request);
    }

    private boolean sameHost(String url, HttpServletRequest request) {
        try {
            URI uri = URI.create(url);
            int requestPort = effectivePort(request.getScheme(), request.getServerPort());
            int uriPort = effectivePort(uri.getScheme(), uri.getPort());
            return request.getServerName().equalsIgnoreCase(uri.getHost())
                    && request.getScheme().equalsIgnoreCase(uri.getScheme())
                    && requestPort == uriPort;
        } catch (Exception e) {
            return false;
        }
    }

    private int effectivePort(String scheme, int port) {
        if (port > 0) {
            return port;
        }
        return "https".equalsIgnoreCase(scheme) ? 443 : 80;
    }

    @Override
    public void destroy() {
        // Nothing to release.
    }
}
