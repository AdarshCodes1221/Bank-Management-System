package bank.management.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class Deposit extends JFrame implements ActionListener {
    String PIN;
    JTextField amountField;
    JButton depositButton, backButton;

    // Constructor
    public Deposit(String PIN) {
        this.PIN = PIN;

        // ATM Background Image
        ImageIcon i1 = new ImageIcon(ClassLoader.getSystemResource("icon/atm2.png"));
        Image i2 = i1.getImage().getScaledInstance(1550, 830, Image.SCALE_DEFAULT);
        ImageIcon i3 = new ImageIcon(i2);
        JLabel background = new JLabel(i3);
        background.setBounds(0, 0, 1550, 830);
        add(background);

        // Enter Amount Label
        JLabel label = new JLabel("Enter Amount:");
        label.setFont(new Font("Arial", Font.BOLD, 18));
        label.setForeground(Color.WHITE);
        label.setBounds(550, 300, 200, 30);
        background.add(label);

        // Amount Input Field
        amountField = new JTextField();
        amountField.setBounds(550, 350, 200, 30);
        background.add(amountField);

        // Deposit Button
        depositButton = new JButton("Deposit");
        depositButton.setBounds(550, 400, 100, 30);
        depositButton.addActionListener(this);
        background.add(depositButton);

        // Back Button
        backButton = new JButton("Back");
        backButton.setBounds(650, 400, 100, 30);
        backButton.addActionListener(this);
        background.add(backButton);

        setLayout(null);
        setSize(1550, 1080);
        setLocation(0, 0);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == depositButton) {
            String amount = amountField.getText();

            if (amount.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please enter an amount to deposit");
                return;
            }

            try {
                int depositAmount = Integer.parseInt(amount);

                // Use the Con class to get the database connection
                DatabaseConnection con = new DatabaseConnection();
                Connection connection = con.connection;

                String query = "INSERT INTO transactions (pin, type, amount) VALUES (?, 'Deposit', ?)";
                PreparedStatement pstmt = connection.prepareStatement(query);
                pstmt.setString(1, PIN);
                pstmt.setInt(2, depositAmount);
                pstmt.executeUpdate();

                JOptionPane.showMessageDialog(null, "Rs. " + depositAmount + " Deposited Successfully!");

               setVisible(false);
                new Main_Class(PIN);


            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(null, "Invalid amount entered!");
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Database error!");
            }
        } else if (e.getSource() == backButton) {
            setVisible(false);
            new Main_Class(PIN);

        }
    }

    // Main Method
    public static void main(String[] args) {
        new Deposit(" ");
    }
}
