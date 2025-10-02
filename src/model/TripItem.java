package model;

/**
 * One line in a shopping trip (what we plan to buy).
 */
public class TripItem {

    public Integer id;               // primary key
    public int tripId;               // which trip this belongs to

    public String itemName;          // e.g., "Milk 1L"
    public String unit;              // e.g., "bottle"

    public int plannedQty;           // how many we plan to buy

    public Integer expectedPriceCents;   // nullable if we don't know yet
    public int lineTotalCents;           // plannedQty * expectedPriceCents (0 if price unknown)

    public TripItem() { }            //???
}