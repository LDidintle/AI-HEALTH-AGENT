package za.ac.tut.util;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public final class MailService {

    private static final Logger LOGGER = Logger.getLogger(MailService.class.getName());

    private MailService() {
    }

    public static boolean sendPasswordResetOtp(String toEmail, String otp) {
        String host = config("SMARTHEALTH_SMTP_HOST");
        String port = config("SMARTHEALTH_SMTP_PORT", "587");
        String username = config("SMARTHEALTH_SMTP_USER");
        String password = config("SMARTHEALTH_SMTP_PASSWORD");
        String from = config("SMARTHEALTH_SMTP_FROM", username);

        if (isBlank(host) || isBlank(username) || isBlank(password) || isBlank(from)) {
            LOGGER.info("SMTP is not configured; password reset OTP email was not sent.");
            return false;
        }

        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", config("SMARTHEALTH_SMTP_STARTTLS", "true"));
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.port", port);

        Session session = Session.getInstance(props, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("SmartHealth password reset OTP");
            message.setText("Your SmartHealth password reset OTP is: " + otp
                    + "\n\nThis code expires in 10 minutes. If you did not request this reset, ignore this email.");
            Transport.send(message);
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Password reset OTP email could not be sent: {0}", e.getMessage());
            return false;
        }
    }

    private static String config(String name) {
        return config(name, null);
    }

    private static String config(String name, String fallback) {
        String property = trimToNull(System.getProperty(name));
        if (property != null) {
            return property;
        }
        String env = trimToNull(System.getenv(name));
        return env == null ? fallback : env;
    }

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
