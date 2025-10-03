package app;

import java.sql.Connection;                                                     // JDBC conncetion type
import java.sql.DriverManager;                                                  // opens JDBC URLs

/*
    Opening DB connection data layer
*/
public class Db {

    // location of the sqlite file (can be changed with VM option explained in commentary)
    private static final String DEFAULT_PATH = "GrocerEase.db";

    // open a connection
    public static Connection open() {
        try {
            String path = System.getProperty("dbPath", DEFAULT_PATH);           // check if user supplied a custom path

            System.out.println("[DB] opening sqlite at: " + path);              // debug print so i know where it’s connecting

            return DriverManager.getConnection("jdbc:sqlite:" + path);          // connect

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("could not open DB: " + e.getMessage());
        }
    }
}
