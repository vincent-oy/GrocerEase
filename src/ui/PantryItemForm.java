package ui.pantry;

import model.PantryItem;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.time.LocalDate;

/**
 * Small modal dialog to add or edit a PantryItem.
 * We don't use the NetBeans GUI builder here; we lay it out in code so you can see how it works.
 */
class PantryItemForm extends JDialog {

    // Text boxes for the inputs
    private final JTextField name = new JTextField(18);
    private final JTextField category = new JTextField(12);
    private final JTextField onHand = new JTextField(6);
    private final JTextField unit = new JTextField(8);
    private final JTextField minQty = new JTextField(6);
    private final JTextField expiry = new JTextField(10);   // YYYY-MM-DD or blank

    // When user presses OK, we fill this object; if Cancel, we keep it null.
    private PantryItem result = null;

    /**
     * @param owner   parent window (for centering)
     * @param initial if not null, pre-fill the form for editing
     */
    PantryItemForm(Frame owner, PantryItem initial) {
        super(owner, true);  // "true" makes this dialog modal (blocks other windows until closed)

        setTitle(initial == null ? "Add Pantry Item" : "Edit Pantry Item");
        setSize(460, 260);
        setLocationRelativeTo(owner);

        // Use GridBagLayout for a simple form: labels on left, fields on right
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8);

        // Row 0: Name
        c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.LINE_END;
        add(new JLabel("Name:"), c);
        c.gridx = 1; c.anchor = GridBagConstraints.LINE_START;
        add(name, c);

        // Row 1: Category
        c.gridx = 0; c.gridy = 1; c.anchor = GridBagConstraints.LINE_END;
        add(new JLabel("Category:"), c);
        c.gridx = 1; c.anchor = GridBagConstraints.LINE_START;
        add(category, c);

        // Row 2: On-hand quantity
        c.gridx = 0; c.gridy = 2; c.anchor = GridBagConstraints.LINE_END;
        add(new JLabel("On-hand:"), c);
        c.gridx = 1; c.anchor = GridBagConstraints.LINE_START;
        add(onHand, c);

        // Row 3: Unit
        c.gridx = 0; c.gridy = 3; c.anchor = GridBagConstraints.LINE_END;
        add(new JLabel("Unit:"), c);
        c.gridx = 1; c.anchor = GridBagConstraints.LINE_START;
        add(unit, c);

        // Row 4: Minimum quantity
        c.gridx = 0; c.gridy = 4; c.anchor = GridBagConstraints.LINE_END;
        add(new JLabel("Min qty:"), c);
        c.gridx = 1; c.anchor = GridBagConstraints.LINE_START;
        add(minQty, c);

        // Row 5: Expiry
        c.gridx = 0; c.gridy = 5; c.anchor = GridBagConstraints.LINE_END;
        add(new JLabel("Expiry (YYYY-MM-DD):"), c);
        c.gridx = 1; c.anchor = GridBagConstraints.LINE_START;
        add(expiry, c);

        // Row 6: Buttons (right aligned)
        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.add(cancel);
        buttons.add(ok);

        c.gridx = 0; c.gridy = 6; c.gridwidth = 2;
        c.anchor = GridBagConstraints.LINE_END;
        add(buttons, c);

        // If editing, prefill fields
        if (initial != null) {
            name.setText(initial.name);
            category.setText(initial.category);
            onHand.setText(String.valueOf(initial.onHandQty));
            unit.setText(initial.unit);
            minQty.setText(String.valueOf(initial.minQty));
            expiry.setText(initial.expiry == null ? "" : initial.expiry.toString());
        }

        // OK button logic: validate and build PantryItem
        ok.addActionListener(e -> {
            try {
                PantryItem p = new PantryItem();

                p.name = name.getText().trim();
                p.category = category.getText().trim();
                p.onHandQty = Integer.parseInt(onHand.getText().trim());
                p.unit = unit.getText().trim();
                p.minQty = Integer.parseInt(minQty.getText().trim());

                String expText = expiry.getText().trim();
                p.expiry = expText.isBlank() ? null : LocalDate.parse(expText);

                // If we get here, inputs were valid
                result = p;
                dispose();  // close dialog

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Please check your inputs.\n" +
                        "- On-hand and Min must be integers.\n" +
                        "- Expiry must be YYYY-MM-DD or left blank.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        // Cancel button: just close without saving
        cancel.addActionListener(e -> {
            result = null;
            dispose();
        });
    }

    /**
     * Show the dialog and return the PantryItem created/edited.
     * Returns null if user pressed Cancel.
     */
    PantryItem showDialog() {
        setVisible(true);   // this blocks until the dialog is closed
        return result;
    }
}

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

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

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
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

        /* Create and display the dialog */
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                PantryItemForm dialog = new PantryItemForm(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    @Override
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
