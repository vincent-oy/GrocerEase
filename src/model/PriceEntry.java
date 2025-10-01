package model;

import java.time.Instant;

/**
 * A price record for (store, itemName).
 * We keep the latest one to estimate trip budgets.
 */
public class PriceEntry {

    public Integer id;           // primary key
    public int storeId;          // which store this price belongs to
    public String itemName;      // item name string (e.g., "Milk 1L")
    public int priceCents;       // store money as cents to avoid rounding issues
    public Instant updatedAt;    // when this price was added

    public PriceEntry() { }
}

}
