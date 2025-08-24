package com.academiahub.alerts.model;

import java.time.LocalDateTime;

public class Alert {
    public int id;
    public String title;
    public String details;
    public LocalDateTime dueAt;
    public int notifyBeforeMinutes;
    public String status; // PENDING, NOTIFIED, SNOOZED, COMPLETED
    public LocalDateTime lastNotifiedAt;

    @Override public String toString() {
        return "%d | %s | due %s | status %s".formatted(id, title, dueAt, status);
    }
}
