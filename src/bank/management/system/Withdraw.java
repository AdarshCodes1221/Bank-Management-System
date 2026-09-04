package bank.management.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class Withdraw extends JFrame implements ActionListener {
    String pin;
    JTextField amountField;
    JButton withdrawButton, backButton;
    Connection connection;

    Withdraw(String pin) {
        this.pin = pin;
        DatabaseConnection con = new DatabaseConnection();
        this.connection = con.connection;

        // ATM Background Image
        ImageIcon i1 = new ImageIcon(ClassLoader.getSystemResource("icon/atm2.png"));
        Image i2 = i1.getImage().getScaledInstance(1550, 830, Image.SCALE_DEFAULT);
        ImageIcon i3 = new ImageIcon(i2);
        JLabel background = new JLabel(i3);
        background.setBounds(0, 0, 1550, 830);
        add(background);

        // Enter Amount Label
        JLabel label = new JLabel("Maximum Withdrawal is ₹10,000");
        label.setFont(new Font("Arial", Font.BOLD, 18));
        label.setForeground(Color.WHITE);
        label.setBounds(500, 250, 400, 30);
        background.add(label);

        JLabel label2 = new JLabel("Please Enter Your Amount:");
        label2.setFont(new Font("Arial", Font.BOLD, 18));
        label2.setForeground(Color.WHITE);
        label2.setBounds(500, 300, 400, 30);
        background.add(label2);

        // Amount Input Field
        amountField = new JTextField();
        amountField.setBounds(550, 350, 200, 30);
        background.add(amountField);

        // Withdraw Button
        withdrawButton = new JButton(" Withdraw");
        withdrawButton.setBounds(550, 400, 100, 30);
        withdrawButton.addActionListener(this);
        background.add(withdrawButton);

        // Back Button
        backButton = new JButton("Back");
        backButton.setBounds(670, 400, 100, 30);
        backButton.addActionListener(this);
        background.add(backButton);

        setLayout(null);
        setSize(1550, 1080);
        setLocation(0, 0);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == withdrawButton) {
            String amountText = amountField.getText().trim();

            if (amountText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Please enter an amount!");
                return;
            }

            try {
                int withdrawAmount = Integer.parseInt(amountText);
                if (withdrawAmount > 10000) {
                    JOptionPane.showMessageDialog(this, "Withdrawal limit exceeded! Max ₹10,000.");
                    return;
                }
                if (withdrawAmount <= 0) {
                    JOptionPane.showMessageDialog(this, "Invalid amount entered.");
                    return;
                }

                // Fetch Current Balance from transaction table
                int currentBalance = getCurrentBalance();
                if (currentBalance < withdrawAmount) {
                    JOptionPane.showMessageDialog(this, "Insufficient Balance! Available: ₹" + currentBalance);
                    return;
                }

                // OTP verification for sensitive operation (Withdrawal)
                if (!requestAndVerifyOtp(pin)) {
                    return;
                }

                // Update Balance and Record Transaction
                boolean success = withdrawAmountFromAccount(withdrawAmount);
                if (success) {
                    JOptionPane.showMessageDialog(this, "₹" + withdrawAmount + " withdrawn successfully!");
                    amountField.setText("");
                } else {
                    JOptionPane.showMessageDialog(this, "Transaction Failed! Please try again.");
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number.");
            }
        } else if (e.getSource() == backButton) {
            setVisible(false);
            new Main_Class(pin);
        }
    }

    // Get Current Balance from transaction table
    private int getCurrentBalance() {
        int balance = 0;
        try {
            String query = "SELECT SUM(amount) AS balance FROM transactions WHERE pin = ?";
            PreparedStatement pst = connection.prepareStatement(query);
            pst.setString(1, pin);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                balance = rs.getInt("balance");
            }
            rs.close();
            pst.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return balance;
    }

    // Withdraw Amount and Update Database
    private boolean withdrawAmountFromAccount(int withdrawAmount) {
        try {
            connection.setAutoCommit(false); // Start transaction

            // Insert Transaction Record as Withdrawal (Negative Value)
            String insertQuery = "INSERT INTO transactions (pin, amount, type, transaction_date) VALUES (?, ?, 'Withdraw', NOW())";
            PreparedStatement insertPst = connection.prepareStatement(insertQuery);
            insertPst.setString(1, pin);
            insertPst.setInt(2, -withdrawAmount); // Fix: Store as negative value
            int insertResult = insertPst.executeUpdate();

            if (insertResult > 0) {
                connection.commit();
                return true;
            } else {
                connection.rollback();
                return false;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            try {
                connection.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            return false;
        } finally {
            try {
                connection.setAutoCommit(true);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
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
                "OTP verification failed. Withdrawal cancelled.",
                "Cancelled", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }

        return true;
    }

    public static void main(String[] args) {
        new Withdraw("");
    }
}
