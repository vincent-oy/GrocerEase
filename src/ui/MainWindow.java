package ui;

import service.SqlitePantryService;
import service.SqliteTripService;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/** Simple main menu with Pantry, Trip, and About. */
public class MainWindow extends JFrame {

    public MainWindow() {
        setTitle("GrocerEase — Main Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(520, 280);
        setLocationRelativeTo(null);

        JButton btnPantry = new JButton("Pantry");
        JButton btnTrip   = new JButton("Plan a Trip");
        JButton btnAbout  = new JButton("About");

        btnPantry.setHorizontalAlignment(SwingConstants.CENTER);

        btnPantry.addActionListener(e ->
            new PantryWindow(new SqlitePantryService()).setVisible(true)
        );

        btnTrip.addActionListener(e ->
            new TripWindow(new SqliteTripService()).setVisible(true)
        );

        btnAbout.addActionListener(e ->
            JOptionPane.showMessageDialog(this,
                "GrocerEase: a simple grocery planner.\n" +
                "Apache Netbeans + SQLite.\nBeta Version V1.")
        );

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10,10,10,10);
        c.gridx=0; c.gridy=0; add(btnPantry, c);
        c.gridy=1; add(btnTrip, c);
        c.gridy=2; add(btnAbout, c);
    }
}
