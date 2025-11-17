package model;

/*
    each trip has date (as text), budget, and optional note
 */
public class Trip {
    public Integer id;
    public String  tripDateText; // keep as text for simplicity
    public Integer storeId;      // not used here but db has it (Maybe extension)
    public int     budgetCents;  // ex. 12345 = NT$123.45
    public String  note;         // Optional free text

    //Debug helper
    @Override
    public String toString() {
        return "Trip{id=" + id + ", date=" + tripDateText + ", budgetCents=" + budgetCents + "}";
    }
}
