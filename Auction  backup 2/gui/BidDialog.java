package gui;

import javax.swing.*;
import java.awt.*;
import model.Buyer;

public class BidDialog extends JDialog {
    public BidDialog(JFrame parent, Buyer buyer, int itemId) {
        super(parent, "Place Bid", true);
        setSize(400, 200);
        setLocationRelativeTo(parent);

        JPanel panel = new JPanel(new GridLayout(3, 2, 10, 10));

        panel.add(new JLabel("Item ID:"));
        panel.add(new JLabel(String.valueOf(itemId)));

        panel.add(new JLabel("Your Maximum Bid:"));
        JTextField bidField = new JTextField();
        panel.add(bidField);

        JButton submitButton = new JButton("Submit Bid");
        panel.add(submitButton);

        submitButton.addActionListener(e -> {
            try {
                double bidAmount = Double.parseDouble(bidField.getText());
                // Here you would add logic to process the bid
                JOptionPane.showMessageDialog(this, "Bid placed successfully!");
                dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid bid amount",
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        add(panel);
    }
}