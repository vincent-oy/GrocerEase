    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
package ui;

import service.SqlitePantryService;
import service.SqliteTripService;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingConstants;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

/**
 * Very simple main menu with three buttons.
 * From here, the user can go to Pantry or Trip planning windows.
 */
public class MainWindow extends JFrame {

    public MainWindow() {

        // Title bar text
        setTitle("GrocerEase — Main Menu");

        // When the main window closes, exit the app
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Width and height in pixels
        setSize(480, 260);

        // Center the window on screen
        setLocationRelativeTo(null);

        // Create buttons
        JButton btnPantry = new JButton("Pantry");
        JButton btnTrip   = new JButton("Plan a Trip");
        JButton btnAbout  = new JButton("About");

        // Make the text centered just for nice looks
        btnPantry.setHorizontalAlignment(SwingConstants.CENTER);
        btnTrip.setHorizontalAlignment(SwingConstants.CENTER);

        // Button actions (open other windows)
        btnPantry.addActionListener(e -> {
            // Each window creates its own service object.
            // This keeps classes independent and easy to test.
            new ui.pantry.PantryWindow(new SqlitePantryService()).setVisible(true);
        });

        btnTrip.addActionListener(e -> {
            new ui.trip.TripWindow(new SqliteTripService()).setVisible(true);
        });

        btnAbout.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "GrocerEase — a simple grocery planner for Mom.\n" +
                    "Built with Java Swing + SQLite.\n" +
                    "Practice version for IB-style learning.");
        });

        // Use GridBagLayout so buttons are nicely spaced vertically
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(10, 10, 10, 10);  // padding around buttons

        c.gridx = 0;
        c.gridy = 0;
        add(btnPantry, c);

        c.gridy = 1;
        add(btnTrip, c);

        c.gridy = 2;
        add(btnAbout, c);
    }
}      

//<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            logger.log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form *
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

