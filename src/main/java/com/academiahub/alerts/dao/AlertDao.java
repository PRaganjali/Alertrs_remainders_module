package com.academiahub.alerts.dao;

import com.academiahub.alerts.Database;
import com.academiahub.alerts.model.Alert;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class AlertDao {

    public void insert(Alert a) throws SQLException {
        String sql = """
            INSERT INTO alerts(title, details, due_at, notify_before_minutes, status, last_notified_at)
            VALUES(?,?,?,?,?,?)
        """;
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, a.title);
            ps.setString(2, a.details);
            ps.setTimestamp(3, Timestamp.valueOf(a.dueAt));
            ps.setInt(4, a.notifyBeforeMinutes);
            ps.setString(5, a.status == null ? "PENDING" : a.status);
            ps.setTimestamp(6, a.lastNotifiedAt == null ? null : Timestamp.valueOf(a.lastNotifiedAt));
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) a.id = rs.getInt(1);
            }
        }
    }

    public List<Alert> listAll() throws SQLException {
        String sql = "SELECT * FROM alerts ORDER BY due_at ASC";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Alert> out = new ArrayList<>();
            while (rs.next()) out.add(from(rs));
            return out;
        }
    }

    public void updateStatus(int id, String status) throws SQLException {
        String sql = "UPDATE alerts SET status=? WHERE id=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public void snooze(int id, int minutes) throws SQLException {
        String sql = "UPDATE alerts SET due_at = DATE_ADD(due_at, INTERVAL ? MINUTE), status='SNOOZED' WHERE id=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, minutes);
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    public List<Alert> findDueForNotification(LocalDateTime now) throws SQLException {
        // Notify when time until due <= notify_before_minutes and avoid spamming (>= 5 min since last)
        String sql = """
            SELECT * FROM alerts
            WHERE status IN ('PENDING','SNOOZED','NOTIFIED')
              AND TIMESTAMPDIFF(MINUTE, ?, due_at) <= notify_before_minutes
              AND (last_notified_at IS NULL OR TIMESTAMPDIFF(MINUTE, last_notified_at, ?) >= 5)
              AND due_at >= DATE_SUB(?, INTERVAL 1 DAY) -- ignore very old items
        """;
        Timestamp t = Timestamp.valueOf(now);
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, t);
            ps.setTimestamp(2, t);
            ps.setTimestamp(3, t);
            try (ResultSet rs = ps.executeQuery()) {
                List<Alert> out = new ArrayList<>();
                while (rs.next()) out.add(from(rs));
                return out;
            }
        }
    }

    public void markNotified(int id, LocalDateTime at) throws SQLException {
        String sql = "UPDATE alerts SET last_notified_at=?, status=IF(status='COMPLETED','COMPLETED','NOTIFIED') WHERE id=?";
        try (Connection c = Database.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(at));
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    private Alert from(ResultSet rs) throws SQLException {
        Alert a = new Alert();
        a.id = rs.getInt("id");
        a.title = rs.getString("title");
        a.details = rs.getString("details");
        Timestamp due = rs.getTimestamp("due_at");
        a.dueAt = due == null ? null : due.toLocalDateTime();
        a.notifyBeforeMinutes = rs.getInt("notify_before_minutes");
        a.status = rs.getString("status");
        Timestamp ln = rs.getTimestamp("last_notified_at");
        a.lastNotifiedAt = ln == null ? null : ln.toLocalDateTime();
        return a;
    }
}
