package bank.management.system;
import java.sql.PreparedStatement;
import javax.swing.*;
import java.sql.ResultSet;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import java.sql.SQLException;

public class Login extends JFrame implements ActionListener {

    JLabel label1, label2, label3;
    JTextField textField2; // Card
    JPasswordField passwordField3; // PIN
    JButton loginButton, clearButton, signupButton;

    Login() {
        super("Bank Management System");
        setLayout(null);

        // Bank Icon
        ImageIcon i1 = new ImageIcon(ClassLoader.getSystemResource("icon/bank.png"));
        Image i2 = i1.getImage().getScaledInstance(100, 100, Image.SCALE_DEFAULT);
        ImageIcon i3 = new ImageIcon(i2);
        JLabel bankImage = new JLabel(i3);
        bankImage.setBounds(370, 30, 100, 100);
        add(bankImage);


        ImageIcon ii1 = new ImageIcon(ClassLoader.getSystemResource("icon/card.png"));
        Image ii2 = ii1.getImage().getScaledInstance(100, 100, Image.SCALE_DEFAULT);
        ImageIcon ii3 = new ImageIcon(ii2);
        JLabel cardImage = new JLabel(ii3);
        cardImage.setBounds(620, 350, 100, 100);
        add(cardImage);

        // Welcome Label
        label1 = new JLabel("WELCOME TO BANK ");
        label1.setForeground(Color.green);
        label1.setFont(new Font("AvantGrade", Font.BOLD, 36));
        label1.setBounds(230, 125, 450, 40);
        add(label1);

        // Card Number Label
        label2 = new JLabel("Card No");
        label2.setFont(new Font("Railway", Font.BOLD, 28));
        label2.setForeground(Color.red);
        label2.setBounds(150, 190, 375, 30);
        add(label2);

        // Card Number Input Field
        textField2 = new JTextField();
        textField2.setFont(new Font("Arial", Font.BOLD, 14));
        textField2.setBounds(330, 190, 250, 30);
        add(textField2);

        // PIN Label
        label3 = new JLabel("PIN");
        label3.setFont(new Font("Railway", Font.BOLD, 28));
        label3.setForeground(Color.red);
        label3.setBounds(150, 250, 375, 30);
        add(label3);

        // PIN Input Field
        passwordField3 = new JPasswordField();
        passwordField3.setFont(new Font("Arial", Font.BOLD, 14));
        passwordField3.setBounds(330, 250, 250, 30);
        add(passwordField3);

        // Login Button
        loginButton = new JButton("Login");
        loginButton.setBounds(330, 310, 100, 30);
        loginButton.setFont(new Font("Arial", Font.BOLD, 14));
        loginButton.setForeground(Color.white);
        loginButton.setBackground(Color.black);
        loginButton.addActionListener(this);
        add(loginButton);

        // Clear Button
        clearButton = new JButton("Clear");
        clearButton.setBounds(480, 310, 100, 30);
        clearButton.setFont(new Font("Arial", Font.BOLD, 14));
        clearButton.setForeground(Color.white);
        clearButton.setBackground(Color.black);
        clearButton.addActionListener(this);
        add(clearButton);

        // Signup Button
        signupButton = new JButton("Sign Up");
        signupButton.setBounds(330, 360, 250, 30);
        signupButton.setFont(new Font("Arial", Font.BOLD, 14));
        signupButton.setForeground(Color.white);
        signupButton.setBackground(Color.black);
        signupButton.addActionListener(this);
        add(signupButton);

        // Background Image
        ImageIcon iii1 = new ImageIcon(ClassLoader.getSystemResource("icon/back2.jpeg"));
        Image iii2 = iii1.getImage().getScaledInstance(850, 480, Image.SCALE_DEFAULT);
        ImageIcon iii3 = new ImageIcon(iii2);
        JLabel bgImage = new JLabel(iii3);
        bgImage.setBounds(0, 0, 850, 480);
        add(bgImage);

        // Frame settings
        setSize(850, 480);
        setLocation(450, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);
        setVisible(true);
    }

    // Handling button actions
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == loginButton) {
            String cardNumber = textField2.getText();
            String pin = new String(passwordField3.getPassword());
            String hashedPin = PinHasher.hashPin(pin, cardNumber);

            try {
                DatabaseConnection conn = new DatabaseConnection(); // Create database connection
                String query = "SELECT * FROM signup WHERE card_number=? AND pin=?";

                try (PreparedStatement pstmt = conn.connection.prepareStatement(query)) {
                    pstmt.setString(1, cardNumber);
                    pstmt.setString(2, hashedPin);

                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            JOptionPane.showMessageDialog(null, "Login Successful!");
                            setVisible(false);
                            new  Main_Class(hashedPin);
                        } else {
                            JOptionPane.showMessageDialog(null, "Invalid Card Number or PIN. Try Again.");
                        }
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Database Error: " + ex.getMessage());
            }

        } else if (e.getSource() == clearButton) {
            textField2.setText("");
            passwordField3.setText("");
        } else if (e.getSource() == signupButton) {
            JOptionPane.showMessageDialog(null, "Loading...", "Please Wait", JOptionPane.INFORMATION_MESSAGE);
            new Signup(); // Open Signup Form
            setVisible(false);
        }
    }

    public static void main(String[] args) {
        new Login();
    }
}