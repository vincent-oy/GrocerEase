package service;

import model.Store;
import java.util.List;
import java.util.Optional;

/**
 * Optional service for keeping a simple "price book" per store.
 * This helps suggest expected prices when planning trips.
 */
public interface PriceBookService extends AutoCloseable {
    List<Store> listStores();
    Store addStore(String name);
    Optional<Integer> findLatestPriceCents(int storeId, String itemName);
    void upsertPrice(int storeId, String itemName, int priceCents);
    @Override void close();
}
