// dao/TransactionDAO.java
package dao;

import db.DatabaseConnection;
import model.Transaction;

import java.sql.*;
import java.util.*;

public class TransactionDAO {
    public void addTransaction(Transaction txn) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO transactions (buyer_id, item_id, quantity, date) VALUES (?, ?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, txn.getBuyerId());
            stmt.setInt(2, txn.getItemId());
            stmt.setInt(3, txn.getQuantity());
            stmt.setString(4, txn.getDate());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
