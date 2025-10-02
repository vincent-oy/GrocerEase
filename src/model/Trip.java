package model;

/*
 * trip data (student style)
 * each trip has date (as text), budget, and optional note
 */
public class Trip {
    public Integer id;
    public String  tripDateText; // keep as text for simplicity
    public Integer storeId;      // not really used here, but db has it
    public int     budgetCents;
    public String  note;

    @Override
    public String toString() {
        return "Trip{id=" + id + ", date=" + tripDateText + ", budgetCents=" + budgetCents + "}";
    }
}
