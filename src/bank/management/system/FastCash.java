package bank.management.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class FastCash extends JFrame implements ActionListener {
    String PIN;
    int balance;
    JButton b500, b1000, b2000, b5000, b10000, b20000, exit;
    DatabaseConnection con;

    FastCash(String PIN) {
        this.PIN = PIN;
        con = new DatabaseConnection();
        fetchBalance();

        ImageIcon i1 = new ImageIcon(ClassLoader.getSystemResource("icon/atm2.png"));
        Image i2 = i1.getImage().getScaledInstance(1550, 830, Image.SCALE_DEFAULT);
        ImageIcon i3 = new ImageIcon(i2);
        JLabel background = new JLabel(i3);
        background.setBounds(0, 0, 1550, 830);
        add(background);

        JLabel label = new JLabel("SELECT WITHDRAWAL AMOUNT");
        label.setBounds(450, 180, 600, 35);
        label.setForeground(Color.WHITE);
        label.setFont(new Font("System", Font.BOLD, 18));
        background.add(label);

        b500 = new JButton("₹500");
        b500.setBounds(410, 274, 150, 35);
        b500.setForeground(Color.WHITE);
        b500.setBackground(new Color(102, 153, 255));
        b500.addActionListener(this);
        background.add(b500);

        b1000 = new JButton("₹1000");
        b1000.setBounds(700, 274, 150, 35);
        b1000.setForeground(Color.WHITE);
        b1000.setBackground(new Color(102, 153, 255));
        b1000.addActionListener(this);
        background.add(b1000);

        b2000 = new JButton("₹2000");
        b2000.setBounds(410, 318, 150, 35);
        b2000.setForeground(Color.WHITE);
        b2000.setBackground(new Color(102, 153, 255));
        b2000.addActionListener(this);
        background.add(b2000);

        b5000 = new JButton("₹5000");
        b5000.setBounds(700, 318, 150, 35);
        b5000.setForeground(Color.WHITE);
        b5000.setBackground(new Color(102, 153, 255));
        b5000.addActionListener(this);
        background.add(b5000);

        b10000 = new JButton("₹10000");
        b10000.setBounds(410, 362, 150, 35);
        b10000.setForeground(Color.WHITE);
        b10000.setBackground(new Color(102, 153, 255));
        b10000.addActionListener(this);
        background.add(b10000);

        b20000 = new JButton("₹20000");
        b20000.setBounds(700, 362, 150, 35);
        b20000.setForeground(Color.WHITE);
        b20000.setBackground(new Color(102, 153, 255));
        b20000.addActionListener(this);
        background.add(b20000);

        exit = new JButton("EXIT");
        exit.setBounds(700, 406, 150, 35);
        exit.setForeground(Color.WHITE);
        exit.setBackground(new Color(255, 51, 51));
        exit.addActionListener(this);
        background.add(exit);

        setSize(1550, 1080);
        setLocation(0, 0);
        setVisible(true);
    }

    private void fetchBalance() {
        try {
            PreparedStatement ps = con.connection.prepareStatement("SELECT COALESCE(SUM(amount), 0) AS balance FROM transactions WHERE pin = ?");
            ps.setString(1, PIN);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                balance = rs.getInt("balance");
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateBalance(int amount) {
        try {
            PreparedStatement ps = con.connection.prepareStatement("INSERT INTO transactions (pin, amount, type, transaction_date) VALUES (?, ?, 'Withdraw', NOW())");
            ps.setString(1, PIN);
            ps.setInt(2, -amount);
            ps.executeUpdate();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == exit) {
            setVisible(false);
            new Main_Class(PIN);
        } else {
            String amountStr = ((JButton) e.getSource()).getText().replace("₹", "");
            int amount = Integer.parseInt(amountStr);

            if (amount > balance) {
                JOptionPane.showMessageDialog(this, "Insufficient balance!");
            } else {
                // OTP verification for sensitive operation (Fast Cash withdrawal)
                if (!requestAndVerifyOtp(PIN)) {
                    return;
                }
                balance -= amount;
                updateBalance(amount);
                JOptionPane.showMessageDialog(this, "You have withdrawn ₹" + amount + "\nRemaining balance: ₹" + balance);
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
                "OTP verification failed. Transaction cancelled.",
                "Cancelled", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }

        return true;
    }

    public static void main(String[] args) {
        new FastCash("");
    }
}
