package com.academiahub.alerts.ui;

import javax.swing.table.DefaultTableModel;

public final class Tables {
    private Tables() {}
    public static DefaultTableModel alertsModel() {
        return new DefaultTableModel(new Object[]{"ID","Title","Due At","Notify (min)","Status"}, 0) {
            @Override public boolean isCellEditable(int r,int c){ return false; }
        };
    }
    public static DefaultTableModel notificationsModel() {
        return new DefaultTableModel(new Object[]{"ID","Alert ID","Message","Created At","Seen"}, 0) {
            @Override public boolean isCellEditable(int r,int c){ return false; }
        };
    }
}
