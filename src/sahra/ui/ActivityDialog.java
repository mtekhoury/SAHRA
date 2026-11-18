package sahra.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import sahra.db.ActivityDao;
import sahra.db.ActivityDao.Field;
import sahra.db.ActivityDao.Kind;
import sahra.db.ActivityDao.SubType;
import sahra.db.Models.Activity;

/** Add / edit form for a place, plus a "details" card. */
class ActivityDialog extends JDialog {

    private final Integer activityId;   // null = adding a new one
    private boolean saved;

    private final JTextField name = Theme.field(22, "Name of the place");
    private final JComboBox<String> type = Ui.combo(new JComboBox<>(ActivityDao.TYPES));
    private final JComboBox<String> area = Ui.combo(new JComboBox<>(ActivityDao.AREAS));
    private final JComboBox<String> atmosphere = Ui.combo(new JComboBox<>(ActivityDao.ATMOSPHERES));
    private final JTextField price = Theme.field(8, "e.g. 22");
    private final JTextField address = Theme.field(22, "Street, neighbourhood");
    private final JTextField phone = Theme.field(12, "01-234567");
    private final JPanel subPanel = Theme.transparent(new GridBagLayout());
    private final JLabel subTitle = Theme.caps("");
    private final Map<String, JComponent> subInputs = new LinkedHashMap<>();

    /** Opens the form. Returns true if something was saved. */
    static boolean edit(Component parent, Integer activityId) {
        ActivityDialog d = new ActivityDialog(SwingUtilities.getWindowAncestor(parent), activityId);
        d.setVisible(true);
        return d.saved;
    }

    /** Shows every column of the place and of its sub-type table, as a card. */
    static void showDetails(Component parent, int activityId) {
        try {
            Activity a = ActivityDao.get(activityId);
            if (a == null) return;

            JDialog d = new JDialog(SwingUtilities.getWindowAncestor(parent), a.name(), ModalityType.APPLICATION_MODAL);
            JPanel body = new JPanel();
            body.setBackground(Theme.BG);
            body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
            body.setBorder(BorderFactory.createEmptyBorder(26, 30, 20, 30));

            JPanel chips = Theme.transparent(new FlowLayout(FlowLayout.LEFT, 6, 0));
            chips.add(new Theme.Chip(a.type(), Theme.typeColor(a.type())));
            chips.add(new Theme.Chip(a.area(), Theme.MUTED));
            chips.add(new Theme.Chip(a.atmosphere(), Theme.MUTED));
            chips.setBorder(BorderFactory.createEmptyBorder(0, -6, 0, 0));
            JLabel title = Theme.h2(a.name());
            JLabel price = Theme.text(Ui.money(a.price()) + " per person", Font.BOLD, 16, Theme.ACCENT);
            for (JComponent c : new JComponent[]{chips, title, price}) c.setAlignmentX(LEFT_ALIGNMENT);
            body.add(chips);
            body.add(Box.createVerticalStrut(12));
            body.add(title);
            body.add(Box.createVerticalStrut(4));
            body.add(price);
            body.add(Box.createVerticalStrut(18));

            Theme.Card info = new Theme.Card(new GridBagLayout());
            info.setAlignmentX(LEFT_ALIGNMENT);
            GridBagConstraints g = new GridBagConstraints();
            g.anchor = GridBagConstraints.WEST;
            g.insets = new Insets(5, 0, 5, 24);
            int row = 0;
            row = infoRow(info, g, row, "Address", a.address());
            row = infoRow(info, g, row, "Phone", a.phone() == null ? "–" : a.phone());
            SubType st = ActivityDao.SUBTYPES.get(a.type());
            if (st != null) {
                Map<String, Object> values = ActivityDao.subtypeValues(activityId, a.type());
                for (Field f : st.fields()) {
                    Object v = values.get(f.column());
                    String shown = v instanceof Boolean b ? (b ? "Yes" : "No") : String.valueOf(v);
                    row = infoRow(info, g, row, f.label().replaceAll(" \\(.*\\)", ""), shown);
                }
            }
            body.add(info);

            JButton close = Theme.secondary("Close");
            close.addActionListener(e -> d.dispose());
            JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttons.setBackground(Theme.BG);
            buttons.setBorder(BorderFactory.createEmptyBorder(0, 24, 18, 24));
            buttons.add(close);

            d.getContentPane().setBackground(Theme.BG);
            d.add(body, BorderLayout.CENTER);
            d.add(buttons, BorderLayout.SOUTH);
            d.pack();
            d.setMinimumSize(new Dimension(440, d.getHeight()));
            d.setResizable(false);
            d.setLocationRelativeTo(parent);
            d.setVisible(true);
        } catch (Exception ex) {
            Ui.error(parent, ex);
        }
    }

    private static int infoRow(JPanel p, GridBagConstraints g, int row, String label, String value) {
        g.gridy = row;
        g.gridx = 0;
        g.weightx = 0;
        p.add(Theme.text(label, Font.PLAIN, 13, Theme.MUTED), g);
        g.gridx = 1;
        g.weightx = 1;
        p.add(Theme.text(value, Font.BOLD, 13, Theme.TEXT), g);
        return row + 1;
    }

    private ActivityDialog(Window owner, Integer activityId) {
        super(owner, activityId == null ? "Add place" : "Edit place", ModalityType.APPLICATION_MODAL);
        this.activityId = activityId;

        JPanel body = new JPanel();
        body.setBackground(Theme.BG);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(26, 30, 10, 30));

        JLabel heading = Theme.h2(activityId == null ? "Add a place" : "Edit place");
        heading.setAlignmentX(LEFT_ALIGNMENT);
        body.add(heading);
        body.add(Box.createVerticalStrut(18));

        JPanel common = Theme.transparent(new GridBagLayout());
        GridBagConstraints g = gbc();
        int r = 0;
        r = addRow(common, g, r, "Name", name);
        r = addRow(common, g, r, "Type", type);
        r = addRow(common, g, r, "Area", area);
        r = addRow(common, g, r, "Vibe", atmosphere);
        r = addRow(common, g, r, "Price per person ($)", price);
        r = addRow(common, g, r, "Address", address);
        addRow(common, g, r, "Phone", phone);
        common.setAlignmentX(LEFT_ALIGNMENT);
        body.add(common);
        body.add(Box.createVerticalStrut(18));

        Theme.Card sub = new Theme.Card(new BorderLayout(0, 10));
        sub.add(subTitle, BorderLayout.NORTH);
        sub.add(subPanel, BorderLayout.CENTER);
        sub.setAlignmentX(LEFT_ALIGNMENT);
        body.add(sub);
        type.addActionListener(e -> buildSubFields(null));

        JButton save = Theme.primary(activityId == null ? "Add place" : "Save changes");
        JButton cancel = Theme.secondary("Cancel");
        save.addActionListener(e -> save());
        cancel.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setBackground(Theme.BG);
        buttons.setBorder(BorderFactory.createEmptyBorder(14, 30, 24, 30));
        buttons.add(cancel);
        buttons.add(save);

        getContentPane().setBackground(Theme.BG);
        setLayout(new BorderLayout());
        add(body, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);

        if (activityId != null) {
            loadExisting();
            type.setEnabled(false);   // the type decides the sub-type table, so it cannot change
        } else {
            buildSubFields(null);
        }
        pack();
        setLocationRelativeTo(owner);
    }

    private static GridBagConstraints gbc() {
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 0, 5, 14);
        g.anchor = GridBagConstraints.WEST;
        g.fill = GridBagConstraints.HORIZONTAL;
        return g;
    }

    private static int addRow(JPanel p, GridBagConstraints g, int row, String label, JComponent field) {
        g.gridx = 0; g.gridy = row; g.weightx = 0;
        p.add(Theme.text(label, Font.PLAIN, 13, Theme.MUTED), g);
        g.gridx = 1; g.weightx = 1;
        p.add(field, g);
        return row + 1;
    }

    /** Rebuilds the type-specific inputs. values = existing data when editing, null when adding. */
    private void buildSubFields(Map<String, Object> values) {
        subPanel.removeAll();
        subInputs.clear();
        String t = (String) type.getSelectedItem();
        subTitle.setText((t + " details").toUpperCase());
        subTitle.setForeground(Theme.typeColor(t));
        SubType st = ActivityDao.SUBTYPES.get(t);
        GridBagConstraints g = gbc();
        int r = 0;
        for (Field f : st.fields()) {
            Object v = values == null ? null : values.get(f.column());
            JComponent input;
            if (f.kind() == Kind.BOOL) {
                JCheckBox box = new JCheckBox();
                box.setOpaque(false);
                box.setSelected(Boolean.TRUE.equals(v));
                input = box;
            } else {
                input = Theme.tall(new JTextField(v == null ? "" : v.toString(), 18));
            }
            subInputs.put(f.column(), input);
            r = addRow(subPanel, g, r, f.label(), input);
        }
        subPanel.revalidate();
        subPanel.repaint();
        pack();
    }

    private void loadExisting() {
        try {
            Activity a = ActivityDao.get(activityId);
            name.setText(a.name());
            type.setSelectedItem(a.type());
            area.setSelectedItem(a.area());
            atmosphere.setSelectedItem(a.atmosphere());
            price.setText(String.valueOf(a.price()));
            address.setText(a.address());
            phone.setText(a.phone() == null ? "" : a.phone());
            buildSubFields(ActivityDao.subtypeValues(activityId, a.type()));
        } catch (Exception ex) {
            Ui.error(this, ex);
        }
    }

    private void save() {
        try {
            if (name.getText().isBlank() || address.getText().isBlank()) {
                Ui.info(this, "Name and address are required.");
                return;
            }
            double p;
            try {
                p = Double.parseDouble(price.getText().trim().replace("$", ""));
            } catch (NumberFormatException ex) {
                Ui.info(this, "The price must be a number, for example 22 or 18.5");
                return;
            }
            String t = (String) type.getSelectedItem();
            Activity a = new Activity(activityId == null ? 0 : activityId, name.getText().trim(), t,
                    (String) area.getSelectedItem(), address.getText().trim(),
                    (String) atmosphere.getSelectedItem(), p, phone.getText().trim());

            Map<String, Object> sub = new LinkedHashMap<>();
            for (Field f : ActivityDao.SUBTYPES.get(t).fields()) {
                JComponent input = subInputs.get(f.column());
                Object raw = input instanceof JCheckBox box ? box.isSelected() : ((JTextField) input).getText();
                sub.put(f.column(), ActivityDao.parse(f, raw));
            }

            if (activityId == null) ActivityDao.insert(a, sub);
            else ActivityDao.update(a, sub);
            saved = true;
            dispose();
        } catch (IllegalArgumentException ex) {
            Ui.info(this, ex.getMessage());
        } catch (Exception ex) {
            Ui.error(this, ex);
        }
    }
}
