package app;

import java.sql.Connection;
import java.sql.DriverManager;

/*
 * this class just opens the database connection.
 * in IB words: this is the "data layer".
 * student-style = keep it really short and direct.
 */
public class Db {

    // location of the sqlite file (can be changed with VM option)
    private static final String DEFAULT_PATH = "GrocerEase.db";

    // open a connection
    public static Connection open() {
        try {
            // check if user supplied a custom path
            String path = System.getProperty("dbPath", DEFAULT_PATH);

            // debug print so i know where it’s connecting
            System.out.println("[DB] opening sqlite at: " + path);

            // connect
            return DriverManager.getConnection("jdbc:sqlite:" + path);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("could not open DB: " + e.getMessage());
        }
    }
}
