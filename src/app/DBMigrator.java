package app;

import java.sql.Connection;
import java.sql.Statement;

/**
 * DBMigrator creates our database tables if they don't already exist.
 * Safe to call multiple times. Each service calls migrate() in its constructor.
 */
public final class DBMigrator {

    private DBMigrator() { }

    public static void migrate() {

        // ----- Pantry items (home inventory) -----
        String pantrySql = """
            CREATE TABLE IF NOT EXISTS pantry_items (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              name TEXT NOT NULL,
              category TEXT,
              on_hand_qty INTEGER NOT NULL CHECK(on_hand_qty >= 0),
              unit TEXT,
              expiry TEXT,
              min_qty INTEGER NOT NULL DEFAULT 0 CHECK(min_qty >= 0),
              updated_at TEXT NOT NULL
            );
            CREATE INDEX IF NOT EXISTS idx_pantry_name ON pantry_items(name);
            """;

        // ----- Stores (for optional price book) -----
        String storesSql = """
            CREATE TABLE IF NOT EXISTS stores (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              name TEXT UNIQUE NOT NULL
            );
            """;

        // ----- Price entries (store + item -> last price) -----
        String priceSql = """
            CREATE TABLE IF NOT EXISTS price_entries (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              store_id INTEGER NOT NULL REFERENCES stores(id) ON DELETE CASCADE,
              item_name TEXT NOT NULL,
              price_cents INTEGER NOT NULL CHECK(price_cents >= 0),
              updated_at TEXT NOT NULL
            );
            CREATE INDEX IF NOT EXISTS idx_price_item_store
              ON price_entries(item_name, store_id);
            """;

        // ----- Trips (planned shopping runs) -----
        String tripsSql = """
            CREATE TABLE IF NOT EXISTS trips (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              trip_date TEXT NOT NULL,           -- YYYY-MM-DD
              store_id INTEGER REFERENCES stores(id),
              budget_cents INTEGER NOT NULL CHECK(budget_cents >= 0),
              note TEXT
            );
            """;

        // ----- Trip items (lines on a trip) -----
        String tripItemsSql = """
            CREATE TABLE IF NOT EXISTS trip_items (
              id INTEGER PRIMARY KEY AUTOINCREMENT,
              trip_id INTEGER NOT NULL REFERENCES trips(id) ON DELETE CASCADE,
              item_name TEXT NOT NULL,
              unit TEXT,
              planned_qty INTEGER NOT NULL CHECK(planned_qty > 0),
              expected_price_cents INTEGER,
              line_total_cents INTEGER NOT NULL
            );
            CREATE INDEX IF NOT EXISTS idx_trip_items_trip ON trip_items(trip_id);
            """;

        // Execute DDL
        try (Connection c = Db.open(); Statement st = c.createStatement()) {
            st.executeUpdate(pantrySql);
            st.executeUpdate(storesSql);
            st.executeUpdate(priceSql);
            st.executeUpdate(tripsSql);
            st.executeUpdate(tripItemsSql);
        } catch (Exception e) {
            throw new RuntimeException("Database migration failed: " + e.getMessage(), e);
        }
    }
}
