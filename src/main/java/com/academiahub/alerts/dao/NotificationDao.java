package com.academiahub.alerts.dao;

import com.academiahub.alerts.Database;
import com.academiahub.alerts.model.Notification;

import java.sql.*;
//import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NotificationDao {

    public void insert(int alertId, String message) throws SQLException {
        String sql = "INSERT INTO notifications(alert_id, message) VALUES(?,?)";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, alertId);
            ps.setString(2, message);
            ps.executeUpdate();
        }
    }

    public List<Notification> list(boolean onlyUnseen) throws SQLException {
        String sql = onlyUnseen
                ? "SELECT * FROM notifications WHERE seen=FALSE ORDER BY created_at DESC"
                : "SELECT * FROM notifications ORDER BY created_at DESC";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Notification> out = new ArrayList<>();
            while (rs.next()) {
                Notification n = new Notification();
                n.id = rs.getInt("id");
                n.alertId = rs.getInt("alert_id");
                n.message = rs.getString("message");
                Timestamp ts = rs.getTimestamp("created_at");
                n.createdAt = ts == null ? null : ts.toLocalDateTime();
                n.seen = rs.getBoolean("seen");
                out.add(n);
            }
            return out;
        }
    }

    public void markSeen(int id) throws SQLException {
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE notifications SET seen=TRUE WHERE id=?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }
}
