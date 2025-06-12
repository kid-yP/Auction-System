package gui;

import javax.swing.*;
import java.awt.*;
import model.User;
import model.Seller;
import dao.ItemDAO;
import model.Item;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.sql.Timestamp;

public class SellerDashboard extends JFrame {
    private User seller;
    private JPanel itemsPanel;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm");
    private ItemDAO itemDAO;

    public SellerDashboard(User seller) {
        this.seller = seller;
        this.itemDAO = new ItemDAO();
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Auction System - Seller Dashboard");
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
        JLabel welcomeLabel = new JLabel("Welcome, " + seller.getFirstName() + "!");
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 24));
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        topPanel.add(welcomeLabel, BorderLayout.CENTER);

        // Right panel with Create Auction button
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton createAuctionButton = new JButton("Create Auction");
        createAuctionButton.setFont(new Font("Arial", Font.BOLD, 14));
        createAuctionButton.setPreferredSize(new Dimension(150, 35));
        createAuctionButton.setBackground(new Color(70, 130, 180));
        createAuctionButton.setForeground(Color.WHITE);
        createAuctionButton.setFocusPainted(false);
        createAuctionButton.setBorderPainted(false);
        rightPanel.add(createAuctionButton);
        topPanel.add(rightPanel, BorderLayout.EAST);

        // Add action listeners
        logoutButton.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        createAuctionButton.addActionListener(e -> {
            AddItemDialog dialog = new AddItemDialog(this, seller.getUserId());
            dialog.setVisible(true);
            if (dialog.isItemAdded()) {
                loadAllItems();
            }
        });

        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Navigation panel
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        navPanel.setBackground(new Color(240, 240, 240));

        JButton myItemsButton = new JButton("My Items");
        JButton activeButton = new JButton("Active Auctions");
        JButton finishedButton = new JButton("Finished Auctions");

        // Style buttons
        for (JButton button : new JButton[]{myItemsButton, activeButton, finishedButton}) {
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
        myItemsButton.addActionListener(e -> loadAllItems());
        activeButton.addActionListener(e -> loadActiveItems());
        finishedButton.addActionListener(e -> loadFinishedItems());

        // Initial load of items
        loadAllItems();

        add(mainPanel);
    }

    private void loadAllItems() {
        itemsPanel.removeAll();
        List<Item> items = itemDAO.getItemsBySeller(seller.getUserId());
        displayItems(items);
    }

    private void loadActiveItems() {
        itemsPanel.removeAll();
        List<Item> items = itemDAO.getItemsByStatus(seller.getUserId(), "active");
        displayItems(items);
    }

    private void loadFinishedItems() {
        itemsPanel.removeAll();
        List<Item> items = itemDAO.getFinishedAuctions(seller.getUserId());
        displayItems(items);
    }

    private void displayItems(List<Item> items) {
        itemsPanel.removeAll();
        for (Item item : items) {
            JPanel itemPanel = createItemPanel(item);
            itemsPanel.add(itemPanel);
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

        // Format the end time
        JLabel endTimeLabel = new JLabel("Ends: " + item.getEndTime().toString());
        endTimeLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        endTimeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(endTimeLabel);

        JLabel statusLabel = new JLabel("Status: " + item.getStatus());
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.add(statusLabel);

        // Add action buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 5));
        buttonPanel.setBackground(Color.WHITE);

        JButton editButton = new JButton("Edit");
        JButton viewBidsButton = new JButton("View Bids");
        JButton endButton = new JButton("End");

        // Style buttons
        for (JButton button : new JButton[]{editButton, viewBidsButton, endButton}) {
            button.setFont(new Font("Arial", Font.PLAIN, 12));
            button.setPreferredSize(new Dimension(80, 25));
            button.setBackground(new Color(70, 130, 180));
            button.setForeground(Color.WHITE);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
        }

        buttonPanel.add(editButton);
        buttonPanel.add(viewBidsButton);
        buttonPanel.add(endButton);

        // Add action listeners
        editButton.addActionListener(e -> {
            Seller sellerObj = new Seller(seller);
            EditItemDialog dialog = new EditItemDialog(this, sellerObj, item.getItemId());
            dialog.setVisible(true);
            loadAllItems(); // Refresh items after edit
        });

        viewBidsButton.addActionListener(e -> {
            // TODO: Implement view bids functionality
            JOptionPane.showMessageDialog(this, "View bids functionality coming soon!");
        });

        endButton.addActionListener(e -> {
            if (item.getStatus().equals("active")) {
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to end this auction early?",
                    "Confirm End Auction",
                    JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    itemDAO.updateItemStatus(item.getItemId(), "ended");
                    loadAllItems();
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "This auction is already ended",
                    "Cannot End Auction",
                    JOptionPane.INFORMATION_MESSAGE);
            }
        });

        panel.add(buttonPanel);
        panel.add(Box.createVerticalStrut(10));

        return panel;
    }
}