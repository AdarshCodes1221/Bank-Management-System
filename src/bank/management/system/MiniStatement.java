package bank.management.system;

import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class MiniStatement extends JFrame {
    String PIN;

    MiniStatement(String PIN) {
        this.PIN = PIN;

        // Frame Properties
        setTitle("Mini Statement");
        setSize(400, 600);
        setLocation(20, 20);
        setLayout(null);
        getContentPane().setBackground(Color.BLACK); // Set background to black

        // Title Label
        JLabel title = new JLabel("Mini Statement", JLabel.CENTER);
        title.setBounds(50, 20, 300, 30);
        title.setFont(new Font("Arial", Font.BOLD, 18));
        title.setForeground(Color.WHITE);
        add(title);

        // Card Number Label
        JLabel cardLabel = new JLabel("Card Number: ");
        cardLabel.setBounds(20, 60, 200, 20);
        cardLabel.setFont(new Font("Arial", Font.BOLD, 14));
        cardLabel.setForeground(Color.WHITE);
        add(cardLabel);

        JLabel cardNumber = new JLabel();
        cardNumber.setBounds(140, 60, 240, 20);
        cardNumber.setFont(new Font("Arial", Font.PLAIN, 14));
        cardNumber.setForeground(Color.WHITE);
        add(cardNumber);

        // Transaction Details Area
        JTextArea transactionArea = new JTextArea();
        transactionArea.setBounds(20, 150, 350, 200);
        transactionArea.setFont(new Font("Arial", Font.PLAIN, 14));
        transactionArea.setBackground(Color.BLACK);
        transactionArea.setForeground(Color.WHITE);
        transactionArea.setEditable(false);
        add(transactionArea);

        // Total Balance Label
        JLabel balanceLabel = new JLabel("Your Total Balance is: Rs ");
        balanceLabel.setBounds(20, 370, 250, 20);
        balanceLabel.setFont(new Font("Arial", Font.BOLD, 14));
        balanceLabel.setForeground(Color.WHITE);
        add(balanceLabel);

        JLabel balanceAmount = new JLabel();
        balanceAmount.setBounds(200, 370, 150, 20);
        balanceAmount.setFont(new Font("Arial", Font.BOLD, 14));
        balanceAmount.setForeground(Color.WHITE);
        add(balanceAmount);

        // Fetching Card Number
        try {
            DatabaseConnection c = new DatabaseConnection();
            PreparedStatement ps = c.connection.prepareStatement("SELECT card_number FROM signup WHERE pin = ?");
            ps.setString(1, PIN);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String cardNum = rs.getString("card_number");
                cardNumber.setText(cardNum.substring(0, 4) + "XXXXXXXXXXXX" + cardNum.substring(12));
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Fetching Last 4 Transactions
        try {
            DatabaseConnection c = new DatabaseConnection();
            PreparedStatement ps = c.connection.prepareStatement(
                    "SELECT transaction_date, type, amount FROM transactions WHERE pin = ? ORDER BY transaction_date DESC LIMIT 4"
            );
            ps.setString(1, PIN);
            ResultSet rs = ps.executeQuery();

            StringBuilder transactions = new StringBuilder();
            while (rs.next()) {
                transactions.append(rs.getString("transaction_date")).append("   ")
                        .append(rs.getString("type")).append("   ")
                        .append(rs.getInt("amount")).append("\n");
            }
            transactionArea.setText(transactions.toString());
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Fetching Total Balance
        try {
            int balance = 0;
            DatabaseConnection c = new DatabaseConnection();
            PreparedStatement ps = c.connection.prepareStatement(
                    "SELECT COALESCE(SUM(amount), 0) AS balance FROM transactions WHERE pin = ?"
            );
            ps.setString(1, PIN);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                balance = rs.getInt("balance");
            }
            balanceAmount.setText("Rs " + balance);
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Exit Button
        JButton exitButton = new JButton("Exit");
        exitButton.setBounds(150, 470, 100, 30);
        exitButton.setFont(new Font("Arial", Font.BOLD, 14));
        exitButton.setBackground(Color.WHITE);
        exitButton.setForeground(Color.BLACK);
        exitButton.addActionListener(e -> dispose());
        add(exitButton);

        new Main_Class(PIN);

        setVisible(true);

    }

    public static void main(String[] args) {
        new MiniStatement(" ");
    }
}
