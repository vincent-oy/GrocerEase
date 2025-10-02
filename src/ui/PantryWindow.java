package ui;

import model.PantryItem;
import service.PantryService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Pantry window with a table and controls for:
 *  Add, Edit, Delete, Low Stock, Expiring Soon, Show All.
 */
public class PantryWindow extends JFrame {

    private final PantryService service;

    private final DefaultTableModel model = new DefaultTableModel(
        new Object[] { "ID", "Name", "Category", "On-hand", "Unit", "Min", "Expiry" }, 0
    ) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
        @Override public Class<?> getColumnClass(int c) {
            return (c==0||c==3||c==5) ? Integer.class : String.class;
        }
    };

    private final JTable table = new JTable(model);

    public PantryWindow(PantryService svc) {
        this.service = svc;

        setTitle("Pantry");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(920, 540);
        setLocationRelativeTo(null);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JButton add   = new JButton("Add");
        JButton edit  = new JButton("Edit");
        JButton del   = new JButton("Delete");
        JButton low   = new JButton("Low Stock");
        JButton soon  = new JButton("Expiring ≤ 3 days");
        JButton all   = new JButton("Show All");

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Pantry Actions:"));
        top.add(add); top.add(edit); top.add(del); top.add(low); top.add(soon); top.add(all);

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        add.addActionListener(e -> onAdd());
        edit.addActionListener(e -> onEdit());
        del.addActionListener(e -> onDelete());
        low.addActionListener(e -> load(service.lowStock()));
        soon.addActionListener(e -> load(service.expiringSoon(3)));
        all.addActionListener(e -> load(service.listAll()));

        load(service.listAll());
    }

    private void load(List<PantryItem> items) {
        model.setRowCount(0);
        for (PantryItem p : items) {
            model.addRow(new Object[] {
                p.id, p.name, p.category, p.onHandQty, p.unit, p.minQty,
                (p.expiry == null ? "" : p.expiry.toString())
            });
        }

        // Row coloring by expiry (red = expired, amber = soon)
        table.setDefaultRenderer(Object.class, (tbl, value, isSel, hasFocus, row, col) -> {
            JLabel label = new JLabel(value == null ? "" : value.toString());
            label.setOpaque(true);
            label.setForeground(Color.BLACK);
            label.setBackground(isSel ? tbl.getSelectionBackground() : Color.WHITE);

            String exp = (String) model.getValueAt(row, 6);
            if (exp != null && !exp.isBlank()) {
                LocalDate d = LocalDate.parse(exp);
                LocalDate today = LocalDate.now();
                if (d.isBefore(today)) {
                    if (!isSel) label.setBackground(new Color(255,200,200));
                } else if (!d.isAfter(today.plusDays(3))) {
                    if (!isSel) label.setBackground(new Color(255,240,200));
                }
            }
            return label;
        });
    }

    private Integer selectedId() {
        int r = table.getSelectedRow();
        return r < 0 ? null : (Integer) model.getValueAt(r, 0);
    }

    private void onAdd() {
        PantryItemForm f = new PantryItemForm(this, null);
        PantryItem p = f.showDialog();
        if (p != null) {
            try { service.add(p); load(service.listAll()); }
            catch (RuntimeException ex) { JOptionPane.showMessageDialog(this, ex.getMessage()); }
        }
    }

    private void onEdit() {
        Integer id = selectedId();
        if (id == null) { JOptionPane.showMessageDialog(this, "Select a row"); return; }
        PantryItem curr = service.listAll().stream().filter(x -> x.id.equals(id)).findFirst().orElse(null);
        if (curr == null) return;
        PantryItemForm f = new PantryItemForm(this, curr);
        PantryItem edited = f.showDialog();
        if (edited != null) {
            try { edited.id = id; service.update(edited); load(service.listAll()); }
            catch (RuntimeException ex) { JOptionPane.showMessageDialog(this, ex.getMessage()); }
        }
    }

    private void onDelete() {
        Integer id = selectedId();
        if (id == null) { JOptionPane.showMessageDialog(this, "Select a row"); return; }
        if (JOptionPane.showConfirmDialog(this, "Delete this item?") == JOptionPane.YES_OPTION) {
            try { service.delete(id); load(service.listAll()); }
            catch (RuntimeException ex) { JOptionPane.showMessageDialog(this, ex.getMessage()); }
        }
    }
}
