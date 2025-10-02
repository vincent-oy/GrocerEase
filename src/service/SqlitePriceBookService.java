package service;

import app.DBMigrator;
import app.Db;
import model.Store;

import java.sql.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** SQLite implementation of the price book. */
public class SqlitePriceBookService implements PriceBookService {

    public SqlitePriceBookService() {
        DBMigrator.migrate();
    }

    @Override
    public List<Store> listStores() {
        String sql = "SELECT * FROM stores ORDER BY name";
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            List<Store> out = new ArrayList<>();
            while (rs.next()) {
                Store s = new Store();
                s.id = rs.getInt("id");
                s.name = rs.getString("name");
                out.add(s);
            }
            return out;

        } catch (Exception e) {
            throw new RuntimeException("listStores failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Store addStore(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Store name required");
        String sql = "INSERT INTO stores(name) VALUES (?)";
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name.trim());
            ps.executeUpdate();

            Store s = new Store();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) s.id = keys.getInt(1);
            }
            s.name = name.trim();
            return s;

        } catch (Exception e) {
            throw new RuntimeException("addStore failed: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Integer> findLatestPriceCents(int storeId, String itemName) {
        String sql = "SELECT price_cents FROM price_entries WHERE store_id=? AND item_name=? "
                   + "ORDER BY updated_at DESC LIMIT 1";
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, storeId);
            ps.setString(2, itemName);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(rs.getInt("price_cents"));
                return Optional.empty();
            }

        } catch (Exception e) {
            throw new RuntimeException("findLatestPriceCents failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void upsertPrice(int storeId, String itemName, int priceCents) {
        if (priceCents < 0) throw new IllegalArgumentException("price must be >= 0");
        String sql = "INSERT INTO price_entries(store_id, item_name, price_cents, updated_at) VALUES (?,?,?,?)";
        try (Connection c = Db.open();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, storeId);
            ps.setString(2, itemName);
            ps.setInt(3, priceCents);
            ps.setString(4, Instant.now().toString());
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("upsertPrice failed: " + e.getMessage(), e);
        }
    }

    @Override public void close() { }
}
