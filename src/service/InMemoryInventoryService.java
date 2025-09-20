package service;

import model.InventoryItem;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class InMemoryInventoryService implements InventoryService {
    private static InMemoryInventoryService INSTANCE;
    public static InMemoryInventoryService shared() {
        if (INSTANCE == null) INSTANCE = new InMemoryInventoryService();
        return INSTANCE;
    }

    private final List<InventoryItem> data = new ArrayList<>();
    private final AtomicInteger seq = new AtomicInteger(1000);

    private InMemoryInventoryService() {
        data.add(new InventoryItem(seq.getAndIncrement(), "Milk", 2, LocalDate.now().plusDays(5)));
        data.add(new InventoryItem(seq.getAndIncrement(), "Bread", 1, LocalDate.now().plusDays(2)));
        data.add(new InventoryItem(seq.getAndIncrement(), "Eggs", 12, LocalDate.now().plusDays(14)));
    }

    @Override public List<InventoryItem> listAll() { return new ArrayList<>(data); }
    @Override public void add(String n, int q, LocalDate e){ data.add(new InventoryItem(seq.getAndIncrement(), n, q, e)); }
    @Override public void update(int id, String n, int q, LocalDate e){
        for (var it : data) if (it.getId()==id){ it.setName(n); it.setQuantity(q); it.setExpiry(e); return; }
    }
    @Override public void delete(int id){ data.removeIf(it -> it.getId()==id); }
}
