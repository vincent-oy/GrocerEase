package model;

/*
    Pantry Item Data, just fields
 */
public class PantryItem {
    public Integer id;       // auto id from SQlite DB
    public String  name;     // ex. "Eggs"
    public String  category; // ex. "Dairy"
    public int     onHandQty;// ex. "5"
    public String  unit;     // "dozen"
    public String  expiry;   // TEXT store format: "YYYY-MM-DD"
    public int     minQty;   // minimum before low stock
    public String  updatedAt;// iso string

    @Override
    public String toString() {
        return "PantryItem{id=" + id + ", name=" + name + ", qty=" + onHandQty + "}";
    }
}
