package service;

import model.Trip;
import model.TripItem;

import java.time.LocalDate;
import java.util.List;

/**
 * TripService describes the operations for planning shopping trips.
 */
public interface TripService extends AutoCloseable {

    Trip create(LocalDate date, Integer storeId, int budgetCents, String note);

    List<Trip> listAll();

    List<TripItem> listItems(int tripId);

    TripItem addItem(int tripId, String itemName, String unit, int qty, Integer expectedPriceCents);

    boolean updateItemQty(int tripItemId, int newQty);

    boolean removeItem(int tripItemId);

    int computeSubtotalCents(int tripId);

    @Override
    void close();
}
