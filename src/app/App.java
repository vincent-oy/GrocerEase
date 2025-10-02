package app;

// Swing imports for GUI
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

// Our own main window class
import ui.MainWindow;

/**
 * Entry point of the GrocerEase program.
 * Sets a nice theme and opens the main menu window.
 */
public class App {

    public static void main(String[] args) {

        // A quick console log to know the app started.
        System.out.println("GrocerEase application starting...");

        // Try the "Nimbus" Look & Feel (nicer default).
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {
            System.out.println("Nimbus not available, using default Look & Feel.");
        }

        // All Swing UI must be created on the Event Dispatch Thread (EDT).
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                MainWindow main = new MainWindow();
                main.setVisible(true);
            }
        });
    }
}
