

package bank.management.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class BalanceEnquiry extends JFrame implements ActionListener {
    String PIN;
    JLabel label2;
    JButton backButton;

    BalanceEnquiry(String PIN) {
        this.PIN = PIN;

        ImageIcon i1 = new ImageIcon(ClassLoader.getSystemResource("icon/atm2.png"));
        Image i2 = i1.getImage().getScaledInstance(1550, 830, Image.SCALE_DEFAULT);
        ImageIcon i3 = new ImageIcon(i2);
        JLabel background = new JLabel(i3);
        background.setBounds(0, 0, 1550, 830);
        add(background);

        JLabel label = new JLabel("Your Current Balance is:");
        label.setFont(new Font("Arial", Font.BOLD, 18));
        label.setForeground(Color.WHITE);
        label.setBounds(430, 250, 400, 30);
        background.add(label);

        // Back Button
        backButton = new JButton("Back");
        backButton.setBounds(550, 400, 100, 30);
        backButton.addActionListener(this);
        background.add(backButton);

        label2 = new JLabel();
        label2.setFont(new Font("Arial", Font.BOLD, 18));
        label2.setForeground(Color.WHITE);
        label2.setBounds(650, 250, 400, 30);
        background.add(label2);

        int balance = 0;
        try {
            DatabaseConnection c = new DatabaseConnection();
            Connection conn = c.connection;
            String query = "SELECT SUM(amount) AS balance FROM transactions WHERE pin = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, PIN);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                balance = rs.getInt("balance");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        label2.setText("₹ " + balance);

        setLayout(null);
        setSize(1550, 1080);
        setLocation(0, 0);
        setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == backButton) {
            setVisible(false);
            new Main_Class(PIN);
        }
    }

    public static void main(String[] args) {
        new BalanceEnquiry("");
    }
}
