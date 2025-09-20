package service;

import model.InventoryItem;
import java.time.LocalDate;
import java.util.List;

public interface InventoryService {
    List<InventoryItem> listAll();
    void add(String name, int qty, LocalDate exp);
    void update(int id, String name, int qty, LocalDate exp);
    void delete(int id);
}
