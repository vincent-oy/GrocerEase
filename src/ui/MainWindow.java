package ui;

import javax.swing.*;
import java.awt.*;
import service.InMemoryInventoryService;
import service.InventoryService;
import service.SqliteInventoryService;

import java.nio.file.Path;

public class MainWindow extends JFrame {

    private final InventoryService inventoryService;

    public MainWindow() {
        this(createInventoryService());
    }

    public MainWindow(InventoryService service) {
        this.inventoryService = service;
        setTitle("GrocerEase — Main Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 320);                // without size/pack, nothing is visible
        setLocationRelativeTo(null);      // center on screen

        JButton invBtn = new JButton("Inventory");
        invBtn.addActionListener(e ->
            new InventoryWindow(inventoryService).setVisible(true)
        );

        // Simple layout so you can SEE something immediately
        setLayout(new FlowLayout());
        add(invBtn);

        // Optional: prove the window exists even if covered by other windows
        // setAlwaysOnTop(true);
    }

    private static InventoryService createInventoryService() {
        String forceMemory = System.getProperty("grocer.inventory.inMemory");
        if (forceMemory != null && forceMemory.equalsIgnoreCase("true")) {
            return InMemoryInventoryService.shared();
        }

        String dbPathProp = System.getProperty("grocer.inventory.db");
        Path dbPath = (dbPathProp != null && !dbPathProp.isBlank())
                ? Path.of(dbPathProp)
                : Path.of(System.getProperty("user.dir"), "inventory.db");

        try {
            return new SqliteInventoryService(dbPath);
        } catch (RuntimeException ex) {
            System.err.println("Unable to start SQLite inventory service: " + ex.getMessage());
            System.err.println("Falling back to in-memory inventory service. Set -Dgrocer.inventory.inMemory=true to hide this warning.");
            return InMemoryInventoryService.shared();
        }
    }
}
