package gui;

import javax.swing.*;
import java.awt.*;
import model.User;
import dao.ItemDAO;
import dao.BidDAO;
import model.Item;
import model.Bid;
import java.util.List;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.sql.Timestamp;

public class BuyerDashboard extends JFrame {
    private User user;
    private JPanel itemsPanel;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    private ItemDAO itemDAO;
    private BidDAO bidDAO;

    public BuyerDashboard(User user) {
        this.user = user;
        this.itemDAO = new ItemDAO();
        this.bidDAO = new BidDAO();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Auction System - Buyer Dashboard");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel with welcome message and buttons
        JPanel topPanel = new JPanel(new BorderLayout(10, 10));
        topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

        // Left panel with logout button
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        JButton logoutButton = new JButton("LOGOUT");
        logoutButton.setFont(new Font("Arial", Font.BOLD, 16));
        logoutButton.setPreferredSize(new Dimension(150, 45));
        logoutButton.setBackground(new Color(220, 53, 69));
        logoutButton.setForeground(Color.WHITE);
        logoutButton.setFocusPainted(false);
        logoutButton.setBorderPainted(false);
        logoutButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        leftPanel.add(logoutButton);
        topPanel.add(leftPanel, BorderLayout.WEST);

        // Welcome message in center
        JLabel welcomeLabel = new JLabel("Welcome, " + user.getFirstName() + "!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        topPanel.add(welcomeLabel, BorderLayout.CENTER);

        // Right panel with Add Funds button
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton addFundButton = new JButton("Add Funds");
        addFundButton.setFont(new Font("Arial", Font.BOLD, 14));
        addFundButton.setPreferredSize(new Dimension(120, 35));
        addFundButton.setBackground(new Color(70, 130, 180));
        addFundButton.setForeground(Color.WHITE);
        addFundButton.setFocusPainted(false);
        addFundButton.setBorderPainted(false);
        rightPanel.add(addFundButton);
        topPanel.add(rightPanel, BorderLayout.EAST);

        // Add action listeners
        logoutButton.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        addFundButton.addActionListener(e -> {
            String amount = JOptionPane.showInputDialog(this, "Enter amount to add:", "Add Funds", JOptionPane.PLAIN_MESSAGE);
            if (amount != null && !amount.isEmpty()) {
                try {
                    double fundAmount = Double.parseDouble(amount);
                    if (fundAmount > 0) {
                        user.setBalance(user.getBalance() + fundAmount);
                        JOptionPane.showMessageDialog(this,
                            String.format("Successfully added $%.2f to your balance", fundAmount),
                            "Success", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this,
                            "Please enter a positive amount",
                            "Invalid Amount", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this,
                        "Please enter a valid number",
                        "Invalid Input", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Navigation panel
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        navPanel.setBackground(new Color(240, 240, 240));

        JButton browseButton = new JButton("Browse Items");
        JButton myItemsButton = new JButton("My Items");
        JButton wonButton = new JButton("Won Auctions");
        JButton lostButton = new JButton("Lost Auctions");

        // Style buttons
        for (JButton button : new JButton[]{browseButton, myItemsButton, wonButton, lostButton}) {
            button.setFont(new Font("Arial", Font.BOLD, 14));
            button.setPreferredSize(new Dimension(150, 35));
            button.setBackground(new Color(70, 130, 180));
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            navPanel.add(button);
        }

        mainPanel.add(navPanel, BorderLayout.CENTER);

        // Items panel with scroll
        itemsPanel = new JPanel(new GridLayout(0, 3, 15, 15));
        itemsPanel.setBackground(new Color(240, 240, 240));
        JScrollPane scrollPane = new JScrollPane(itemsPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        mainPanel.add(scrollPane, BorderLayout.SOUTH);

        // Add action listeners for navigation buttons
        browseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                System.out.println("Browse Items button clicked");
                loadBrowseItems();
            }
        });

        myItemsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                System.out.println("My Items button clicked");
                loadMyItems();
            }
        });

        wonButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                System.out.println("Won Auctions button clicked");
                loadWonAuctions();
            }
        });

        lostButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                System.out.println("Lost Auctions button clicked");
                loadLostAuctions();
            }
        });

        // Initial load of items
        loadBrowseItems();

        add(mainPanel);
    }

    private void loadBrowseItems() {
        System.out.println("Loading Browse Items");
        itemsPanel.removeAll();
        List<Item> items = itemDAO.getAllActiveItems();
        System.out.println("Found " + items.size() + " active items");
        displayItems(items);
    }

    private void loadMyItems() {
        System.out.println("Loading My Items for user: " + user.getUserId());
        itemsPanel.removeAll();
        List<Item> items = itemDAO.getItemsBySeller(user.getUserId());
        System.out.println("Found " + items.size() + " items for user");
        displayItems(items);
    }

    private void loadWonAuctions() {
        System.out.println("Loading Won Auctions for user: " + user.getUserId());
        itemsPanel.removeAll();
        List<Item> items = itemDAO.getWonItems(user.getUserId());
        System.out.println("Found " + items.size() + " won items");
        displayItems(items);
    }

    private void loadLostAuctions() {
        System.out.println("Loading Lost Auctions for user: " + user.getUserId());
        itemsPanel.removeAll();
        List<Item> items = itemDAO.getLostItems(user.getUserId());
        System.out.println("Found " + items.size() + " lost items");
        displayItems(items);
    }

    private void displayItems(List<Item> items) {
        itemsPanel.removeAll();
        if (items.isEmpty()) {
            JLabel noItemsLabel = new JLabel("No items found", SwingConstants.CENTER);
            noItemsLabel.setFont(new Font("Arial", Font.BOLD, 16));
            itemsPanel.add(noItemsLabel);
        } else {
            for (Item item : items) {
                JPanel itemPanel = createItemPanel(item);
                itemsPanel.add(itemPanel);
            }
        }
        itemsPanel.revalidate();
        itemsPanel.repaint();
    }

    private JPanel createItemPanel(Item item) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        panel.setBackground(Color.WHITE);
        panel.setPreferredSize(new Dimension(300, 400));

        // Item image
        JLabel imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(300, 200));
        imageLabel.setMaximumSize(new Dimension(300, 200));
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
            ImageIcon icon = new ImageIcon(item.getImagePath());
            Image image = icon.getImage().getScaledInstance(300, 200, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(image));
            } else {
            imageLabel.setIcon(new ImageIcon("images/default_item.png"));
        }
        panel.add(imageLabel);

        // Item details
        JLabel nameLabel = new JLabel(item.getName());
        nameLabel.setFont(new Font("Arial", Font.BOLD, 16));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(nameLabel);

        JLabel priceLabel = new JLabel(String.format("Current Price: $%.2f", item.getCurrentPrice()));
        priceLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(priceLabel);

        JLabel endTimeLabel = new JLabel("Ends: " + item.getEndTime().toString());
        endTimeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        endTimeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(endTimeLabel);

        JLabel statusLabel = new JLabel("Status: " + item.getStatus());
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(statusLabel);

        // Make panel clickable
        panel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                new ItemDetailView(item, user).setVisible(true);
            }
        });

        panel.add(Box.createVerticalStrut(10));

        return panel;
    }
}