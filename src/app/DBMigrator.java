package app;

import java.sql.Connection;
import java.sql.Statement;

/*
    Sets up tables when the code first runs
*/
public class DBMigrator {

    public static void migrate() {
        try (Connection c = Db.open(); Statement st = c.createStatement()) {    //run this to make sure all tables exist


            // pantry table --> used by SQlitePantryService & PantryWindow
            String pantrySql = "CREATE TABLE IF NOT EXISTS pantry_items (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL," +
                    "category TEXT," +
                    "on_hand_qty INTEGER NOT NULL," +
                    "unit TEXT," +
                    "expiry TEXT," +
                    "min_qty INTEGER NOT NULL DEFAULT 0," +
                    "updated_at TEXT" +
                    ")";
            st.executeUpdate(pantrySql);

            // trips --> Used by SQliteTripService & TripWindow
            String tripsSql = "CREATE TABLE IF NOT EXISTS trips (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "trip_date TEXT NOT NULL," +
                    "store_id INTEGER," +
                    "budget_cents INTEGER NOT NULL," +
                    "note TEXT" +
                    ")";
            st.executeUpdate(tripsSql);

            // trip items --> Used by SQliteTripService & TripWindow
            String tripItemsSql = "CREATE TABLE IF NOT EXISTS trip_items (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "trip_id INTEGER NOT NULL," +
                    "item_name TEXT NOT NULL," +
                    "unit TEXT," +
                    "planned_qty INTEGER NOT NULL," +
                    "expected_price_cents INTEGER," +
                    "line_total_cents INTEGER NOT NULL" +
                    ")";
            st.executeUpdate(tripItemsSql);

            System.out.println("[DB] migration ok");

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("db migration failed: " + e.getMessage());
        }
    }
}