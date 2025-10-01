package ui.trip;

import model.Trip;
import model.TripItem;
import service.TripService;
import util.Money;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.time.LocalDate;
import java.util.List;

/**
 * TripWindow lets the user create a new shopping trip with a budget,
 * add items to that trip with quantities and (optional) expected price,
 * and see a running subtotal vs the budget.
 */
public class TripWindow extends JFrame {

    // Service that talks to the database for trips/items
    private final TripService service;

    // The "current" trip the user is editing (null until created)
    private Trip currentTrip = null;

    // Input fields for the header section (date, budget, note)
    private final JTextField dateField   = new JTextField(10);
    private final JTextField budgetField = new JTextField(8);
    private final JTextField noteField   = new JTextField(16);

    // Table for items in the trip
    private final DefaultTableModel model = new DefaultTableModel(
            new Object[] { "ID", "Item", "Unit", "Qty", "Expected", "Line Total" }, 0
    ) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0 || columnIndex == 3) return Integer.class;
            return String.class;
        }
    };

    private final JTable table = new JTable(model);

    // Totals display
    private final JLabel subtotalLabel = new JLabel("Subtotal: NT$0.00");
    private final JLabel remainingLabel = new JLabel("Remaining: NT$0.00");

    public TripWindow(TripService svc) {
        this.service = svc;

        // Basic window setup
        setTitle("Plan a Shopping Trip");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(900, 560);
        setLocationRelativeTo(null);

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // ----- Header panel: date + budget + note + Create Trip button -----
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT));
        dateField.setText(LocalDate.now().toString());

        header.add(new JLabel("Date:"));
        header.add(dateField);

        header.add(new JLabel("Budget (NT$):"));
        header.add(budgetField);

        header.add(new JLabel("Note:"));
        header.add(noteField);

        JButton createTripBtn = new JButton("Create Trip");
        header.add(createTripBtn);

        add(header, BorderLayout.NORTH);

        // ----- Middle toolbar: actions on items -----
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addItemBtn = new JButton("Add Item");
        JButton changeQtyBtn = new JButton("Change Qty");
        JButton removeBtn = new JButton("Remove Item");
        JButton refreshBtn = new JButton("Refresh");
        toolbar.add(addItemBtn);
        toolbar.add(changeQtyBtn);
        toolbar.add(removeBtn);
        toolbar.add(refreshBtn);

        add(toolbar, BorderLayout.CENTER);
        add(new JScrollPane(table), BorderLayout.SOUTH);

        // ----- Footer totals -----
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 8));
        footer.add(subtotalLabel);
        footer.add(remainingLabel);
        add(footer, BorderLayout.PAGE_END);

        // ----- Button behaviors -----
        createTripBtn.addActionListener(e -> onCreateTrip());
        addItemBtn.addActionListener(e -> onAddItem());
        changeQtyBtn.addActionListener(e -> onChangeQty());
        removeBtn.addActionListener(e -> onRemove());
        refreshBtn.addActionListener(e -> {
            refreshTable();
            updateTotals();
        });
    }

    // Create a new trip from header inputs
    private void onCreateTrip() {
        try {
            LocalDate date = LocalDate.parse(dateField.getText().trim());
            int budgetCents = Money.parseCents(budgetField.getText().trim());
            String note = noteField.getText().trim();

            currentTrip = service.create(date, null, budgetCents, note);

            JOptionPane.showMessageDialog(this, "Trip created. Now add items.");
            refreshTable();
            updateTotals();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Please check Date (YYYY-MM-DD) and Budget (number).",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Add a new item to the current trip
    private void onAddItem() {
        if (currentTrip == null) {
            JOptionPane.showMessageDialog(this, "Please create a trip first.");
            return;
        }

        // Small ad-hoc form with 4 text fields
        JTextField name = new JTextField(14);
        JTextField unit = new JTextField(8);
        JTextField qty  = new JTextField(6);
        JTextField price = new JTextField(8);

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6));
        p.add(new JLabel("Item:"));   p.add(name);
        p.add(new JLabel("Unit:"));   p.add(unit);
        p.add(new JLabel("Qty:"));    p.add(qty);
        p.add(new JLabel("Expected price (NT$):")); p.add(price);

        int ok = JOptionPane.showConfirmDialog(
                this, p, "Add Trip Item", JOptionPane.OK_CANCEL_OPTION);

        if (ok == JOptionPane.OK_OPTION) {
            try {
                int q = Integer.parseInt(qty.getText().trim());
                Integer cents = price.getText().trim().isBlank()
                        ? null
                        : Money.parseCents(price.getText().trim());

                service.addItem(currentTrip.id, name.getText().trim(), unit.getText().trim(), q, cents);
                refreshTable();
                updateTotals();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Quantity must be a whole number. Price can be blank or a number.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // Change quantity of selected item
    private void onChangeQty() {
        Integer id = selectedItemId();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Please select a row.");
            return;
        }
        String s = JOptionPane.showInputDialog(this, "New quantity:", "1");
        if (s == null) return;

        try {
            int q = Integer.parseInt(s.trim());
            service.updateItemQty(id, q);
            refreshTable();
            updateTotals();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Quantity must be a positive whole number.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // Remove selected item
    private void onRemove() {
        Integer id = selectedItemId();
        if (id == null) {
            JOptionPane.showMessageDialog(this, "Please select a row.");
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this, "Remove this item?");
        if (confirm == JOptionPane.YES_OPTION) {
            service.removeItem(id);
            refreshTable();
            updateTotals();
        }
    }

    // Helper: currently selected table row's id (or null)
    private Integer selectedItemId() {
        int r = table.getSelectedRow();
        return (r < 0) ? null : (Integer) model.getValueAt(r, 0);
    }

    // Re-fill the table from the database for the current trip
    private void refreshTable() {
        model.setRowCount(0);
        if (currentTrip == null) return;

        List<TripItem> items = service.listItems(currentTrip.id);
        for (TripItem t : items) {
            String expected = (t.expectedPriceCents == null)
                    ? "—"
                    : Money.formatNTD(t.expectedPriceCents);
            model.addRow(new Object[]{
                    t.id,
                    t.itemName,
                    t.unit,
                    t.plannedQty,
                    expected,
                    Money.formatNTD(t.lineTotalCents)
            });
        }
    }

    // Update subtotal and remaining labels
    private void updateTotals() {
        if (currentTrip == null) return;

        int subtotal = service.computeSubtotalCents(currentTrip.id);
        subtotalLabel.setText("Subtotal: " + Money.formatNTD(subtotal));

        int remaining = currentTrip.budgetCents - subtotal;
        remainingLabel.setText("Remaining: " + Money.formatNTD(remaining));

        // Color the remaining amount: red if negative, green if positive
        remainingLabel.setForeground(remaining < 0 ? new Color(180, 0, 0) : new Color(0, 130, 0));
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
        java.awt.EventQueue.invokeLater(() -> new TripWindow().setVisible(true));
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
