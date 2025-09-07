package model;

import java.time.LocalDate;

public class InventoryItem {
    private final int id;
    private String name;
    private int quantity;
    private LocalDate expiry;

    public InventoryItem(int id, String name, int quantity, LocalDate expiry) {
        this.id = id; this.name = name; this.quantity = quantity; this.expiry = expiry;
    }
    public int getId() { return id; }
    public String getName() { return name; }
    public int getQuantity() { return quantity; }
    public LocalDate getExpiry() { return expiry; }
    public void setName(String n) { name = n; }
    public void setQuantity(int q) { quantity = q; }
    public void setExpiry(LocalDate d) { expiry = d; }
}
