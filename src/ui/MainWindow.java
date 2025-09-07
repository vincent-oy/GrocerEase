package ui;

import javax.swing.*;
import java.awt.*;
import service.InMemoryInventoryService;

public class MainWindow extends JFrame {

    public MainWindow() {
        setTitle("GrocerEase — Main Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 320);                // without size/pack, nothing is visible
        setLocationRelativeTo(null);      // center on screen

        JButton invBtn = new JButton("Inventory");
        invBtn.addActionListener(e ->
            new InventoryWindow(InMemoryInventoryService.shared()).setVisible(true)
        );

        // Simple layout so you can SEE something immediately
        setLayout(new FlowLayout());
        add(invBtn);

        // Optional: prove the window exists even if covered by other windows
        // setAlwaysOnTop(true);
    }
}
