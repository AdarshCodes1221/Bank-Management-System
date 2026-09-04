// Signup2.java
package bank.management.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Signup2 extends JFrame implements ActionListener {
    private final long applicationNumber;
    JComboBox<String> religionBox, categoryBox, incomeBox, educationBox, occupationBox;
    JTextField panField, aadharField;
    JRadioButton seniorYes, seniorNo, accountYes, accountNo;
    JButton nextButton;

    Signup2(long applicationNumber) {
        this.applicationNumber = applicationNumber;

        setTitle("APPLICATION FORM - Page 2");
        setSize(800, 600);
        setLocation(300, 100);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);

        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(64, 64, 64), 0, getHeight(), new Color(32, 32, 32));
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        panel.setLayout(null);
        panel.setBounds(0, 0, 800, 600);
        add(panel);

        JLabel title = new JLabel("Page 2: Additional Details - Application #" + applicationNumber);
        title.setFont(new Font("Arial", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        title.setBounds(200, 30, 400, 30);
        panel.add(title);

        String[] religions = {"Hindu", "Muslim", "Christian", "Sikh", "Other"};
        String[] categories = {"General", "OBC", "SC", "ST", "Other"};
        String[] incomes = {"Null", "< 2 LPA", "2-5 LPA", "5-10 LPA", "> 10 LPA"};
        String[] educations = {"Non-Graduate", "Graduate", "Post-Graduate", "Doctorate", "Other"};
        String[] occupations = {"Salaried", "Self-Employed", "Business", "Student", "Retired"};

        addLabel(panel, "Religion :", 100, 100);
        religionBox = new JComboBox<>(religions);
        addComboBox(panel, religionBox, 250, 100);

        addLabel(panel, "Category :", 100, 150);
        categoryBox = new JComboBox<>(categories);
        addComboBox(panel, categoryBox, 250, 150);

        addLabel(panel, "Income :", 100, 200);
        incomeBox = new JComboBox<>(incomes);
        addComboBox(panel, incomeBox, 250, 200);

        addLabel(panel, "Educational :", 100, 250);
        educationBox = new JComboBox<>(educations);
        addComboBox(panel, educationBox, 250, 250);

        addLabel(panel, "Occupation :", 100, 300);
        occupationBox = new JComboBox<>(occupations);
        addComboBox(panel, occupationBox, 250, 300);

        addLabel(panel, "PAN Number :", 100, 350);
        panField = new JTextField();
        addTextField(panel, panField, 250, 350);

        addLabel(panel, "Aadhar Number :", 100, 400);
        aadharField = new JTextField();
        addTextField(panel, aadharField, 250, 400);

        addLabel(panel, "Senior Citizen :", 100, 450);
        seniorYes = new JRadioButton("Yes");
        seniorNo = new JRadioButton("No");
        addRadioButtons(panel, seniorYes, seniorNo, 250, 450);

        addLabel(panel, "Existing Account :", 100, 500);
        accountYes = new JRadioButton("Yes");
        accountNo = new JRadioButton("No");
        addRadioButtons(panel, accountYes, accountNo, 250, 500);

        nextButton = new JButton("Next");
        nextButton.setBounds(350, 530, 100, 30);
        nextButton.setFont(new Font("Arial", Font.BOLD, 14));
        nextButton.setBackground(Color.WHITE);
        nextButton.setForeground(Color.BLACK);
        nextButton.addActionListener(this);
        panel.add(nextButton);

        setVisible(true);
    }

    private void addLabel(JPanel panel, String text, int x, int y) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Arial", Font.BOLD, 14));
        label.setForeground(Color.WHITE);
        label.setBounds(x, y, 150, 30);
        panel.add(label);
    }

    private void addComboBox(JPanel panel, JComboBox<String> comboBox, int x, int y) {
        comboBox.setBounds(x, y, 200, 30);
        panel.add(comboBox);
    }

    private void addTextField(JPanel panel, JTextField textField, int x, int y) {
        textField.setBounds(x, y, 200, 30);
        panel.add(textField);
    }

    private void addRadioButtons(JPanel panel, JRadioButton rb1, JRadioButton rb2, int x, int y) {
        rb1.setBounds(x, y, 60, 30);
        rb2.setBounds(x + 80, y, 60, 30);
        ButtonGroup group = new ButtonGroup();
        group.add(rb1);
        group.add(rb2);
        panel.add(rb1);
        panel.add(rb2);
    }

    private boolean isValidPAN(String pan) {
        return pan != null && pan.matches("[A-Z]{5}[0-9]{4}[A-Z]{1}");
    }

    private boolean isValidAadhar(String aadhar) {
        return aadhar != null && aadhar.matches("\\d{12}");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == nextButton) {
            String religion = (String) religionBox.getSelectedItem();
            String category = (String) categoryBox.getSelectedItem();
            String income = (String) incomeBox.getSelectedItem();
            String education = (String) educationBox.getSelectedItem();
            String occupation = (String) occupationBox.getSelectedItem();
            String pan = panField.getText().trim().toUpperCase();
            String aadhar = aadharField.getText().trim();
            String senior = seniorYes.isSelected() ? "Yes" : "No";
            String account = accountYes.isSelected() ? "Yes" : "No";

            // Validate PAN and Aadhar
            if (pan.isEmpty() || aadhar.isEmpty()) {
                JOptionPane.showMessageDialog(this, "PAN and Aadhar Number cannot be empty!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!isValidPAN(pan)) {
                JOptionPane.showMessageDialog(this, "Invalid PAN Number! Format: ABCDE1234F", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!isValidAadhar(aadhar)) {
                JOptionPane.showMessageDialog(this, "Invalid Aadhar Number! Must be 12 digits", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                DatabaseConnection c = new DatabaseConnection();
                Connection conn = c.connection;
                String query = "UPDATE signup SET religion=?, category=?, income=?, education=?, occupation=?, pan=?, aadhar=?, senior_citizen=?, existing_account=? WHERE application_number=?";

                try (PreparedStatement pst = conn.prepareStatement(query)) {
                    pst.setString(1, religion);
                    pst.setString(2, category);
                    pst.setString(3, income);
                    pst.setString(4, education);
                    pst.setString(5, occupation);
                    pst.setString(6, pan);
                    pst.setString(7, aadhar);
                    pst.setString(8, senior);
                    pst.setString(9, account);
                    pst.setLong(10, applicationNumber);

                    int rowsUpdated = pst.executeUpdate();
                    if (rowsUpdated > 0) {
                        JOptionPane.showMessageDialog(this, "Details Submitted Successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                        new Signup3(applicationNumber); // Pass application number to next form
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to update application!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        // For testing only
        new Signup2(123456); // Provide a dummy application number
    }
}