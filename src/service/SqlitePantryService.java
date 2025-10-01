package service;

import app.Db;
import app.DbMigrator;
import model.PantryItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Concrete implementation of PantryService that stores data in SQLite.
 * Each method opens a connection, does its job, and closes the connection.
 * This is not the fastest in the world, but it is simple and easy to understand.
 */
public class SqlitePantryService implements PantryService {

    public SqlitePantryService() {
        // Make sure tables exist. If already created, nothing bad happens.
        DbMigrator.migrate();
    }

    // Convert a database row (ResultSet) into a PantryItem object
    private PantryItem map(ResultSet rs) throws Exception {

        PantryItem p = new PantryItem();

        p.id = rs.getInt("id");
        p.name = rs.getString("name");
        p.category = rs.getString("category");
        p.onHandQty = rs.getInt("on_hand_qty");
        p.unit = rs.getString("unit");

        String exp = rs.getString("expiry");
        p.expiry = (exp == null) ? null : LocalDate.parse(exp);

        p.minQty = rs.getInt("min_qty");
        p.updatedAt = Instant.parse(rs.getString("updated_at"));

        return p;
    }

    @Override
    public List<PantryItem> listAll() {
        String sql = "SELECT * FROM pantry_items ORDER BY name";
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<PantryItem> out = new ArrayList<>();
            while (rs.next()) {
                out.add(map(rs));
            }
            return out;

        } catch (Exception e) {
            throw new RuntimeException("listAll failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<PantryItem> lowStock() {
        String sql = "SELECT * FROM pantry_items WHERE on_hand_qty <= min_qty ORDER BY name";
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<PantryItem> out = new ArrayList<>();
            while (rs.next()) {
                out.add(map(rs));
            }
            return out;

        } catch (Exception e) {
            throw new RuntimeException("lowStock failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<PantryItem> expiringSoon(int days) {
        LocalDate limit = LocalDate.now().plusDays(days);
        String sql = "SELECT * FROM pantry_items WHERE expiry IS NOT NULL AND expiry <= ? ORDER BY expiry ASC";

        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, limit.toString());

            try (ResultSet rs = ps.executeQuery()) {
                List<PantryItem> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(map(rs));
                }
                return out;
            }

        } catch (Exception e) {
            throw new RuntimeException("expiringSoon failed: " + e.getMessage(), e);
        }
    }

    @Override
    public PantryItem add(PantryItem p) {

        validate(p);

        String sql = "INSERT INTO pantry_items(name, category, on_hand_qty, unit, expiry, min_qty, updated_at) "
                   + "VALUES (?,?,?,?,?,?,?)";

        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.name.trim());
            ps.setString(2, p.category);
            ps.setInt(3, Math.max(0, p.onHandQty));
            ps.setString(4, p.unit);
            ps.setString(5, p.expiry == null ? null : p.expiry.toString());
            ps.setInt(6, Math.max(0, p.minQty));
            ps.setString(7, Instant.now().toString());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    p.id = keys.getInt(1);
                }
            }

            return p;

        } catch (Exception e) {
            throw new RuntimeException("add failed: " + e.getMessage(), e);
        }
    }

    @Override
    public PantryItem update(PantryItem p) {

        if (p.id == null || p.id <= 0) {
            throw new IllegalArgumentException("Invalid id for update");
        }

        validate(p);

        String sql = "UPDATE pantry_items SET name=?, category=?, on_hand_qty=?, unit=?, expiry=?, min_qty=?, updated_at=? "
                   + "WHERE id=?";

        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, p.name.trim());
            ps.setString(2, p.category);
            ps.setInt(3, Math.max(0, p.onHandQty));
            ps.setString(4, p.unit);
            ps.setString(5, p.expiry == null ? null : p.expiry.toString());
            ps.setInt(6, Math.max(0, p.minQty));
            ps.setString(7, Instant.now().toString());
            ps.setInt(8, p.id);

            int changed = ps.executeUpdate();

            if (changed == 0) {
                throw new IllegalArgumentException("No pantry item with id " + p.id);
            }

            return p;

        } catch (Exception e) {
            throw new RuntimeException("update failed: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM pantry_items WHERE id=?";
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            int n = ps.executeUpdate();
            return n > 0;

        } catch (Exception e) {
            throw new RuntimeException("delete failed: " + e.getMessage(), e);
        }
    }

    // Very basic data checks to keep database clean
    private void validate(PantryItem p) {
        if (p == null) {
            throw new IllegalArgumentException("Item cannot be null");
        }
        if (p.name == null || p.name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }
        if (p.onHandQty < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        if (p.minQty < 0) {
            throw new IllegalArgumentException("Min quantity cannot be negative");
        }
    }

    @Override
    public void close() {
        // Nothing to close (we open/close connections per method).
    }
}
