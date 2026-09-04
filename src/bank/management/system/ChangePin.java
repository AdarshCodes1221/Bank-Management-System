package bank.management.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class ChangePin extends JFrame implements ActionListener {
    JButton changeButton, backButton;
    JPasswordField passwordField1, passwordField2;
    String PIN;

    ChangePin(String PIN) {
        this.PIN = PIN;

        // Background image
        ImageIcon i1 = new ImageIcon(ClassLoader.getSystemResource("icon/atm2.png"));
        Image i2 = i1.getImage().getScaledInstance(1550, 830, Image.SCALE_DEFAULT);
        ImageIcon i3 = new ImageIcon(i2);
        JLabel background = new JLabel(i3);
        background.setBounds(0, 0, 1550, 830);
        add(background);

        // Label for changing PIN
        JLabel label = new JLabel("Change Your PIN");
        label.setFont(new Font("Arial", Font.BOLD, 18));
        label.setForeground(Color.WHITE);
        label.setBounds(550, 250, 300, 30);
        background.add(label);

        // New PIN input
        JLabel pinLabel1 = new JLabel("New PIN:");
        pinLabel1.setFont(new Font("Arial", Font.BOLD, 14));
        pinLabel1.setForeground(Color.WHITE);
        pinLabel1.setBounds(500, 300, 100, 30);
        background.add(pinLabel1);

        passwordField1 = new JPasswordField();
        passwordField1.setBounds(650, 300, 150, 30);
        background.add(passwordField1);

        // Re-enter PIN input
        JLabel pinLabel2 = new JLabel("Re-Enter New PIN:");
        pinLabel2.setFont(new Font("Arial", Font.BOLD, 14));
        pinLabel2.setForeground(Color.WHITE);
        pinLabel2.setBounds(500, 350, 150, 30);
        background.add(pinLabel2);

        passwordField2 = new JPasswordField();
        passwordField2.setBounds(650, 350, 150, 30);
        background.add(passwordField2);

        // Change Button
        changeButton = new JButton("Change");
        changeButton.setBounds(550, 400, 100, 30);
        changeButton.addActionListener(this);
        background.add(changeButton);

        // Back Button
        backButton = new JButton("Back");
        backButton.setBounds(650, 400, 100, 30);
        backButton.addActionListener(this);
        background.add(backButton);

        setSize(1550, 1080);
        setLayout(null);
        setLocation(0, 0);
        setVisible(true);
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == changeButton) {
            String newPin = new String(passwordField1.getPassword());
            String confirmPin = new String(passwordField2.getPassword());

            // Validation: Check if both PINs match
            if (!newPin.equals(confirmPin)) {
                JOptionPane.showMessageDialog(null, "PINs do not match. Try again!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // OTP verification for sensitive operation (PIN change)
            if (!requestAndVerifyOtp(PIN)) {
                return;
            }

            // Update PIN in Database
            try {
                Connection con = DriverManager.getConnection("jdbc:mysql://localhost:3306/banksystem", "root", "090658");
                con.setAutoCommit(false); // Start transaction

                try {
                    // Look up card_number using the current PIN hash
                    String lookupQuery = "SELECT card_number FROM signup WHERE pin = ?";
                    PreparedStatement lookupStmt = con.prepareStatement(lookupQuery);
                    lookupStmt.setString(1, PIN);
                    ResultSet rs = lookupStmt.executeQuery();
                    String cardNumber = null;
                    if (rs.next()) {
                        cardNumber = rs.getString("card_number");
                    }
                    rs.close();
                    lookupStmt.close();

                    // Hash the new PIN using the card number as salt
                    String newHashedPin = PinHasher.hashPin(newPin, cardNumber);

                    // Update PIN in signup table
                    String query1 = "UPDATE signup SET pin = ? WHERE pin = ?";
                    PreparedStatement pstmt1 = con.prepareStatement(query1);
                    pstmt1.setString(1, newHashedPin);
                    pstmt1.setString(2, PIN);
                    pstmt1.executeUpdate();
                    pstmt1.close();

                    // Update PIN in transactions table
                    String query2 = "UPDATE transactions SET pin = ? WHERE pin = ?";
                    PreparedStatement pstmt2 = con.prepareStatement(query2);
                    pstmt2.setString(1, newHashedPin);
                    pstmt2.setString(2, PIN);
                    pstmt2.executeUpdate();
                    pstmt2.close();

                    con.commit(); // Commit transaction
                    JOptionPane.showMessageDialog(null, "PIN successfully updated!");
                    PIN = newHashedPin; // Update the current PIN hash in memory
                } catch (SQLException ex) {
                    con.rollback(); // Rollback transaction in case of failure
                    JOptionPane.showMessageDialog(null, "Error updating PIN: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                } finally {
                    con.setAutoCommit(true); // Restore default auto-commit behavior
                    con.close();
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        } else if (e.getSource() == backButton) {
            setVisible(false);
            new Main_Class(PIN);
        }
    }

    /**
     * Requests an OTP via email and verifies the user's entry.
     * Returns true if verified, false if cancelled or failed.
     */
    private boolean requestAndVerifyOtp(String pinHash) {
        OtpService otpService = OtpService.getInstance();
        boolean sent = otpService.generateAndSendOtp(pinHash);
        if (!sent) {
            if (!otpService.isEmailConfigured()) {
                JOptionPane.showMessageDialog(this,
                    "Email service not configured. Contact administrator.",
                    "Configuration Error", JOptionPane.ERROR_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to send OTP to your registered email.",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
            return false;
        }

        OtpEntryDialog dialog = new OtpEntryDialog(this, pinHash);
        dialog.setVisible(true);

        String otp = dialog.getEnteredOtp();
        if (otp == null) {
            JOptionPane.showMessageDialog(this,
                "OTP verification failed. PIN change cancelled.",
                "Cancelled", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }

        return true;
    }

    public static void main(String[] args) {
        new ChangePin("1234");
    }
}
