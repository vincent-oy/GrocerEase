package service; // Define the package for trip-related data access services

import app.DBMigrator; // Import the migrator to guarantee required tables exist before using them
import app.Db; // Import the database helper responsible for opening JDBC connections
import model.Trip; // Import the Trip data model representing shopping trips
import model.TripItem; // Import the TripItem model representing individual planned purchases

import java.sql.Connection; // Import JDBC Connection for interacting with the database
import java.sql.PreparedStatement; // Import PreparedStatement for parameterized SQL execution
import java.sql.ResultSet; // Import ResultSet to iterate over query results
import java.sql.Statement; // Import Statement constants for generated keys
import java.sql.Types; // Import SQL Types constants to set NULL values appropriately
import java.util.ArrayList; // Import ArrayList to collect query results
import java.util.List; // Import List as the collection interface for results

public class SqliteTripService { // Declare the service providing CRUD operations for trips and trip items

    public SqliteTripService() { // Constructor runs once when the service is instantiated
        DBMigrator.migrate(); // Ensure database schema is up to date before executing queries
    } // End constructor

    public Trip create(String dateText, Integer storeId, int budgetCents, String note) { // Create a new trip row and return the populated Trip object
        String sql = "INSERT INTO trips(trip_date, store_id, budget_cents, note) VALUES (?,?,?,?)"; // SQL insert statement defining columns and placeholders

        try (Connection c = Db.open(); // Open a database connection
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) { // Prepare the insert and request generated keys

            ps.setString(1, dateText); // Bind the required trip date text to the first placeholder
            if (storeId == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, storeId); // Bind the optional store identifier or NULL
            ps.setInt(3, Math.max(0, budgetCents)); // Bind a non-negative budget value in cents
            ps.setString(4, note == null ? "" : note); // Bind the trip note, defaulting to an empty string when null
            ps.executeUpdate(); // Execute the insert to persist the trip row

            Trip t = new Trip(); // Create a Trip instance to populate with stored values
            try (ResultSet keys = ps.getGeneratedKeys()) { // Retrieve generated keys from the insert operation
                if (keys.next()) t.id = keys.getInt(1); // Assign the generated trip ID if available
            } // End try-with-resources for generated keys
            t.tripDateText = dateText; // Set the trip date string on the returned object
            t.storeId = storeId; // Set the optional store identifier
            t.budgetCents = Math.max(0, budgetCents); // Store the sanitized budget value
            t.note = note == null ? "" : note; // Store the note using an empty string fallback
            return t; // Return the populated Trip to the caller

        } catch (Exception e) { // Handle any SQL or connection errors during trip creation
            e.printStackTrace(); // Print diagnostics for troubleshooting
            throw new RuntimeException("create trip failed"); // Inform callers that the create operation failed
        } // End catch block for create errors
    } // End create method

    public List<TripItem> listItems(int tripId) { // Retrieve all items associated with a specific trip
        String sql = "SELECT * FROM trip_items WHERE trip_id=? ORDER BY id"; // SQL query ordering items by insertion order
        List<TripItem> out = new ArrayList<>(); // Prepare a list to store the resulting trip items

        try (Connection c = Db.open(); // Open a database connection
             PreparedStatement ps = c.prepareStatement(sql)) { // Prepare the query for execution

            ps.setInt(1, tripId); // Bind the target trip identifier to the statement
            try (ResultSet rs = ps.executeQuery()) { // Execute the query and obtain the result set
                while (rs.next()) { // Iterate over each row returned for the trip
                    TripItem ti = new TripItem(); // Instantiate a TripItem model to populate
                    ti.id = rs.getInt("id"); // Read the unique identifier for the trip item
                    ti.tripId = rs.getInt("trip_id"); // Read the parent trip identifier
                    ti.itemName = rs.getString("item_name"); // Read the stored item name
                    ti.unit = rs.getString("unit"); // Read the optional unit value
                    ti.plannedQty = rs.getInt("planned_qty"); // Read the planned quantity to purchase
                    int px = rs.getInt("expected_price_cents"); // Read the expected price in cents, defaulting to zero when NULL
                    ti.expectedPriceCents = rs.wasNull() ? null : px; // Set expected price to null when the column was NULL
                    ti.lineTotalCents = rs.getInt("line_total_cents"); // Read the stored line total value
                    out.add(ti); // Append the populated TripItem to the results list
                } // End while loop iterating through result rows
            } // End try-with-resources managing the ResultSet

        } catch (Exception e) { // Handle SQL issues encountered while listing items
            e.printStackTrace(); // Print stack trace information for debugging
            throw new RuntimeException("list items failed"); // Propagate failure information to the caller
        } // End catch block for list errors
        return out; // Return the list of trip items for the specified trip
    } // End listItems method

    public TripItem addItem(int tripId, String itemName, String unit, int qty, Integer expectedPriceCents) { // Insert a new trip item row tied to a trip
        if (qty <= 0) throw new IllegalArgumentException("qty must be > 0"); // Validate that quantity is positive before inserting

        int lineTotal = (expectedPriceCents == null) ? 0 : expectedPriceCents * qty; // Compute line total in cents using expected price when provided

        String sql = "INSERT INTO trip_items(trip_id, item_name, unit, planned_qty, expected_price_cents, line_total_cents) " + // Begin insert statement specifying all columns
                "VALUES (?,?,?,?,?,?)"; // Provide placeholders for each column value

        try (Connection c = Db.open(); // Open a database connection
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) { // Prepare the insert and request generated keys

            ps.setInt(1, tripId); // Bind the parent trip identifier
            ps.setString(2, itemName); // Bind the required item name string
            ps.setString(3, emptyToNull(unit)); // Bind the normalized unit value, converting blanks to NULL
            ps.setInt(4, qty); // Bind the planned quantity to purchase

            if (expectedPriceCents == null) ps.setNull(5, Types.INTEGER); // Store NULL when no expected price is provided
            else ps.setInt(5, expectedPriceCents); // Otherwise store the provided expected price in cents

            ps.setInt(6, lineTotal); // Bind the computed line total for later reporting
            ps.executeUpdate(); // Execute the insert statement

            TripItem ti = new TripItem(); // Instantiate a TripItem to return to the caller
            try (ResultSet keys = ps.getGeneratedKeys()) { // Retrieve generated keys from the insert operation
                if (keys.next()) ti.id = keys.getInt(1); // Populate the trip item ID if one was generated
            } // End try-with-resources for generated keys
            ti.tripId = tripId; // Populate the parent trip identifier on the returned object
            ti.itemName = itemName; // Populate the item name value
            ti.unit = unit; // Populate the original unit string (may be null or blank)
            ti.plannedQty = qty; // Populate the planned quantity
            ti.expectedPriceCents = expectedPriceCents; // Populate the expected price as provided
            ti.lineTotalCents = lineTotal; // Populate the computed line total

            return ti; // Return the newly inserted trip item to the caller

        } catch (Exception e) { // Handle any SQL or connection issues during insertion
            e.printStackTrace(); // Print diagnostic output
            throw new RuntimeException("add item failed"); // Propagate a runtime exception indicating failure
        } // End catch block for add item errors
    } // End addItem method

    public void updateItemQty(int tripItemId, int newQty) { // Update the planned quantity for a specific trip item
        if (newQty <= 0) throw new IllegalArgumentException("qty must be > 0"); // Validate that the new quantity remains positive

        String sql = "UPDATE trip_items " + // Begin update statement targeting the trip_items table
                "SET planned_qty=?, line_total_cents = COALESCE(expected_price_cents, 0) * ? " + // Update quantity and recompute line total using expected price or zero
                "WHERE id=?"; // Apply the update to the row matching the provided identifier

        try (Connection c = Db.open(); // Open a database connection
             PreparedStatement ps = c.prepareStatement(sql)) { // Prepare the update statement for execution

            ps.setInt(1, newQty); // Bind the new planned quantity
            ps.setInt(2, newQty); // Bind the same quantity for use in the line total calculation
            ps.setInt(3, tripItemId); // Bind the identifier of the row to update
            ps.executeUpdate(); // Execute the update command

        } catch (Exception e) { // Handle SQL issues during the update
            e.printStackTrace(); // Print troubleshooting information
            throw new RuntimeException("update qty failed"); // Signal failure to the caller
        } // End catch block for quantity update errors
    } // End updateItemQty method

    public void removeItem(int tripItemId) { // Delete a trip item row from the database
        String sql = "DELETE FROM trip_items WHERE id=?"; // SQL statement removing the specified row
        try (Connection c = Db.open(); // Open a database connection
             PreparedStatement ps = c.prepareStatement(sql)) { // Prepare the delete statement

            ps.setInt(1, tripItemId); // Bind the identifier of the row to delete
            ps.executeUpdate(); // Execute the delete command

        } catch (Exception e) { // Handle SQL errors encountered during deletion
            e.printStackTrace(); // Print diagnostic information
            throw new RuntimeException("remove item failed"); // Propagate a runtime exception to the caller
        } // End catch block for remove errors
    } // End removeItem method

    public int computeSubtotalCents(int tripId) { // Compute the subtotal for a trip by summing line totals
        String sql = "SELECT COALESCE(SUM(line_total_cents), 0) FROM trip_items WHERE trip_id=?"; // SQL query summing line totals with a zero fallback
        try (Connection c = Db.open(); // Open a database connection
             PreparedStatement ps = c.prepareStatement(sql)) { // Prepare the aggregate query

            ps.setInt(1, tripId); // Bind the trip identifier used to filter rows
            try (ResultSet rs = ps.executeQuery()) { // Execute the query and obtain the aggregate result
                if (rs.next()) return rs.getInt(1); // Return the computed subtotal if a result row exists
                return 0; // Fallback to zero if no row is present, though aggregate queries should return one
            } // End try-with-resources for the ResultSet

        } catch (Exception e) { // Handle SQL errors during subtotal computation
            e.printStackTrace(); // Print stack trace information for debugging
            throw new RuntimeException("subtotal failed"); // Inform callers that subtotal computation failed
        } // End catch block for subtotal errors
    } // End computeSubtotalCents method

    private String emptyToNull(String s) { // Helper to convert blank strings to null values
        if (s == null) return null; // Immediately return null if the input is already null
        String t = s.trim(); // Trim surrounding whitespace from the input string
        return t.isEmpty() ? null : t; // Return null when trimmed string is empty, otherwise return the trimmed text
    } // End emptyToNull helper method
} // End SqliteTripService class definition
