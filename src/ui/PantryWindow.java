package ui; // Define the package containing Swing UI classes for the application

import service.SqlitePantryService; // Import the service responsible for pantry CRUD operations
import model.PantryItem; // Import the data model representing pantry records

import javax.swing.JButton; // Import JButton for toolbar actions
import javax.swing.JFrame; // Import JFrame as the base class for the window
import javax.swing.JLabel; // Import JLabel for simple text labels in forms
import javax.swing.JOptionPane; // Import JOptionPane for dialog prompts
import javax.swing.JPanel; // Import JPanel to group form fields
import javax.swing.JScrollPane; // Import JScrollPane to provide scrolling for the table
import javax.swing.JTable; // Import JTable to display pantry data in tabular form
import javax.swing.JTextField; // Import JTextField for simple input fields
import javax.swing.ListSelectionModel; // Import ListSelectionModel to control selection mode
import javax.swing.table.DefaultTableModel; // Import DefaultTableModel to manage table data
import java.awt.BorderLayout; // Import BorderLayout to arrange toolbar and table
import java.awt.FlowLayout; // Import FlowLayout for the toolbar panel
import java.awt.GridLayout; // Import GridLayout for the add/edit forms
import java.util.List; // Import List to work with collections of PantryItem

public class PantryWindow extends JFrame { // Define the window used to manage pantry items

    private final SqlitePantryService service = new SqlitePantryService(); // Instantiate the SQLite-backed pantry service used by this window

    private final DefaultTableModel model = new DefaultTableModel( // Create a table model describing the pantry columns
            new Object[]{"ID", "Name", "Category", "On-hand", "Unit", "Min", "Expiry"}, 0 // Define the column headers and initial row count of zero
    ) { // Begin anonymous subclass customizing the table model
        @Override public boolean isCellEditable(int r, int c) { return false; } // Prevent direct editing of cells within the table
        @Override public Class<?> getColumnClass(int c) { return (c == 0 || c == 3 || c == 5) ? Integer.class : String.class; } // Provide column classes so sorting and rendering behave correctly
    }; // End of the customized DefaultTableModel definition

    private final JTable table = new JTable(model); // Create a JTable bound to the defined model to display pantry items

    public PantryWindow() { // Construct and initialize the pantry management window
        setTitle("Pantry"); // Set the window title shown in the frame header
        setDefaultCloseOperation(DISPOSE_ON_CLOSE); // Dispose only this window when closed, keeping the application running
        setSize(920, 560); // Set the initial size large enough to show the table comfortably
        setLocationRelativeTo(null); // Center the window on screen for usability

        JButton btnAdd = new JButton("Add"); // Create a button for adding new pantry items
        JButton btnEdit = new JButton("Edit"); // Create a button for editing the selected item
        JButton btnDel = new JButton("Delete"); // Create a button for deleting the selected item
        JButton btnLow = new JButton("Low Stock"); // Create a button for filtering items at or below minimum quantity
        JButton btnSoon = new JButton("Expiring ≤ 3 days"); // Create a button for filtering items expiring soon
        JButton btnAll = new JButton("Show All"); // Create a button to reload the full list of items

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Create a top toolbar panel with left-aligned buttons
        top.add(new JLabel("Pantry actions: ")); // Add a label describing the toolbar purpose
        top.add(btnAdd); // Add the add button to the toolbar
        top.add(btnEdit); // Add the edit button to the toolbar
        top.add(btnDel); // Add the delete button to the toolbar
        top.add(btnLow); // Add the low-stock button to the toolbar
        top.add(btnSoon); // Add the expiring-soon button to the toolbar
        top.add(btnAll); // Add the show-all button to the toolbar

        add(top, BorderLayout.NORTH); // Place the toolbar panel at the top of the window
        add(new JScrollPane(table), BorderLayout.CENTER); // Place the table in the center wrapped with a scroll pane

        btnAdd.addActionListener(e -> onAdd()); // Wire the add button to the onAdd handler method
        btnEdit.addActionListener(e -> onEdit()); // Wire the edit button to the onEdit handler
        btnDel.addActionListener(e -> onDelete()); // Wire the delete button to the onDelete handler
        btnLow.addActionListener(e -> load(service.lowStock())); // Load low-stock items when the low button is pressed
        btnSoon.addActionListener(e -> load(service.expiringSoon(3))); // Load items expiring within three days when requested
        btnAll.addActionListener(e -> load(service.listAll())); // Reload all items when the show-all button is pressed

        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Restrict selection to a single row for clarity

        load(service.listAll()); // Populate the table initially with all pantry items
    } // End PantryWindow constructor

    private void load(List<PantryItem> items) { // Replace the table contents with the provided list of pantry items
        model.setRowCount(0); // Clear any existing rows from the table model
        for (PantryItem p : items) { // Iterate over each pantry item to add to the table
            model.addRow(new Object[]{ // Append a new row containing the pantry item fields of interest
                    p.id, p.name, p.category, p.onHandQty, p.unit, p.minQty, // Insert identifier, name, category, quantity, unit, and minimum values
                    (p.expiry == null ? "" : p.expiry) // Display an empty string when no expiry is set, otherwise show the text value
            }); // End row addition
        } // End loop over pantry items
        System.out.println("[Pantry] loaded rows = " + items.size()); // Log how many rows were loaded for debugging
    } // End load method

    private Integer selectedId() { // Obtain the ID of the currently selected table row, or null when nothing is selected
        int r = table.getSelectedRow(); // Retrieve the index of the selected row from the table
        if (r < 0) return null; // Return null when no row is selected
        return (Integer) model.getValueAt(r, 0); // Return the ID stored in the first column of the selected row
    } // End selectedId helper

    private void onAdd() { // Display a form to add a new pantry item and persist it when confirmed
        JTextField name = new JTextField(16); // Create a field for the item name with preferred width
        JTextField cat = new JTextField(12); // Create a field for the category text
        JTextField qty = new JTextField(6); // Create a field for the on-hand quantity
        JTextField unit = new JTextField(8); // Create a field for the unit text
        JTextField min = new JTextField(6); // Create a field for the minimum quantity threshold
        JTextField exp = new JTextField(10); // Create a field for the expiry date in YYYY-MM-DD format

        JPanel p = new JPanel(new GridLayout(0, 2, 8, 6)); // Create a form panel with two columns and spacing
        p.add(new JLabel("Name:")); p.add(name); // Add the name label and field to the form
        p.add(new JLabel("Category:")); p.add(cat); // Add the category label and field
        p.add(new JLabel("On-hand:")); p.add(qty); // Add the on-hand quantity label and field
        p.add(new JLabel("Unit:")); p.add(unit); // Add the unit label and field
        p.add(new JLabel("Min qty:")); p.add(min); // Add the minimum quantity label and field
        p.add(new JLabel("Expiry:")); p.add(exp); // Add the expiry label and field

        int ok = JOptionPane.showConfirmDialog(this, p, "Add Pantry Item", // Show a confirmation dialog containing the form
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE); // Present OK and Cancel buttons with a plain style
        if (ok != JOptionPane.OK_OPTION) return; // Abort if the user cancelled the dialog

        try { // Attempt to create and persist the new pantry item from the provided inputs
            PantryItem x = new PantryItem(); // Instantiate a new PantryItem model object
            x.name = name.getText().trim(); // Capture the entered name without surrounding whitespace
            x.category = cat.getText().trim(); // Capture the category text trimmed of whitespace
            x.onHandQty = Integer.parseInt(qty.getText().trim()); // Parse the on-hand quantity as an integer
            x.unit = unit.getText().trim(); // Capture the unit text trimmed of whitespace
            x.minQty = Integer.parseInt(min.getText().trim()); // Parse the minimum quantity as an integer
            String ex = exp.getText().trim(); // Retrieve the expiry text for later processing
            x.expiry = ex.isEmpty() ? null : ex; // Store null when expiry is blank, otherwise keep the provided text

            service.add(x); // Persist the new pantry item using the service
            load(service.listAll()); // Refresh the table to include the new item
            System.out.println("[Pantry] added: " + x.name); // Log the addition for debugging

        } catch (Exception ex2) { // Handle validation or persistence errors during addition
            ex2.printStackTrace(); // Print the stack trace to help diagnose the issue
            JOptionPane.showMessageDialog(this, "check inputs: qty/min must be whole numbers; date as YYYY-MM-DD"); // Inform the user that the input was invalid
        } // End catch block for add errors
    } // End onAdd method

    private void onEdit() { // Display a form to edit the currently selected pantry item
        Integer id = selectedId(); // Determine which row is selected
        if (id == null) { JOptionPane.showMessageDialog(this, "select a row first"); return; } // Require the user to choose a row before editing

        PantryItem curr = service.listAll().stream() // Retrieve the full list and stream over it to locate the selected item
                .filter(p -> p.id.equals(id)).findFirst().orElse(null); // Find the first item whose ID matches the selection
        if (curr == null) return; // Abort if the item could not be found (unlikely but defensive)

        JTextField name = new JTextField(curr.name, 16); // Prepopulate the name field with the existing value
        JTextField cat = new JTextField(curr.category, 12); // Prepopulate the category field
        JTextField qty = new JTextField(String.valueOf(curr.onHandQty), 6); // Prepopulate the quantity field
        JTextField unit = new JTextField(curr.unit, 8); // Prepopulate the unit field
        JTextField min = new JTextField(String.valueOf(curr.minQty), 6); // Prepopulate the minimum quantity field
        JTextField exp = new JTextField(curr.expiry == null ? "" : curr.expiry, 10); // Prepopulate the expiry field with empty text when null

        JPanel p = new JPanel(new GridLayout(0, 2, 8, 6)); // Create the edit form panel similar to the add form
        p.add(new JLabel("Name:")); p.add(name); // Add the name label and field
        p.add(new JLabel("Category:")); p.add(cat); // Add the category label and field
        p.add(new JLabel("On-hand:")); p.add(qty); // Add the on-hand label and field
        p.add(new JLabel("Unit:")); p.add(unit); // Add the unit label and field
        p.add(new JLabel("Min qty:")); p.add(min); // Add the minimum label and field
        p.add(new JLabel("Expiry:")); p.add(exp); // Add the expiry label and field

        int ok = JOptionPane.showConfirmDialog(this, p, "Edit Pantry Item", // Display the edit dialog for confirmation
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE); // Use the same options and styling as the add dialog
        if (ok != JOptionPane.OK_OPTION) return; // Abort if the user cancelled the edit

        try { // Attempt to update the pantry item with the new values
            PantryItem x = new PantryItem(); // Instantiate a PantryItem to send to the service
            x.id = id; // Preserve the existing ID so the service updates the correct row
            x.name = name.getText().trim(); // Capture the updated name
            x.category = cat.getText().trim(); // Capture the updated category
            x.onHandQty = Integer.parseInt(qty.getText().trim()); // Parse the updated quantity
            x.unit = unit.getText().trim(); // Capture the updated unit
            x.minQty = Integer.parseInt(min.getText().trim()); // Parse the updated minimum quantity
            String ex = exp.getText().trim(); // Retrieve the updated expiry text
            x.expiry = ex.isEmpty() ? null : ex; // Store null when blank, otherwise keep the provided text

            service.update(x); // Persist the changes via the service
            load(service.listAll()); // Refresh the table to reflect updates
            System.out.println("[Pantry] updated: " + x.name); // Log the update for debugging

        } catch (Exception ex2) { // Handle validation or persistence errors during update
            ex2.printStackTrace(); // Print the stack trace to diagnose the error
            JOptionPane.showMessageDialog(this, "check inputs: qty/min must be whole numbers; date as YYYY-MM-DD"); // Notify the user of invalid input
        } // End catch block for edit errors
    } // End onEdit method

    private void onDelete() { // Delete the currently selected pantry item after confirmation
        Integer id = selectedId(); // Determine which row is selected for deletion
        if (id == null) { JOptionPane.showMessageDialog(this, "select a row first"); return; } // Require selection before deleting

        if (JOptionPane.showConfirmDialog(this, "delete this item?", "confirm", // Ask the user to confirm the deletion
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) { // Proceed only if the user selects Yes
            try { // Attempt to remove the item via the service
                service.delete(id); // Delete the pantry item from the database
                load(service.listAll()); // Refresh the table to remove the deleted row
                System.out.println("[Pantry] deleted id=" + id); // Log the deletion for auditing
            } catch (RuntimeException ex) { // Catch runtime exceptions thrown by the service
                ex.printStackTrace(); // Print diagnostic information
                JOptionPane.showMessageDialog(this, "delete failed: " + ex.getMessage()); // Inform the user that deletion failed
            } // End catch block for delete errors
        } // End conditional handling the user's confirmation choice
    } // End onDelete method
} // End PantryWindow class definition
