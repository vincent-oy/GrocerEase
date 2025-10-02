package model;

/*
 * pantry item data (just fields, student style)
 * i don’t use getters/setters, just public fields
 */
public class PantryItem {
    public Integer id;       // auto id from DB
    public String  name;     // "Eggs"
    public String  category; // e.g. "Dairy"
    public int     onHandQty;
    public String  unit;     // "dozen"
    public String  expiry;   // stored as TEXT "YYYY-MM-DD" (simple)
    public int     minQty;   // minimum before low stock
    public String  updatedAt;// iso string

    @Override
    public String toString() {
        return "PantryItem{id=" + id + ", name=" + name + ", qty=" + onHandQty + "}";
    }
}
