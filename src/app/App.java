package app;

// Import Swing classes we need to run GUI safely
import service.SqlitePantryService;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

// Import our main window class (the GUI entry point)
import ui.MainWindow;

/**
 * The App class is the starting point of the program.
 * It contains the "main" method which runs when the program starts.
 */
public class App {

    /**
     * Main method.
     * This is the first method that Java runs when you launch the program.
     */
    public static void main(String[] args) {

        // Simple log to the console to check that the program has started.
        // This is useful for debugging to know that the app actually launched.
        System.out.println("GrocerEase application starting...");

        // Try to set the "Nimbus" look and feel (a nicer style for Swing GUIs).
        // If Nimbus is not available, it will silently fall back to the default look.
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {
            // If it fails, we don't do anything, just use default.
            System.out.println("Nimbus look and feel not available, using default.");
        }

        // Use SwingUtilities.invokeLater to make sure that
        // the GUI (Graphical User Interface) code runs on the
        // Event Dispatch Thread (EDT). This is important in Java Swing
        // to avoid bugs or crashes when working with windows and buttons.
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Create an instance of our main menu window
                // and make it visible on the screen.
                MainWindow mainMenu = new MainWindow();
                mainMenu.setVisible(true);
            }
        });
    }
}
