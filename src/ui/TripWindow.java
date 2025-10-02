package ui;

import service.SqliteTripService;
import util.Money;
import model.Trip;
import model.TripItem;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/*
 * plan a trip window (student style)
 * - create a trip (date + budget + note)
 * - add items (qty + expected price)
 * - live subtotal + remaining vs budget
 * notes: i keep most logic inline and use direct SqliteTripService (no interface)
 */
public class TripWindow extends JFrame {

    // direct service (student simple)
    private final SqliteTripService service = new SqliteTripService();

    // current trip (null until user creates one)
    private Trip currentTrip = null;

    // top inputs
    private final JTextField dateField   = new JTextField(10); // keep as text like "2025-10-03"
    private final JTextField budgetField = new JTextField(8);  // accepts "1200" or "1200.50"
    private final JTextField noteField   = new JTextField(16); // optional

    // table (items)
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "ID", "Item", "Unit", "Qty", "Expected", "Line Total" }, 0
    ) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
        @Override public Class<?> getColumnClass(int c) { return (c==0 || c==3) ? Integer.class : String.class; }
    };
    private final JTable table = new JTable(model);

    // footer labels
    private final JLabel subtotalLabel  = new JLabel("Subtotal: NT$0.00");
    private final JLabel remainingLabel = new JLabel("Remaining: NT$0.00");

    public TripWindow() {
        setTitle("Plan a Trip");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(960, 580);
        setLocationRelativeTo(null);

        // selection: single row (easier)
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // ===== header row =====
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        dateField.setText(java.time.LocalDate.now().toString()); // default today
        header.add(new JLabel("Date:"));          header.add(dateField);
        header.add(new JLabel("Budget (NT$):"));  header.add(budgetField);
        header.add(new JLabel("Note:"));          header.add(noteField);
        JButton btnCreate = new JButton("Create Trip");
        header.add(btnCreate);

        // ===== toolbar row =====
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        JButton btnAdd = new JButton("Add Item");
        JButton btnQty = new JButton("Change Qty");
        JButton btnDel = new JButton("Remove Item");
        JButton btnRef = new JButton("Refresh");
        bar.add(btnAdd); bar.add(btnQty); bar.add(btnDel); bar.add(btnRef);

        // stack header + bar at NORTH (borderlayout only allows one per region, so combine in a panel)
        JPanel north = new JPanel(new BorderLayout());
        north.add(header, BorderLayout.NORTH);
        north.add(bar, BorderLayout.CENTER);
        add(north, BorderLayout.NORTH);

        // table in center (so it expands)
        add(new JScrollPane(table), BorderLayout.CENTER);

        // footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 6));
        footer.add(subtotalLabel);
        footer.add(remainingLabel);
        add(footer, BorderLayout.SOUTH);

        // wire actions
        btnCreate.addActionListener(e -> createTrip());
        btnAdd.addActionListener(e -> addItem());
        btnQty.addActionListener(e -> changeQty());
        btnDel.addActionListener(e -> removeItem());
        btnRef.addActionListener(e -> { refreshTable(); updateTotals(); });
    }

    // create a new trip from inputs
    private void createTrip() {
        try {
            String dateText = dateField.getText().trim();      // keep as text
            int budgetCents = Money.parseCents(budgetField.getText().trim()); // handles 1000 or 1000.50
            String note     = noteField.getText().trim();

            if (dateText.isEmpty()) {
                JOptionPane.showMessageDialog(this, "enter a date like YYYY-MM-DD");
                return;
            }
            if (budgetCents < 0) {
                JOptionPane.showMessageDialog(this, "budget must be >= 0");
                return;
            }

            currentTrip = service.create(dateText, null, budgetCents, note);
            JOptionPane.showMessageDialog(this, "trip created. now add items.");

            // refresh screen
            refreshTable();
            updateTotals();

            System.out.println("[Trip] created id=" + currentTrip.id + " date=" + currentTrip.tripDateText +
                    " budgetCents=" + currentTrip.budgetCents);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "check date and budget format");
        }
    }

    // add an item to current trip
    private void addItem() {
        if (currentTrip == null) {
            JOptionPane.showMessageDialog(this, "create a trip first");
            return;
        }

        JTextField name = new JTextField(14);
        JTextField unit = new JTextField(8);
        JTextField qty  = new JTextField(6);
        JTextField px   = new JTextField(8); // optional price (NT$)

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        p.add(new JLabel("Item:"));                 p.add(name);
        p.add(new JLabel("Unit:"));                 p.add(unit);
        p.add(new JLabel("Qty:"));                  p.add(qty);
        p.add(new JLabel("Expected price (NT$):")); p.add(px);

        int ok = JOptionPane.showConfirmDialog(this, p, "Add Item", JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) return;

        try {
            String itemName = name.getText().trim();
            String unitTxt  = unit.getText().trim();
            if (itemName.isEmpty()) {
                JOptionPane.showMessageDialog(this, "item name required");
                return;
            }
            int q = Integer.parseInt(qty.getText().trim());
            if (q <= 0) {
                JOptionPane.showMessageDialog(this, "qty must be > 0");
                return;
            }

            String pxText = px.getText().trim();
            Integer priceCents = pxText.isEmpty() ? null : Money.parseCents(pxText);

            service.addItem(currentTrip.id, itemName, unitTxt, q, priceCents);

            refreshTable();
            updateTotals();

            System.out.println("[Trip] added item '" + itemName + "' q=" + q +
                    " priceCents=" + (priceCents==null? "null" : priceCents) +
                    " tripId=" + currentTrip.id);

        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "qty must be a whole number");
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "add failed: " + ex.getMessage());
        }
    }

    // change qty of selected row
    private void changeQty() {
        if (currentTrip == null) { JOptionPane.showMessageDialog(this, "create a trip first"); return; }

        int r = table.getSelectedRow();
        if (r < 0) { JOptionPane.showMessageDialog(this, "select a row"); return; }

        Integer id = (Integer) model.getValueAt(r, 0);
        String s = JOptionPane.showInputDialog(this, "new qty:", "1");
        if (s == null) return;

        try {
            int q = Integer.parseInt(s.trim());
            if (q <= 0) { JOptionPane.showMessageDialog(this, "qty must be > 0"); return; }

            service.updateItemQty(id, q);
            refreshTable();
            updateTotals();

            System.out.println("[Trip] changed qty id=" + id + " -> " + q);

        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "invalid qty");
        }
    }

    // remove selected row
    private void removeItem() {
        if (currentTrip == null) { JOptionPane.showMessageDialog(this, "create a trip first"); return; }

        int r = table.getSelectedRow();
        if (r < 0) { JOptionPane.showMessageDialog(this, "select a row"); return; }
        Integer id = (Integer) model.getValueAt(r, 0);

        if (JOptionPane.showConfirmDialog(this, "remove this item?") == JOptionPane.YES_OPTION) {
            try {
                service.removeItem(id);
                refreshTable();
                updateTotals();
                System.out.println("[Trip] removed id=" + id);
            } catch (RuntimeException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "remove failed: " + ex.getMessage());
            }
        }
    }

    // reload table rows from db
    private void refreshTable() {
        model.setRowCount(0);
        if (currentTrip == null) return;

        List<TripItem> items = service.listItems(currentTrip.id);
        for (TripItem t : items) {
            String exp = (t.expectedPriceCents == null) ? "-" : Money.formatNTD(t.expectedPriceCents);
            model.addRow(new Object[] {
                    t.id, t.itemName, t.unit, t.plannedQty, exp, Money.formatNTD(t.lineTotalCents)
            });
        }
        System.out.println("[Trip] refreshTable -> " + items.size() + " rows");
    }

    // recompute subtotal + remaining and color
    private void updateTotals() {
        if (currentTrip == null) {
            subtotalLabel.setText("Subtotal: NT$0.00");
            remainingLabel.setText("Remaining: NT$0.00");
            remainingLabel.setForeground(Color.BLACK);
            return;
        }
        int subtotal = service.computeSubtotalCents(currentTrip.id);
        subtotalLabel.setText("Subtotal: " + Money.formatNTD(subtotal));

        int remaining = currentTrip.budgetCents - subtotal;
        remainingLabel.setText("Remaining: " + Money.formatNTD(remaining));
        remainingLabel.setForeground(remaining < 0 ? new Color(180,0,0) : new Color(0,130,0));
    }
}
