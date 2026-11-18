package sahra.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import sahra.db.ActivityDao;
import sahra.db.Models.Activity;

/** Search every place with any combination of filters, and add / edit / delete them. */
class ActivitiesPanel extends JPanel {

    private final JComboBox<String> type = combo("Any type", ActivityDao.TYPES);
    private final JComboBox<String> area = combo("Any area", ActivityDao.AREAS);
    private final JComboBox<String> atmosphere = combo("Any vibe", ActivityDao.ATMOSPHERES);
    private final JTextField minPrice = Theme.field(5, "Min $");
    private final JTextField maxPrice = Theme.field(5, "Max $");
    private final JTextField name = Theme.field(14, "Search by name...");
    private final JLabel count = Theme.muted("");

    private final DefaultTableModel model =
            Ui.readOnlyModel("ID", "Name", "Type", "Area", "Vibe", "Price / person", "Address", "Phone");
    private final JTable table = Ui.table(model);

    ActivitiesPanel() {
        super(new BorderLayout(0, 16));
        setBackground(Theme.BG);
        setBorder(BorderFactory.createEmptyBorder(28, 32, 24, 32));

        // ----- heading -----
        JPanel head = Theme.transparent(new BorderLayout());
        JPanel titles = Theme.transparent(null);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.add(Theme.h1("Explore places"));
        titles.add(Box.createVerticalStrut(4));
        titles.add(count);
        head.add(titles, BorderLayout.WEST);
        JButton add = Theme.primary("+  Add place");
        add.setPreferredSize(new Dimension(150, 44));
        add.addActionListener(e -> { if (ActivityDialog.edit(this, null)) search(); });
        JPanel addWrap = Theme.transparent(new FlowLayout(FlowLayout.RIGHT, 0, 6));
        addWrap.add(add);
        head.add(addWrap, BorderLayout.EAST);

        // ----- filter bar -----
        Theme.Card filters = new Theme.Card(new GridBagLayout());
        filters.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(0, 0, 0, 10);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridy = 0;
        g.weightx = 1;
        filters.add(name, g);
        g.weightx = 0;
        for (JComponent c : new JComponent[]{type, area, atmosphere, minPrice, maxPrice}) filters.add(c, g);
        JButton search = Theme.primary("Search");
        JButton clear = Theme.link("Clear");
        filters.add(search, g);
        g.insets = new Insets(0, 0, 0, 0);
        filters.add(clear, g);
        search.addActionListener(e -> search());
        name.addActionListener(e -> search());
        minPrice.addActionListener(e -> search());
        maxPrice.addActionListener(e -> search());
        for (JComboBox<String> c : List.of(type, area, atmosphere)) c.addActionListener(e -> search());
        clear.addActionListener(e -> {
            name.setText(""); minPrice.setText(""); maxPrice.setText("");
            type.setSelectedIndex(0); area.setSelectedIndex(0); atmosphere.setSelectedIndex(0);
            search();
        });

        JPanel top = Theme.transparent(new BorderLayout(0, 16));
        top.add(head, BorderLayout.NORTH);
        top.add(filters, BorderLayout.CENTER);

        // ----- table -----
        table.getColumnModel().getColumn(0).setMaxWidth(50);
        table.getColumnModel().getColumn(1).setPreferredWidth(200);
        table.getColumnModel().getColumn(2).setCellRenderer(new ChipRenderer());
        table.getColumnModel().getColumn(5).setCellRenderer(new PriceRenderer());
        table.getColumnModel().getColumn(6).setPreferredWidth(200);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) details();
            }
        });
        Theme.Card tableCard = new Theme.Card(new BorderLayout());
        tableCard.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        tableCard.add(Theme.scroll(table));

        // ----- actions -----
        JButton details = Theme.secondary("Details");
        JButton edit = Theme.secondary("Edit");
        JButton delete = Theme.danger("Delete");
        details.addActionListener(e -> details());
        edit.addActionListener(e -> {
            Integer id = selectedId();
            if (id == null) { Ui.info(this, "Select a place first."); return; }
            if (ActivityDialog.edit(this, id)) search();
        });
        delete.addActionListener(e -> delete());
        JPanel actions = Theme.transparent(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actions.add(Theme.muted("Double-click a row for details   "));
        actions.add(details);
        actions.add(edit);
        actions.add(delete);

        add(top, BorderLayout.NORTH);
        add(tableCard, BorderLayout.CENTER);
        add(actions, BorderLayout.SOUTH);

        search();
    }

    private static JComboBox<String> combo(String any, String[] values) {
        JComboBox<String> c = new JComboBox<>();
        c.addItem(any);
        for (String v : values) c.addItem(v);
        return Ui.combo(c);
    }

    private static String pick(JComboBox<String> c) {
        return c.getSelectedIndex() <= 0 ? null : (String) c.getSelectedItem();
    }

    private static Double number(JTextField f) {
        String t = f.getText().trim().replace("$", "");
        return t.isEmpty() ? null : Double.parseDouble(t);
    }

    /** Type column: colored pill. */
    private static class ChipRenderer implements TableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                boolean focus, int row, int col) {
            JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 7));
            p.setBackground(selected ? t.getSelectionBackground()
                    : (row % 2 == 0 ? Theme.SURFACE : Theme.ROW_ALT));
            String s = value == null ? "" : value.toString();
            p.add(new Theme.Chip(s, Theme.typeColor(s)));
            return p;
        }
    }

    /** Price column: bold coral, right-aligned. */
    private static class PriceRenderer extends DefaultTableCellRenderer {
        @Override public Component getTableCellRendererComponent(JTable t, Object value, boolean selected,
                boolean focus, int row, int col) {
            super.getTableCellRendererComponent(t, value, selected, false, row, col);
            if (!selected) setBackground(row % 2 == 0 ? Theme.SURFACE : Theme.ROW_ALT);
            setFont(Theme.font(Font.BOLD, 14));
            setForeground(Theme.ACCENT);
            setHorizontalAlignment(SwingConstants.RIGHT);
            setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 24));
            return this;
        }
    }

    private void search() {
        try {
            List<Activity> list = ActivityDao.search(pick(type), pick(area), pick(atmosphere),
                    number(minPrice), number(maxPrice), name.getText());
            model.setRowCount(0);
            for (Activity a : list) {
                model.addRow(new Object[]{a.id(), a.name(), a.type(), a.area(), a.atmosphere(),
                        Ui.money(a.price()), a.address(), a.phone()});
            }
            count.setText(list.size() + (list.size() == 1 ? " place found" : " places found"));
        } catch (NumberFormatException ex) {
            Ui.info(this, "Prices must be numbers, for example 15 or 22.5");
        } catch (Exception ex) {
            Ui.error(this, ex);
        }
    }

    private Integer selectedId() {
        int row = table.getSelectedRow();
        return row < 0 ? null : (Integer) model.getValueAt(table.convertRowIndexToModel(row), 0);
    }

    private void details() {
        Integer id = selectedId();
        if (id == null) { Ui.info(this, "Select a place first."); return; }
        ActivityDialog.showDetails(this, id);
    }

    private void delete() {
        Integer id = selectedId();
        if (id == null) { Ui.info(this, "Select a place first."); return; }
        String n = (String) model.getValueAt(table.convertRowIndexToModel(table.getSelectedRow()), 1);
        if (!Ui.confirm(this, "Delete \"" + n + "\"?")) return;
        try {
            ActivityDao.delete(id);
            search();
        } catch (Exception ex) {
            Ui.error(this, ex);
        }
    }
}
