package ui;

import javax.swing.*;
import java.awt.*;

// i directly create the concrete services in each window later (student style)
// so main window just opens those windows. simple.

public class MainWindow extends JFrame {

    public MainWindow() {
        setTitle("GrocerEase - Main Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 300);
        setLocationRelativeTo(null); // center

        // buttons i actually use in this project
        JButton btnPantry = new JButton("Pantry");
        JButton btnTrip   = new JButton("Plan a Trip");
        JButton btnAbout  = new JButton("About");

        // layout: simple gridbag so i can space them
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);

        c.gridx = 0; c.gridy = 0; add(btnPantry, c);
        c.gridx = 0; c.gridy = 1; add(btnTrip, c);
        c.gridx = 0; c.gridy = 2; add(btnAbout, c);

        // open pantry window
        btnPantry.addActionListener(e -> {
            try {
                // student style: directly new the concrete service inside the window
                // pantry window will create its own Sqlite service
                PantryWindow pw = new PantryWindow();
                pw.setVisible(true);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "couldn't open pantry window");
            }
        });

        // open trip planner
        btnTrip.addActionListener(e -> {
            try {
                TripWindow tw = new TripWindow();
                tw.setVisible(true);
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "couldn't open trip window");
            }
        });

        // small about dialog
        btnAbout.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "GrocerEase (practice IA)\n" +
                    "- Apache Netbeans + SQLite\n" +
                    "- Pantry + Trip planning\n" +
                    "- Created to ease grocery shopping");
        });
    }
}
