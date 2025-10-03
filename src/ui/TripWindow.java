package ui; // Define the package containing Swing UI components for the application

import service.SqliteTripService; // Import the SQLite-backed service managing trips and trip items
import util.Money; // Import money helper utilities for parsing and formatting currency values
import model.Trip; // Import the Trip data model representing a shopping trip
import model.TripItem; // Import the TripItem model representing items planned for purchase

import javax.swing.JButton; // Import JButton for user-triggered actions
import javax.swing.JFrame; // Import JFrame as the base window class
import javax.swing.JLabel; // Import JLabel for displaying text
import javax.swing.JOptionPane; // Import JOptionPane for dialogs and prompts
import javax.swing.JPanel; // Import JPanel for grouping UI controls
import javax.swing.JScrollPane; // Import JScrollPane to provide scrolling for the items table
import javax.swing.JTable; // Import JTable to display trip items in tabular form
import javax.swing.JTextField; // Import JTextField for user input fields
import javax.swing.ListSelectionModel; // Import ListSelectionModel to configure selection behavior
import javax.swing.table.DefaultTableModel; // Import DefaultTableModel as the backing store for the table
import java.awt.BorderLayout; // Import BorderLayout to arrange header, table, and footer
import java.awt.Color; // Import Color to adjust label colors based on budget status
import java.awt.FlowLayout; // Import FlowLayout to align groups of controls
import java.util.List; // Import List for handling collections of TripItem

public class TripWindow extends JFrame { // Define the window used to plan grocery trips

    private final SqliteTripService service = new SqliteTripService(); // Instantiate the SQLite service responsible for trip persistence

    private Trip currentTrip = null; // Track the currently active trip, remaining null until created by the user

    private final JTextField dateField = new JTextField(10); // Input field capturing the trip date as text (YYYY-MM-DD)
    private final JTextField budgetField = new JTextField(8); // Input field capturing the trip budget as currency text
    private final JTextField noteField = new JTextField(16); // Input field capturing an optional note for the trip

    private final DefaultTableModel model = new DefaultTableModel( // Create a table model describing the trip item columns
            new Object[]{"ID", "Item", "Unit", "Qty", "Expected", "Line Total"}, 0 // Define table headers with zero initial rows
    ) { // Begin anonymous subclass customizing table behavior
        @Override public boolean isCellEditable(int r, int c) { return false; } // Prevent direct editing of table cells by the user
        @Override public Class<?> getColumnClass(int c) { return (c == 0 || c == 3) ? Integer.class : String.class; } // Provide column classes so sorting works appropriately
    }; // End customized table model
    private final JTable table = new JTable(model); // Create the JTable that will render trip items using the model

    private final JLabel subtotalLabel = new JLabel("Subtotal: NT$0.00"); // Label displaying the current subtotal for the trip
    private final JLabel remainingLabel = new JLabel("Remaining: NT$0.00"); // Label displaying remaining budget or overspend amount

    public TripWindow() { // Construct and initialize the trip planning window
        setTitle("Plan a Trip"); // Set the window title shown in the frame decoration
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // Dispose this window without terminating the whole application
        setSize(960, 580); // Set the initial window size to provide ample space for controls and table
        setLocationRelativeTo(null); // Center the window on screen for convenience

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Allow selection of only one trip item row at a time

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6)); // Create a header panel with left-aligned controls and spacing
        dateField.setText(java.time.LocalDate.now().toString()); // Initialize the date field with today's date in ISO format
        header.add(new JLabel("Date:")); header.add(dateField); // Add the date label and field to the header
        header.add(new JLabel("Budget (NT$):")); header.add(budgetField); // Add the budget label and field to the header
        header.add(new JLabel("Note:")); header.add(noteField); // Add the note label and field to the header
        JButton btnCreate = new JButton("Create Trip"); // Create a button to submit trip details and create a trip
        header.add(btnCreate); // Add the create button to the header panel

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6)); // Create a toolbar panel for item operations
        JButton btnAdd = new JButton("Add Item"); // Button to add an item to the current trip
        JButton btnQty = new JButton("Change Qty"); // Button to adjust the quantity of a selected item
        JButton btnDel = new JButton("Remove Item"); // Button to remove the selected item from the trip
        JButton btnRef = new JButton("Refresh"); // Button to reload data from the database
        bar.add(btnAdd); bar.add(btnQty); bar.add(btnDel); bar.add(btnRef); // Add all toolbar buttons to the panel in order

        JPanel north = new JPanel(new BorderLayout()); // Create a container panel combining header and toolbar for the north region
        north.add(header, BorderLayout.NORTH); // Place the header panel at the top of the container
        north.add(bar, BorderLayout.CENTER); // Place the toolbar directly beneath the header
        add(north, BorderLayout.NORTH); // Add the combined panel to the top of the window

        add(new JScrollPane(table), BorderLayout.CENTER); // Add the items table wrapped in a scroll pane to the center region

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT, 16, 6)); // Create a footer panel for subtotal and remaining labels
        footer.add(subtotalLabel); // Add the subtotal label to the footer
        footer.add(remainingLabel); // Add the remaining label to the footer
        add(footer, BorderLayout.SOUTH); // Place the footer panel at the bottom of the window

        btnCreate.addActionListener(e -> createTrip()); // Wire the create button to the createTrip handler
        btnAdd.addActionListener(e -> addItem()); // Wire the add item button to the addItem handler
        btnQty.addActionListener(e -> changeQty()); // Wire the change quantity button to the changeQty handler
        btnDel.addActionListener(e -> removeItem()); // Wire the remove item button to the removeItem handler
        btnRef.addActionListener(e -> { refreshTable(); updateTotals(); }); // Refresh data and totals when the refresh button is pressed
    } // End TripWindow constructor

    private void createTrip() { // Create a new trip using data from the header fields
        try { // Attempt to parse and validate the input values
            String dateText = dateField.getText().trim(); // Retrieve the trip date string entered by the user
            int budgetCents = Money.parseCents(budgetField.getText().trim()); // Convert the budget text into cents for storage
            String note = noteField.getText().trim(); // Retrieve the optional note text

            if (dateText.isEmpty()) { // Ensure the user provided a date value
                JOptionPane.showMessageDialog(this, "enter a date like YYYY-MM-DD"); // Prompt the user to enter a valid date
                return; // Abort creation when the date is missing
            } // End empty date check
            if (budgetCents < 0) { // Validate that the parsed budget is not negative
                JOptionPane.showMessageDialog(this, "budget must be >= 0"); // Inform the user about the invalid budget value
                return; // Abort creation when budget is invalid
            } // End negative budget check

            currentTrip = service.create(dateText, null, budgetCents, note); // Persist the trip using the service (storeId unused so pass null)
            JOptionPane.showMessageDialog(this, "trip created. now add items."); // Notify the user that the trip is ready for items

            refreshTable(); // Clear any previous items and load the empty state for the new trip
            updateTotals(); // Recalculate subtotal and remaining budget for the new trip

            System.out.println("[Trip] created id=" + currentTrip.id + " date=" + currentTrip.tripDateText + // Log trip creation for debugging
                    " budgetCents=" + currentTrip.budgetCents); // Continue the log message with budget information

        } catch (Exception ex) { // Handle parsing or persistence errors
            ex.printStackTrace(); // Print the stack trace to aid debugging
            JOptionPane.showMessageDialog(this, "check date and budget format"); // Inform the user that input validation failed
        } // End catch block for createTrip
    } // End createTrip method

    private void addItem() { // Prompt the user to add a new trip item
        if (currentTrip == null) { JOptionPane.showMessageDialog(this, "create a trip first"); return; } // Require an active trip before adding items

        JTextField name = new JTextField(12); // Field for the item name input
        JTextField unit = new JTextField(8); // Field for the unit input
        JTextField qty = new JTextField(6); // Field for the quantity input
        JTextField px = new JTextField(8); // Field for the expected price input (optional)

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 6)); // Create a compact panel holding item entry fields
        p.add(new JLabel("Item:")); p.add(name); // Add the item label and field to the panel
        p.add(new JLabel("Unit:")); p.add(unit); // Add the unit label and field to the panel
        p.add(new JLabel("Qty:")); p.add(qty); // Add the quantity label and field to the panel
        p.add(new JLabel("Expected price (NT$):")); p.add(px); // Add the expected price label and field to the panel

        int ok = JOptionPane.showConfirmDialog(this, p, "Add Item", JOptionPane.OK_CANCEL_OPTION); // Display the item entry dialog with OK/Cancel options
        if (ok != JOptionPane.OK_OPTION) return; // Abort if the user canceled the dialog

        try { // Attempt to validate and persist the new item
            String itemName = name.getText().trim(); // Retrieve the item name entered by the user
            String unitTxt = unit.getText().trim(); // Retrieve the unit string entered by the user
            if (itemName.isEmpty()) { // Ensure an item name was provided
                JOptionPane.showMessageDialog(this, "item name required"); // Inform the user that the name is mandatory
                return; // Abort addition when the name is missing
            } // End item name check
            int q = Integer.parseInt(qty.getText().trim()); // Parse the quantity as an integer
            if (q <= 0) { // Ensure the quantity is positive
                JOptionPane.showMessageDialog(this, "qty must be > 0"); // Inform the user about the invalid quantity
                return; // Abort addition when the quantity is non-positive
            } // End quantity check

            String pxText = px.getText().trim(); // Retrieve the expected price text entered by the user
            Integer priceCents = pxText.isEmpty() ? null : Money.parseCents(pxText); // Convert the expected price to cents or null when empty

            service.addItem(currentTrip.id, itemName, unitTxt, q, priceCents); // Persist the trip item associated with the current trip

            refreshTable(); // Reload table data to include the new item
            updateTotals(); // Update subtotal and remaining budget to reflect the new item

            System.out.println("[Trip] added item '" + itemName + "' q=" + q + // Log the addition for debugging purposes
                    " priceCents=" + (priceCents == null ? "null" : priceCents) + // Include expected price information in the log
                    " tripId=" + currentTrip.id); // Include the associated trip ID in the log message

        } catch (NumberFormatException nfe) { // Handle invalid numeric input for quantity or price
            JOptionPane.showMessageDialog(this, "qty must be a whole number"); // Inform the user about the numeric parsing issue
        } catch (RuntimeException ex) { // Handle service-level failures such as validation errors
            ex.printStackTrace(); // Print the stack trace for debugging
            JOptionPane.showMessageDialog(this, "add failed: " + ex.getMessage()); // Display the failure reason to the user
        } // End catch blocks for addItem
    } // End addItem method

    private void changeQty() { // Prompt the user to update the quantity of the selected item
        if (currentTrip == null) { JOptionPane.showMessageDialog(this, "create a trip first"); return; } // Ensure a trip exists before modifying items

        int r = table.getSelectedRow(); // Determine the currently selected row in the table
        if (r < 0) { JOptionPane.showMessageDialog(this, "select a row"); return; } // Require the user to select a row before changing quantity

        Integer id = (Integer) model.getValueAt(r, 0); // Retrieve the item ID from the first column of the selected row
        String s = JOptionPane.showInputDialog(this, "new qty:", "1"); // Prompt the user for a new quantity value
        if (s == null) return; // Abort if the user cancelled the input dialog

        try { // Attempt to parse and apply the new quantity
            int q = Integer.parseInt(s.trim()); // Parse the entered quantity string into an integer
            if (q <= 0) { JOptionPane.showMessageDialog(this, "qty must be > 0"); return; } // Validate the quantity remains positive

            service.updateItemQty(id, q); // Persist the new quantity using the service
            refreshTable(); // Reload table data to show updated totals
            updateTotals(); // Update subtotal and remaining budget to reflect the new quantity

            System.out.println("[Trip] changed qty id=" + id + " -> " + q); // Log the quantity change for debugging

        } catch (Exception e) { // Handle parsing or service errors
            e.printStackTrace(); // Print the stack trace to diagnose the issue
            JOptionPane.showMessageDialog(this, "invalid qty"); // Inform the user that the quantity change failed
        } // End catch block for changeQty
    } // End changeQty method

    private void removeItem() { // Remove the currently selected item from the trip
        if (currentTrip == null) { JOptionPane.showMessageDialog(this, "create a trip first"); return; } // Require an active trip before removing items

        int r = table.getSelectedRow(); // Determine the selected row in the table
        if (r < 0) { JOptionPane.showMessageDialog(this, "select a row"); return; } // Require selection before attempting removal
        Integer id = (Integer) model.getValueAt(r, 0); // Retrieve the item ID to remove

        if (JOptionPane.showConfirmDialog(this, "remove this item?") == JOptionPane.YES_OPTION) { // Confirm removal with the user
            try { // Attempt to delete the item using the service
                service.removeItem(id); // Remove the item from the database
                refreshTable(); // Reload table data to reflect the removal
                updateTotals(); // Update totals since the subtotal likely changed
                System.out.println("[Trip] removed id=" + id); // Log the removal for debugging
            } catch (RuntimeException ex) { // Handle service-level failures
                ex.printStackTrace(); // Print stack trace for troubleshooting
                JOptionPane.showMessageDialog(this, "remove failed: " + ex.getMessage()); // Inform the user about the failure
            } // End catch block for removal errors
        } // End conditional triggered when the user confirms removal
    } // End removeItem method

    private void refreshTable() { // Reload trip items from the database into the table model
        model.setRowCount(0); // Clear existing rows from the table model
        if (currentTrip == null) return; // Do nothing if no trip is active

        List<TripItem> items = service.listItems(currentTrip.id); // Retrieve the current trip's items from the service
        for (TripItem t : items) { // Iterate through each trip item to display
            String exp = (t.expectedPriceCents == null) ? "-" : Money.formatNTD(t.expectedPriceCents); // Format expected price or show a dash when unknown
            model.addRow(new Object[]{ // Append a row containing trip item details and formatted values
                    t.id, t.itemName, t.unit, t.plannedQty, exp, Money.formatNTD(t.lineTotalCents) // Insert ID, name, unit, quantity, expected price, and line total
            }); // End row addition
        } // End loop over trip items
        System.out.println("[Trip] refreshTable -> " + items.size() + " rows"); // Log the number of items loaded into the table
    } // End refreshTable method

    private void updateTotals() { // Recalculate subtotal and remaining budget labels
        if (currentTrip == null) { // When no trip exists, reset labels to zero
            subtotalLabel.setText("Subtotal: NT$0.00"); // Display zero subtotal for clarity
            remainingLabel.setText("Remaining: NT$0.00"); // Display zero remaining budget
            remainingLabel.setForeground(Color.BLACK); // Reset label color to neutral
            return; // Exit early since no further computation is needed
        } // End null trip check
        int subtotal = service.computeSubtotalCents(currentTrip.id); // Calculate the subtotal in cents via the service
        subtotalLabel.setText("Subtotal: " + Money.formatNTD(subtotal)); // Display the formatted subtotal value

        int remaining = currentTrip.budgetCents - subtotal; // Determine remaining budget by subtracting subtotal from planned budget
        remainingLabel.setText("Remaining: " + Money.formatNTD(remaining)); // Display the formatted remaining amount (negative indicates overspend)
        remainingLabel.setForeground(remaining < 0 ? new Color(180, 0, 0) : new Color(0, 130, 0)); // Use red when overspent and green otherwise
    } // End updateTotals method
} // End TripWindow class definition
