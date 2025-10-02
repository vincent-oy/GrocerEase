package app;

// swing = java's gui stuff
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

// my window (main menu)
import ui.MainWindow;

/*
 * entry point. i just start the GUI here.
 * kept this simple on purpose.
 */
public class App {

    public static void main(String[] args) {

        // just so i can see it actually launched
        System.out.println("== GrocerEase starting ==");

        // try nicer theme. if it fails, whatever.
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            System.out.println("nimbus failed -> using default");
        }

        // swing rule: make windows on the event dispatch thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // open the main menu
                MainWindow w = new MainWindow();
                w.setVisible(true);
                System.out.println("main window visible"); // debug
            }
        });
    }
}
