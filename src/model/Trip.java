package model;

import java.time.LocalDate;

/**
 * A planned shopping trip (date + budget + optional store + note).
 */
public class Trip {

    public Integer id;           // primary key
    public LocalDate tripDate;   // the day of the trip
    public Integer storeId;      // can be null if no store selected
    public int budgetCents;      // total budget in cents
    public String note;          // optional notes

    public Trip() { }
}
