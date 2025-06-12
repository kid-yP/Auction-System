package dao;

import model.Item;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO {
    public List<Item> getActiveItems() {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT * FROM items WHERE status = 'active' AND end_time > NOW()";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                items.add(extractItemFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting active items: " + e.getMessage());
            e.printStackTrace();
        }
        return items;
    }

    public boolean createItem(Item item) {
        String sql = "INSERT INTO items (seller_id, category_id, name, description, " +
                    "starting_price, current_price, start_time, end_time, status, image_path, auction_type) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Ensure current price is at least starting price
            double currentPrice = Math.max(item.getStartingPrice(), item.getCurrentPrice());

            stmt.setInt(1, item.getSellerId());
            stmt.setInt(2, item.getCategoryId());
            stmt.setString(3, item.getName());
            stmt.setString(4, item.getDescription());
            stmt.setDouble(5, item.getStartingPrice());
            stmt.setDouble(6, currentPrice);
            stmt.setTimestamp(7, Timestamp.valueOf(item.getStartTime()));
            stmt.setTimestamp(8, Timestamp.valueOf(item.getEndTime()));
            stmt.setString(9, "active"); // Always start as active
            stmt.setString(10, item.getImagePath());
            stmt.setString(11, item.getAuctionType());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        item.setItemId(rs.getInt(1));
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<Item> getItemsBySeller(int sellerId) {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT * FROM items WHERE seller_id = ? ORDER BY end_time DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, sellerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(extractItemFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return items;
    }

    public List<Item> getWonItems(int buyerId) {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT i.* FROM items i " +
                    "JOIN bids b ON i.item_id = b.item_id " +
                    "WHERE b.buyer_id = ? AND b.status = 'won'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, buyerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(extractItemFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting won items: " + e.getMessage());
            e.printStackTrace();
        }
        return items;
    }

    public List<Item> getLostItems(int buyerId) {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT i.* FROM items i " +
                    "JOIN bids b ON i.item_id = b.item_id " +
                    "WHERE b.buyer_id = ? AND b.status = 'lost'";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, buyerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(extractItemFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting lost items: " + e.getMessage());
            e.printStackTrace();
        }
        return items;
    }

    public boolean updateCurrentPrice(int itemId, double newPrice) {
        String sql = "UPDATE items SET current_price = ? WHERE item_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            System.out.println("Updating item price - itemId: " + itemId + ", newPrice: " + newPrice);

            pstmt.setDouble(1, newPrice);
            pstmt.setInt(2, itemId);

            int rowsAffected = pstmt.executeUpdate();
            System.out.println("Rows affected by price update: " + rowsAffected);

            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating item price: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public double getHighestBid(int itemId) {
        String sql = "SELECT MAX(amount) as highest_bid FROM bids WHERE item_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, itemId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("highest_bid");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    public void updateItemStatus(int itemId, String status) {
        String sql = "UPDATE items SET status = ? WHERE item_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, itemId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating item status: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Item> getItemsByStatus(int userId, String status) {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT * FROM items WHERE seller_id = ? AND status = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setString(2, status);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(extractItemFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting items by status: " + e.getMessage());
            e.printStackTrace();
        }
        return items;
    }

    public List<Item> getFinishedAuctions(int sellerId) {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT * FROM items WHERE seller_id = ? AND status IN ('sold', 'ended')";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, sellerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    items.add(extractItemFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting finished auctions: " + e.getMessage());
            e.printStackTrace();
        }
        return items;
    }

    public List<Item> getAllActiveItems() {
        List<Item> items = new ArrayList<>();
        String sql = "SELECT * FROM items WHERE status = 'active' ORDER BY end_time ASC";

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                items.add(extractItemFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting active items: " + e.getMessage());
            e.printStackTrace();
        }
        return items;
    }

    public Item getItemById(int itemId) {
        String sql = "SELECT * FROM items WHERE item_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, itemId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return extractItemFromResultSet(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting item by ID: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    private Item extractItemFromResultSet(ResultSet rs) throws SQLException {
        return new Item(
            rs.getInt("item_id"),
            rs.getInt("seller_id"),
            rs.getInt("category_id"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getDouble("starting_price"),
            rs.getDouble("current_price"),
            rs.getTimestamp("start_time").toLocalDateTime(),
            rs.getTimestamp("end_time").toLocalDateTime(),
            rs.getString("status"),
            rs.getString("image_path"),
            rs.getString("auction_type")
        );
    }
}