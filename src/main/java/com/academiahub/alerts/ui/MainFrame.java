package com.academiahub.alerts.ui;

import com.academiahub.alerts.dao.AlertDao;
import com.academiahub.alerts.model.Alert;
import com.academiahub.alerts.service.ReminderService;
import java.awt.image.BufferedImage;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class MainFrame extends JFrame implements ReminderService.Listener {
    private final AlertDao alertDao = new AlertDao();
    private final DefaultTableModel alertsModel = Tables.alertsModel();
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");
    private final ReminderService service = new ReminderService();
    private TrayIcon trayIcon;

    public MainFrame() {
        super("AcademiaHub — Alerts & Reminders");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(980, 600);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());
        add(buildToolbar(), BorderLayout.NORTH);
        add(buildAlertsTable(), BorderLayout.CENTER);

        installTray();
        refreshAlerts();

        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent e) {
                service.close();
                dispose();
                System.exit(0);
            }
        });

        service.start(this);
    }

    private JComponent buildToolbar() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRefresh = new JButton("Refresh");
        JButton btnExport = new JButton("Export Alerts CSV");
        JButton btnComplete = new JButton("Mark Completed");
        JButton btnSnooze = new JButton("Snooze +10m");

        btnRefresh.addActionListener(e -> refreshAlerts());
        btnExport.addActionListener(e -> exportCsv());
        btnComplete.addActionListener(e -> onComplete());
        btnSnooze.addActionListener(e -> onSnooze());

        p.add(btnComplete);
        p.add(btnSnooze);
        p.add(btnExport);
        p.add(btnRefresh);

        return p;
    }

    private JComponent buildAlertsTable() {
        JTable tblAlerts = new JTable(alertsModel);
        tblAlerts.setName("alertsTable");
        tblAlerts.setAutoCreateRowSorter(true);
        return new JScrollPane(tblAlerts);
    }

    private Integer selectedAlertId() {
        JTable tbl = findTable("alertsTable");
        int row = tbl.getSelectedRow();
        if (row < 0) return null;
        int modelIdx = tbl.convertRowIndexToModel(row);
        return (Integer) alertsModel.getValueAt(modelIdx, 0);
    }

    private JTable findTable(String name) {
        for (Component c : getContentPane().getComponents()) {
            if (c instanceof JScrollPane sp) {
                if (sp.getViewport().getView() instanceof JTable t && name.equals(t.getName())) {
                    return t;
                }
            }
        }
        return null;
    }

    private void onComplete() {
        Integer id = selectedAlertId();
        if (id == null) { toast("Select an alert"); return; }
        try {
            alertDao.updateStatus(id, "COMPLETED");
            refreshAlerts();
        } catch (SQLException e) { showError(e); }
    }

    private void onSnooze() {
        Integer id = selectedAlertId();
        if (id == null) { toast("Select an alert"); return; }
        try {
            alertDao.snooze(id, 1);
            refreshAlerts();
        } catch (SQLException e) { showError(e); }
    }

    private void exportCsv() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Export Alerts CSV");
        fc.setFileFilter(new FileNameExtensionFilter("CSV", "csv"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            String path = fc.getSelectedFile().getAbsolutePath();
            if (!path.toLowerCase().endsWith(".csv")) path += ".csv";
            try (BufferedWriter w = new BufferedWriter(new FileWriter(path))) {
                w.write("ID,Title,Due At,Notify (min),Status\n");
                for (int r = 0; r < alertsModel.getRowCount(); r++) {
                    w.write(alertsModel.getValueAt(r, 0) + "," +
                            escape(alertsModel.getValueAt(r, 1)) + "," +
                            alertsModel.getValueAt(r, 2) + "," +
                            alertsModel.getValueAt(r, 3) + "," +
                            alertsModel.getValueAt(r, 4) + "\n");
                }
                toast("Exported: " + path);
            } catch (Exception ex) { showError(ex); }
        }
    }

    private String escape(Object o) {
        String s = String.valueOf(o);
        return "\"" + s.replace("\"", "\"\"") + "\"";
    }

    private void refreshAlerts() {
        try {
            List<Alert> list = alertDao.listAll();
            alertsModel.setRowCount(0);
            for (Alert a : list) {
                alertsModel.addRow(new Object[]{
                        a.id, a.title, a.dueAt == null ? "" : a.dueAt.format(fmt),
                        a.notifyBeforeMinutes, a.status
                });
            }
        } catch (SQLException e) { showError(e); }
    }

    private void installTray() {
        if (!SystemTray.isSupported()) return;
        try {
            SystemTray tray = SystemTray.getSystemTray();
            Image img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
            trayIcon = new TrayIcon(img, "AcademiaHub Alerts");
            trayIcon.setImageAutoSize(true);
            tray.add(trayIcon);
        } catch (Exception ignored) {}
    }

    private void toast(String msg) {
        if (trayIcon != null) trayIcon.displayMessage("AcademiaHub", msg, TrayIcon.MessageType.INFO);
        else JOptionPane.showMessageDialog(this, msg);
    }

    private void showError(Throwable t) {
        t.printStackTrace();
        JOptionPane.showMessageDialog(this, t.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
    }

    // ReminderService callbacks
    @Override public void onNewNotification(String msg) { toast(msg); }
    @Override public void onError(Throwable t) { showError(t); }
}
