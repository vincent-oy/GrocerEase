package app;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Db is a tiny helper class whose only job is to
 *  1) decide where the SQLite file lives
 *  2) open a connection to it
 *
 * We keep it simple on purpose, to make it understandable.
 */
public final class Db {

    // Private constructor so nobody can create "new Db()"
    private Db() { }

    /**
     * Figures out which database file we should use.
     * Priority:
     *   A) If the user supplied a VM option -Dgrocerease.db="C:\path\file.db"
     *      then use that exact file.
     *   B) Otherwise, create/use "inventory.db" in the project working folder.
     */
    public static Path resolveDbPath() {

        // Read the Java "system property" named grocerease.db
        // (set this in NetBeans → Project Properties → Run → VM Options)
        String customPath = System.getProperty("grocerease.db");

        // If user set a custom path, use it
        if (customPath != null && !customPath.isBlank()) {
            return Paths.get(customPath);
        }

        // Fallback: keep the DB next to the project as "inventory.db"
        return Paths.get(System.getProperty("user.dir"), "inventory.db");
    }

    /**
     * Opens a brand new connection to SQLite.
     * We open/close per operation in services to keep things simple.
     */
    public static Connection open() throws SQLException {

        // Build a JDBC URL like: jdbc:sqlite:C:\...\inventory.db
        Path dbPath = resolveDbPath();
        String url = "jdbc:sqlite:" + dbPath.toAbsolutePath();

        // Ask the JDBC DriverManager to give us a live Connection
        // If the file doesn't exist yet, SQLite will create it.
        Connection conn = DriverManager.getConnection(url);

        // Return it to the caller (services will close it)
        return conn;
    }
}
