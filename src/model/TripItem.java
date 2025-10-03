package model; // Define the package containing trip item data models

public class TripItem { // Represent a single item planned for purchase within a trip
    public Integer id; // Primary key assigned after the row is inserted into the database
    public int tripId; // Foreign key referencing the owning trip record
    public String itemName; // Name describing the product to buy
    public String unit; // Optional unit string such as "kg" or "box"
    public int plannedQty; // Quantity planned for purchase
    public Integer expectedPriceCents; // Optional expected unit price in cents (null when unknown)
    public int lineTotalCents; // Total planned cost for the item, typically quantity multiplied by expected price

    @Override // Indicate that we are overriding Object.toString
    public String toString() { // Provide a readable representation of the trip item for debugging
        return "TripItem{id=" + id + ", item=" + itemName + ", qty=" + plannedQty + "}"; // Compose a concise summary of the key fields
    } // End toString override
} // End TripItem class definition
