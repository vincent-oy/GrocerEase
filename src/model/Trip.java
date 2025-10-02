package model;

import java.time.LocalDate;

/** A planned shopping trip with a date and a budget. */
public class Trip {
    public Integer  id;
    public LocalDate tripDate;
    public Integer  storeId;       // may be null
    public int      budgetCents;   // store money in cents
    public String   note;
}
