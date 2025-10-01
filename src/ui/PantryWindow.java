/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */

package ui.pantry;

import model.PantryItem;
import service.PantryService;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.util.List;

/**
 * PantryWindow shows a table of everything in the pantry.
 * It lets the user Add, Edit, Delete, and filter by Low Stock or Expiring Soon.
 *
 * NOTE: This window does not know SQL or database details.
 * It only talks to the PantryService interface. This is good abstraction.
 */
public class PantryWindow extends JFrame {

    // The service object that does the actual data work (database)
    private final PantryService service;

    // Table model stores the rows/columns shown in the JTable
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "ID", "Name", "Category", "On-hand", "Unit", "Min", "Expiry" }, 0
    ) {
        // We do not want the user to type directly into cells
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }

        // Return column data types (helps sorting and rendering)
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0 || columnIndex == 3 || columnIndex == 5) {
                return Integer.class;
            }
            return String.class;
        }
    };

    // The actual table UI component
    private final JTable table = new JTable(model);

    /**
     * Constructor: pass in a PantryService (SQLite or in-memory).
     */
    public PantryWindow(PantryService svc) {
        this.service = svc;

        // Basic window setup
        setTitle("Pantry");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(900, 520);
        setLocationRelativeTo(null);  // center on screen

        // Configure table selection behavior
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // ----- Top toolbar with buttons -----
        JButton btnAdd     = new JButton("Add");
        JButton btnEdit    = new JButton("Edit");
        JButton btnDelete  = new JButton("Delete");
        JButton btnLow     = new JButton("Low Stock");
        JButton btnSoon    = new JButton("Expiring ≤ 3 days");
        JButton btnAll     = new JButton("Show All");

        // Put buttons into a panel (left-aligned)
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Pantry Actions:"));
        top.add(btnAdd);
        top.add(btnEdit);
        top.add(btnDelete);
        top.add(btnLow);
        top.add(btnSoon);
        top.add(btnAll);

        // Add the top panel and the table (inside a scroll pane) to the window
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // ----- Button actions -----
        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDelete.addActionListener(e -> onDelete());
        btnLow.addActionListener(e -> load(service.lowStock()));
        btnSoon.addActionListener(e -> load(service.expiringSoon(3)));
        btnAll.addActionListener(e -> load(service.listAll()));

        // Load initial data
        load(service.listAll());
    }

    /**
     * Fill the table with the given list of pantry items.
     * Also color rows by expiry status (red = expired, amber = soon).
     */
    private void load(List<PantryItem> items) {
        // Clear existing rows
        model.setRowCount(0);

        // Add rows one by one
        for (PantryItem p : items) {
            model.addRow(new Object[] {
                    p.id,
                    p.name,
                    p.category,
                    p.onHandQty,
                    p.unit,
                    p.minQty,
                    (p.expiry == null) ? "" : p.expiry.toString()
            });
        }

        // Very simple "cell renderer" via JTable override:
        // We color rows based on the Expiry column value.
        table.setDefaultRenderer(Object.class, (tbl, value, isSelected, hasFocus, row, col) -> {
            JLabel label = new JLabel(value == null ? "" : value.toString());
            label.setOpaque(true);
            label.setForeground(Color.BLACK);

            // base background (selected vs normal)
            label.setBackground(isSelected ? tbl.getSelectionBackground() : Color.WHITE);

            // check expiry value in column 6 (index starts at 0)
            String exp = (String) model.getValueAt(row, 6);
            if (exp != null && !exp.isBlank()) {
                LocalDate date = LocalDate.parse(exp);
                LocalDate today = LocalDate.now();

                if (date.isBefore(today)) {
                    // expired
                    if (!isSelected) label.setBackground(new Color(255, 200, 200)); // red-ish
                } else if (!date.isAfter(today.plusDays(3))) {
                    // within 3 days
                    if (!isSelected) label.setBackground(new Color(255, 240, 200)); // amber
                }
            }
            return label;
        });
    }

    // Helper: get selected row's ID as Integer (null if nothing selected)
    private Integer selectedId() {
        int r = table.getSelectedRow();
        if (r < 0) return null;
        return (Integer) model.getValueAt(r, 0);
    }

    // Add new pantry item via small dialog
    private void onAdd() {
        PantryItemForm form = new PantryItemForm(this, null);
        PantryItem newItem = form.showDialog();
        if (newItem != null) {
            try {
                service.add(newItem);
                load(service.listAll());
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Edit currently selected pantry item
    private void onEdit() {
        Integer id = selectedId();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Please select a row first.");
            return;
        }

        // Find the current data to prefill the form (simple approach)
        PantryItem current = service.listAll().stream()
                .filter(p -> p.id.equals(id))
                .findFirst()
                .orElse(null);

        if (current == null) return;

        PantryItemForm form = new PantryItemForm(this, current);
        PantryItem edited = form.showDialog();
        if (edited != null) {
            try {
                edited.id = id;      // keep the same id
                service.update(edited);
                load(service.listAll());
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Delete selected item
    private void onDelete() {
        Integer id = selectedId();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Please select a row first.");
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete the selected item?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            try {
                service.delete(id);
                load(service.listAll());
            } catch (RuntimeException ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new PantryWindow().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
