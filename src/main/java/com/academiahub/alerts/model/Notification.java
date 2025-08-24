package com.academiahub.alerts.model;

import java.time.LocalDateTime;

public class Notification {
    public int id;
    public int alertId;
    public String message;
    public LocalDateTime createdAt;
    public boolean seen;
}
