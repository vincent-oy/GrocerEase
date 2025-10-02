package ui;

import service.SqlitePantryService;
import model.PantryItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/*
 * pantry window: table + buttons for add/edit/delete + 2 quick filters
 * student style:
 * - window creates its own SqlitePantryService (no interface layer)
 * - expiry handled as plain text "YYYY-MM-DD" (less parsing headaches)
 * - lots of printlns so i can see what's happening
 */
public class PantryWindow extends JFrame {

    // i directly use the concrete sqlite service here
    private final SqlitePantryService service = new SqlitePantryService();

    // table model (columns match the object fields i care about)
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] {"ID", "Name", "Category", "On-hand", "Unit", "Min", "Expiry"}, 0
    ) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
        @Override public Class<?> getColumnClass(int c) {
            return (c==0 || c==3 || c==5) ? Integer.class : String.class;
        }
    };

    private final JTable table = new JTable(model);

    public PantryWindow() {
        setTitle("Pantry");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(920, 560);
        setLocationRelativeTo(null);

        // ==== top toolbar ====
        JButton btnAdd  = new JButton("Add");
        JButton btnEdit = new JButton("Edit");
        JButton btnDel  = new JButton("Delete");
        JButton btnLow  = new JButton("Low Stock");
        JButton btnSoon = new JButton("Expiring ≤ 3 days");
        JButton btnAll  = new JButton("Show All");

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Pantry actions: "));
        top.add(btnAdd);
        top.add(btnEdit);
        top.add(btnDel);
        top.add(btnLow);
        top.add(btnSoon);
        top.add(btnAll);

        // put toolbar + table into the frame
        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        // wire buttons to actions
        btnAdd.addActionListener(e -> onAdd());
        btnEdit.addActionListener(e -> onEdit());
        btnDel.addActionListener(e -> onDelete());
        btnLow.addActionListener(e -> load(service.lowStock()));
        btnSoon.addActionListener(e -> load(service.expiringSoon(3)));
        btnAll.addActionListener(e -> load(service.listAll()));

        // selection mode: one row at a time (easier)
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // first load
        load(service.listAll());
    }

    // reloads the table with given list (replace everything)
    private void load(List<PantryItem> items) {
        model.setRowCount(0);
        for (PantryItem p : items) {
            model.addRow(new Object[] {
                    p.id, p.name, p.category, p.onHandQty, p.unit, p.minQty,
                    (p.expiry == null ? "" : p.expiry)
            });
        }
        System.out.println("[Pantry] loaded rows = " + items.size());
    }

    // helper: get selected id or null if nothing selected
    private Integer selectedId() {
        int r = table.getSelectedRow();
        if (r < 0) return null;
        return (Integer) model.getValueAt(r, 0);
    }

    // add item flow with simple inputs
    private void onAdd() {
        JTextField name = new JTextField(16);
        JTextField cat  = new JTextField(12);
        JTextField qty  = new JTextField(6);
        JTextField unit = new JTextField(8);
        JTextField min  = new JTextField(6);
        JTextField exp  = new JTextField(10); // YYYY-MM-DD or blank

        JPanel p = new JPanel(new GridLayout(0,2,8,6));
        p.add(new JLabel("Name:"));     p.add(name);
        p.add(new JLabel("Category:")); p.add(cat);
        p.add(new JLabel("On-hand:"));  p.add(qty);
        p.add(new JLabel("Unit:"));     p.add(unit);
        p.add(new JLabel("Min qty:"));  p.add(min);
        p.add(new JLabel("Expiry:"));   p.add(exp);

        int ok = JOptionPane.showConfirmDialog(this, p, "Add Pantry Item",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;

        try {
            PantryItem x = new PantryItem();
            x.name      = name.getText().trim();
            x.category  = cat.getText().trim();
            x.onHandQty = Integer.parseInt(qty.getText().trim());
            x.unit      = unit.getText().trim();
            x.minQty    = Integer.parseInt(min.getText().trim());
            String ex   = exp.getText().trim();
            x.expiry    = ex.isEmpty() ? null : ex;  // keep as text (student simple)

            service.add(x);
            load(service.listAll());
            System.out.println("[Pantry] added: " + x.name);

        } catch (Exception ex2) {
            ex2.printStackTrace();
            JOptionPane.showMessageDialog(this, "check inputs: qty/min must be whole numbers; date as YYYY-MM-DD");
        }
    }

    // edit selected row
    private void onEdit() {
        Integer id = selectedId();
        if (id == null) { JOptionPane.showMessageDialog(this, "select a row first"); return; }

        // get the current object from db (simple way: listAll then find)
        PantryItem curr = service.listAll().stream()
                .filter(p -> p.id.equals(id)).findFirst().orElse(null);
        if (curr == null) return;

        JTextField name = new JTextField(curr.name, 16);
        JTextField cat  = new JTextField(curr.category, 12);
        JTextField qty  = new JTextField(String.valueOf(curr.onHandQty), 6);
        JTextField unit = new JTextField(curr.unit, 8);
        JTextField min  = new JTextField(String.valueOf(curr.minQty), 6);
        JTextField exp  = new JTextField(curr.expiry == null ? "" : curr.expiry, 10);

        JPanel p = new JPanel(new GridLayout(0,2,8,6));
        p.add(new JLabel("Name:"));     p.add(name);
        p.add(new JLabel("Category:")); p.add(cat);
        p.add(new JLabel("On-hand:"));  p.add(qty);
        p.add(new JLabel("Unit:"));     p.add(unit);
        p.add(new JLabel("Min qty:"));  p.add(min);
        p.add(new JLabel("Expiry:"));   p.add(exp);

        int ok = JOptionPane.showConfirmDialog(this, p, "Edit Pantry Item",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok != JOptionPane.OK_OPTION) return;

        try {
            PantryItem x = new PantryItem();
            x.id        = id;
            x.name      = name.getText().trim();
            x.category  = cat.getText().trim();
            x.onHandQty = Integer.parseInt(qty.getText().trim());
            x.unit      = unit.getText().trim();
            x.minQty    = Integer.parseInt(min.getText().trim());
            String ex   = exp.getText().trim();
            x.expiry    = ex.isEmpty() ? null : ex;

            service.update(x);
            load(service.listAll());
            System.out.println("[Pantry] updated: " + x.name);

        } catch (Exception ex2) {
            ex2.printStackTrace();
            JOptionPane.showMessageDialog(this, "check inputs: qty/min must be whole numbers; date as YYYY-MM-DD");
        }
    }

    // delete selected
    private void onDelete() {
        Integer id = selectedId();
        if (id == null) { JOptionPane.showMessageDialog(this, "select a row first"); return; }

        if (JOptionPane.showConfirmDialog(this, "delete this item?", "confirm",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            try {
                service.delete(id);
                load(service.listAll());
                System.out.println("[Pantry] deleted id=" + id);
            } catch (RuntimeException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "delete failed: " + ex.getMessage());
            }
        }
    }
}
