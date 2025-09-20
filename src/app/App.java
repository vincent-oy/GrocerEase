package app;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import ui.MainWindow;

public class App {
    public static void main(String[] args) {
        System.out.println("App main started"); // a quick sanity log/check

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception ignored) {}

        SwingUtilities.invokeLater(() -> {
            new MainWindow().setVisible(true); // show main menu    Event Dispatch Thread
        });
    }
}
