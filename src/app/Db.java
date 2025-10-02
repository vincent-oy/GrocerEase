package app;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Db is a tiny helper class to:
 *  1) choose where the SQLite file lives
 *  2) open a JDBC connection to that file
 *
 * VM option (optional):
 *   -Dgrocerease.db="C:\\Users\\ruby_\\OneDrive\\Desktop\\GrocerEase.db"
 * If not provided, we use "inventory.db" in the project folder.
 */
public final class Db {

    private Db() { }

    /** Decide the DB file location. */
    public static Path resolveDbPath() {
        String custom = System.getProperty("grocerease.db");
        if (custom != null && !custom.isBlank()) {
            return Paths.get(custom);
        }
        return Paths.get(System.getProperty("user.dir"), "inventory.db");
    }

    /** Open a new connection. Caller closes it in a try-with-resources block. */
    public static Connection open() throws SQLException {
        Path dbPath = resolveDbPath();
        String url = "jdbc:sqlite:" + dbPath.toAbsolutePath();
        return DriverManager.getConnection(url);
    }
}
