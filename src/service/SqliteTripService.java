package service;

import app.DBMigrator;
import app.Db;
import model.Trip;
import model.TripItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/*
 * sqlite trip "service" (student style):
 * - constructor runs DBMigrator
 * - methods are direct + a bit repetitive (typical coursework)
 * - i store the date as TEXT (YYYY-MM-DD), not a LocalDate in the model
 */
public class SqliteTripService {

    public SqliteTripService() {
        DBMigrator.migrate();
    }

    // create a new trip row and return the Trip object (with generated id)
    public Trip create(String dateText, Integer storeId, int budgetCents, String note) {
        String sql = "INSERT INTO trips(trip_date, store_id, budget_cents, note) VALUES (?,?,?,?)";

        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, dateText);
            if (storeId == null) ps.setNull(2, Types.INTEGER); else ps.setInt(2, storeId);
            ps.setInt(3, Math.max(0, budgetCents));
            ps.setString(4, note == null ? "" : note);
            ps.executeUpdate();

            Trip t = new Trip();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) t.id = keys.getInt(1);
            }
            t.tripDateText = dateText;
            t.storeId = storeId;
            t.budgetCents = Math.max(0, budgetCents);
            t.note = note == null ? "" : note;
            return t;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("create trip failed");
        }
    }

    // list items for a trip id
    public List<TripItem> listItems(int tripId) {
        String sql = "SELECT * FROM trip_items WHERE trip_id=? ORDER BY id";
        List<TripItem> out = new ArrayList<>();

        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, tripId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    TripItem ti = new TripItem();
                    ti.id = rs.getInt("id");
                    ti.tripId = rs.getInt("trip_id");
                    ti.itemName = rs.getString("item_name");
                    ti.unit = rs.getString("unit");
                    ti.plannedQty = rs.getInt("planned_qty");
                    int px = rs.getInt("expected_price_cents");
                    ti.expectedPriceCents = rs.wasNull() ? null : px;
                    ti.lineTotalCents = rs.getInt("line_total_cents");
                    out.add(ti);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("list items failed");
        }
        return out;
    }

    // add item into trip_items (price can be null = unknown)
    public TripItem addItem(int tripId, String itemName, String unit, int qty, Integer expectedPriceCents) {
        if (qty <= 0) throw new IllegalArgumentException("qty must be > 0");

        int lineTotal = (expectedPriceCents == null) ? 0 : expectedPriceCents * qty;

        String sql = "INSERT INTO trip_items(trip_id, item_name, unit, planned_qty, expected_price_cents, line_total_cents) " +
                     "VALUES (?,?,?,?,?,?)";

        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, tripId);
            ps.setString(2, itemName);
            ps.setString(3, emptyToNull(unit));
            ps.setInt(4, qty);

            if (expectedPriceCents == null) ps.setNull(5, Types.INTEGER);
            else                            ps.setInt(5, expectedPriceCents);

            ps.setInt(6, lineTotal);
            ps.executeUpdate();

            TripItem ti = new TripItem();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) ti.id = keys.getInt(1);
            }
            ti.tripId = tripId;
            ti.itemName = itemName;
            ti.unit = unit;
            ti.plannedQty = qty;
            ti.expectedPriceCents = expectedPriceCents;
            ti.lineTotalCents = lineTotal;

            return ti;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("add item failed");
        }
    }

    // change quantity and recompute line total
    public void updateItemQty(int tripItemId, int newQty) {
        if (newQty <= 0) throw new IllegalArgumentException("qty must be > 0");

        String sql = "UPDATE trip_items " +
                     "SET planned_qty=?, line_total_cents = COALESCE(expected_price_cents, 0) * ? " +
                     "WHERE id=?";

        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, newQty);
            ps.setInt(2, newQty);
            ps.setInt(3, tripItemId);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("update qty failed");
        }
    }

    // remove item
    public void removeItem(int tripItemId) {
        String sql = "DELETE FROM trip_items WHERE id=?";
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, tripItemId);
            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("remove item failed");
        }
    }

    // sum of line_total_cents for a trip (if none, 0)
    public int computeSubtotalCents(int tripId) {
        String sql = "SELECT COALESCE(SUM(line_total_cents), 0) FROM trip_items WHERE trip_id=?";
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, tripId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
                return 0;
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("subtotal failed");
        }
    }

    // small helper to keep DB clean
    private String emptyToNull(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
