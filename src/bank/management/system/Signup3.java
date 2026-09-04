package bank.management.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Random;

public class Signup3 extends JFrame implements ActionListener {
    private final long applicationNumber;
    private JRadioButton savingAccount, currentAccount, fixedDeposit, recurringDeposit;
    private JCheckBox atmCard, internetBanking, mobileBanking, emailAlerts, chequeBook, eStatement;
    private JButton submit, cancel;
    private String generatedCardNumber;
    private String generatedPin;

    Signup3(long applicationNumber) {
        this.applicationNumber = applicationNumber;

        // Generate random card number and PIN
        this.generatedCardNumber = generateCardNumber();
        this.generatedPin = generatePin();

        initializeUI();
    }

    private void initializeUI() {
        setTitle("Page 3: Account Details - Application #" + applicationNumber);
        setLayout(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Set dark theme background
        getContentPane().setBackground(new Color(30, 30, 30));

        // Title
        JLabel title = new JLabel("Account Details");
        title.setFont(new Font("Arial", Font.BOLD, 22));
        title.setForeground(new Color(0, 200, 255));
        title.setBounds(200, 20, 300, 30);
        add(title);

        // Account Type Section
        addAccountTypeSection();

        // Card Details Section
        addCardDetailsSection();

        // Services Section
        addServicesSection();

        // Buttons
        addButtons();

        setSize(600, 550);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void addAccountTypeSection() {
        JLabel accountTypeLabel = new JLabel("Account Type:");
        accountTypeLabel.setFont(new Font("Arial", Font.BOLD, 16));
        accountTypeLabel.setForeground(Color.WHITE);
        accountTypeLabel.setBounds(100, 70, 200, 30);
        add(accountTypeLabel);

        savingAccount = createRadioButton("Saving Account", 100, 100);
        currentAccount = createRadioButton("Current Account", 300, 100);
        fixedDeposit = createRadioButton("Fixed Deposit", 100, 130);
        recurringDeposit = createRadioButton("Recurring Deposit", 300, 130);

        ButtonGroup accountGroup = new ButtonGroup();
        accountGroup.add(savingAccount);
        accountGroup.add(currentAccount);
        accountGroup.add(fixedDeposit);
        accountGroup.add(recurringDeposit);
    }

    private JRadioButton createRadioButton(String text, int x, int y) {
        JRadioButton radio = new JRadioButton(text);
        radio.setBounds(x, y, 180, 30);
        radio.setBackground(new Color(30, 30, 30));
        radio.setForeground(Color.WHITE);
        radio.setFocusPainted(false);
        add(radio);
        return radio;
    }

    private void addCardDetailsSection() {
        JLabel cardNumberLabel = new JLabel("Card Number:");
        cardNumberLabel.setFont(new Font("Arial", Font.BOLD, 16));
        cardNumberLabel.setForeground(Color.WHITE);
        cardNumberLabel.setBounds(100, 180, 200, 30);
        add(cardNumberLabel);

        JLabel cardNumber = new JLabel(formatCardNumber(generatedCardNumber));
        cardNumber.setFont(new Font("Arial", Font.BOLD, 16));
        cardNumber.setForeground(new Color(0, 255, 200));
        cardNumber.setBounds(300, 180, 250, 30);
        add(cardNumber);

        JLabel pinLabel = new JLabel("PIN:");
        pinLabel.setFont(new Font("Arial", Font.BOLD, 16));
        pinLabel.setForeground(Color.WHITE);
        pinLabel.setBounds(100, 220, 100, 30);
        add(pinLabel);

        JLabel pin = new JLabel(generatedPin);
        pin.setFont(new Font("Arial", Font.BOLD, 16));
        pin.setForeground(new Color(0, 255, 200));
        pin.setBounds(300, 220, 100, 30);
        add(pin);
    }

    private void addServicesSection() {
        JLabel servicesLabel = new JLabel("Services Required:");
        servicesLabel.setFont(new Font("Arial", Font.BOLD, 16));
        servicesLabel.setForeground(Color.WHITE);
        servicesLabel.setBounds(100, 260, 200, 30);
        add(servicesLabel);

        atmCard = createCheckBox("ATM Card", 100, 300);
        internetBanking = createCheckBox("Internet Banking", 300, 300);
        mobileBanking = createCheckBox("Mobile Banking", 100, 330);
        emailAlerts = createCheckBox("Email Alerts", 300, 330);
        chequeBook = createCheckBox("Cheque Book", 100, 360);
        eStatement = createCheckBox("E-Statement", 300, 360);
    }

    private JCheckBox createCheckBox(String text, int x, int y) {
        JCheckBox checkBox = new JCheckBox(text);
        checkBox.setBounds(x, y, 150, 30);
        checkBox.setBackground(new Color(30, 30, 30));
        checkBox.setForeground(Color.WHITE);
        checkBox.setFocusPainted(false);
        add(checkBox);
        return checkBox;
    }

    private void addButtons() {
        submit = new JButton("Submit");
        cancel = new JButton("Cancel");

        submit.setBounds(150, 420, 120, 35);
        cancel.setBounds(300, 420, 120, 35);

        // Button styling
        styleButton(submit, new Color(0, 150, 0));
        styleButton(cancel, new Color(200, 0, 0));

        submit.addActionListener(this);
        cancel.addActionListener(this);

        add(submit);
        add(cancel);
    }

    private void styleButton(JButton button, Color bgColor) {
        button.setBackground(bgColor);
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder(5, 15, 5, 15));
    }

    private String generateCardNumber() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    private String formatCardNumber(String cardNumber) {
        return cardNumber.replaceAll("(.{4})", "$1-").substring(0, 19);
    }

    private String generatePin() {
        Random random = new Random();
        return String.format("%04d", random.nextInt(10000));
    }

    private String getSelectedAccountType() {
        if (savingAccount.isSelected()) return "Saving Account";
        if (currentAccount.isSelected()) return "Current Account";
        if (fixedDeposit.isSelected()) return "Fixed Deposit";
        if (recurringDeposit.isSelected()) return "Recurring Deposit";
        return "";
    }

    private String getSelectedServices() {
        StringBuilder services = new StringBuilder();
        if (atmCard.isSelected()) services.append("ATM Card, ");
        if (internetBanking.isSelected()) services.append("Internet Banking, ");
        if (mobileBanking.isSelected()) services.append("Mobile Banking, ");
        if (emailAlerts.isSelected()) services.append("Email Alerts, ");
        if (chequeBook.isSelected()) services.append("Cheque Book, ");
        if (eStatement.isSelected()) services.append("E-Statement");

        // Remove trailing comma if exists
        String result = services.toString().trim();
        if (result.endsWith(",")) {
            result = result.substring(0, result.length() - 1);
        }
        return result;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == submit) {
            handleSubmit();
        } else if (e.getSource() == cancel) {
            handleCancel();
        }
    }

    private void handleSubmit() {
        String accountType = getSelectedAccountType();
        if (accountType.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select an account type!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String services = getSelectedServices();

        try {
            DatabaseConnection c = new DatabaseConnection();
            Connection conn = c.connection;

            // Update the existing record with account details
            String query = "UPDATE signup SET account_type=?, services=?, card_number=?, pin=? WHERE application_number=?";

            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                pstmt.setString(1, accountType);
                pstmt.setString(2, services);
                pstmt.setString(3, generatedCardNumber);
                pstmt.setString(4, PinHasher.hashPin(generatedPin, generatedCardNumber));
                pstmt.setLong(5, applicationNumber);

                int rowsUpdated = pstmt.executeUpdate();
                if (rowsUpdated > 0) {
                    JOptionPane.showMessageDialog(this,
                            "<html><center>Account Created Successfully!<br>" +
                                    "Card Number: " + formatCardNumber(generatedCardNumber) + "</center></html>",
                            "Success", JOptionPane.INFORMATION_MESSAGE);

                    // Return to Login screen with generated credentials stored in DB
                    new Login();

                    // Close current window
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to update account details!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }

    }

    private void handleCancel() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to cancel account creation?",
                "Confirm Cancellation",
                JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            dispose();
            // Optionally return to previous screen or close application
        }
    }

    public static void main(String[] args) {
        // For testing only
        SwingUtilities.invokeLater(() -> new Signup3(123456L));
    }
}