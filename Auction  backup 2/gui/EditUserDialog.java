package gui;

import javax.swing.*;
import java.awt.*;

public class EditUserDialog extends JDialog {
    public EditUserDialog(JFrame parent, int userId) {
        super(parent, "Edit User", true);
        setSize(500, 400);
        setLocationRelativeTo(parent);

        // Add your form components here
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));

        panel.add(new JLabel("Username:"));
        panel.add(new JTextField(20));

        // Add more fields as needed

        add(panel, BorderLayout.CENTER);

        JButton saveButton = new JButton("Save Changes");
        add(saveButton, BorderLayout.SOUTH);
    }
}