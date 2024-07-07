import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class HospitalInformationSystem extends JFrame {
    private static final String URL = "jdbc:mysql://localhost:3306/hospital";
    private static final String USERNAME = "your_username";
    private static final String PASSWORD = "your_password";

    private Connection conn;
    private JLabel nameLabel, dobLabel, genderLabel, contactLabel;
    private JTextField nameField, dobField, genderField, contactField;
    private JButton addButton, retrieveButton;
    private JTextArea outputArea;

    public HospitalInformationSystem() {
        super("Hospital Information System");
        initializeGUI();

        try {
            conn = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Connected to database.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void initializeGUI() {
        nameLabel = new JLabel("Name:");
        dobLabel = new JLabel("DOB (YYYY-MM-DD):");
        genderLabel = new JLabel("Gender:");
        contactLabel = new JLabel("Contact:");

        nameField = new JTextField(20);
        dobField = new JTextField(10);
        genderField = new JTextField(10);
        contactField = new JTextField(15);

        addButton = new JButton("Add Patient");
        retrieveButton = new JButton("Retrieve Patient");

        outputArea = new JTextArea(10, 30);
        outputArea.setEditable(false);

        JPanel inputPanel = new JPanel(new GridLayout(5, 2));
        inputPanel.add(nameLabel);
        inputPanel.add(nameField);
        inputPanel.add(dobLabel);
        inputPanel.add(dobField);
        inputPanel.add(genderLabel);
        inputPanel.add(genderField);
        inputPanel.add(contactLabel);
        inputPanel.add(contactField);
        inputPanel.add(addButton);
        inputPanel.add(retrieveButton);

        JPanel outputPanel = new JPanel();
        outputPanel.add(new JScrollPane(outputArea));

        JPanel mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(outputPanel, BorderLayout.CENTER);

        add(mainPanel);

        addButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                addPatient();
            }
        });

        retrieveButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                retrievePatient();
            }
        });

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 400);
        setLocationRelativeTo(null); // Center the JFrame
        setVisible(true);
    }

    private void addPatient() {
        String name = nameField.getText().trim();
        String dob = dobField.getText().trim();
        String gender = genderField.getText().trim();
        String contact = contactField.getText().trim();

        if (name.isEmpty() || dob.isEmpty() || gender.isEmpty() || contact.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            return;
        }

        String query = "INSERT INTO patients (name, dob, gender, contact_info) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, name);
            stmt.setString(2, dob);
            stmt.setString(3, gender);
            stmt.setString(4, contact);

            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating patient failed, no rows affected.");
            }

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int patientId = generatedKeys.getInt(1);
                    outputArea.append("Patient added successfully with ID: " + patientId + "\n");
                } else {
                    throw new SQLException("Creating patient failed, no ID obtained.");
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: Failed to add patient.");
        }
    }

    private void retrievePatient() {
        int patientId = Integer.parseInt(JOptionPane.showInputDialog(this, "Enter Patient ID:"));
        String query = "SELECT * FROM patients WHERE patient_id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String name = rs.getString("name");
                String dob = rs.getString("dob");
                String gender = rs.getString("gender");
                String contactInfo = rs.getString("contact_info");
                outputArea.setText("Patient ID: " + patientId + "\nName: " + name + "\nDOB: " + dob + "\nGender: " + gender + "\nContact: " + contactInfo);
            } else {
                JOptionPane.showMessageDialog(this, "Patient not found.");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error: Failed to retrieve patient.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new HospitalInformationSystem();
            }
        });
    }
}
