package gui;

import javax.swing.*;
import java.awt.*;
import model.Seller;

public class EditItemDialog extends JDialog {
    public EditItemDialog(JFrame parent, Seller seller, int itemId) {
        super(parent, "Edit Auction Item", true);
        setSize(500, 400);
        setLocationRelativeTo(parent);

        // Add your form components here
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));

        panel.add(new JLabel("Item Name:"));
        panel.add(new JTextField(20));

        // Add more fields as needed

        add(panel, BorderLayout.CENTER);

        JButton saveButton = new JButton("Save Changes");
        add(saveButton, BorderLayout.SOUTH);
    }
}