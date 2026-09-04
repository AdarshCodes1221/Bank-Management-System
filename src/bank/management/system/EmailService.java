package bank.management.system;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Base64;

/**
 * Minimal SMTP email service using Java's built-in SSLSocket.
 * No external JAR dependencies required.
 *
 * SMTP credentials are read from environment variables:
 *   SMTP_HOST      - e.g. smtp.gmail.com
 *   SMTP_PORT      - e.g. 465 (SSL) or 587 (TLS)
 *   SMTP_USERNAME  - sender email address
 *   SMTP_PASSWORD  - app-specific password or token
 *
 * NOTE: For port 587 (STARTTLS), this implementation uses SSL mode.
 *       Port 465 is recommended for SSL SMTP.
 */
public class EmailService {

    private static final String OTP_TYPE = "Bank OTP Verification";

    private final String smtpHost;
    private final int smtpPort;
    private final String username;
    private final String password;
    private final boolean configured;

    public EmailService() {
        smtpHost = System.getenv("SMTP_HOST");
        String portStr = System.getenv("SMTP_PORT");
        smtpPort = (portStr != null) ? Integer.parseInt(portStr) : 465;
        username = System.getenv("SMTP_USERNAME");
        password = System.getenv("SMTP_PASSWORD");
        configured = (smtpHost != null && username != null && password != null);
    }

    public boolean isConfigured() {
        return configured;
    }

    /**
     * Sends an OTP email to the specified recipient.
     * The OTP itself is never logged or printed to console.
     */
    public boolean sendOtpEmail(String toEmail, String otp) {
        if (!configured) {
            return false;
        }

        String body = "Your One-Time Password (OTP) is: " + otp
                    + "\n\nThis OTP will expire in 5 minutes."
                    + "\nDo not share this OTP with anyone."
                    + "\n\nThank you,\nSecure Bank Management System";

        return sendEmail(toEmail, OTP_TYPE, body);
    }

    private boolean sendEmail(String toEmail, String subject, String body) {
        SSLSocket socket = null;
        try {
            SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            socket = (SSLSocket) factory.createSocket(smtpHost, smtpPort);
            socket.startHandshake();

            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

            // Read greeting (220)
            readResponse(reader);

            // EHLO
            writer.println("EHLO " + smtpHost);
            readResponse(reader);

            // AUTH LOGIN
            writer.println("AUTH LOGIN");
            readResponse(reader);

            // Send Base64-encoded username
            writer.println(Base64.getEncoder().encodeToString(username.getBytes("UTF-8")));
            readResponse(reader);

            // Send Base64-encoded password
            writer.println(Base64.getEncoder().encodeToString(password.getBytes("UTF-8")));
            String authResponse = readResponse(reader);
            if (!authResponse.startsWith("235")) {
                return false;
            }

            // MAIL FROM
            writer.println("MAIL FROM: <" + username + ">");
            readResponse(reader);

            // RCPT TO
            writer.println("RCPT TO: <" + toEmail + ">");
            readResponse(reader);

            // DATA
            writer.println("DATA");
            readResponse(reader);

            // Send message (SMTP requires CRLF line endings in DATA)
            writer.print("Subject: " + subject + "\r\n");
            writer.print("From: " + username + "\r\n");
            writer.print("To: " + toEmail + "\r\n");
            writer.print("Content-Type: text/plain; charset=UTF-8\r\n");
            writer.print("\r\n");
            writer.print(body + "\r\n");
            writer.print(".\r\n");
            readResponse(reader);

            // QUIT
            writer.println("QUIT");

            return true;

        } catch (Exception e) {
            // Never print OTP or credentials to console
            return false;
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException ignored) {}
            }
        }
    }

    /**
     * Read an SMTP response, handling both single-line and multi-line responses.
     * Multi-line responses have continuation lines starting with "-".
     */
    private String readResponse(BufferedReader reader) throws IOException {
        StringBuilder response = new StringBuilder();
        String line;
        do {
            line = reader.readLine();
            if (line == null) break;
            response.append(line).append("\n");
        } while (line.length() >= 4 && line.charAt(3) == '-');
        return response.toString().trim();
    }
}
