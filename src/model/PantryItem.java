package model;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Represents an item in our home pantry.
 * Example: Milk (unit "bottle", onHandQty 2, minQty 1, expiry 2026-01-10)
 */
public class PantryItem {

    public Integer id;          // set after insert
    public String  name;        // required
    public String  category;    // e.g., Dairy, Fruit, Snacks
    public int     onHandQty;   // >= 0
    public String  unit;        // e.g., pack, bottle, kg
    public LocalDate expiry;    // null if non-perishable
    public int     minQty;      // threshold for "low stock" (>= 0)
    public Instant updatedAt;   // when last changed (ISO-8601)

    public PantryItem() { }
}
