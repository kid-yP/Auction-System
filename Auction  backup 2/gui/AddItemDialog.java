package gui;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import model.Item;
import dao.ItemDAO;
import dao.CategoryDAO;
import model.Category;

public class AddItemDialog extends JDialog {
    private JTextField nameField;
    private JTextArea descriptionArea;
    private JTextField startingPriceField;
    private JComboBox<Category> categoryComboBox;
    private JComboBox<String> auctionTypeComboBox;
    private JTextField endDateField;
    private JTextField endTimeField;
    private JLabel imagePathLabel;
    private String selectedImagePath;
    private boolean itemAdded = false;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public AddItemDialog(Frame parent, int sellerId) {
        super(parent, "Add New Item", true);
        setSize(500, 600);
        setLocationRelativeTo(parent);
        initializeUI(sellerId);
    }

    private void initializeUI(int sellerId) {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Form panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Name:"), gbc);
        gbc.gridx = 1;
        nameField = new JTextField(20);
        formPanel.add(nameField, gbc);

        // Description
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Description:"), gbc);
        gbc.gridx = 1;
        descriptionArea = new JTextArea(4, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane descriptionScroll = new JScrollPane(descriptionArea);
        formPanel.add(descriptionScroll, gbc);

        // Starting Price
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Starting Price ($):"), gbc);
        gbc.gridx = 1;
        startingPriceField = new JTextField(20);
        formPanel.add(startingPriceField, gbc);

        // Category
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Category:"), gbc);
        gbc.gridx = 1;
        categoryComboBox = new JComboBox<>();
        loadCategories();
        formPanel.add(categoryComboBox, gbc);

        // Auction Type
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Auction Type:"), gbc);
        gbc.gridx = 1;
        auctionTypeComboBox = new JComboBox<>(new String[] {"Ascending Bid Auction", "Sealed-Bid Auction"});
        formPanel.add(auctionTypeComboBox, gbc);

        // End Date
        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(new JLabel("End Date:"), gbc);
        gbc.gridx = 1;
        JPanel dateTimePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        endDateField = new JTextField(10);
        endDateField.setEditable(false);
        JButton dateButton = new JButton("Choose Date");
        dateButton.addActionListener(e -> showDatePicker());
        dateTimePanel.add(endDateField);
        dateTimePanel.add(dateButton);
        formPanel.add(dateTimePanel, gbc);

        // End Time
        gbc.gridx = 0;
        gbc.gridy = 6;
        formPanel.add(new JLabel("End Time:"), gbc);
        gbc.gridx = 1;
        JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        endTimeField = new JTextField(5);
        endTimeField.setEditable(false);
        JButton timeButton = new JButton("Choose Time");
        timeButton.addActionListener(e -> showTimePicker());
        timePanel.add(endTimeField);
        timePanel.add(timeButton);
        formPanel.add(timePanel, gbc);

        // Image
        gbc.gridx = 0;
        gbc.gridy = 7;
        formPanel.add(new JLabel("Image:"), gbc);
        gbc.gridx = 1;
        JPanel imagePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        imagePathLabel = new JLabel("No image selected");
        JButton browseButton = new JButton("Browse");
        browseButton.addActionListener(e -> chooseImage());
        imagePanel.add(imagePathLabel);
        imagePanel.add(browseButton);
        formPanel.add(imagePanel, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton addButton = new JButton("Add Item");
        JButton cancelButton = new JButton("Cancel");

        addButton.addActionListener(e -> {
            if (validateAndAddItem(sellerId)) {
                itemAdded = true;
                dispose();
            }
        });

        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(addButton);
        buttonPanel.add(cancelButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void loadCategories() {
        CategoryDAO categoryDAO = new CategoryDAO();
        for (Category category : categoryDAO.getAllCategories()) {
            categoryComboBox.addItem(category);
        }
    }

    private void showDatePicker() {
        JSpinner.DateEditor editor;
        JSpinner spinner = new JSpinner(new SpinnerDateModel());
        editor = new JSpinner.DateEditor(spinner, "yyyy-MM-dd");
        spinner.setEditor(editor);

        JOptionPane.showMessageDialog(this, spinner, "Choose Date",
            JOptionPane.QUESTION_MESSAGE);

        java.util.Date date = (java.util.Date) spinner.getValue();
        endDateField.setText(DATE_FORMATTER.format(date.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate()));
    }

    private void showTimePicker() {
        JSpinner spinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, "HH:mm");
        spinner.setEditor(editor);

        JOptionPane.showMessageDialog(this, spinner, "Choose Time",
            JOptionPane.QUESTION_MESSAGE);

        java.util.Date time = (java.util.Date) spinner.getValue();
        endTimeField.setText(TIME_FORMATTER.format(time.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalTime()));
    }

    private void chooseImage() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
            "Image files", "jpg", "jpeg", "png", "gif");
        fileChooser.setFileFilter(filter);

        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            selectedImagePath = selectedFile.getAbsolutePath();
            imagePathLabel.setText(selectedFile.getName());
        }
    }

    private boolean validateAndAddItem(int sellerId) {
        // Validate inputs
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter item name");
            return false;
        }
        if (descriptionArea.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter item description");
            return false;
        }
        if (startingPriceField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter starting price");
            return false;
        }
        if (endDateField.getText().isEmpty() || endTimeField.getText().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please select end date and time");
            return false;
        }
        // Auction type
        String auctionType = auctionTypeComboBox.getSelectedIndex() == 0 ? "ascending" : "sealed";

        try {
            double startingPrice = Double.parseDouble(startingPriceField.getText());
            if (startingPrice <= 0) {
                JOptionPane.showMessageDialog(this, "Starting price must be greater than 0");
                return false;
            }

            // Create item
            Item item = new Item();
            item.setSellerId(sellerId);
            item.setCategoryId(((Category) categoryComboBox.getSelectedItem()).getCategoryId());
            item.setName(nameField.getText().trim());
            item.setDescription(descriptionArea.getText().trim());
            item.setStartingPrice(startingPrice);
            item.setCurrentPrice(startingPrice);
            item.setStartTime(LocalDateTime.now());

            // Parse end date and time
            String endDateTimeStr = endDateField.getText() + " " + endTimeField.getText();
            LocalDateTime endDateTime = LocalDateTime.parse(endDateTimeStr,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
            item.setEndTime(endDateTime);

            item.setStatus("active");
            item.setImagePath(selectedImagePath);
            item.setAuctionType(auctionType);

            // Save to database
            ItemDAO itemDAO = new ItemDAO();
            if (itemDAO.createItem(item)) {
                JOptionPane.showMessageDialog(this, "Item added successfully!");
                return true;
            } else {
                JOptionPane.showMessageDialog(this, "Failed to add item");
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Please enter a valid price");
            return false;
        }
    }

    public boolean isItemAdded() {
        return itemAdded;
    }
}