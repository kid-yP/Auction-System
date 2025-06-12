package dao;

import model.Bid;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BidDAO {
    public boolean createBid(Bid bid) {
        String sql = "INSERT INTO bids (item_id, buyer_id, amount, bid_time, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Set bid time to current time if not set
            if (bid.getBidTime() == null) {
                bid.setBidTime(new Timestamp(System.currentTimeMillis()));
            }

            // Set bid status to winning initially
            bid.setStatus("winning");

            stmt.setInt(1, bid.getItemId());
            stmt.setInt(2, bid.getBuyerId());
            stmt.setDouble(3, bid.getAmount());
            stmt.setTimestamp(4, bid.getBidTime());
            stmt.setString(5, bid.getStatus());

            System.out.println("Executing bid creation with values: " +
                             "itemId=" + bid.getItemId() + ", " +
                             "buyerId=" + bid.getBuyerId() + ", " +
                             "amount=" + bid.getAmount() + ", " +
                             "bidTime=" + bid.getBidTime() + ", " +
                             "status=" + bid.getStatus());

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        bid.setBidId(rs.getInt(1));

                        // Update the item's current price
                        ItemDAO itemDAO = new ItemDAO();
                        if (!itemDAO.updateCurrentPrice(bid.getItemId(), bid.getAmount())) {
                            System.err.println("Failed to update item current price");
                        }

                        // Update status of previous winning bid to outbid
                        updatePreviousWinningBid(bid.getItemId(), bid.getBidId());

                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating bid: " + e.getMessage());
            e.printStackTrace();
        }
        return false;
    }

    private void updatePreviousWinningBid(int itemId, int newBidId) {
        String sql = "UPDATE bids SET status = 'outbid' WHERE item_id = ? AND status = 'winning' AND bid_id != ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, itemId);
            stmt.setInt(2, newBidId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating previous winning bid: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<Bid> getBidsForItem(int itemId) {
        List<Bid> bids = new ArrayList<>();
        String sql = "SELECT * FROM bids WHERE item_id = ? ORDER BY bid_time DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, itemId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    bids.add(extractBidFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting bids for item: " + e.getMessage());
            e.printStackTrace();
        }
        return bids;
    }

    private Bid extractBidFromResultSet(ResultSet rs) throws SQLException {
        Bid bid = new Bid();
        bid.setBidId(rs.getInt("bid_id"));
        bid.setItemId(rs.getInt("item_id"));
        bid.setBuyerId(rs.getInt("buyer_id"));
        bid.setAmount(rs.getDouble("amount"));
        bid.setBidTime(rs.getTimestamp("bid_time"));
        bid.setStatus(rs.getString("status"));
        return bid;
    }

    public void updateBidStatus(int bidId, String status) {
        String sql = "UPDATE bids SET status = ? WHERE bid_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, bidId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating bid status: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateAllBidsForItem(int itemId, String status) {
        String sql = "UPDATE bids SET status = ? WHERE item_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, itemId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating all bids for item: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean isValidBidStatus(String status) {
        return status != null &&
               (status.equals("winning") ||
                status.equals("outbid") ||
                status.equals("won") ||
                status.equals("lost"));
    }

    public List<Bid> getBidsByBuyer(int buyerId) {
        List<Bid> bids = new ArrayList<>();
        String sql = "SELECT * FROM bids WHERE buyer_id = ? ORDER BY bid_time DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, buyerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    bids.add(extractBidFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting bids by buyer: " + e.getMessage());
            e.printStackTrace();
        }
        return bids;
    }
}