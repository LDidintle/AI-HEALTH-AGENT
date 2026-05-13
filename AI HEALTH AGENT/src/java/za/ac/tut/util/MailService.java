package za.ac.tut.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLSocketFactory;

public final class MailService {

    private static final Logger LOGGER = Logger.getLogger(MailService.class.getName());
    private static final int CONNECT_TIMEOUT_MILLIS = 15000;
    private static final int READ_TIMEOUT_MILLIS = 15000;

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

        try {
            sendViaSmtp(host, Integer.parseInt(port), username, password, from, toEmail, otp,
                    Boolean.parseBoolean(config("SMARTHEALTH_SMTP_STARTTLS", "true")));
            return true;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Password reset OTP email could not be sent: {0}", e.getMessage());
            return false;
        }
    }

    private static void sendViaSmtp(String host, int port, String username, String password,
            String from, String toEmail, String otp, boolean startTls) throws Exception {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), CONNECT_TIMEOUT_MILLIS);
        socket.setSoTimeout(READ_TIMEOUT_MILLIS);

        try {
            SmtpConnection smtp = new SmtpConnection(socket);
            smtp.expect(220);
            smtp.command("EHLO smarthealthcom.com", 250);

            if (startTls) {
                smtp.command("STARTTLS", 220);
                Socket secureSocket = ((SSLSocketFactory) SSLSocketFactory.getDefault())
                        .createSocket(socket, host, port, true);
                secureSocket.setSoTimeout(READ_TIMEOUT_MILLIS);
                socket = secureSocket;
                smtp = new SmtpConnection(socket);
                smtp.command("EHLO smarthealthcom.com", 250);
            }

            smtp.command("AUTH LOGIN", 334);
            smtp.command(base64(username), 334);
            smtp.command(base64(password), 235);
            smtp.command("MAIL FROM:<" + from + ">", 250);
            smtp.command("RCPT TO:<" + toEmail + ">", 250);
            smtp.command("DATA", 354);
            smtp.data(buildMessage(from, toEmail, otp));
            smtp.expect(250);
            smtp.command("QUIT", 221);
        } finally {
            socket.close();
        }
    }

    private static String buildMessage(String from, String toEmail, String otp) {
        String body = "Your SmartHealth password reset OTP is: " + otp
                + "\r\n\r\nThis code expires in 10 minutes. If you did not request this reset, ignore this email.";
        return "From: " + from + "\r\n"
                + "To: " + toEmail + "\r\n"
                + "Subject: SmartHealth password reset OTP\r\n"
                + "MIME-Version: 1.0\r\n"
                + "Content-Type: text/plain; charset=UTF-8\r\n"
                + "\r\n"
                + body;
    }

    private static String base64(String value) {
        return Base64.getEncoder().encodeToString(value.getBytes(StandardCharsets.UTF_8));
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

    private static final class SmtpConnection {
        private final BufferedReader reader;
        private final PrintWriter writer;

        SmtpConnection(Socket socket) throws Exception {
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
            this.writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
        }

        void command(String command, int expectedCode) throws Exception {
            writer.print(command + "\r\n");
            writer.flush();
            expect(expectedCode);
        }

        void data(String message) {
            String safeMessage = message.replace("\r\n.", "\r\n..");
            writer.print(safeMessage + "\r\n.\r\n");
            writer.flush();
        }

        void expect(int expectedCode) throws Exception {
            String response = readResponse();
            int code = smtpCode(response);
            if (code != expectedCode) {
                throw new IllegalStateException("SMTP expected " + expectedCode + " but received " + response);
            }
        }

        private String readResponse() throws Exception {
            StringBuilder response = new StringBuilder();
            String line;
            do {
                line = reader.readLine();
                if (line == null) {
                    throw new IllegalStateException("SMTP server closed the connection.");
                }
                if (response.length() > 0) {
                    response.append(" | ");
                }
                response.append(line);
            } while (line.length() > 3 && line.charAt(3) == '-');
            return response.toString();
        }

        private int smtpCode(String response) {
            if (response.length() < 3) {
                return -1;
            }
            try {
                return Integer.parseInt(response.substring(0, 3));
            } catch (NumberFormatException e) {
                return -1;
            }
        }
    }
}
