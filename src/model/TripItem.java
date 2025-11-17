package model;

/*
    Belongs to a trip in tripid
 */
public class TripItem {
    public Integer id;
    public int     tripId;
    public String  itemName;
    public String  unit;
    public int     plannedQty;
    public Integer expectedPriceCents; // can be null if unknown
    public int     lineTotalCents;

    @Override
    public String toString() {
        return "TripItem{id=" + id + ", item=" + itemName + ", qty=" + plannedQty + "}";
    }
}
