package ui; // Define the package containing Swing user interface classes

import javax.swing.JButton; // Import JButton for clickable actions
import javax.swing.JFrame; // Import JFrame as the base window class
import javax.swing.JOptionPane; // Import JOptionPane for simple dialogs
import java.awt.GridBagConstraints; // Import GridBagConstraints to configure GridBagLayout
import java.awt.GridBagLayout; // Import GridBagLayout to arrange buttons vertically with spacing
import java.awt.Insets; // Import Insets to add padding around components

public class MainWindow extends JFrame { // Define the primary navigation window for the application

    public MainWindow() { // Construct and initialize the main menu window
        setTitle("GrocerEase - Main Menu"); // Set the window title shown in the frame header
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Close the entire application when this window exits
        setSize(520, 300); // Configure the initial window size
        setLocationRelativeTo(null); // Center the window on the screen

        JButton btnPantry = new JButton("Pantry"); // Create a button to open the pantry management window
        JButton btnTrip = new JButton("Plan a Trip"); // Create a button to open the trip planning window
        JButton btnAbout = new JButton("About"); // Create a button to show an about dialog

        setLayout(new GridBagLayout()); // Use GridBagLayout for flexible component placement
        GridBagConstraints c = new GridBagConstraints(); // Instantiate layout constraints for positioning controls
        c.insets = new Insets(10, 10, 10, 10); // Apply uniform padding around each button

        c.gridx = 0; c.gridy = 0; add(btnPantry, c); // Place the pantry button in the first row
        c.gridx = 0; c.gridy = 1; add(btnTrip, c); // Place the trip button beneath the pantry button
        c.gridx = 0; c.gridy = 2; add(btnAbout, c); // Place the about button at the bottom of the column

        btnPantry.addActionListener(e -> { // Register a listener to handle pantry button clicks
            try { // Attempt to construct and show the pantry window
                PantryWindow pw = new PantryWindow(); // Create a new pantry management window instance
                pw.setVisible(true); // Display the pantry window to the user
            } catch (Exception ex) { // Handle unexpected errors when opening the pantry window
                ex.printStackTrace(); // Print details about the failure for debugging
                JOptionPane.showMessageDialog(this, "couldn't open pantry window"); // Inform the user that the window could not be opened
            } // End catch block for pantry window errors
        }); // End pantry button listener registration

        btnTrip.addActionListener(e -> { // Register a listener for the trip planning button
            try { // Attempt to construct and show the trip planning window
                TripWindow tw = new TripWindow(); // Create a new trip planning window instance
                tw.setVisible(true); // Display the trip planning window
            } catch (Exception ex) { // Handle failures while opening the trip window
                ex.printStackTrace(); // Print the exception stack trace for debugging
                JOptionPane.showMessageDialog(this, "couldn't open trip window"); // Show an error message to the user
            } // End catch block for trip window errors
        }); // End trip button listener registration

        btnAbout.addActionListener(e -> { // Register a listener for the about button
            JOptionPane.showMessageDialog(this, // Show a simple message dialog anchored to this window
                    "GrocerEase (practice IA)\n" + // Provide the application name and context
                    "- Apache Netbeans + SQLite\n" + // Describe the tooling used to build the project
                    "- Pantry + Trip planning\n" + // Highlight the main features available
                    "- Created to ease grocery shopping"); // Explain the goal of the application
        }); // End about button listener registration
    } // End MainWindow constructor
} // End MainWindow class definition
