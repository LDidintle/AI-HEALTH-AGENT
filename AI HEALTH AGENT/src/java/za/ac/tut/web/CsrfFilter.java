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

        if (isSafeMethod(httpRequest) || isApiRequest(httpRequest) || isPublicLoginPost(httpRequest)
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
        String uri = request.getRequestURI();
        return uri != null && (uri.contains("/api/mobile/") || uri.endsWith("/AIChatServlet.do"));
    }

    private boolean isPublicLoginPost(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod()) || request.getRequestURI() == null) {
            return false;
        }
        String path = request.getRequestURI();
        return path.endsWith("/UserConfirmServlet.do")
                || path.endsWith("/AdminServlet.do")
                || path.endsWith("/HospitalLoginServlet.do");
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
            String requestScheme = forwardedValue(request, "X-Forwarded-Proto", request.getScheme());
            String requestHost = forwardedHost(request);
            int requestPort = forwardedPort(request, requestScheme, request.getServerPort());
            int uriPort = effectivePort(uri.getScheme(), uri.getPort());
            return requestHost.equalsIgnoreCase(uri.getHost())
                    && requestScheme.equalsIgnoreCase(uri.getScheme())
                    && requestPort == uriPort;
        } catch (Exception e) {
            return false;
        }
    }

    private String forwardedHost(HttpServletRequest request) {
        String host = forwardedValue(request, "X-Forwarded-Host", request.getServerName());
        int colonIndex = host.indexOf(':');
        return colonIndex >= 0 ? host.substring(0, colonIndex) : host;
    }

    private int forwardedPort(HttpServletRequest request, String scheme, int fallbackPort) {
        String port = request.getHeader("X-Forwarded-Port");
        if (port != null && !port.trim().isEmpty()) {
            try {
                return Integer.parseInt(port.trim());
            } catch (NumberFormatException ignored) {
                // Fall through to host/scheme inference.
            }
        }

        String host = request.getHeader("X-Forwarded-Host");
        if (host != null) {
            int colonIndex = host.indexOf(':');
            if (colonIndex >= 0 && colonIndex + 1 < host.length()) {
                try {
                    return Integer.parseInt(host.substring(colonIndex + 1));
                } catch (NumberFormatException ignored) {
                    // Fall through to scheme inference.
                }
            }
        }

        return effectivePort(scheme, fallbackPort);
    }

    private String forwardedValue(HttpServletRequest request, String headerName, String fallback) {
        String value = request.getHeader(headerName);
        if (value == null || value.trim().isEmpty()) {
            return fallback;
        }
        int commaIndex = value.indexOf(',');
        return (commaIndex >= 0 ? value.substring(0, commaIndex) : value).trim();
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
