package gui;

import javax.swing.*;
import java.awt.*;
import model.User;
import model.Item;
import dao.ItemDAO;
import dao.UserDAO;
import java.util.List;
import java.util.ArrayList;

public class UserDashboard extends JFrame {
    private User user;
    private JPanel mainPanel;
    private JLabel balanceLabel;
    private JTabbedPane tabbedPane;

    // Card size: 25% smaller
    private static final int CARD_WIDTH = 240; // 320 * 0.75
    private static final int CARD_HEIGHT = 315; // 420 * 0.75

    static {
        // Set global UI font and colors
        UIManager.put("Panel.background", new Color(34, 40, 49)); // Main background
        UIManager.put("TabbedPane.background", new Color(34, 40, 49));
        UIManager.put("TabbedPane.selected", new Color(44, 52, 63));
        UIManager.put("TabbedPane.foreground", Color.WHITE);
        UIManager.put("Label.foreground", Color.WHITE);
        UIManager.put("Button.background", new Color(0, 173, 181));
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("Button.font", new Font("Segoe UI", Font.BOLD, 14));
        UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 15));
        UIManager.put("TextField.background", new Color(57, 62, 70));
        UIManager.put("TextField.foreground", Color.WHITE);
        UIManager.put("TextField.caretForeground", Color.WHITE);
        UIManager.put("ScrollBar.thumb", new Color(57, 62, 70));
        UIManager.put("ScrollBar.track", new Color(34, 40, 49));

        // Alert box styling
        UIManager.put("OptionPane.background", new Color(34, 40, 49));
        UIManager.put("OptionPane.messageBackground", new Color(34, 40, 49));
        UIManager.put("OptionPane.messageForeground", Color.WHITE);
        UIManager.put("OptionPane.buttonBackground", new Color(0, 173, 181));
        UIManager.put("OptionPane.buttonForeground", Color.WHITE);
        UIManager.put("OptionPane.buttonFont", new Font("Segoe UI", Font.BOLD, 12));
        UIManager.put("OptionPane.titleText", Color.WHITE);
        UIManager.put("OptionPane.titleFont", new Font("Segoe UI", Font.BOLD, 14));
        UIManager.put("OptionPane.messageFont", new Font("Segoe UI", Font.PLAIN, 13));
    }

    public UserDashboard(User user) {
        this.user = user;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Auction System - User Dashboard");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top panel with user info and wallet
        JPanel topPanel = createTopPanel();
        mainPanel.add(topPanel, BorderLayout.NORTH);

        // Tabbed pane for different sections
        tabbedPane = new JTabbedPane();

        // Browse Items tab
        tabbedPane.addTab("Browse Items", createBrowseItemsPanel());

        // My Items tab (for sellers)
        if (user != null && user.isSeller()) {
            tabbedPane.addTab("My Items", createMyItemsPanel());
        }

        // My Bids tab (for buyers)
        if (user != null && user.isBuyer()) {
            tabbedPane.addTab("My Bids", createMyBidsPanel());
        }

        // Won Auctions and Lost Auctions tabs (only for logged-in users)
        if (user != null) {
            tabbedPane.addTab("Won Auctions", createWonAuctionsPanel());
            tabbedPane.addTab("Lost Auctions", createLostAuctionsPanel());
        }

        mainPanel.add(tabbedPane, BorderLayout.CENTER);
        add(mainPanel);

        // Center and enlarge tabs
        tabbedPane.setFont(new Font("Segoe UI", Font.BOLD, 20));
        tabbedPane.setForeground(Color.WHITE);
        tabbedPane.setBackground(new Color(44, 52, 63));
        tabbedPane.putClientProperty("JTabbedPane.tabAlignment", "center");
        tabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected int calculateTabAreaHeight(int tabPlacement, int horizRunCount, int maxTabHeight) {
                return super.calculateTabAreaHeight(tabPlacement, horizRunCount, maxTabHeight) + 20;
            }
            @Override
            protected Insets getTabInsets(int tabPlacement, int tabIndex) {
                return new Insets(15, 40, 15, 40); // More padding for larger tabs
            }
            @Override
            protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
                int width = super.calculateTabWidth(tabPlacement, tabIndex, metrics);
                return width + 40; // Make tabs wider
            }
        });

        // Add ChangeListener to refresh My Items tab when selected
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            String selectedTitle = tabbedPane.getTitleAt(selectedIndex);
            if (selectedTitle.equals("My Items")) {
                JPanel myItemsPanel = (JPanel) tabbedPane.getComponentAt(selectedIndex);
                JScrollPane scrollPane = (JScrollPane) myItemsPanel.getComponent(0);
                JPanel itemsPanel = (JPanel) scrollPane.getViewport().getView();
                loadMyItems(itemsPanel);
            } else if (selectedTitle.equals("My Bids")) {
                JPanel myBidsPanel = (JPanel) tabbedPane.getComponentAt(selectedIndex);
                JScrollPane scrollPane = (JScrollPane) myBidsPanel.getComponent(0);
                JPanel itemsPanel = (JPanel) scrollPane.getViewport().getView();
                loadMyBids(itemsPanel);
            } else if (selectedTitle.equals("Won Auctions")) {
                JPanel wonPanel = (JPanel) tabbedPane.getComponentAt(selectedIndex);
                JScrollPane scrollPane = (JScrollPane) wonPanel.getComponent(0);
                JPanel itemsPanel = (JPanel) scrollPane.getViewport().getView();
                loadWonItems(itemsPanel);
            } else if (selectedTitle.equals("Lost Auctions")) {
                JPanel lostPanel = (JPanel) tabbedPane.getComponentAt(selectedIndex);
                JScrollPane scrollPane = (JScrollPane) lostPanel.getComponent(0);
                JPanel itemsPanel = (JPanel) scrollPane.getViewport().getView();
                loadLostItems(itemsPanel);
            }
        });
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // Centered welcome label
        String welcomeText = (user == null || "guest".equals(user.getRole())) ? "Welcome, Guest!" : "Welcome, " + user.getFullName() + "!";
        JLabel welcomeLabel = new JLabel(welcomeText, SwingConstants.CENTER);
        welcomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        welcomeLabel.setForeground(new Color(0, 173, 181));
        welcomeLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        panel.add(welcomeLabel, BorderLayout.CENTER);

        // Top right panel
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);
        if (user == null || "guest".equals(user.getRole())) {
            JButton loginButton = new JButton("Login");
            loginButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
            loginButton.setBackground(new Color(0, 173, 181));
            loginButton.setForeground(Color.WHITE);
            loginButton.setFocusPainted(false);
            loginButton.setBorderPainted(false);
            loginButton.addActionListener(e -> {
                new LoginFrame().setVisible(true);
                SwingUtilities.getWindowAncestor(panel).dispose();
            });
            JButton registerButton = new JButton("Register");
            registerButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
            registerButton.setBackground(new Color(0, 173, 181));
            registerButton.setForeground(Color.WHITE);
            registerButton.setFocusPainted(false);
            registerButton.setBorderPainted(false);
            registerButton.addActionListener(e -> {
                new RegistrationFrame().setVisible(true);
                SwingUtilities.getWindowAncestor(panel).dispose();
            });
            rightPanel.add(loginButton);
            rightPanel.add(registerButton);
        } else {
            balanceLabel = new JLabel(String.format("Wallet Balance: $%.2f", user.getBalance()));
            balanceLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
            rightPanel.add(balanceLabel);
            JButton addFundsButton = new JButton("Add Funds");
            addFundsButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
            addFundsButton.setBackground(new Color(0, 173, 181));
            addFundsButton.setForeground(Color.WHITE);
            addFundsButton.setFocusPainted(false);
            addFundsButton.setBorderPainted(false);
            addFundsButton.addActionListener(e -> showAddFundsDialog());
            JButton logoutButton = new JButton("Logout");
            logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
            logoutButton.setBackground(new Color(220, 53, 69));
            logoutButton.setForeground(Color.WHITE);
            logoutButton.setFocusPainted(false);
            logoutButton.setBorderPainted(false);
            logoutButton.setPreferredSize(new Dimension(100, 35));
            logoutButton.addActionListener(e -> {
                // Relaunch dashboard as guest
                SwingUtilities.getWindowAncestor(panel).dispose();
                new UserDashboard(null).setVisible(true);
            });
            rightPanel.add(logoutButton);
            rightPanel.add(addFundsButton);
        }
        panel.add(rightPanel, BorderLayout.EAST);
        panel.setOpaque(false);
        return panel;
    }

    private JPanel createBrowseItemsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));

        // Search panel
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // Add new item button for sellers
        if (user != null && user.isSeller()) {
            JButton addItemButton = new JButton("List New Item");
            addItemButton.addActionListener(e -> {
                AddItemDialog dialog = new AddItemDialog(this, user.getUserId());
                dialog.setVisible(true);
                if (dialog.isItemAdded()) {
                    JPanel browsePanel = (JPanel) tabbedPane.getComponentAt(0);
                    JScrollPane scrollPane = (JScrollPane) browsePanel.getComponent(1);
                    JPanel itemsPanel = (JPanel) scrollPane.getViewport().getView();
                    itemsPanel.removeAll();
                    loadItems(itemsPanel);
                    itemsPanel.revalidate();
                    itemsPanel.repaint();
                }
            });
            searchPanel.add(addItemButton);
        }

        panel.add(searchPanel, BorderLayout.NORTH);

        // Items grid
        JPanel itemsPanel = new JPanel(new GridLayout(0, 3, 10, 10));
        JScrollPane scrollPane = new JScrollPane(itemsPanel);
        panel.add(scrollPane, BorderLayout.CENTER);

        // Load items
        loadItems(itemsPanel);

        // Add search functionality
        searchButton.addActionListener(e -> {
            String query = searchField.getText().trim().toLowerCase();
            itemsPanel.removeAll();
            ItemDAO itemDAO = new ItemDAO();
            java.util.List<Item> items = itemDAO.getActiveItems();
            java.util.List<Item> filtered = new java.util.ArrayList<>();
            for (Item item : items) {
                if (item.getName().toLowerCase().contains(query) ||
                    (item.getDescription() != null && item.getDescription().toLowerCase().contains(query))) {
                    filtered.add(item);
                }
            }
            if (filtered.isEmpty()) {
                JLabel noItemsLabel = new JLabel("No items found");
                noItemsLabel.setFont(new Font("Arial", Font.BOLD, 16));
                noItemsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
                itemsPanel.add(noItemsLabel);
            } else {
                for (Item item : filtered) {
                    JPanel card = createItemCard(item);
                    itemsPanel.add(card);
                }
            }
            itemsPanel.revalidate();
            itemsPanel.repaint();
        });

        return panel;
    }

    private JPanel createMyItemsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JPanel itemsPanel = new JPanel(new GridLayout(0, 3, 10, 10));
        JScrollPane scrollPane = new JScrollPane(itemsPanel);
        panel.add(scrollPane, BorderLayout.CENTER);
        loadMyItems(itemsPanel);
        return panel;
    }

    private JPanel createMyBidsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JPanel itemsPanel = new JPanel(new GridLayout(0, 3, 10, 10));
        JScrollPane scrollPane = new JScrollPane(itemsPanel);
        panel.add(scrollPane, BorderLayout.CENTER);
        loadMyBids(itemsPanel);
        return panel;
    }

    private void loadMyBids(JPanel itemsPanel) {
        itemsPanel.removeAll();
        dao.BidDAO bidDAO = new dao.BidDAO();
        dao.ItemDAO itemDAO = new dao.ItemDAO();
        List<model.Bid> bids;
        if (user != null) {
            bids = bidDAO.getBidsByBuyer(user.getUserId());
        } else {
            bids = new ArrayList<>(); // Empty list for guest users
        }
        java.util.Set<Integer> itemIds = new java.util.HashSet<>();
        for (model.Bid bid : bids) {
            itemIds.add(bid.getItemId());
        }
        if (itemIds.isEmpty()) {
            JLabel noItemsLabel = new JLabel("You have not placed any bids.");
            noItemsLabel.setFont(new Font("Arial", Font.BOLD, 16));
            noItemsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            itemsPanel.add(noItemsLabel);
        } else {
            for (Integer itemId : itemIds) {
                model.Item item = itemDAO.getItemById(itemId);
                if (item != null) {
                    JPanel card = createItemCard(item);
                    itemsPanel.add(card);
                }
            }
        }
        itemsPanel.revalidate();
        itemsPanel.repaint();
    }

    private JPanel createWonAuctionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JPanel itemsPanel = new JPanel(new GridLayout(0, 3, 10, 10));
        JScrollPane scrollPane = new JScrollPane(itemsPanel);
        panel.add(scrollPane, BorderLayout.CENTER);
        loadWonItems(itemsPanel);
        return panel;
    }

    private JPanel createLostAuctionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        JPanel itemsPanel = new JPanel(new GridLayout(0, 3, 10, 10));
        JScrollPane scrollPane = new JScrollPane(itemsPanel);
        panel.add(scrollPane, BorderLayout.CENTER);
        loadLostItems(itemsPanel);
        return panel;
    }

    private void loadItems(JPanel itemsPanel) {
        itemsPanel.removeAll(); // Clear existing items

        ItemDAO itemDAO = new ItemDAO();
        List<Item> items = itemDAO.getActiveItems();

        if (items.isEmpty()) {
            JLabel noItemsLabel = new JLabel("No active items available");
            noItemsLabel.setFont(new Font("Arial", Font.BOLD, 16));
            noItemsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            itemsPanel.add(noItemsLabel);
        } else {
            for (Item item : items) {
                JPanel card = createItemCard(item);
                itemsPanel.add(card);
            }
        }

        itemsPanel.revalidate();
        itemsPanel.repaint();
    }

    private void loadMyItems(JPanel itemsPanel) {
        itemsPanel.removeAll();
        ItemDAO itemDAO = new ItemDAO();
        java.util.List<Item> items = itemDAO.getItemsBySeller(user.getUserId());

        if (items.isEmpty()) {
            JLabel noItemsLabel = new JLabel("You have not listed any items.");
            noItemsLabel.setFont(new Font("Arial", Font.BOLD, 16));
            noItemsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            itemsPanel.add(noItemsLabel);
        } else {
            for (Item item : items) {
                JPanel card = createItemCard(item);
                itemsPanel.add(card);
            }
        }
        itemsPanel.revalidate();
        itemsPanel.repaint();
    }

    private void loadWonItems(JPanel itemsPanel) {
        itemsPanel.removeAll();
        ItemDAO itemDAO = new ItemDAO();
        List<Item> items;
        if (user != null) {
            items = itemDAO.getWonItems(user.getUserId());
        } else {
            items = new ArrayList<>(); // Empty list for guest users
        }
        if (items.isEmpty()) {
            JLabel noItemsLabel = new JLabel("You have not won any auctions.");
            noItemsLabel.setFont(new Font("Arial", Font.BOLD, 16));
            noItemsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            itemsPanel.add(noItemsLabel);
        } else {
            for (Item item : items) {
                JPanel card = createItemCard(item);
                itemsPanel.add(card);
            }
        }
        itemsPanel.revalidate();
        itemsPanel.repaint();
    }

    private void loadLostItems(JPanel itemsPanel) {
        itemsPanel.removeAll();
        ItemDAO itemDAO = new ItemDAO();
        List<Item> items;
        if (user != null) {
            items = itemDAO.getLostItems(user.getUserId());
        } else {
            items = new ArrayList<>(); // Empty list for guest users
        }
        if (items.isEmpty()) {
            JLabel noItemsLabel = new JLabel("You have not lost any auctions.");
            noItemsLabel.setFont(new Font("Arial", Font.BOLD, 16));
            noItemsLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            itemsPanel.add(noItemsLabel);
        } else {
            for (Item item : items) {
                JPanel card = createItemCard(item);
                itemsPanel.add(card);
            }
        }
        itemsPanel.revalidate();
        itemsPanel.repaint();
    }

    private JPanel createItemCard(Item item) {
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(44, 52, 63)); // Card background
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createEmptyBorder(9, 9, 9, 9),
            BorderFactory.createLineBorder(new Color(0, 173, 181), 2, true)
        ));
        card.setBackground(new Color(44, 52, 63));
        card.setMaximumSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        card.setPreferredSize(new Dimension(CARD_WIDTH, CARD_HEIGHT));
        card.setAlignmentY(Component.TOP_ALIGNMENT);
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.setCursor(new Cursor(Cursor.HAND_CURSOR));
        card.setOpaque(false);

        // Item image
        JLabel imageLabel = new JLabel("No Image Available");
        imageLabel.setForeground(new Color(200, 200, 200));
        if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
            try {
                ImageIcon imageIcon = new ImageIcon(item.getImagePath());
                Image image = imageIcon.getImage().getScaledInstance((int)(CARD_WIDTH * 0.85), (int)(CARD_HEIGHT * 0.57), Image.SCALE_SMOOTH);
                imageLabel = new JLabel(new ImageIcon(image));
            } catch (Exception e) {
                // Keep default "No Image Available" label
            }
        }
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        imageLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 7, 0));
        card.add(imageLabel);

        // Item details
        JLabel nameLabel = new JLabel(item.getName());
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        nameLabel.setForeground(new Color(0, 173, 181));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(nameLabel);

        JLabel priceLabel = new JLabel(String.format("Current Price: $%.2f", item.getCurrentPrice()));
        priceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        priceLabel.setForeground(Color.WHITE);
        priceLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(priceLabel);

        JLabel statusLabel = new JLabel("Status: " + item.getStatus());
        statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        statusLabel.setForeground(new Color(200, 200, 200));
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(statusLabel);

        card.add(Box.createVerticalStrut(6));

        // Hover effect
        card.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(9, 9, 9, 9),
                    BorderFactory.createLineBorder(new Color(0, 255, 255), 3, true)
                ));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(9, 9, 9, 9),
                    BorderFactory.createLineBorder(new Color(0, 173, 181), 2, true)
                ));
            }
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (user == null) {
                    // For guest users, show a message to login
                    int choice = JOptionPane.showConfirmDialog(
                        card,
                        "Please login to view item details and place bids. Would you like to login now?",
                        "Login Required",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                    );
                    if (choice == JOptionPane.YES_OPTION) {
                        new LoginFrame().setVisible(true);
                        SwingUtilities.getWindowAncestor(card).dispose();
                    }
                } else {
                    // For logged-in users, show item details with bid functionality
                    new ItemDetailView(item, user).setVisible(true);
                }
            }
        });

        return card;
    }

    private void showAddFundsDialog() {
        String amountStr = JOptionPane.showInputDialog(this,
            "Enter amount to add to wallet:", "Add Funds", JOptionPane.PLAIN_MESSAGE);

        if (amountStr != null && !amountStr.trim().isEmpty()) {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount > 0) {
                    UserDAO userDAO = new UserDAO();
                    double newBalance = user.getBalance() + amount;
                    if (userDAO.updateBalance(user.getUserId(), newBalance)) {
                        user.setBalance(newBalance);
                        balanceLabel.setText(String.format("Wallet Balance: $%.2f", newBalance));
                        JOptionPane.showMessageDialog(this, "Funds added successfully!");
                    } else {
                        JOptionPane.showMessageDialog(this, "Failed to add funds. Please try again.");
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Please enter a positive amount.");
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number.");
            }
        }
    }
}