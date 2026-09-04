// Signup.java
package bank.management.system;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.regex.Pattern;

public class Signup extends JFrame implements ActionListener {
    long applicationNumber;
    JTextField nameField, fatherNameField, dobField, emailField, addressField, cityField, pinCodeField, stateField;
    JRadioButton male, female, otherGender, married, unmarried, other;
    JButton nextButton;

    Signup() {
        setTitle("APPLICATION FORM");
        setLayout(null);

        // Generate a unique application number
        Random rand = new Random();
        applicationNumber = Math.abs(rand.nextLong() % 900000L) + 100000L; // 6-digit Application Number

        // Background Color
        getContentPane().setBackground(new Color(180, 255, 200));

        // Application Form Title
        JLabel title = new JLabel("APPLICATION FORM NO. " + applicationNumber);
        title.setFont(new Font("Raleway", Font.BOLD, 24));
        title.setBounds(180, 20, 500, 30);
        add(title);

        JLabel subtitle = new JLabel("Page 1: Personal Details");
        subtitle.setFont(new Font("Raleway", Font.BOLD, 18));
        subtitle.setBounds(250, 60, 400, 30);
        add(subtitle);

        // Labels and Text Fields
        nameField = new JTextField();
        fatherNameField = new JTextField();
        dobField = new JTextField();
        emailField = new JTextField();
        addressField = new JTextField();
        cityField = new JTextField();
        pinCodeField = new JTextField();
        stateField = new JTextField();

        addLabelAndField("Name :", 120, nameField);
        addLabelAndField("Father's Name :", 160, fatherNameField);
        addLabelAndField("Date of Birth (DD-MM-YYYY) :", 200, dobField);
        addLabelAndField("Email Address :", 240, emailField);
        addLabelAndField("Address :", 280, addressField);
        addLabelAndField("City :", 320, cityField);
        addLabelAndField("Pin Code :", 360, pinCodeField);
        addLabelAndField("State :", 400, stateField);

        // Gender
        addLabel("Gender :", 440);
        male = new JRadioButton("Male");
        female = new JRadioButton("Female");
        otherGender = new JRadioButton("Other");
        addRadioButtons(440, male, female, otherGender);

        // Marital Status
        addLabel("Marital Status :", 480);
        married = new JRadioButton("Married");
        unmarried = new JRadioButton("Unmarried");
        other = new JRadioButton("Other");
        addRadioButtons(480, married, unmarried, other);

        // Next Button
        nextButton = new JButton("Next");
        nextButton.setBounds(500, 530, 100, 30);
        nextButton.setBackground(Color.BLACK);
        nextButton.setForeground(Color.WHITE);
        nextButton.addActionListener(this);
        add(nextButton);

        setSize(700, 600);
        setLocation(400, 100);
        setVisible(true);
    }

    private void addLabel(String text, int y) {
        JLabel label = new JLabel(text);
        label.setBounds(100, y, 200, 30);
        add(label);
    }

    private void addLabelAndField(String text, int y, JTextField field) {
        addLabel(text, y);
        field.setBounds(300, y, 300, 30);
        add(field);
    }

    private void addRadioButtons(int y, JRadioButton... buttons) {
        ButtonGroup group = new ButtonGroup();
        int x = 300;
        for (JRadioButton btn : buttons) {
            btn.setBounds(x, y, 100, 30);
            group.add(btn);
            add(btn);
            x += 100;
        }
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
        return Pattern.compile(emailRegex).matcher(email).matches();
    }

    private boolean isValidDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        sdf.setLenient(false);
        try {
            Date date = sdf.parse(dateStr);
            // Additional check to ensure date is not in future
            return !date.after(new Date());
        } catch (ParseException e) {
            return false;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        // Input validation
        if (nameField.getText().trim().isEmpty() || fatherNameField.getText().trim().isEmpty() ||
                dobField.getText().trim().isEmpty() || emailField.getText().trim().isEmpty() ||
                addressField.getText().trim().isEmpty() || cityField.getText().trim().isEmpty() ||
                pinCodeField.getText().trim().isEmpty() || stateField.getText().trim().isEmpty() ||
                (!male.isSelected() && !female.isSelected() && !otherGender.isSelected()) ||
                (!married.isSelected() && !unmarried.isSelected() && !other.isSelected())) {
            JOptionPane.showMessageDialog(this, "Please fill all the fields before proceeding!");
            return;
        }

        // Validate pin code (should be numeric and exactly 6 digits)
        String pinCode = pinCodeField.getText().trim();
        if (!pinCode.matches("\\d{6}")) {
            JOptionPane.showMessageDialog(this, "Pin Code must be a 6-digit number!");
            return;
        }

        // Validate date format
        String dob = dobField.getText().trim();
        if (!isValidDate(dob)) {
            JOptionPane.showMessageDialog(this, "Invalid Date Format or Future Date! Use DD-MM-YYYY.");
            return;
        }

        // Validate email
        String email = emailField.getText().trim();
        if (!isValidEmail(email)) {
            JOptionPane.showMessageDialog(this, "Invalid Email Address!");
            return;
        }

        String gender = male.isSelected() ? "Male" : female.isSelected() ? "Female" : "Other";
        String maritalStatus = married.isSelected() ? "Married" : unmarried.isSelected() ? "Unmarried" : "Other";

        try {
            DatabaseConnection c = new DatabaseConnection();
            String query = "INSERT INTO signup (application_number, name, father_name, dob, gender, email, marital_status, address, city, pin_code, state) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement pstmt = c.connection.prepareStatement(query)) {
                pstmt.setLong(1, applicationNumber);
                pstmt.setString(2, nameField.getText().trim());
                pstmt.setString(3, fatherNameField.getText().trim());
                pstmt.setString(4, dob);
                pstmt.setString(5, gender);
                pstmt.setString(6, email);
                pstmt.setString(7, maritalStatus);
                pstmt.setString(8, addressField.getText().trim());
                pstmt.setString(9, cityField.getText().trim());
                pstmt.setString(10, pinCode);
                pstmt.setString(11, stateField.getText().trim());

                int rowsInserted = pstmt.executeUpdate();
                if (rowsInserted > 0) {
                    JOptionPane.showMessageDialog(this, "Application submitted successfully!");
                    setVisible(false);
                    new Signup2(applicationNumber); // Pass application number to next form
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error Saving Data: " + ex.getMessage());
        }
    }

    public static void main(String[] args) {
        new Signup();
    }
}