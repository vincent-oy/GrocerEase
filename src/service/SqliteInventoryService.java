package service;

import model.InventoryItem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Inventory service backed by an on-disk SQLite database. The service is
 * intentionally light-weight and uses direct JDBC calls so it can run without
 * any additional frameworks.
 */
public class SqliteInventoryService implements InventoryService {

    private final Path databasePath;
    private final String jdbcUrl;

    public SqliteInventoryService(Path databasePath) {
        this.databasePath = databasePath.toAbsolutePath();
        this.jdbcUrl = "jdbc:sqlite:" + this.databasePath;

        ensureParentDirectoryExists();
        initialiseSchema();
    }

    private void ensureParentDirectoryExists() {
        Path parent = databasePath.getParent();
        if (parent == null) {
            return;
        }
        try {
            Files.createDirectories(parent);
        } catch (IOException ex) {
            throw new IllegalStateException(
                    "Unable to prepare directory for SQLite database at " + databasePath + ": " + ex.getMessage(),
                    ex
            );
        }
    }

    private void initialiseSchema() {
        String sql = """
                CREATE TABLE IF NOT EXISTS inventory (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL,
                    quantity INTEGER NOT NULL,
                    expiry TEXT NOT NULL
                )
                """;

        try (Connection conn = open(); Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException ex) {
            throw new IllegalStateException(
                    "Failed to initialise inventory database at " + databasePath + ": " + ex.getMessage(),
                    ex
            );
        }
    }

    private Connection open() throws SQLException {
        return DriverManager.getConnection(jdbcUrl);
    }

    @Override
    public List<InventoryItem> listAll() {
        String sql = "SELECT id, name, quantity, expiry FROM inventory ORDER BY id";

        try (Connection conn = open();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<InventoryItem> items = new ArrayList<>();
            while (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                int quantity = rs.getInt("quantity");
                String expiry = rs.getString("expiry");
                LocalDate date = expiry == null || expiry.isBlank() ? null : LocalDate.parse(expiry);
                items.add(new InventoryItem(id, name, quantity, date));
            }
            return items;
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to read inventory from SQLite: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void add(String name, int qty, LocalDate exp) {
        String sql = "INSERT INTO inventory(name, quantity, expiry) VALUES (?, ?, ?)";

        try (Connection conn = open(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, qty);
            ps.setString(3, exp != null ? exp.toString() : null);
            ps.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to add inventory item to SQLite: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void update(int id, String name, int qty, LocalDate exp) {
        String sql = "UPDATE inventory SET name = ?, quantity = ?, expiry = ? WHERE id = ?";

        try (Connection conn = open(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, qty);
            ps.setString(3, exp != null ? exp.toString() : null);
            ps.setInt(4, id);

            int updated = ps.executeUpdate();
            if (updated == 0) {
                throw new IllegalArgumentException("No inventory record exists with id " + id);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to update inventory item " + id + " in SQLite: " + ex.getMessage(), ex);
        }
    }

    @Override
    public void delete(int id) {
        String sql = "DELETE FROM inventory WHERE id = ?";

        try (Connection conn = open(); PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);

            int deleted = ps.executeUpdate();
            if (deleted == 0) {
                throw new IllegalArgumentException("No inventory record exists with id " + id);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Failed to delete inventory item " + id + " from SQLite: " + ex.getMessage(), ex);
        }
    }
}
