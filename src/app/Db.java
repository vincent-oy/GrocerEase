package app; // Define the package that contains database utility classes

import java.sql.Connection; // Import JDBC Connection so we can return active database connections
import java.sql.DriverManager; // Import DriverManager to establish new JDBC connections

public class Db { // Declare a utility class responsible for opening database connections

    private static final String DEFAULT_PATH = "GrocerEase.db"; // Define the default SQLite database file path

    public static Connection open() { // Provide a helper method to obtain a connection to the SQLite database
        try { // Attempt to compute the target database path and open a connection
            String path = System.getProperty("dbPath", DEFAULT_PATH); // Read an optional JVM property for overriding the database location

            System.out.println("[DB] opening sqlite at: " + path); // Log the database path being used for easier troubleshooting

            return DriverManager.getConnection("jdbc:sqlite:" + path); // Create and return a JDBC connection to the SQLite database file

        } catch (Exception e) { // Capture any checked or runtime exception during connection creation
            e.printStackTrace(); // Print the stack trace to aid debugging of the failure
            throw new RuntimeException("could not open DB: " + e.getMessage()); // Convert the failure to an unchecked exception with context
        } // End catch block handling connection failures
    } // End open method definition
} // End Db class
