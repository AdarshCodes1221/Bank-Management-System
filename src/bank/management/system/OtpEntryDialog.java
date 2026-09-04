package bank.management.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Modal dialog for OTP (One-Time Password) entry.
 *
 * Displays a 6-digit OTP input field with masking (JPasswordField),
 * a 5-minute countdown timer, a resend button, and attempt tracking.
 *
 * The dialog validates the OTP internally via OtpService and only
 * returns a non-null OTP value when validation succeeds.
 */
public class OtpEntryDialog extends JDialog {
    private JPasswordField otpField;
    private JLabel timerLabel;
    private JLabel messageLabel;
    private JButton okButton;
    private JButton resendButton;
    private JButton cancelButton;

    private String enteredOtp; // null if cancelled or failed
    private final String pinHash;
    private final OtpService otpService;

    private Timer countdownTimer;
    private int timeRemainingSeconds = 300; // 5 minutes

    public OtpEntryDialog(Frame parent, String pinHash) {
        super(parent, "OTP Verification", true);
        this.pinHash = pinHash;
        this.otpService = OtpService.getInstance();

        initializeUI();
        startCountdown();
    }

    private void initializeUI() {
        setLayout(null);
        setSize(420, 260);
        setLocationRelativeTo(getParent());
        setResizable(false);

        // Message label
        messageLabel = new JLabel("Enter the 6-digit OTP sent to your email:");
        messageLabel.setBounds(30, 25, 370, 22);
        messageLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        add(messageLabel);

        // OTP input field (masked)
        otpField = new JPasswordField();
        otpField.setBounds(130, 60, 160, 30);
        otpField.setFont(new Font("Arial", Font.BOLD, 16));
        add(otpField);

        // Focus on OTP field
        otpField.requestFocusInWindow();

        // Timer label
        timerLabel = new JLabel("Expires in: " + formatTime(timeRemainingSeconds));
        timerLabel.setBounds(130, 100, 200, 22);
        timerLabel.setFont(new Font("Arial", Font.BOLD, 13));
        timerLabel.setForeground(Color.RED);
        add(timerLabel);

        // OK (Verify) Button
        okButton = new JButton("Verify");
        okButton.setBounds(70, 150, 100, 30);
        okButton.addActionListener(e -> onOkClick());
        add(okButton);

        // Resend Button
        resendButton = new JButton("Resend OTP");
        resendButton.setBounds(180, 150, 110, 30);
        resendButton.addActionListener(e -> onResendClick());
        add(resendButton);

        // Cancel Button
        cancelButton = new JButton("Cancel");
        cancelButton.setBounds(300, 150, 80, 30);
        cancelButton.addActionListener(e -> onCancelClick());
        add(cancelButton);

        // Set default button (Enter key triggers Verify)
        getRootPane().setDefaultButton(okButton);
    }

    private void onOkClick() {
        String otp = new String(otpField.getPassword()).trim();

        if (otp.length() != 6 || !otp.matches("\\d{6}")) {
            messageLabel.setText("Please enter a valid 6-digit OTP.");
            otpField.setText("");
            return;
        }

        OtpService.OtpStatus status = otpService.validateOtp(pinHash, otp);

        switch (status) {
            case VALID:
                enteredOtp = otp;
                dispose();
                break;
            case INVALID:
                int attempts = otpService.getRemainingAttempts(pinHash);
                if (attempts > 0) {
                    messageLabel.setText("Invalid OTP. " + attempts + " attempt(s) remaining.");
                } else {
                    messageLabel.setText("Too many failed attempts. Click Resend.");
                    okButton.setEnabled(false);
                }
                otpField.setText("");
                break;
            case EXPIRED:
                messageLabel.setText("OTP expired. Click Resend for a new one.");
                okButton.setEnabled(false);
                countdownTimer.stop();
                break;
            case ATTEMPTS_EXHAUSTED:
                messageLabel.setText("Attempts exhausted. Click Resend for a new OTP.");
                okButton.setEnabled(false);
                countdownTimer.stop();
                break;
            case NOT_FOUND:
                messageLabel.setText("No OTP found. Click Resend to request one.");
                okButton.setEnabled(false);
                countdownTimer.stop();
                break;
        }
    }

    private void onResendClick() {
        boolean sent = otpService.generateAndSendOtp(pinHash);
        if (sent) {
            messageLabel.setText("New OTP sent to your email.");
            timeRemainingSeconds = 300;
            timerLabel.setText(formatTime(timeRemainingSeconds));
            okButton.setEnabled(true);
            otpField.setText("");
        } else {
            if (!otpService.isEmailConfigured()) {
                messageLabel.setText("Email service not configured.");
            } else {
                messageLabel.setText("Failed to send OTP. Please try again.");
            }
        }
    }

    private void onCancelClick() {
        enteredOtp = null;
        dispose();
    }

    private void startCountdown() {
        countdownTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                timeRemainingSeconds--;
                if (timeRemainingSeconds <= 0) {
                    countdownTimer.stop();
                    okButton.setEnabled(false);
                    messageLabel.setText("OTP expired. Click Resend for a new one.");
                    timerLabel.setText("Expires in: 00:00");
                } else {
                    timerLabel.setText("Expires in: " + formatTime(timeRemainingSeconds));
                }
            }
        });
        countdownTimer.start();
    }

    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }

    /**
     * Returns the verified OTP if validation succeeded, or null if the
     * dialog was cancelled, timed out, or verification failed.
     */
    public String getEnteredOtp() {
        return enteredOtp;
    }
}
