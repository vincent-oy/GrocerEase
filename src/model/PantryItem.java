package model;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Represents an item in our home pantry.
 * Example: "Milk", unit "bottle", onHandQty 2, expiry 2025-10-05
 */
public class PantryItem {

    public Integer id;                // primary key (null until inserted)

    public String name;               // item name (required)
    public String category;           // e.g., "Dairy", "Fruit"

    public int onHandQty;             // how many we have at home
    public String unit;               // e.g., "pack", "bottle", "kg"

    public LocalDate expiry;          // may be null (if non-perishable)
    public int minQty;                // threshold for "low stock"

    public Instant updatedAt;         // last time this row changed (ISO timestamp)

    public PantryItem() { }
}
