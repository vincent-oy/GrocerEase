package model; // Define the package containing pantry data models

public class PantryItem { // Represent a single pantry item row persisted in SQLite
    public Integer id; // Primary key assigned by the database or null before insertion
    public String name; // Human-readable item name such as "Eggs"
    public String category; // Optional grouping category like "Dairy"
    public int onHandQty; // Quantity currently available in inventory
    public String unit; // Optional unit description such as "dozen"
    public String expiry; // Optional ISO-8601 date string tracking expiry (YYYY-MM-DD)
    public int minQty; // Minimum quantity threshold before the item is considered low stock
    public String updatedAt; // Timestamp string recording the last modification moment

    @Override // Indicate that we are overriding Object.toString
    public String toString() { // Provide a human-friendly representation useful for debugging
        return "PantryItem{id=" + id + ", name=" + name + ", qty=" + onHandQty + "}"; // Build a concise summary string using key fields
    } // End toString override
} // End PantryItem class definition
