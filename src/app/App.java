package app;

// swing = java gui stuff import
import javax.swing.SwingUtilities;                         // for running GUI creation on EDT
import javax.swing.UIManager;                              // for enhanced look & usability

import ui.MainWindow;   //MainWindow.java (my window)

//Entry point, GUI starts here
public class App {
    
    public static void main(String[] args) {

        // so i can see it actually launched
        System.out.println("== GrocerEase starting ==");

        // try nicer theme; if it fails stick to the older one
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            System.out.println("nimbus failed -> using default");
        }

        // swing rule: make windows on the event dispatch thread (EDT). The main program therad doens't wait for thsi task to be done --> It continues to the next line of code right away
        SwingUtilities.invokeLater(new Runnable() {
            @Override                                      // Catch error if method signature incorrect
            public void run() {
                MainWindow w = new MainWindow();           // open the main menu

                w.setVisible(true);                        // show it
                System.out.println("main window visible"); // debug print
            }
        });
    }
}
