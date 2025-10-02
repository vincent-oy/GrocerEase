package service;

import model.PantryItem;
import java.util.List;

/**
 * PantryService describes what pantry operations we can do.
 * The UI only knows this interface, not how the database works.
 * This is an example of "abstraction" in OOP.
 */
public interface PantryService extends AutoCloseable {

    // Read all pantry rows (sorted by name)
    List<PantryItem> listAll();

    // Items where on_hand_qty <= min_qty
    List<PantryItem> lowStock();

    // Items whose expiry is on or before (today + days)
    List<PantryItem> expiringSoon(int days);

    // Insert a new pantry item and return it (with id filled)
    PantryItem add(PantryItem p);

    // Update an existing pantry item (id must not be null)
    PantryItem update(PantryItem p);

    // Delete by id, return true if something was deleted
    boolean delete(int id);

    // From AutoCloseable (nothing to close here, but it's tidy)
    @Override
    void close();
}