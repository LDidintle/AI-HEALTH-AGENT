package za.ac.tut.web;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.SessionCookieConfig;

public class SessionCookieConfigListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        SessionCookieConfig config = event.getServletContext().getSessionCookieConfig();
        config.setHttpOnly(true);
        config.setSecure(Boolean.parseBoolean(setting("SMARTHEALTH_SECURE_COOKIES", "false")));
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        // Nothing to release.
    }

    private String setting(String name, String fallback) {
        String property = System.getProperty(name);
        if (property != null && !property.trim().isEmpty()) {
            return property.trim();
        }
        String env = System.getenv(name);
        return env == null || env.trim().isEmpty() ? fallback : env.trim();
    }
}
