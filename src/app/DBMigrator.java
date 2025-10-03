package app; // Define the package containing database migration utilities

import java.sql.Connection; // Import JDBC Connection so we can interact with the database
import java.sql.Statement; // Import Statement to execute SQL commands that create tables

public class DBMigrator { // Declare a helper class responsible for preparing database schema

    public static void migrate() { // Provide a static method to run all required schema migrations
        try (Connection c = Db.open(); Statement st = c.createStatement()) { // Open a connection and create a statement using try-with-resources for automatic cleanup

            String pantrySql = "CREATE TABLE IF NOT EXISTS pantry_items (" + // Begin SQL for creating the pantry items table if it is missing
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," + // Define the primary key column that auto-increments
                    "name TEXT NOT NULL," + // Store the item name as required text
                    "category TEXT," + // Optionally record a category for grouping pantry items
                    "on_hand_qty INTEGER NOT NULL," + // Track the quantity currently on hand as a required integer
                    "unit TEXT," + // Optionally note the measurement unit for the quantity
                    "expiry TEXT," + // Optionally store expiration information as text
                    "min_qty INTEGER NOT NULL DEFAULT 0," + // Maintain a minimum desired quantity with a default of zero
                    "updated_at TEXT" + // Record the last update timestamp as text for simplicity
                    ")"; // Close the CREATE TABLE statement definition
            st.executeUpdate(pantrySql); // Execute the pantry table creation SQL statement

            String tripsSql = "CREATE TABLE IF NOT EXISTS trips (" + // Begin SQL for creating the trips table if it does not exist
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," + // Define the primary key for individual trips
                    "trip_date TEXT NOT NULL," + // Store the trip date as required text
                    "store_id INTEGER," + // Optionally reference a store identifier for the trip
                    "budget_cents INTEGER NOT NULL," + // Persist the planned budget in cents as a required integer
                    "note TEXT" + // Allow storing an optional note about the trip
                    ")"; // Terminate the CREATE TABLE definition
            st.executeUpdate(tripsSql); // Execute the trips table creation SQL command

            String tripItemsSql = "CREATE TABLE IF NOT EXISTS trip_items (" + // Begin SQL for creating the trip items table if it is absent
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," + // Provide an auto-incrementing identifier for each trip item
                    "trip_id INTEGER NOT NULL," + // Reference the parent trip that owns this item
                    "item_name TEXT NOT NULL," + // Store the name of the item to purchase
                    "unit TEXT," + // Optionally store the measurement unit for the planned quantity
                    "planned_qty INTEGER NOT NULL," + // Record the planned quantity as a required integer
                    "expected_price_cents INTEGER," + // Optionally track the expected unit price in cents
                    "line_total_cents INTEGER NOT NULL" + // Require a total planned cost for the line item in cents
                    ")"; // Finish the CREATE TABLE statement
            st.executeUpdate(tripItemsSql); // Execute the trip items table creation SQL command

            System.out.println("[DB] migration ok"); // Log that all migration steps completed successfully

        } catch (Exception e) { // Handle any SQL or connection issues encountered during migration
            e.printStackTrace(); // Print the stack trace to make debugging easier
            throw new RuntimeException("db migration failed: " + e.getMessage()); // Propagate an unchecked exception with context about the failure
        } // End catch block for migration errors
    } // End migrate method
} // End DBMigrator class definition
