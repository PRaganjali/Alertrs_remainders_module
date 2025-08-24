package com.academiahub.alerts.service;

import com.academiahub.alerts.dao.AlertDao;
import com.academiahub.alerts.dao.NotificationDao;
import com.academiahub.alerts.model.Alert;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.*;

public class ReminderService implements AutoCloseable {
    private final ScheduledExecutorService exec = Executors.newSingleThreadScheduledExecutor();
    private final AlertDao alertDao = new AlertDao();
    private final NotificationDao notificationDao = new NotificationDao();
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

    public interface Listener {
        void onNewNotification(String msg);
        void onError(Throwable t);
    }

    public void start(Listener listener) {
        exec.scheduleAtFixedRate(() -> {
            try {
                LocalDateTime now = LocalDateTime.now();
                List<Alert> due = alertDao.findDueForNotification(now);
                for (Alert a : due) {
                    String when = a.dueAt.format(fmt);
                    String msg = "Reminder: " + a.title + " (due " + when + ")";
                    notificationDao.insert(a.id, msg);
                    alertDao.markNotified(a.id, now);
                    if (listener != null) listener.onNewNotification(msg);
                }
            } catch (Exception e) {
                if (listener != null) listener.onError(e);
            }
        }, 0, 60, TimeUnit.SECONDS);
    }

    @Override public void close() {
        exec.shutdownNow();
    }
}
