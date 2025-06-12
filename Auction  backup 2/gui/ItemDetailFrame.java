package gui;

import javax.swing.*;
import java.awt.*;
import model.Buyer;
import model.Item;
import dao.BidDAO;
import model.Bid;
import java.util.List;
import java.sql.Timestamp;

public class ItemDetailFrame extends JFrame {
    private Buyer buyer;
    private Item item;

    public ItemDetailFrame(JFrame parent, Buyer buyer, Item item) {
        this.buyer = buyer;
        this.item = item;

        setTitle("Auction Details: " + item.getName());
        setSize(800, 600);
        setLocationRelativeTo(parent);

        JPanel mainPanel = new JPanel(new BorderLayout());

        // Item details panel
        JPanel itemPanel = new JPanel(new GridLayout(0, 2, 10, 10));
        itemPanel.setBorder(BorderFactory.createTitledBorder("Item Details"));

        itemPanel.add(new JLabel("Name:"));
        itemPanel.add(new JLabel(item.getName()));

        itemPanel.add(new JLabel("Description:"));
        itemPanel.add(new JLabel(item.getDescription()));

        itemPanel.add(new JLabel("Current Price:"));
        itemPanel.add(new JLabel(String.format("$%.2f", item.getCurrentPrice())));

        itemPanel.add(new JLabel("Starting Price:"));
        itemPanel.add(new JLabel(String.format("$%.2f", item.getStartingPrice())));

        itemPanel.add(new JLabel("Auction Ends:"));
        itemPanel.add(new JLabel(item.getEndTime().toString()));

        mainPanel.add(itemPanel, BorderLayout.NORTH);

        // Bids history panel
        JPanel bidsPanel = new JPanel(new BorderLayout());
        bidsPanel.setBorder(BorderFactory.createTitledBorder("Bid History"));

        BidDAO bidDAO = new BidDAO();
        List<Bid> bids = bidDAO.getBidsForItem(item.getItemId());

        String[] columnNames = {"Bidder", "Amount", "Time"};
        Object[][] data = new Object[bids.size()][3];

        for (int i = 0; i < bids.size(); i++) {
            Bid bid = bids.get(i);
            data[i][0] = bid.getBuyerId(); // You might want to show buyer name instead
            data[i][1] = String.format("$%.2f", bid.getAmount());
            data[i][2] = bid.getBidTime().toString();
        }

        JTable bidsTable = new JTable(data, columnNames);
        bidsPanel.add(new JScrollPane(bidsTable), BorderLayout.CENTER);

        mainPanel.add(bidsPanel, BorderLayout.CENTER);

        // Bid form panel
        JPanel bidPanel = new JPanel(new FlowLayout());
        bidPanel.setBorder(BorderFactory.createTitledBorder("Place Bid"));

        JLabel bidLabel = new JLabel("Your Bid: $");
        JTextField bidField = new JTextField(10);
        JButton bidButton = new JButton("Place Bid");

        bidButton.addActionListener(e -> {
            try {
                double bidAmount = Double.parseDouble(bidField.getText());
                if (bidAmount <= item.getCurrentPrice()) {
                    JOptionPane.showMessageDialog(this,
                        "Your bid must be higher than the current price",
                        "Invalid Bid", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                if (bidAmount > buyer.getBalance()) {
                    JOptionPane.showMessageDialog(this,
                        "You don't have enough balance for this bid",
                        "Insufficient Funds", JOptionPane.WARNING_MESSAGE);
                    return;
                }

                // Create new bid with current timestamp
                Bid newBid = new Bid(0, item.getItemId(), buyer.getUserId(),
                                    bidAmount, new Timestamp(System.currentTimeMillis()), "winning");

                BidDAO bidDAO = new BidDAO();
                if (bidDAO.createBid(newBid)) {
                    // Update item current price
                    item.setCurrentPrice(bidAmount);

                    // Refresh the view
                    JOptionPane.showMessageDialog(this,
                        "Bid placed successfully!",
                        "Success", JOptionPane.INFORMATION_MESSAGE);
                    dispose();
                    new ItemDetailFrame(parent, buyer, item).setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Failed to place bid",
                        "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this,
                    "Please enter a valid bid amount",
                    "Invalid Input", JOptionPane.ERROR_MESSAGE);
            }
        });

        bidPanel.add(bidLabel);
        bidPanel.add(bidField);
        bidPanel.add(bidButton);

        mainPanel.add(bidPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }
}