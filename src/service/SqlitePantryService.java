package service;

import app.DBMigrator;
import app.Db;
import model.PantryItem;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/*
 * pantry sqlite "service" (student style):
 * - constructor runs DBMigrator once
 * - methods are simple and a bit repetitive on purpose (typical coursework style)
 * - expiry kept as TEXT; updated_at is Instant ISO string
 */
public class SqlitePantryService {

    public SqlitePantryService() {
        // make sure tables exist
        DBMigrator.migrate();
    }

    // list everything ordered by name
    public List<PantryItem> listAll() {
        String sql = "SELECT * FROM pantry_items ORDER BY name";
        List<PantryItem> out = new ArrayList<>();

        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                PantryItem p = new PantryItem();
                p.id        = rs.getInt("id");
                p.name      = rs.getString("name");
                p.category  = rs.getString("category");
                p.onHandQty = rs.getInt("on_hand_qty");
                p.unit      = rs.getString("unit");
                p.expiry    = rs.getString("expiry"); // may be null
                p.minQty    = rs.getInt("min_qty");
                p.updatedAt = rs.getString("updated_at"); // ISO text
                out.add(p);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("pantry list failed");
        }
        return out;
    }

    // items where on_hand <= min
    public List<PantryItem> lowStock() {
        String sql = "SELECT * FROM pantry_items WHERE on_hand_qty <= min_qty ORDER BY name";
        List<PantryItem> out = new ArrayList<>();

        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                PantryItem p = new PantryItem();
                p.id        = rs.getInt("id");
                p.name      = rs.getString("name");
                p.category  = rs.getString("category");
                p.onHandQty = rs.getInt("on_hand_qty");
                p.unit      = rs.getString("unit");
                p.expiry    = rs.getString("expiry");
                p.minQty    = rs.getInt("min_qty");
                p.updatedAt = rs.getString("updated_at");
                out.add(p);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("low stock query failed");
        }
        return out;
    }

    // expiry <= today+days (string compare ok for YYYY-MM-DD)
    public List<PantryItem> expiringSoon(int days) {
        // very simple way: compute yyyy-mm-dd string in java, compare as text
        java.time.LocalDate limit = java.time.LocalDate.now().plusDays(days);
        String limitStr = limit.toString();

        String sql = "SELECT * FROM pantry_items WHERE expiry IS NOT NULL AND expiry <= ? ORDER BY expiry ASC";
        List<PantryItem> out = new ArrayList<>();

        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, limitStr);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PantryItem p = new PantryItem();
                    p.id        = rs.getInt("id");
                    p.name      = rs.getString("name");
                    p.category  = rs.getString("category");
                    p.onHandQty = rs.getInt("on_hand_qty");
                    p.unit      = rs.getString("unit");
                    p.expiry    = rs.getString("expiry");
                    p.minQty    = rs.getInt("min_qty");
                    p.updatedAt = rs.getString("updated_at");
                    out.add(p);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("expiring soon query failed");
        }
        return out;
    }

    // add a new item (basic validation)
    public PantryItem add(PantryItem p) {
        if (p == null)                   throw new IllegalArgumentException("null item");
        if (p.name == null || p.name.isBlank()) throw new IllegalArgumentException("name required");
        if (p.onHandQty < 0)             throw new IllegalArgumentException("qty cannot be negative");
        if (p.minQty < 0)                throw new IllegalArgumentException("min cannot be negative");

        String sql = "INSERT INTO pantry_items(name, category, on_hand_qty, unit, expiry, min_qty, updated_at) " +
                     "VALUES (?,?,?,?,?,?,?)";

        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.name.trim());
            ps.setString(2, emptyToNull(p.category));
            ps.setInt(3, p.onHandQty);
            ps.setString(4, emptyToNull(p.unit));
            if (p.expiry == null || p.expiry.isBlank()) ps.setNull(5, Types.VARCHAR); else ps.setString(5, p.expiry);
            ps.setInt(6, p.minQty);
            ps.setString(7, Instant.now().toString());

            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) p.id = keys.getInt(1);
            }
            return p;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("add failed");
        }
    }

    // update by id
    public PantryItem update(PantryItem p) {
        if (p == null || p.id == null || p.id <= 0) throw new IllegalArgumentException("bad id");
        if (p.name == null || p.name.isBlank())     throw new IllegalArgumentException("name required");

        String sql = "UPDATE pantry_items SET name=?, category=?, on_hand_qty=?, unit=?, expiry=?, min_qty=?, updated_at=? " +
                     "WHERE id=?";

        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, p.name.trim());
            ps.setString(2, emptyToNull(p.category));
            ps.setInt(3, Math.max(0, p.onHandQty));
            ps.setString(4, emptyToNull(p.unit));
            if (p.expiry == null || p.expiry.isBlank()) ps.setNull(5, Types.VARCHAR); else ps.setString(5, p.expiry);
            ps.setInt(6, Math.max(0, p.minQty));
            ps.setString(7, Instant.now().toString());
            ps.setInt(8, p.id);

            int n = ps.executeUpdate();
            if (n == 0) throw new IllegalArgumentException("no row with id " + p.id);
            return p;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("update failed");
        }
    }

    // delete by id
    public boolean delete(int id) {
        String sql = "DELETE FROM pantry_items WHERE id=?";
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            int n = ps.executeUpdate();
            return n > 0;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("delete failed");
        }
    }

    // small helper: turn "" into null so db stays cleaner
    private String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
