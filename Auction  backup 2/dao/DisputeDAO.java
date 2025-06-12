// dao/DisputeDAO.java
package dao;

import db.DatabaseConnection;
import model.Dispute;

import java.sql.*;
import java.util.*;

public class DisputeDAO {
    public List<Dispute> getPendingDisputes() {
        List<Dispute> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM disputes WHERE status='pending'";
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                Dispute dispute = new Dispute();
                // Populate dispute
                list.add(dispute);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
