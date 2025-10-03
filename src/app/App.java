package app; // Define the package namespace for the application entry point

import javax.swing.SwingUtilities; // Import SwingUtilities so UI operations occur on the Event Dispatch Thread (EDT)
import javax.swing.UIManager; // Import UIManager to control the Swing look and feel at runtime

import ui.MainWindow; // Import the main application window that hosts the primary UI

public class App { // Declare the App class that contains the main method

    public static void main(String[] args) { // JVM entry point that bootstraps the GrocerEase UI

        System.out.println("== GrocerEase starting =="); // Print a banner to confirm that the application has started

        try { // Attempt to configure the Swing look and feel to Nimbus for a modern appearance
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel"); // Request Nimbus by its fully qualified class name
        } catch (Exception e) { // Handle any failures when applying the Nimbus look and feel
            System.out.println("nimbus failed -> using default"); // Log that the default Swing theme will be used as a fallback
        }

        SwingUtilities.invokeLater(new Runnable() { // Schedule UI construction on the EDT as recommended by Swing
            @Override // Annotate that we are overriding the run method from Runnable
            public void run() { // Provide the code that will execute on the EDT to create the UI
                MainWindow w = new MainWindow(); // Instantiate the main application window that provides navigation

                w.setVisible(true); // Display the window so the user can interact with the application
                System.out.println("main window visible"); // Log that the main window has been made visible
            } // End of the run method implementation
        }); // Submit the Runnable to be executed asynchronously on the EDT
    } // End of the main method
} // End of the App class definition
