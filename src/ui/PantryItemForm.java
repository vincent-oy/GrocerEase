package ui;

import model.PantryItem;

import javax.swing.JButton;
import javax.swing.JDialog;
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
 * Modal dialog to add or edit a PantryItem.
 * We code layout by hand (no .form) so that logic is easy to see.
 */
class PantryItemForm extends JDialog {

    private final JTextField name     = new JTextField(18);
    private final JTextField category = new JTextField(12);
    private final JTextField onHand   = new JTextField(6);
    private final JTextField unit     = new JTextField(8);
    private final JTextField minQty   = new JTextField(6);
    private final JTextField expiry   = new JTextField(10);  // YYYY-MM-DD or blank

    private PantryItem result;  // set when OK pressed; null if Cancel

    PantryItemForm(Frame owner, PantryItem initial) {
        super(owner, true);

        setTitle(initial == null ? "Add Pantry Item" : "Edit Pantry Item");
        setSize(480, 260);
        setLocationRelativeTo(owner);

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8);

        int y = 0;

        c.gridx=0; c.gridy=y; c.anchor=GridBagConstraints.LINE_END; add(new JLabel("Name:"), c);
        c.gridx=1; c.anchor=GridBagConstraints.LINE_START; add(name, c); y++;

        c.gridx=0; c.gridy=y; c.anchor=GridBagConstraints.LINE_END; add(new JLabel("Category:"), c);
        c.gridx=1; c.anchor=GridBagConstraints.LINE_START; add(category, c); y++;

        c.gridx=0; c.gridy=y; c.anchor=GridBagConstraints.LINE_END; add(new JLabel("On-hand:"), c);
        c.gridx=1; c.anchor=GridBagConstraints.LINE_START; add(onHand, c); y++;

        c.gridx=0; c.gridy=y; c.anchor=GridBagConstraints.LINE_END; add(new JLabel("Unit:"), c);
        c.gridx=1; c.anchor=GridBagConstraints.LINE_START; add(unit, c); y++;

        c.gridx=0; c.gridy=y; c.anchor=GridBagConstraints.LINE_END; add(new JLabel("Min qty:"), c);
        c.gridx=1; c.anchor=GridBagConstraints.LINE_START; add(minQty, c); y++;

        c.gridx=0; c.gridy=y; c.anchor=GridBagConstraints.LINE_END; add(new JLabel("Expiry (YYYY-MM-DD):"), c);
        c.gridx=1; c.anchor=GridBagConstraints.LINE_START; add(expiry, c); y++;

        JButton ok = new JButton("OK");
        JButton cancel = new JButton("Cancel");

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btns.add(cancel);
        btns.add(ok);

        c.gridx=0; c.gridy=y; c.gridwidth=2; c.anchor=GridBagConstraints.LINE_END;
        add(btns, c);

        if (initial != null) {
            name.setText(initial.name);
            category.setText(initial.category);
            onHand.setText(String.valueOf(initial.onHandQty));
            unit.setText(initial.unit);
            minQty.setText(String.valueOf(initial.minQty));
            expiry.setText(initial.expiry == null ? "" : initial.expiry.toString());
        }

        ok.addActionListener(e -> {
            try {
                PantryItem p = new PantryItem();
                p.name      = name.getText().trim();
                p.category  = category.getText().trim();
                p.onHandQty = Integer.parseInt(onHand.getText().trim());
                p.unit      = unit.getText().trim();
                p.minQty    = Integer.parseInt(minQty.getText().trim());

                String ex = expiry.getText().trim();
                p.expiry   = ex.isBlank() ? null : LocalDate.parse(ex);

                result = p;
                dispose();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Please check inputs.\n" +
                    "- On-hand and Min must be whole numbers.\n" +
                    "- Expiry must be YYYY-MM-DD or blank.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        });

        cancel.addActionListener(e -> { result = null; dispose(); });
    }

    PantryItem showDialog() { setVisible(true); return result; }
}
