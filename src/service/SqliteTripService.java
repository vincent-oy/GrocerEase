package service;

import app.Db;
import app.DbMigrator;
import model.Trip;
import model.TripItem;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite implementation of TripService.
 * Keeps budget calculations simple and easy to follow.
 */
public class SqliteTripService implements TripService {

    public SqliteTripService() {
        DbMigrator.migrate();
    }

    @Override
    public Trip create(LocalDate date, Integer storeId, int budgetCents, String note) {

        if (date == null) {
            throw new IllegalArgumentException("date is required");
        }
        if (budgetCents < 0) {
            throw new IllegalArgumentException("budget cannot be negative");
        }

        String sql = "INSERT INTO trips(trip_date, store_id, budget_cents, note) VALUES (?,?,?,?)";

        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, date.toString());

            if (storeId == null) {
                ps.setNull(2, Types.INTEGER);
            } else {
                ps.setInt(2, storeId);
            }

            ps.setInt(3, budgetCents);
            ps.setString(4, note);

            ps.executeUpdate();

            Trip t = new Trip();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    t.id = keys.getInt(1);
                }
            }

            t.tripDate = date;
            t.storeId = storeId;
            t.budgetCents = budgetCents;
            t.note = note;

            return t;

        } catch (Exception e) {
            throw new RuntimeException("create trip failed: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Trip> listAll() {
        String sql = "SELECT * FROM trips ORDER BY trip_date DESC, id DESC";

        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Trip> out = new ArrayList<>();

            while (rs.next()) {
                Trip t = new Trip();

                t.id = rs.getInt("id");
                t.tripDate = LocalDate.parse(rs.getString("trip_date"));

                int s = rs.getInt("store_id");
                t.storeId = rs.wasNull() ? null : s;

                t.budgetCents = rs.getInt("budget_cents");
                t.note = rs.getString("note");

                out.add(t);
            }

            return out;

        } catch (Exception e) {
            throw new RuntimeException("listAll trips failed: " + e.getMessage(), e);
        }
    }

    private TripItem mapItem(ResultSet rs) throws Exception {

        TripItem ti = new TripItem();

        ti.id = rs.getInt("id");
        ti.tripId = rs.getInt("trip_id");
        ti.itemName = rs.getString("item_name");
        ti.unit = rs.getString("unit");
        ti.plannedQty = rs.getInt("planned_qty");

        int p = rs.getInt("expected_price_cents");
        ti.expectedPriceCents = rs.wasNull() ? null : p;

        ti.lineTotalCents = rs.getInt("line_total_cents");

        return ti;
    }

    @Override
    public List<TripItem> listItems(int tripId) {
        String sql = "SELECT * FROM trip_items WHERE trip_id=? ORDER BY id";

        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, tripId);

            try (ResultSet rs = ps.executeQuery()) {
                List<TripItem> out = new ArrayList<>();
                while (rs.next()) {
                    out.add(mapItem(rs));
                }
                return out;
            }

        } catch (Exception e) {
            throw new RuntimeException("listItems failed: " + e.getMessage(), e);
        }
    }

    @Override
    public TripItem addItem(int tripId, String itemName, String unit, int qty, Integer expectedPriceCents) {

        if (qty <= 0) {
            throw new IllegalArgumentException("qty must be > 0");
        }

        int lineTotal = (expectedPriceCents == null) ? 0 : expectedPriceCents * qty;

        String sql = "INSERT INTO trip_items(trip_id, item_name, unit, planned_qty, expected_price_cents, line_total_cents) "
                   + "VALUES (?,?,?,?,?,?)";

        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, tripId);
            ps.setString(2, itemName);
            ps.setString(3, unit);
            ps.setInt(4, qty);

            if (expectedPriceCents == null) {
                ps.setNull(5, Types.INTEGER);
            } else {
                ps.setInt(5, expectedPriceCents);
            }

            ps.setInt(6, lineTotal);

            ps.executeUpdate();

            TripItem ti = new TripItem();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    ti.id = keys.getInt(1);
                }
            }

            ti.tripId = tripId;
            ti.itemName = itemName;
            ti.unit = unit;
            ti.plannedQty = qty;
            ti.expectedPriceCents = expectedPriceCents;
            ti.lineTotalCents = lineTotal;

            return ti;

        } catch (Exception e) {
            throw new RuntimeException("addItem failed: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean updateItemQty(int tripItemId, int newQty) {

        if (newQty <= 0) {
            throw new IllegalArgumentException("qty must be > 0");
        }

        // Recalculate line total = newQty * expected_price (or 0 if price unknown)
        String sql = "UPDATE trip_items "
                   + "SET planned_qty=?, "
                   + "    line_total_cents = COALESCE(expected_price_cents, 0) * ? "
                   + "WHERE id=?";

        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, newQty);
            ps.setInt(2, newQty);
            ps.setInt(3, tripItemId);

            int n = ps.executeUpdate();
            return n > 0;

        } catch (Exception e) {
            throw new RuntimeException("updateItemQty failed: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean removeItem(int tripItemId) {
        String sql = "DELETE FROM trip_items WHERE id=?";

        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, tripItemId);
            int n = ps.executeUpdate();
            return n > 0;

        } catch (Exception e) {
            throw new RuntimeException("removeItem failed: " + e.getMessage(), e);
        }
    }

    @Override
    public int computeSubtotalCents(int tripId) {
        String sql = "SELECT COALESCE(SUM(line_total_cents), 0) AS subtotal FROM trip_items WHERE trip_id=?";

        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, tripId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("subtotal") : 0;
            }

        } catch (Exception e) {
            throw new RuntimeException("computeSubtotal failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        // nothing to close (we open/close per method)
    }
}
