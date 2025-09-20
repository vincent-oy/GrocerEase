/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ui;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import model.InventoryItem;
import service.InventoryService;

public class InventoryWindow extends JFrame {

    private final InventoryService service;          // dependency: business logic
    private final DefaultTableModel model;           // table model to show data
    private final JTable table;                      // table widget
    public InventoryWindow(InventoryService service) {
        this.service = service;
        
    
        setTitle("Inventory");
        setSize(720, 420);
        setLocationRelativeTo(null);

        // Table columns
        String[] cols = {"ID", "Product", "Qty", "Expiry (YYYY-MM-DD)"};
        model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; } // read-only rows
        };
        table = new JTable(model);

        // Buttons
        JButton addBtn = new JButton("Add");
        JButton editBtn = new JButton("Edit");
        JButton delBtn = new JButton("Delete");
        JButton refreshBtn = new JButton("Refresh");

        // Wire actions
        addBtn.addActionListener(e -> onAdd());
        editBtn.addActionListener(e -> onEdit());
        delBtn.addActionListener(e -> onDelete());
        refreshBtn.addActionListener(e -> reload());

        // Layout
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btns.add(addBtn);
        btns.add(editBtn);
        btns.add(delBtn);
        btns.add(refreshBtn);

        add(btns, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);   // scroll bar (the whole area, draw scroll bar according to how many)

        // First load
        reload();
    }

    private void reload() {
        model.setRowCount(0);                        // clear
        for (InventoryItem it : service.listAll()) { // fetch in-memory list
            model.addRow(new Object[]{
                    it.getId(), it.getName(), it.getQuantity(), it.getExpiry().toString()
            });
        }
    }

    private void onAdd() {
        // Quick inputs via dialogs (fast to implement)
        String name = JOptionPane.showInputDialog(this, "Product name:");
        if (name == null || name.isBlank()) return;

        String qtyStr = JOptionPane.showInputDialog(this, "Quantity (integer):");
        if (qtyStr == null) return;

        String expStr = JOptionPane.showInputDialog(this, "Expiry YYYY-MM-DD:");
        if (expStr == null) return;

        try {
            int qty = Integer.parseInt(qtyStr.trim());
            LocalDate exp = LocalDate.parse(expStr.trim());
            service.add(name.trim(), qty, exp);      // write to in-memory repo
            reload();                                 // refresh view
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage());
        }
    }

    private void onEdit() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a row to edit."); return; }

        int id = (int) model.getValueAt(row, 0);
        String oldName = (String) model.getValueAt(row, 1);
        int oldQty = (int) model.getValueAt(row, 2);
        String oldExp = (String) model.getValueAt(row, 3);

        String name = JOptionPane.showInputDialog(this, "Product name:", oldName);
        if (name == null || name.isBlank()) return;

        String qtyStr = JOptionPane.showInputDialog(this, "Quantity:", String.valueOf(oldQty));
        if (qtyStr == null) return;

        String expStr = JOptionPane.showInputDialog(this, "Expiry YYYY-MM-DD:", oldExp);
        if (expStr == null) return;

        try {
            int qty = Integer.parseInt(qtyStr.trim());
            LocalDate exp = LocalDate.parse(expStr.trim());
            service.update(id, name.trim(), qty, exp);  // update in memory
            reload();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid input: " + ex.getMessage());
        }
    }

    private void onDelete() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a row to delete."); return; }
        int id = (int) model.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete this item?");
        if (confirm == JOptionPane.YES_OPTION) {
            service.delete(id);
            reload();
        }
    }
}
