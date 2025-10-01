/*

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SQLiteJDBCExample {

    // Database URL
    private static final String DATABASE_URL = "jdbc:sqlite:sample.db";

    public static void main(String[] args) {
        // Create a new database connection
        try (Connection connection = DriverManager.getConnection(DATABASE_URL)) {
            if (connection != null) {
                System.out.println("Connected to the database.");

                // Create a new table
                createTable(connection);

                // Insert a record
                insertRecord(connection, "Sample Item", 10);

                // Query the records
                queryRecords(connection);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void createTable(Connection connection) {
        String sql = "CREATE TABLE IF NOT EXISTS items ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " name TEXT NOT NULL,"
                + " quantity INTEGER NOT NULL"
                + ");";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.execute();
            System.out.println("Table created.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void insertRecord(Connection connection, String name, int quantity) {
        String sql = "INSERT INTO items(name, quantity) VALUES(?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            pstmt.setInt(2, quantity);
            pstmt.executeUpdate();
            System.out.println("Record inserted.");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    private static void queryRecords(Connection connection) {
        String sql = "SELECT id, name, quantity FROM items";

        try (PreparedStatement pstmt = connection.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") + ", Name: " + rs.getString("name") + ", Quantity: " + rs.getInt("quantity"));
            }
        } catch (SQLException e) {
            System.out.println("Error querying records: " + e.getMessage());
        }
    }
}

/*