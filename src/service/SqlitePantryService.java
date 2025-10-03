package service; // Define the package for pantry-related data access services

import app.DBMigrator; // Import the migrator to ensure schema is ready before use
import app.Db; // Import the database helper used to open JDBC connections
import model.PantryItem; // Import the data model representing pantry items

import java.sql.Connection; // Import Connection for interacting with the database
import java.sql.PreparedStatement; // Import PreparedStatement for parameterized SQL commands
import java.sql.ResultSet; // Import ResultSet to iterate over query results
import java.sql.Statement; // Import Statement constants for returning generated keys
import java.sql.Types; // Import SQL type constants to set NULL values properly
import java.time.Instant; // Import Instant to record timestamps for updates
import java.util.ArrayList; // Import ArrayList to collect results
import java.util.List; // Import List as the method return type

public class SqlitePantryService { // Declare the service that performs pantry CRUD operations using SQLite

    public SqlitePantryService() { // Constructor ensures prerequisites are met before use
        DBMigrator.migrate(); // Run database migrations so required tables exist
    } // End constructor

    public List<PantryItem> listAll() { // Retrieve all pantry items ordered by name
        String sql = "SELECT * FROM pantry_items ORDER BY name"; // Define the SQL query used to fetch every row sorted alphabetically
        List<PantryItem> out = new ArrayList<>(); // Prepare a mutable list to store the resulting pantry items

        try (Connection c = Db.open(); // Open a database connection that will be closed automatically
             PreparedStatement ps = c.prepareStatement(sql); // Prepare the SQL statement for execution
             ResultSet rs = ps.executeQuery()) { // Execute the query and obtain a result set to iterate over

            while (rs.next()) { // Loop through each row returned by the query
                PantryItem p = new PantryItem(); // Create a new PantryItem instance to hold row data
                p.id = rs.getInt("id"); // Populate the identifier field from the result set
                p.name = rs.getString("name"); // Populate the name from the database column
                p.category = rs.getString("category"); // Populate the optional category value
                p.onHandQty = rs.getInt("on_hand_qty"); // Populate the quantity currently on hand
                p.unit = rs.getString("unit"); // Populate the measurement unit if provided
                p.expiry = rs.getString("expiry"); // Populate the optional expiration date string
                p.minQty = rs.getInt("min_qty"); // Populate the minimum desired quantity
                p.updatedAt = rs.getString("updated_at"); // Populate the last update timestamp stored as text
                out.add(p); // Add the hydrated pantry item to the results list
            } // End while loop processing all rows

        } catch (Exception e) { // Handle any SQL or connection errors encountered during the query
            e.printStackTrace(); // Print the stack trace to aid in debugging issues
            throw new RuntimeException("pantry list failed"); // Throw a runtime exception to signal failure to callers
        } // End catch block for query errors
        return out; // Return the populated list of pantry items to the caller
    } // End listAll method

    public List<PantryItem> lowStock() { // Retrieve items whose on-hand quantity is at or below the minimum threshold
        String sql = "SELECT * FROM pantry_items WHERE on_hand_qty <= min_qty ORDER BY name"; // Define the SQL filtering low-stock items
        List<PantryItem> out = new ArrayList<>(); // Prepare a list to accumulate the results

        try (Connection c = Db.open(); // Open a database connection
             PreparedStatement ps = c.prepareStatement(sql); // Prepare the low-stock query
             ResultSet rs = ps.executeQuery()) { // Execute the query and obtain the results

            while (rs.next()) { // Iterate through each matching row
                PantryItem p = new PantryItem(); // Create a new pantry item instance for the row
                p.id = rs.getInt("id"); // Populate the identifier from the database
                p.name = rs.getString("name"); // Populate the name field
                p.category = rs.getString("category"); // Populate the optional category value
                p.onHandQty = rs.getInt("on_hand_qty"); // Populate the current quantity on hand
                p.unit = rs.getString("unit"); // Populate the unit of measurement
                p.expiry = rs.getString("expiry"); // Populate the optional expiration value
                p.minQty = rs.getInt("min_qty"); // Populate the minimum target quantity
                p.updatedAt = rs.getString("updated_at"); // Populate the timestamp indicating last update
                out.add(p); // Append the item to the output list
            } // End while loop processing results

        } catch (Exception e) { // Handle exceptions during the low-stock query
            e.printStackTrace(); // Print diagnostic information for troubleshooting
            throw new RuntimeException("low stock query failed"); // Indicate failure to the caller
        } // End catch block for low-stock query errors
        return out; // Return the list of low-stock items
    } // End lowStock method

    public List<PantryItem> expiringSoon(int days) { // Retrieve items expiring within the next supplied number of days
        java.time.LocalDate limit = java.time.LocalDate.now().plusDays(days); // Calculate the cutoff date by adding the requested days to today
        String limitStr = limit.toString(); // Convert the cutoff date to ISO-8601 text for SQL comparison

        String sql = "SELECT * FROM pantry_items WHERE expiry IS NOT NULL AND expiry <= ? ORDER BY expiry ASC"; // Define SQL to fetch items with expirations on or before the cutoff
        List<PantryItem> out = new ArrayList<>(); // Prepare a list to hold the expiring items

        try (Connection c = Db.open(); // Open a database connection
             PreparedStatement ps = c.prepareStatement(sql)) { // Prepare the parameterized query for execution

            ps.setString(1, limitStr); // Bind the cutoff date string to the SQL parameter
            try (ResultSet rs = ps.executeQuery()) { // Execute the query and capture the results for iteration
                while (rs.next()) { // Iterate over each result row
                    PantryItem p = new PantryItem(); // Instantiate a pantry item to populate from the row
                    p.id = rs.getInt("id"); // Populate the identifier
                    p.name = rs.getString("name"); // Populate the name field
                    p.category = rs.getString("category"); // Populate the optional category
                    p.onHandQty = rs.getInt("on_hand_qty"); // Populate the quantity on hand
                    p.unit = rs.getString("unit"); // Populate the unit value
                    p.expiry = rs.getString("expiry"); // Populate the expiration text
                    p.minQty = rs.getInt("min_qty"); // Populate the minimum quantity threshold
                    p.updatedAt = rs.getString("updated_at"); // Populate the update timestamp text
                    out.add(p); // Add the expiring item to the list
                } // End while loop iterating over expiring results
            } // End try-with-resources for the ResultSet

        } catch (Exception e) { // Handle any SQL issues during the expiring-soon query
            e.printStackTrace(); // Print the stack trace for debugging purposes
            throw new RuntimeException("expiring soon query failed"); // Signal the failure to the caller
        } // End catch block for expiring query errors
        return out; // Return the list of items expiring soon
    } // End expiringSoon method

    public PantryItem add(PantryItem p) { // Insert a new pantry item record after basic validation
        if (p == null) throw new IllegalArgumentException("null item"); // Ensure the caller provided a pantry item instance
        if (p.name == null || p.name.isBlank()) throw new IllegalArgumentException("name required"); // Enforce that the name is present and non-empty
        if (p.onHandQty < 0) throw new IllegalArgumentException("qty cannot be negative"); // Prevent negative quantities on hand
        if (p.minQty < 0) throw new IllegalArgumentException("min cannot be negative"); // Prevent negative minimum threshold values

        String sql = "INSERT INTO pantry_items(name, category, on_hand_qty, unit, expiry, min_qty, updated_at) " + // Begin the INSERT statement specifying columns
                "VALUES (?,?,?,?,?,?,?)"; // Provide placeholders for each value to insert

        try (Connection c = Db.open(); // Open a database connection
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) { // Prepare the insert statement and request generated keys

            ps.setString(1, p.name.trim()); // Bind the trimmed item name to the first parameter
            ps.setString(2, emptyToNull(p.category)); // Bind the normalized category value, converting blanks to null
            ps.setInt(3, p.onHandQty); // Bind the quantity on hand to the third parameter
            ps.setString(4, emptyToNull(p.unit)); // Bind the unit, storing null when the value is blank
            if (p.expiry == null || p.expiry.isBlank()) ps.setNull(5, Types.VARCHAR); else ps.setString(5, p.expiry); // Bind the expiration string or NULL depending on input
            ps.setInt(6, p.minQty); // Bind the minimum quantity threshold
            ps.setString(7, Instant.now().toString()); // Bind the current timestamp as the updated_at value

            ps.executeUpdate(); // Execute the insert command to persist the new item

            try (ResultSet keys = ps.getGeneratedKeys()) { // Retrieve any generated keys to set the new item ID
                if (keys.next()) p.id = keys.getInt(1); // Assign the generated primary key back to the object if present
            } // End try-with-resources for generated keys
            return p; // Return the inserted pantry item with its identifier populated

        } catch (Exception e) { // Handle any SQL errors during insertion
            e.printStackTrace(); // Print the stack trace to assist debugging
            throw new RuntimeException("add failed"); // Signal failure to the caller via an unchecked exception
        } // End catch block for add operation errors
    } // End add method

    public PantryItem update(PantryItem p) { // Update an existing pantry item identified by its ID
        if (p == null || p.id == null || p.id <= 0) throw new IllegalArgumentException("bad id"); // Validate that a positive ID is supplied
        if (p.name == null || p.name.isBlank()) throw new IllegalArgumentException("name required"); // Ensure the updated item retains a name

        String sql = "UPDATE pantry_items SET name=?, category=?, on_hand_qty=?, unit=?, expiry=?, min_qty=?, updated_at=? " + // Define the update statement covering all mutable columns
                "WHERE id=?"; // Restrict the update to the row with the matching ID

        try (Connection c = Db.open(); // Open a database connection
             PreparedStatement ps = c.prepareStatement(sql)) { // Prepare the parameterized update statement

            ps.setString(1, p.name.trim()); // Bind the trimmed name to the first placeholder
            ps.setString(2, emptyToNull(p.category)); // Bind the normalized category value or null
            ps.setInt(3, Math.max(0, p.onHandQty)); // Bind the non-negative on-hand quantity to the third placeholder
            ps.setString(4, emptyToNull(p.unit)); // Bind the normalized unit value
            if (p.expiry == null || p.expiry.isBlank()) ps.setNull(5, Types.VARCHAR); else ps.setString(5, p.expiry); // Bind the expiry string or NULL as appropriate
            ps.setInt(6, Math.max(0, p.minQty)); // Bind the non-negative minimum quantity threshold
            ps.setString(7, Instant.now().toString()); // Bind the current timestamp for updated_at
            ps.setInt(8, p.id); // Bind the ID of the row to update

            int n = ps.executeUpdate(); // Execute the update and capture the number of affected rows
            if (n == 0) throw new IllegalArgumentException("no row with id " + p.id); // Throw if no row matched the provided ID
            return p; // Return the updated pantry item

        } catch (Exception e) { // Handle SQL errors encountered during the update
            e.printStackTrace(); // Print diagnostic information
            throw new RuntimeException("update failed"); // Propagate failure to callers
        } // End catch block for update errors
    } // End update method

    public boolean delete(int id) { // Delete a pantry item row by its identifier
        String sql = "DELETE FROM pantry_items WHERE id=?"; // Define the SQL command to remove the row
        try (Connection c = Db.open(); // Open a database connection
             PreparedStatement ps = c.prepareStatement(sql)) { // Prepare the delete statement

            ps.setInt(1, id); // Bind the target identifier to the SQL parameter
            int n = ps.executeUpdate(); // Execute the delete and capture the number of affected rows
            return n > 0; // Return true if a row was deleted, otherwise false

        } catch (Exception e) { // Handle exceptions during deletion
            e.printStackTrace(); // Print the stack trace for debugging
            throw new RuntimeException("delete failed"); // Inform callers that the delete operation failed
        } // End catch block for delete errors
    } // End delete method

    private String emptyToNull(String s) { // Convert blank strings to null to avoid storing empty text in the database
        if (s == null) return null; // Immediately return null when the input is already null
        String t = s.trim(); // Trim whitespace from the input string
        return t.isEmpty() ? null : t; // Return null when the trimmed string is empty, otherwise return the trimmed value
    } // End emptyToNull helper method
} // End SqlitePantryService class definition
