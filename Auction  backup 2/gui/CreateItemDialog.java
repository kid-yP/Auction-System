package gui;

import javax.swing.*;
import java.awt.*;
import model.Seller;

public class CreateItemDialog extends JDialog {
    public CreateItemDialog(JFrame parent, Seller seller) {
        super(parent, "Create New Auction Item", true);
        setSize(500, 400);
        setLocationRelativeTo(parent);

        // Add your form components here
        JPanel panel = new JPanel(new GridLayout(0, 2, 10, 10));

        panel.add(new JLabel("Item Name:"));
        panel.add(new JTextField(20));

        // Add more fields as needed

        add(panel, BorderLayout.CENTER);

        JButton createButton = new JButton("Create");
        add(createButton, BorderLayout.SOUTH);
    }
}