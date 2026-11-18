package sahra.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Window;
import java.sql.SQLException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.table.DefaultTableModel;

/** Small helpers shared by all screens, including SAHRA's own dark pop-up dialogs. */
final class Ui {

    private Ui() { }

    /** A table model the user cannot type into. */
    static DefaultTableModel readOnlyModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
    }

    static JTable table(DefaultTableModel model) {
        JTable t = new JTable(model);
        t.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        Theme.styleTable(t);
        t.setAutoCreateRowSorter(true);
        t.getTableHeader().setReorderingAllowed(false);
        return t;
    }

    static JLabel title(String text) {
        return Theme.h2(text);
    }

    static <T extends JComponent> T padded(T c, int px) {
        c.setBorder(BorderFactory.createEmptyBorder(px, px, px, px));
        return c;
    }

    static String money(double v) {
        return v == Math.rint(v) ? "$" + (long) v : String.format("$%.2f", v);
    }

    // =================================================================
    //  Dark dropdowns (readable with or without FlatLaf)
    // =================================================================
    static <T> JComboBox<T> combo(JComboBox<T> c) {
        if (!Theme.flatLaf) c.setUI(new BasicComboBoxUI() {
            @Override protected JButton createArrowButton() {
                JButton b = new JButton() {
                    @Override protected void paintComponent(Graphics g) {
                        Graphics2D g2 = Theme.smooth(g);
                        g2.setColor(Theme.SURFACE_2);
                        g2.fillRect(0, 0, getWidth(), getHeight());
                        g2.setColor(Theme.MUTED);
                        int cx = getWidth() / 2, cy = getHeight() / 2;
                        g2.fillPolygon(new int[]{cx - 5, cx + 5, cx}, new int[]{cy - 2, cy - 2, cy + 4}, 3);
                        g2.dispose();
                    }
                };
                b.setBorder(BorderFactory.createEmptyBorder());
                b.setContentAreaFilled(false);
                b.setFocusPainted(false);
                return b;
            }
        });
        c.setBackground(Theme.SURFACE_2);
        c.setForeground(Theme.TEXT);
        if (!Theme.flatLaf) c.setBorder(BorderFactory.createLineBorder(Theme.BORDER, 1));
        c.setRenderer(new DefaultListCellRenderer() {
            @Override public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                    boolean selected, boolean focus) {
                super.getListCellRendererComponent(list, value, index, selected, false);
                setBackground(selected ? new Color(0x3B2A55) : Theme.SURFACE_2);
                setForeground(Theme.TEXT);
                setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));
                list.setBackground(Theme.SURFACE_2);
                list.setSelectionBackground(new Color(0x3B2A55));
                return this;
            }
        });
        return Theme.tall(c);
    }

    // =================================================================
    //  Pop-up dialogs
    // =================================================================
    static void info(Component parent, String msg) {
        show(parent, "Heads up", msg, Theme.ACCENT_2, null, "OK", null);
    }

    static boolean confirm(Component parent, String msg) {
        return show(parent, "Are you sure?", msg, Theme.WARNING, null, "Yes", "Cancel") != null;
    }

    /** Asks for one line of text. Returns null if cancelled. */
    static String input(Component parent, String title, String prompt, String placeholder) {
        JTextField field = Theme.field(24, placeholder);
        String r = show(parent, title, prompt, Theme.ACCENT, field, "OK", "Cancel");
        return r == null ? null : field.getText();
    }

    /** Shows a readable message for database errors (and the raw message underneath). */
    static void error(Component parent, Exception e) {
        String msg = e.getMessage();
        if (e instanceof SQLException sql) {
            String state = sql.getSQLState() == null ? "" : sql.getSQLState();
            switch (sql.getErrorCode()) {
                case 1062 -> msg = "That already exists (duplicate value).\n\n" + msg;
                case 1451 -> msg = "This cannot be deleted because other records still use it "
                        + "(foreign key constraint).\n\n" + msg;
                case 1452 -> msg = "This refers to a record that does not exist (foreign key constraint).\n\n" + msg;
                case 3819 -> msg = "A database rule was broken (CHECK constraint).\n\n" + msg;
                case 1045 -> msg = "MySQL refused the user name or password.\n"
                        + "Check db.user and db.password in config.properties.";
                case 1049 -> msg = "The 'sahra' database does not exist yet.\n"
                        + "Run sql/schema.sql and sql/sample_data.sql in MySQL Workbench first.";
                default -> {
                    if (msg != null && msg.contains("No suitable driver")) {
                        msg = "Java cannot find the MySQL driver.\n"
                                + "Make sure mysql-connector-j-....jar is inside the lib folder,\n"
                                + "then reload VS Code (Ctrl+Shift+P > Developer: Reload Window).";
                    } else if (state.startsWith("08")) {
                        msg = "Could not connect to MySQL.\nIs the MySQL server running? "
                                + "Is db.url in config.properties right?\n\n" + msg;
                    }
                }
            }
        }
        show(parent, "Something went wrong", msg, Theme.DANGER, null, "OK", null);
    }

    /**
     * Builds and shows a dark SAHRA dialog. Returns "ok" when the main button is pressed,
     * null when cancelled or closed.
     */
    private static String show(Component parent, String title, String message, Color tone,
                               JComponent extra, String okText, String cancelText) {
        Window owner = parent instanceof Window w ? w : SwingUtilities.getWindowAncestor(parent);
        JDialog d = new JDialog(owner, "SAHRA", JDialog.ModalityType.APPLICATION_MODAL);
        String[] result = {null};

        JPanel body = new JPanel(new BorderLayout(16, 0));
        body.setBackground(Theme.SURFACE);
        body.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(3, 0, 0, 0, tone),
                BorderFactory.createEmptyBorder(24, 26, 10, 28)));

        // colored round badge on the left
        JComponent badge = new JComponent() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = Theme.smooth(g);
                g2.setColor(Theme.alpha(tone, 45));
                g2.fillOval(0, 0, 40, 40);
                g2.setColor(tone);
                g2.fillOval(14, 14, 12, 12);
                g2.dispose();
            }
        };
        badge.setPreferredSize(new Dimension(40, 40));
        JPanel badgeWrap = Theme.transparent(new BorderLayout());
        badgeWrap.add(badge, BorderLayout.NORTH);
        body.add(badgeWrap, BorderLayout.WEST);

        JPanel text = Theme.transparent(null);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        JLabel t = Theme.h3(title);
        String html = "<html><div style='width:320px'>"
                + (message == null ? "" : escape(message).replace("\n", "<br>")) + "</div></html>";
        JLabel m = Theme.text(html, Font.PLAIN, 14, Theme.MUTED);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        m.setAlignmentX(Component.LEFT_ALIGNMENT);
        text.add(t);
        text.add(Box.createVerticalStrut(6));
        text.add(m);
        if (extra != null) {
            text.add(Box.createVerticalStrut(14));
            extra.setAlignmentX(Component.LEFT_ALIGNMENT);
            extra.setMaximumSize(new Dimension(Integer.MAX_VALUE, extra.getPreferredSize().height));
            text.add(extra);
        }
        body.add(text, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setBackground(Theme.SURFACE);
        buttons.setBorder(BorderFactory.createEmptyBorder(14, 26, 22, 26));
        if (cancelText != null) {
            JButton cancel = Theme.secondary(cancelText);
            cancel.addActionListener(e -> d.dispose());
            buttons.add(cancel);
        }
        JButton ok = tone == Theme.DANGER ? Theme.secondary(okText) : Theme.primary(okText);
        ok.addActionListener(e -> { result[0] = "ok"; d.dispose(); });
        buttons.add(ok);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(Theme.SURFACE);
        content.add(body, BorderLayout.CENTER);
        content.add(buttons, BorderLayout.SOUTH);
        d.setContentPane(content);
        d.getRootPane().setDefaultButton(ok);
        if (extra instanceof JTextField f) f.addActionListener(e -> ok.doClick());
        d.pack();
        d.setMinimumSize(new Dimension(420, d.getHeight()));
        d.setResizable(false);
        d.setLocationRelativeTo(owner);
        if (extra != null) SwingUtilities.invokeLater(extra::requestFocusInWindow);
        d.setVisible(true);
        return result[0];
    }

    private static String escape(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
