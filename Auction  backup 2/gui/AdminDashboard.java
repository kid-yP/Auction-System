package gui;

import javax.swing.*;
import java.awt.*;
import model.Administrator;
import dao.UserDAO;
import model.User;
import java.util.List;

public class AdminDashboard extends JFrame {
    private Administrator admin;
    private JTable usersTable;

    public AdminDashboard(Administrator admin) {
        this.admin = admin;
        setTitle("Auction System - Admin Dashboard");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Main panel with padding
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel with welcome message and logout button
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Welcome message
        JLabel welcomeLabel = new JLabel("Welcome, Admin " + admin.getFirstName() + "!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 20));
        topPanel.add(welcomeLabel, BorderLayout.WEST);

        // Logout button with styling
        JButton logoutButton = new JButton("Logout");
        logoutButton.setFont(new Font("Arial", Font.BOLD, 14));
        logoutButton.setBackground(new Color(220, 53, 69)); // Red color
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setPreferredSize(new Dimension(100, 35));

        logoutButton.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });
        topPanel.add(logoutButton, BorderLayout.EAST);
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Users table
        UserDAO userDAO = new UserDAO();
        List<User> users = userDAO.getAllUsers();

        String[] columnNames = {"ID", "Username", "Email", "First Name", "Last Name", "Role"};
        Object[][] data = new Object[users.size()][columnNames.length];

        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            data[i][0] = user.getUserId();
            data[i][1] = user.getUsername();
            data[i][2] = user.getEmail();
            data[i][3] = user.getFirstName();
            data[i][4] = user.getLastName();
            data[i][5] = user.getRole();
        }

        usersTable = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(usersTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // Action buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton editUserButton = new JButton("Edit User");
        JButton deleteUserButton = new JButton("Delete User");

        // Style the action buttons
        editUserButton.setFont(new Font("Arial", Font.PLAIN, 14));
        deleteUserButton.setFont(new Font("Arial", Font.PLAIN, 14));
        editUserButton.setPreferredSize(new Dimension(120, 35));
        deleteUserButton.setPreferredSize(new Dimension(120, 35));

        buttonPanel.add(editUserButton);
        buttonPanel.add(deleteUserButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        editUserButton.addActionListener(e -> {
            int selectedRow = usersTable.getSelectedRow();
            if (selectedRow >= 0) {
                int userId = (int) usersTable.getValueAt(selectedRow, 0);
                // Open edit dialog
                new EditUserDialog(this, userId).setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Please select a user to edit");
            }
        });

        add(mainPanel);
    }
}