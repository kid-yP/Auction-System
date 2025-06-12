package gui;

import javax.swing.*;
import java.awt.*;
import model.Item;
import model.User;
import model.Bid;
import dao.BidDAO;
import dao.UserDAO;
import java.time.format.DateTimeFormatter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Date;
import dao.ItemDAO;
import javax.swing.Box;
import javax.swing.BoxLayout;

public class ItemDetailView extends JFrame {
    private Item item;
    private User user;
    private JTextArea bidHistoryArea;
    private JLabel currentPriceLabel;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    private JLabel countdownLabel;
    private javax.swing.Timer countdownTimer;
    private JPanel bidHistoryListPanel;
    private JScrollPane bidScrollPane;

    public ItemDetailView(Item item, User user) {
        if (item == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }
        this.item = item;
        this.user = user;

        // Set alert box colors
        UIManager.put("OptionPane.background", new Color(34, 40, 49));
        UIManager.put("Panel.background", new Color(34, 40, 49));
        UIManager.put("OptionPane.messageForeground", Color.WHITE);
        UIManager.put("OptionPane.buttonBackground", new Color(0, 173, 181));
        UIManager.put("OptionPane.buttonForeground", Color.WHITE);
        UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 12));

        try {
            initializeUI();
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error initializing item details: " + e.getMessage(),
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initializeUI() {
        setTitle("Item Details - " + item.getName());
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        // Use vertical BoxLayout for mainPanel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Show auction type at the very top (before details panel)
        JLabel auctionTypeLabel = new JLabel("Auction Type: " + (item.getAuctionType().equals("sealed") ? "Sealed-Bid" : "Ascending Bid"));
        auctionTypeLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        auctionTypeLabel.setForeground(new Color(0, 173, 181));
        auctionTypeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(auctionTypeLabel);

        // Details panel (with countdown, image, info)
        JPanel detailsPanel = new JPanel(new BorderLayout(10, 10));
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Item Details"));
        detailsPanel.setBackground(new Color(34, 40, 49));

        // Countdown timer inside detailsPanel
        countdownLabel = new JLabel("", SwingConstants.CENTER);
        countdownLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        countdownLabel.setForeground(new Color(0, 173, 181));
        countdownLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        detailsPanel.add(countdownLabel, BorderLayout.NORTH);
        startCountdown();

        // Image panel
        JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        imagePanel.setBackground(new Color(34, 40, 49));
        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
            try {
                ImageIcon imageIcon = new ImageIcon(item.getImagePath());
                Image image = imageIcon.getImage().getScaledInstance(300, 200, Image.SCALE_SMOOTH);
                imagePanel.add(new JLabel(new ImageIcon(image)));
            } catch (Exception e) {
                imagePanel.add(new JLabel("Image not available"));
            }
        } else {
            imagePanel.add(new JLabel("No Image Available"));
        }
        detailsPanel.add(imagePanel, BorderLayout.CENTER);

        // Item information
        JPanel infoPanel = new JPanel(new GridLayout(0, 1, 5, 5));
        infoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        infoPanel.setBackground(new Color(34, 40, 49));
        infoPanel.add(createStyledLabel("Name: " + item.getName(), true));
        infoPanel.add(createStyledLabel("Description: " + item.getDescription(), false));
        infoPanel.add(createStyledLabel("Starting Price: $" + String.format("%.2f", item.getStartingPrice()), false));
        currentPriceLabel = createStyledLabel("Current Price: $" + String.format("%.2f", item.getCurrentPrice()), true);
        infoPanel.add(currentPriceLabel);
        infoPanel.add(createStyledLabel("Start Time: " + item.getStartTime().format(DATE_FORMATTER), false));
        infoPanel.add(createStyledLabel("End Time: " + item.getEndTime().format(DATE_FORMATTER), false));
        detailsPanel.add(infoPanel, BorderLayout.SOUTH);
        JScrollPane detailsScroll = new JScrollPane(detailsPanel);
        detailsScroll.setBorder(null);
        detailsScroll.setPreferredSize(new Dimension(800, 260));
        detailsScroll.setMaximumSize(new Dimension(Short.MAX_VALUE, 260));
        mainPanel.add(detailsScroll);

        // Bid history panel (fixed height)
        JPanel bidHistoryPanel = new JPanel(new BorderLayout(5, 5));
        bidHistoryPanel.setBorder(BorderFactory.createTitledBorder("Bid History"));
        bidHistoryPanel.setBackground(new Color(44, 52, 63));
        bidHistoryListPanel = new JPanel();
        bidHistoryListPanel.setLayout(new BoxLayout(bidHistoryListPanel, BoxLayout.Y_AXIS));
        bidHistoryListPanel.setBackground(new Color(44, 52, 63));
        bidHistoryListPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        bidHistoryPanel.add(bidHistoryListPanel, BorderLayout.CENTER);
        bidHistoryPanel.setPreferredSize(new Dimension(800, 4 * 38 + 40));
        // Only add bid history if not sealed-bid
        if (!item.getAuctionType().equals("sealed")) {
            mainPanel.add(bidHistoryPanel);
        }

        // Bid panel
        JPanel bidPanel = new JPanel(new BorderLayout(10, 10));
        bidPanel.setBorder(BorderFactory.createTitledBorder("Place Your Bid"));
        bidPanel.setBackground(new Color(34, 40, 49));

        if (user == null || user.getRole() == null || user.getRole().equals("guest")) {
            JLabel notEligibleLabel = new JLabel(
                "Please login or register to place a bid",
                SwingConstants.CENTER
            );
            notEligibleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            notEligibleLabel.setForeground(Color.WHITE);
            bidPanel.add(notEligibleLabel, BorderLayout.CENTER);
        } else if (user.getRole().equals("buyer") && user.getUserId() != item.getSellerId()) {
            JPanel bidInputPanel = new JPanel(new GridBagLayout());
            bidInputPanel.setBackground(new Color(34, 40, 49));
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(5, 5, 5, 5);
            gbc.fill = GridBagConstraints.HORIZONTAL;

            if (!item.getAuctionType().equals("sealed")) {
                // Current price and minimum bid
                gbc.gridx = 0;
                gbc.gridy = 0;
                gbc.gridwidth = 2;
                JLabel currentPriceInfo = new JLabel(String.format("Current Price: $%.2f", item.getCurrentPrice()));
                currentPriceInfo.setFont(new Font("Segoe UI", Font.BOLD, 14));
                currentPriceInfo.setForeground(new Color(0, 173, 181));
                bidInputPanel.add(currentPriceInfo, gbc);

                gbc.gridy = 1;
                JLabel minBidLabel = new JLabel(String.format("Minimum bid: $%.2f", item.getCurrentPrice() + 1));
                minBidLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
                minBidLabel.setForeground(Color.WHITE);
                bidInputPanel.add(minBidLabel, gbc);
            }

            // Bid amount input
            gbc.gridy = item.getAuctionType().equals("sealed") ? 0 : 2;
            gbc.gridx = 0;
            gbc.gridwidth = 1;
            JLabel bidAmountLabel = new JLabel("Your bid amount: $");
            bidAmountLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            bidAmountLabel.setForeground(Color.WHITE);
            bidInputPanel.add(bidAmountLabel, gbc);

            gbc.gridx = 1;
            JTextField bidAmountField = new JTextField(10);
            bidAmountField.setBackground(new Color(57, 62, 70));
            bidAmountField.setForeground(Color.WHITE);
            bidAmountField.setCaretColor(Color.WHITE);
            bidAmountField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            bidInputPanel.add(bidAmountField, gbc);

            // Place bid button
            gbc.gridx = 0;
            gbc.gridy = item.getAuctionType().equals("sealed") ? 1 : 3;
            gbc.gridwidth = 2;
            JButton bidButton = new JButton("Place Bid");
            bidButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
            bidButton.setBackground(new Color(0, 173, 181));
            bidButton.setForeground(Color.WHITE);
            bidButton.setFocusPainted(false);
            bidButton.setBorderPainted(false);
            bidInputPanel.add(bidButton, gbc);

            // For sealed-bid, check if buyer already placed a bid
            if (item.getAuctionType().equals("sealed")) {
                BidDAO bidDAO = new BidDAO();
                List<Bid> existingBids = bidDAO.getBidsForItem(item.getItemId());
                boolean alreadyBid = false;
                for (Bid b : existingBids) {
                    if (b.getBuyerId() == user.getUserId()) {
                        alreadyBid = true;
                        break;
                    }
                }
                if (alreadyBid) {
                    bidButton.setEnabled(false);
                    bidAmountField.setEnabled(false);
                    bidButton.setText("You have already placed a bid");
                }
            }

            bidButton.addActionListener(e -> {
                try {
                    double bidAmount = Double.parseDouble(bidAmountField.getText());
                    if (item.getAuctionType().equals("sealed")) {
                        // For sealed-bid, just save the bid if not already placed
                        BidDAO bidDAO = new BidDAO();
                        List<Bid> existingBids = bidDAO.getBidsForItem(item.getItemId());
                        for (Bid b : existingBids) {
                            if (b.getBuyerId() == user.getUserId()) {
                                JOptionPane.showMessageDialog(this, "You have already placed a bid for this item.", "Sealed-Bid Auction", JOptionPane.WARNING_MESSAGE);
                                return;
                            }
                        }
                        Bid bid = new Bid();
                        bid.setItemId(item.getItemId());
                        bid.setBuyerId(user.getUserId());
                        bid.setAmount(bidAmount);
                        bid.setStatus("winning");
                        BidDAO dao = new BidDAO();
                        if (dao.createBid(bid)) {
                            JOptionPane.showMessageDialog(this, "Your sealed bid has been submitted!", "Success", JOptionPane.INFORMATION_MESSAGE);
                            bidButton.setEnabled(false);
                            bidAmountField.setEnabled(false);
                        } else {
                            JOptionPane.showMessageDialog(this, "Failed to submit bid.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                        return;
                    } else {
                        placeBid(bidAmount);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Please enter a valid bid amount", "Invalid Input", JOptionPane.ERROR_MESSAGE);
                }
            });

            mainPanel.add(bidInputPanel);
        } else {
            JLabel notEligibleLabel = new JLabel(
                user.getRole().equals("buyer") ?
                "You cannot bid on your own item" :
                "Only buyers can place bids",
                SwingConstants.CENTER
            );
            notEligibleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            notEligibleLabel.setForeground(Color.WHITE);
            bidPanel.add(notEligibleLabel, BorderLayout.CENTER);
        }

        // Update bid history
        updateBidHistory();

        mainPanel.add(bidPanel);

        add(mainPanel);
    }

    private void placeBid(double bidAmount) {
        // Check if user has sufficient balance
        UserDAO userDAO = new UserDAO();
        double currentBalance = userDAO.getBalance(user.getUserId());

        if (currentBalance < bidAmount) {
            JOptionPane.showMessageDialog(this,
                "Insufficient balance. Your current balance is $" + String.format("%.2f", currentBalance) +
                "\nPlease add funds to your wallet.",
                "Bid Failed",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (bidAmount <= item.getCurrentPrice()) {
            JOptionPane.showMessageDialog(this,
                "Bid must be higher than current price",
                "Invalid Bid",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Create and save bid
        Bid bid = new Bid();
        bid.setItemId(item.getItemId());
        bid.setBuyerId(user.getUserId());
        bid.setAmount(bidAmount);
        bid.setStatus("ACTIVE"); // Set status to uppercase to match database enum

        BidDAO bidDAO = new BidDAO();
        if (bidDAO.createBid(bid)) {
            // Update current price in database
            ItemDAO itemDAO = new ItemDAO();
            if (itemDAO.updateCurrentPrice(item.getItemId(), bidAmount)) {
                // Update current price in memory
                item.setCurrentPrice(bidAmount);
                currentPriceLabel.setText("Current Price: $" + String.format("%.2f", item.getCurrentPrice()));

                // Update bid history
                updateBidHistory();

                JOptionPane.showMessageDialog(this,
                    "Bid placed successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to update item price. Please try again.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(this,
                "Failed to place bid. Please try again.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateBidHistory() {
        BidDAO bidDAO = new BidDAO();
        List<Bid> bids = bidDAO.getBidsForItem(item.getItemId());
        bidHistoryListPanel.removeAll();
        if (bids.isEmpty()) {
            JLabel noBidsLabel = new JLabel("No bids yet", SwingConstants.CENTER);
            noBidsLabel.setFont(new Font("Segoe UI", Font.ITALIC, 18));
            noBidsLabel.setForeground(new Color(150, 150, 150));
            noBidsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            bidHistoryListPanel.add(Box.createVerticalGlue());
            bidHistoryListPanel.add(noBidsLabel);
            bidHistoryListPanel.add(Box.createVerticalGlue());
        } else {
            // Sort bids by amount descending
            bids.sort((a, b) -> Double.compare(b.getAmount(), a.getAmount()));
            // Header
            JPanel header = new JPanel(new GridLayout(1, 3));
            header.setBackground(new Color(44, 52, 63));
            header.add(createBidCell("Time", true));
            header.add(createBidCell("Bidder", true));
            header.add(createBidCell("Amount", true));
            bidHistoryListPanel.add(header);
            // Show up to 3 largest bids
            boolean alt = false;
            int shown = Math.min(3, bids.size());
            for (int i = 0; i < shown; i++) {
                Bid bid = bids.get(i);
                UserDAO userDAO = new UserDAO();
                User bidder = userDAO.getUserById(bid.getBuyerId());
                Date bidDate = bid.getBidTime();
                LocalDateTime bidDateTime = LocalDateTime.ofInstant(
                    bidDate.toInstant(),
                    ZoneId.systemDefault()
                );
                JPanel row = new JPanel(new GridLayout(1, 3));
                row.setBackground(alt ? new Color(57, 62, 70) : new Color(44, 52, 63));
                row.add(createBidCell(bidDateTime.format(DATE_FORMATTER), false));
                row.add(createBidCell(bidder.getUsername(), false));
                row.add(createBidCell("$" + String.format("%.2f", bid.getAmount()), false));
                bidHistoryListPanel.add(row);
                alt = !alt;
            }
            // Add empty rows if fewer than 3 bids
            for (int i = shown; i < 3; i++) {
                JPanel emptyRow = new JPanel(new GridLayout(1, 3));
                emptyRow.setBackground((i % 2 == 0) ? new Color(57, 62, 70) : new Color(44, 52, 63));
                emptyRow.add(createBidCell("", false));
                emptyRow.add(createBidCell("", false));
                emptyRow.add(createBidCell("", false));
                bidHistoryListPanel.add(emptyRow);
            }
        }
        bidHistoryListPanel.revalidate();
        bidHistoryListPanel.repaint();
    }

    private JLabel createBidCell(String text, boolean header) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", header ? Font.BOLD : Font.PLAIN, header ? 16 : 15));
        label.setForeground(header ? new Color(0, 173, 181) : Color.WHITE);
        label.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        return label;
    }

    private void startCountdown() {
        countdownTimer = new javax.swing.Timer(1000, e -> updateCountdown());
        countdownTimer.start();
        updateCountdown();
    }

    private void updateCountdown() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        java.time.LocalDateTime end = item.getEndTime();
        java.time.Duration duration = java.time.Duration.between(now, end);
        if (!duration.isNegative()) {
            long seconds = duration.getSeconds();
            long days = seconds / (24 * 3600);
            seconds %= (24 * 3600);
            long hours = seconds / 3600;
            seconds %= 3600;
            long minutes = seconds / 60;
            seconds %= 60;
            String timeLeft = String.format("%02d:%02d:%02d:%02d", days, hours, minutes, seconds);
            countdownLabel.setText("Auction ends in: " + timeLeft);
        } else {
            countdownLabel.setText("Auction Ended");
            if (countdownTimer != null) countdownTimer.stop();
        }
    }

    private JLabel createStyledLabel(String text, boolean highlight) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", highlight ? Font.BOLD : Font.PLAIN, highlight ? 17 : 15));
        label.setForeground(highlight ? new Color(0, 173, 181) : Color.WHITE);
        return label;
    }

    @Override
    protected void finalize() throws Throwable {
        if (countdownTimer != null) countdownTimer.stop();
        super.finalize();
    }
}