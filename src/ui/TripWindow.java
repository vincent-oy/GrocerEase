package ui;

import model.Trip;
import model.TripItem;
import service.TripService;
import util.Money;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * Trip planner window:
 *  - Create a trip with Date + Budget (+ optional note)
 *  - Add items with qty + expected price
 *  - See live Subtotal and Remaining vs Budget
 *
 * Layout notes (IMPORTANT for BorderLayout):
 *  - We combine header + toolbar into ONE "topPanel" and add it to NORTH.
 *  - The table (inside a JScrollPane) goes to CENTER so it can expand.
 *  - The footer (totals) goes to SOUTH (PAGE_END).
 */
public class TripWindow extends JFrame {

    // --- Service and state ---
    private final TripService service;
    private Trip currentTrip = null;  // stays null until user clicks "Create Trip"

    // --- Header inputs ---
    private final JTextField dateField   = new JTextField(10);
    private final JTextField budgetField = new JTextField(8);
    private final JTextField noteField   = new JTextField(16);

    // --- Table model & table ---
    private final DefaultTableModel model = new DefaultTableModel(
        new Object[] { "ID", "Item", "Unit", "Qty", "Expected", "Line Total" }, 0
    ) {
        @Override public boolean isCellEditable(int r, int c) { return false; }
        @Override public Class<?> getColumnClass(int c) { return (c==0||c==3) ? Integer.class : String.class; }
    };

    private final JTable table = new JTable(model);

    // --- Footer labels ---
    private final JLabel subtotalLabel  = new JLabel("Subtotal: NT$0.00");
    private final JLabel remainingLabel = new JLabel("Remaining: NT$0.00");

    public TripWindow(TripService svc) {
        this.service = svc;

        setTitle("Plan a Shopping Trip");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(960, 580);
        setLocationRelativeTo(null);

        // Table selection (one row at a time = simpler)
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // =========================
        // build HEADER (top row)
        // =========================
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dateField.setText(LocalDate.now().toString());       // default today
        header.add(new JLabel("Date:"));          header.add(dateField);
        header.add(new JLabel("Budget (NT$):"));  header.add(budgetField);
        header.add(new JLabel("Note:"));          header.add(noteField);
        JButton createBtn = new JButton("Create Trip");
        header.add(createBtn);

        // =========================
        // build TOOLBAR (second row)
        // =========================
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addBtn = new JButton("Add Item");
        JButton qtyBtn = new JButton("Change Qty");
        JButton rmBtn  = new JButton("Remove Item");
        JButton refBtn = new JButton("Refresh");
        toolbar.add(addBtn);
        toolbar.add(qtyBtn);
        toolbar.add(rmBtn);
        toolbar.add(refBtn);

        // =========================
        // stack HEADER + TOOLBAR at NORTH
        // (BorderLayout only allows one per region, so we combine)
        // =========================
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(header, BorderLayout.NORTH);
        topPanel.add(toolbar, BorderLayout.CENTER);
        add(topPanel, BorderLayout.NORTH);

        // =========================
        // TABLE in CENTER (so it grows)
        // =========================
        add(new JScrollPane(table), BorderLayout.CENTER);

        // =========================
        // FOOTER (totals) in SOUTH
        // =========================
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 6));
        footer.add(subtotalLabel);
        footer.add(remainingLabel);
        add(footer, BorderLayout.SOUTH);  // PAGE_END == SOUTH; use one of them, not both

        // =========================
        // Wire actions
        // =========================
        createBtn.addActionListener(e -> onCreateTrip());
        addBtn.addActionListener(e -> onAddItem());
        qtyBtn.addActionListener(e -> onChangeQty());
        rmBtn.addActionListener(e -> onRemove());
        refBtn.addActionListener(e -> { refreshTable(); updateTotals(); });
    }

    // === Create Trip: parse inputs → create → refresh ===
    private void onCreateTrip() {
        try {
            LocalDate date = LocalDate.parse(dateField.getText().trim()); // YYYY-MM-DD
            int budgetCents = Money.parseCents(budgetField.getText().trim());
            String note = noteField.getText().trim();

            currentTrip = service.create(date, null, budgetCents, note);
            JOptionPane.showMessageDialog(this, "Trip created. Now add items.");

            refreshTable();
            updateTotals();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Check Date (YYYY-MM-DD) and Budget (number).");
        }
    }

    // === Add Item: popup inputs → service.addItem → refresh ===
    private void onAddItem() {
        if (currentTrip == null) {
            JOptionPane.showMessageDialog(this, "Create a trip first (set Date + Budget, then click 'Create Trip').");
            return;
        }

        // Build a simple input panel
        JTextField nameField  = new JTextField(14);
        JTextField unitField  = new JTextField(8);
        JTextField qtyField   = new JTextField(6);
        JTextField priceField = new JTextField(8); // optional

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        p.add(new JLabel("Item:"));                 p.add(nameField);
        p.add(new JLabel("Unit:"));                 p.add(unitField);
        p.add(new JLabel("Qty:"));                  p.add(qtyField);
        p.add(new JLabel("Expected price (NT$):")); p.add(priceField);

        int ok = JOptionPane.showConfirmDialog(this, p, "Add Trip Item", JOptionPane.OK_CANCEL_OPTION);
        if (ok != JOptionPane.OK_OPTION) return;

        try {
            String name = nameField.getText().trim();
            String unit = unitField.getText().trim();
            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Item name is required.");
                return;
            }

            int qty = Integer.parseInt(qtyField.getText().trim());
            if (qty <= 0) {
                JOptionPane.showMessageDialog(this, "Quantity must be a positive whole number.");
                return;
            }

            String px = priceField.getText().trim();
            Integer priceCents = px.isEmpty() ? null : Money.parseCents(px);

            service.addItem(currentTrip.id, name, unit, qty, priceCents);

            // Always refresh after changes
            refreshTable();
            updateTotals();

            System.out.println("[TripWindow] Added item '" + name + "' qty=" + qty +
                    " priceCents=" + (priceCents == null ? "null" : priceCents) +
                    " for tripId=" + currentTrip.id);

        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Qty must be an integer (e.g., 1, 2, 3).");
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Trip Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    // === Change quantity for selected row ===
    private void onChangeQty() {
        Integer id = selectedItemId();
        if (id == null) { JOptionPane.showMessageDialog(this, "Select a row."); return; }

        String s = JOptionPane.showInputDialog(this, "New quantity:", "1");
        if (s == null) return;

        try {
            int q = Integer.parseInt(s.trim());
            if (q <= 0) { JOptionPane.showMessageDialog(this, "Quantity must be a positive whole number."); return; }

            service.updateItemQty(id, q);
            refreshTable();
            updateTotals();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Quantity must be a positive whole number.");
        }
    }

    // === Remove currently selected row ===
    private void onRemove() {
        Integer id = selectedItemId();
        if (id == null) { JOptionPane.showMessageDialog(this, "Select a row."); return; }

        if (JOptionPane.showConfirmDialog(this, "Remove this item?") == JOptionPane.YES_OPTION) {
            service.removeItem(id);
            refreshTable();
            updateTotals();
        }
    }

    // === Helper: get selected row ID (null if none) ===
    private Integer selectedItemId() {
        int r = table.getSelectedRow();
        return r < 0 ? null : (Integer) model.getValueAt(r, 0);
    }

    // === Reload table from DB for current trip ===
    private void refreshTable() {
        model.setRowCount(0);
        if (currentTrip == null) return;

        List<TripItem> items = service.listItems(currentTrip.id);
        for (TripItem t : items) {
            String expected = (t.expectedPriceCents == null) ? "—" : Money.formatNTD(t.expectedPriceCents);
            model.addRow(new Object[] {
                t.id,
                t.itemName,
                t.unit,
                t.plannedQty,
                expected,
                Money.formatNTD(t.lineTotalCents)
            });
        }

        // Debug: see how many rows we actually loaded
        System.out.println("[TripWindow] refreshTable: " + items.size() + " item(s) for tripId=" + currentTrip.id);
    }

    // === Recompute footer numbers and color ===
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
