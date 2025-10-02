package model;

import java.time.Instant;

/** A price record for (store, itemName). We keep latest to estimate trip budgets. */
public class PriceEntry {
    public Integer id;
    public int     storeId;
    public String  itemName;
    public int     priceCents;
    public Instant updatedAt;
}
