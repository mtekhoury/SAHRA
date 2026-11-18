package sahra.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.RenderingHints;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.LookAndFeel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

/**
 * The look of SAHRA: a dark "night out" palette with a coral/purple glow,
 * plus small reusable pieces (buttons, chips, cards, avatars, vote bars, logo).
 * Uses FlatLaf automatically if its .jar is in the lib folder.
 */
final class Theme {

    private Theme() { }

    // ---------------- colors ----------------
    static final Color BG        = new Color(0x0E0F1A);
    static final Color SURFACE   = new Color(0x171827);
    static final Color SURFACE_2 = new Color(0x20223A);
    static final Color BORDER    = new Color(0x2C2F4A);
    static final Color TEXT      = new Color(0xEEEDF7);
    static final Color MUTED     = new Color(0x9B99B8);
    static final Color ACCENT    = new Color(0xFF7A59);   // coral
    static final Color ACCENT_2  = new Color(0x8B5CF6);   // purple
    static final Color SUCCESS   = new Color(0x34D399);
    static final Color WARNING   = new Color(0xFBBF24);
    static final Color DANGER    = new Color(0xF87171);
    static final Color GOLD      = new Color(0xFFC857);
    static final Color ROW_ALT   = new Color(0x1B1C2D);

    static boolean flatLaf;
    private static String family = Font.SANS_SERIF;

    /** Call once at startup, before any window is created. */
    static void install() {
        String[] installed = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        for (String f : new String[]{"Segoe UI Variable Text", "Segoe UI", "Inter", "Helvetica Neue", "Arial"}) {
            if (Arrays.asList(installed).contains(f)) { family = f; break; }
        }

        try {
            Class<?> base = Class.forName("com.formdev.flatlaf.FlatLaf");
            try {
                Map<String, String> vars = new HashMap<>();
                vars.put("@background", "#0E0F1A");
                vars.put("@foreground", "#EEEDF7");
                vars.put("@accentColor", "#FF7A59");
                base.getMethod("setGlobalExtraDefaults", Map.class).invoke(null, vars);
            } catch (ReflectiveOperationException older) {
                // older FlatLaf: fine, the colors below still apply
            }
            Class<?> dark = Class.forName("com.formdev.flatlaf.FlatDarkLaf");
            UIManager.setLookAndFeel((LookAndFeel) dark.getDeclaredConstructor().newInstance());
            flatLaf = true;
        } catch (Exception noFlatLaf) {
            // No FlatLaf in lib: use Java's Metal look with a fully dark SAHRA palette,
            // because the Windows look ignores custom colors (white-on-white text).
            try {
                javax.swing.plaf.metal.MetalLookAndFeel.setCurrentTheme(new DarkMetal());
                UIManager.setLookAndFeel(new javax.swing.plaf.metal.MetalLookAndFeel());
            } catch (Exception ignored) { }
        }

        Font base = new Font(family, Font.PLAIN, 14);
        UIManager.put("defaultFont", base);
        for (String key : new String[]{"Label.font", "Button.font", "TextField.font", "PasswordField.font",
                "ComboBox.font", "Spinner.font", "Table.font", "List.font", "TabbedPane.font",
                "CheckBox.font", "OptionPane.messageFont", "OptionPane.buttonFont", "TableHeader.font"}) {
            UIManager.put(key, base);
        }
        UIManager.put("Panel.background", BG);
if (!flatLaf) {   // borders and scrollbar colors for the fallback look only
            UIManager.put("TextField.border", BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(BORDER), BorderFactory.createEmptyBorder(4, 10, 4, 10)));
            UIManager.put("PasswordField.border", UIManager.get("TextField.border"));
            UIManager.put("FormattedTextField.border", BorderFactory.createEmptyBorder(4, 10, 4, 10));
            UIManager.put("Spinner.border", BorderFactory.createLineBorder(BORDER));
            UIManager.put("ScrollPane.border", BorderFactory.createEmptyBorder());
            UIManager.put("ScrollBar.thumb", new Color(0x34365A));
            UIManager.put("ScrollBar.thumbHighlight", new Color(0x34365A));
            UIManager.put("ScrollBar.thumbShadow", new Color(0x34365A));
            UIManager.put("ScrollBar.thumbDarkShadow", new Color(0x34365A));
            UIManager.put("ScrollBar.background", BG);
            UIManager.put("ScrollBar.shadow", BG);
            UIManager.put("ScrollBar.highlight", BG);
            UIManager.put("ScrollBar.darkShadow", BG);
            UIManager.put("Table.gridColor", SURFACE);
            UIManager.put("Table.focusCellHighlightBorder", BorderFactory.createEmptyBorder());
            UIManager.put("TableHeader.cellBorder", BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER), BorderFactory.createEmptyBorder(0, 14, 0, 14)));
            UIManager.put("CheckBox.background", SURFACE);
            UIManager.put("Spinner.arrowButtonBorder", BorderFactory.createEmptyBorder());
        }
        UIManager.put("OptionPane.background", SURFACE);
        UIManager.put("Component.arc", 14);
        UIManager.put("Button.arc", 14);
        UIManager.put("TextComponent.arc", 12);
        UIManager.put("CheckBox.arc", 6);
        UIManager.put("Component.focusWidth", 1);
        UIManager.put("Component.innerFocusWidth", 0);
        UIManager.put("Component.focusColor", ACCENT);
        UIManager.put("Component.focusedBorderColor", ACCENT);
        UIManager.put("Component.borderColor", BORDER);
        for (String k : new String[]{"TextField", "PasswordField", "FormattedTextField", "ComboBox", "Spinner",
                "Label", "CheckBox", "List", "Table", "TextArea"}) {
            UIManager.put(k + ".foreground", TEXT);
        }
        UIManager.put("TextField.caretForeground", TEXT);
        UIManager.put("PasswordField.caretForeground", TEXT);
        UIManager.put("FormattedTextField.caretForeground", TEXT);
        UIManager.put("TextField.placeholderForeground", new Color(0x6E6C8C));
        UIManager.put("ComboBox.selectionBackground", new Color(0x3B2A55));
        UIManager.put("ComboBox.buttonBackground", SURFACE_2);
        UIManager.put("ComboBox.popupBackground", SURFACE_2);
        UIManager.put("TextField.background", SURFACE_2);
        UIManager.put("PasswordField.background", SURFACE_2);
        UIManager.put("FormattedTextField.background", SURFACE_2);
        UIManager.put("ComboBox.background", SURFACE_2);
        UIManager.put("Spinner.background", SURFACE_2);
        UIManager.put("ScrollBar.thumbArc", 999);
        UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
        UIManager.put("ScrollBar.width", 10);
        UIManager.put("ScrollBar.track", BG);
        UIManager.put("ScrollPane.background", BG);
        UIManager.put("TabbedPane.underlineColor", ACCENT);
        UIManager.put("TabbedPane.inactiveUnderlineColor", ACCENT);
        UIManager.put("TabbedPane.tabHeight", 44);
        UIManager.put("TabbedPane.selectedBackground", BG);
        UIManager.put("TabbedPane.hoverColor", SURFACE);
        UIManager.put("TabbedPane.background", BG);
        UIManager.put("TabbedPane.tabInsets", new Insets(4, 18, 4, 18));
        UIManager.put("Table.background", SURFACE);
        UIManager.put("Table.alternateRowColor", new Color(0x1B1C2D));
        UIManager.put("Table.selectionBackground", new Color(0x3B2A55));
        UIManager.put("Table.selectionForeground", TEXT);
        UIManager.put("TableHeader.background", SURFACE_2);
        UIManager.put("TableHeader.foreground", MUTED);
        UIManager.put("TableHeader.separatorColor", SURFACE_2);
        UIManager.put("TableHeader.bottomSeparatorColor", BORDER);
        UIManager.put("List.background", SURFACE);
        UIManager.put("List.selectionBackground", new Color(0x2A2140));
        UIManager.put("TitlePane.unifiedBackground", true);
        UIManager.put("TitlePane.background", BG);
        UIManager.put("Popup.dropShadowPainted", true);
    }

    /** Dark colors for Java's built-in Metal look (used only when FlatLaf is missing). */
    private static class DarkMetal extends javax.swing.plaf.metal.DefaultMetalTheme {
        private static javax.swing.plaf.ColorUIResource c(Color x) { return new javax.swing.plaf.ColorUIResource(x); }
        private static javax.swing.plaf.FontUIResource f(int style, int size) {
            return new javax.swing.plaf.FontUIResource(family, style, size);
        }
        @Override public String getName() { return "SAHRA Dark"; }
        @Override protected javax.swing.plaf.ColorUIResource getPrimary1()   { return c(ACCENT); }
        @Override protected javax.swing.plaf.ColorUIResource getPrimary2()   { return c(new Color(0x3B2A55)); }
        @Override protected javax.swing.plaf.ColorUIResource getPrimary3()   { return c(new Color(0x3B2A55)); }
        @Override protected javax.swing.plaf.ColorUIResource getSecondary1() { return c(BORDER); }
        @Override protected javax.swing.plaf.ColorUIResource getSecondary2() { return c(BORDER); }
        @Override protected javax.swing.plaf.ColorUIResource getSecondary3() { return c(SURFACE_2); }
        @Override protected javax.swing.plaf.ColorUIResource getBlack()      { return c(TEXT); }
        @Override protected javax.swing.plaf.ColorUIResource getWhite()      { return c(SURFACE_2); }
        @Override public javax.swing.plaf.ColorUIResource getWindowBackground()   { return c(SURFACE_2); }
        @Override public javax.swing.plaf.ColorUIResource getControl()            { return c(SURFACE_2); }
        @Override public javax.swing.plaf.ColorUIResource getControlTextColor()   { return c(TEXT); }
        @Override public javax.swing.plaf.ColorUIResource getSystemTextColor()    { return c(TEXT); }
        @Override public javax.swing.plaf.ColorUIResource getUserTextColor()      { return c(TEXT); }
        @Override public javax.swing.plaf.ColorUIResource getInactiveSystemTextColor() { return c(MUTED); }
        @Override public javax.swing.plaf.ColorUIResource getInactiveControlTextColor() { return c(MUTED); }
        @Override public javax.swing.plaf.ColorUIResource getMenuBackground()     { return c(SURFACE_2); }
        @Override public javax.swing.plaf.ColorUIResource getFocusColor()         { return c(ACCENT); }
        @Override public javax.swing.plaf.ColorUIResource getTextHighlightColor() { return c(new Color(0x5B3F86)); }
        @Override public javax.swing.plaf.ColorUIResource getHighlightedTextColor() { return c(TEXT); }
        @Override public javax.swing.plaf.FontUIResource getControlTextFont() { return f(Font.PLAIN, 14); }
        @Override public javax.swing.plaf.FontUIResource getSystemTextFont()  { return f(Font.PLAIN, 14); }
        @Override public javax.swing.plaf.FontUIResource getUserTextFont()    { return f(Font.PLAIN, 14); }
        @Override public javax.swing.plaf.FontUIResource getMenuTextFont()    { return f(Font.PLAIN, 14); }
        @Override public javax.swing.plaf.FontUIResource getWindowTitleFont() { return f(Font.BOLD, 14); }
        @Override public javax.swing.plaf.FontUIResource getSubTextFont()     { return f(Font.PLAIN, 12); }
    }

    // ---------------- fonts & labels ----------------
    static Font font(int style, float size) {
        return new Font(family, style, Math.round(size));
    }

    static JLabel text(String s, int style, float size, Color color) {
        JLabel l = new JLabel(s);
        l.setFont(font(style, size));
        l.setForeground(color);
        return l;
    }

    static JLabel h1(String s)    { return text(s, Font.BOLD, 28, TEXT); }
    static JLabel h2(String s)    { return text(s, Font.BOLD, 20, TEXT); }
    static JLabel h3(String s)    { return text(s, Font.BOLD, 16, TEXT); }
    static JLabel body(String s)  { return text(s, Font.PLAIN, 14, TEXT); }
    static JLabel muted(String s) { return text(s, Font.PLAIN, 13, MUTED); }
    static JLabel caps(String s)  { return text(s.toUpperCase(), Font.BOLD, 11, MUTED); }

    // ---------------- buttons ----------------
    private static JButton base(String s, Color bg, Color fg) {
        return new Pill(s, bg, fg);
    }

    /** Rounded button that paints itself, so it looks the same with any look-and-feel. */
    static class Pill extends JButton {
        private final Color fill;
        private boolean hover;

        Pill(String s, Color fill, Color fg) {
            super(s);
            this.fill = fill;
            setFont(font(Font.BOLD, 14));
            setForeground(fg);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new java.awt.event.MouseAdapter() {
                @Override public void mouseEntered(java.awt.event.MouseEvent e) { hover = true; repaint(); }
                @Override public void mouseExited(java.awt.event.MouseEvent e)  { hover = false; repaint(); }
            });
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            Color c = fill;
            if (!isEnabled()) c = alpha(fill, 90);
            else if (getModel().isPressed()) c = fill.darker();
            else if (hover) c = mix(fill, Color.WHITE, 0.12);
            g2.setColor(c);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Rounded toggle (used for the type / area / vibe choices). */
    static class PillToggle extends javax.swing.JToggleButton {
        private final Color tint;

        PillToggle(String s, Color tint) {
            super(s);
            this.tint = tint;
            setFont(font(Font.BOLD, 13));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addItemListener(e -> setForeground(isSelected() ? Color.WHITE : MUTED));
            setForeground(MUTED);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            if (isSelected()) {
                g2.setColor(alpha(tint, 60));
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
                g2.setColor(tint);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 14, 14);
            } else {
                g2.setColor(getModel().isRollover() ? mix(SURFACE_2, Color.WHITE, 0.06) : SURFACE_2);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 14, 14);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static Color mix(Color a, Color b, double t) {
        return new Color((int) (a.getRed() + (b.getRed() - a.getRed()) * t),
                (int) (a.getGreen() + (b.getGreen() - a.getGreen()) * t),
                (int) (a.getBlue() + (b.getBlue() - a.getBlue()) * t), a.getAlpha());
    }

    static JButton primary(String s) { return base(s, ACCENT, Color.WHITE); }
    static JButton secondary(String s) { return base(s, SURFACE_2, TEXT); }
    static JButton danger(String s) { return base(s, new Color(0x3A1E26), DANGER); }

    static JButton link(String s) {
        JButton b = new JButton(s);
        b.setFont(font(Font.BOLD, 13));
        b.setForeground(ACCENT);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    // ---------------- inputs ----------------
    static <T extends JComponent> T placeholder(T field, String hint) {
        field.putClientProperty("JTextField.placeholderText", hint);
        return field;
    }

    static <T extends JComponent> T tall(T c) {
        Dimension d = c.getPreferredSize();
        c.setPreferredSize(new Dimension(Math.max(d.width, 120), 38));
        return c;
    }

    static JTextField field(int cols, String hint) {
        JTextField f = new JTextField(cols) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                paintHint(this, g, hint);
            }
        };
        return tall(placeholder(f, hint));
    }

    static javax.swing.JPasswordField passwordField(int cols, String hint) {
        javax.swing.JPasswordField f = new javax.swing.JPasswordField(cols) {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                paintHint(this, g, hint);
            }
        };
        return tall(placeholder(f, hint));
    }

    /** Grey hint text inside an empty box (FlatLaf draws its own, so only needed without it). */
    private static void paintHint(javax.swing.text.JTextComponent f, Graphics g, String hint) {
        if (flatLaf || hint == null || !f.getText().isEmpty()) return;
        Graphics2D g2 = smooth(g);
        g2.setColor(new Color(0x6E6C8C));
        g2.setFont(f.getFont());
        Insets in = f.getInsets();
        var fm = g2.getFontMetrics();
        int y = (f.getHeight() - fm.getHeight()) / 2 + fm.getAscent();
        g2.drawString(hint, in.left, y);
        g2.dispose();
    }

    // ---------------- containers ----------------
    static JPanel transparent(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setOpaque(false);
        return p;
    }

    static JScrollPane scroll(Component c) {
        JScrollPane s = new JScrollPane(c);
        s.setBorder(BorderFactory.createEmptyBorder());
        s.getViewport().setOpaque(false);
        s.setOpaque(false);
        s.getVerticalScrollBar().setUnitIncrement(18);
        return s;
    }

    static void styleTable(JTable t) {
        t.setRowHeight(38);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFillsViewportHeight(true);
        t.setBackground(SURFACE);
        t.setForeground(TEXT);
        t.getTableHeader().setPreferredSize(new Dimension(0, 36));
        t.getTableHeader().setFont(font(Font.BOLD, 12));
        DefaultTableCellRenderer padded = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean selected, boolean focus, int row, int col) {
                super.getTableCellRendererComponent(table, value, selected, false, row, col);
                if (!selected) setBackground(row % 2 == 0 ? SURFACE : ROW_ALT);
                setForeground(TEXT);
                setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
                return this;
            }
        };
        t.setDefaultRenderer(Object.class, padded);
    }

    /** Distinct color per activity type, used for chips. */
    static Color typeColor(String type) {
        if (type == null) return MUTED;
        return switch (type) {
            case "Restaurant"  -> new Color(0xFF7A59);
            case "Cinema"      -> new Color(0x60A5FA);
            case "Party"       -> new Color(0xF472B6);
            case "Concert"     -> new Color(0xA78BFA);
            case "Karting"     -> new Color(0xFBBF24);
            case "Paintball"   -> new Color(0x34D399);
            case "Bowling"     -> new Color(0x22D3EE);
            case "Escape Room" -> new Color(0xFB923C);
            default -> MUTED;
        };
    }

    /** Pleasant color picked from a name, for avatars. */
    static Color nameColor(String name) {
        Color[] palette = {new Color(0xFF7A59), new Color(0x8B5CF6), new Color(0x22D3EE), new Color(0xF472B6),
                new Color(0x34D399), new Color(0xFBBF24), new Color(0x60A5FA), new Color(0xFB923C)};
        if (name == null) return palette[0];
        int h = 7;
        for (char ch : name.toCharArray()) h = h * 131 + ch;
        return palette[Math.floorMod(h ^ (h >>> 7), palette.length)];
    }

    static String initials(String name) {
        if (name == null || name.isBlank()) return "?";
        String[] parts = name.trim().split("\\s+");
        String first = parts[0].substring(0, 1);
        String last = parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "";
        return (first + last).toUpperCase();
    }

    static Graphics2D smooth(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        return g2;
    }

    static Color alpha(Color c, int a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }

    // =================================================================
    //  Reusable painted components
    // =================================================================

    /** A rounded card with an optional colored border. */
    static class Card extends JPanel {
        Color fill = SURFACE;
        Color stroke = BORDER;
        float strokeWidth = 1f;
        int arc = 20;

        Card(LayoutManager layout) {
            super(layout);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        }

        Card fill(Color c)   { fill = c; return this; }
        Card stroke(Color c, float w) { stroke = c; strokeWidth = w; return this; }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            g2.setColor(fill);
            g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            if (stroke != null) {
                g2.setColor(stroke);
                g2.setStroke(new BasicStroke(strokeWidth));
                float o = strokeWidth / 2f;
                g2.drawRoundRect(Math.round(o), Math.round(o), Math.round(getWidth() - 1 - strokeWidth),
                        Math.round(getHeight() - 1 - strokeWidth), arc, arc);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Panel painted with a diagonal purple-to-coral gradient (the SAHRA "night glow"). */
    static class Glow extends JPanel {
        int arc;

        Glow(LayoutManager layout, int arc) {
            super(layout);
            this.arc = arc;
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            g2.setPaint(new GradientPaint(0, 0, new Color(0x4C1D95), getWidth(), getHeight(), new Color(0xE8634A)));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            // soft stars
            g2.setColor(new Color(255, 255, 255, 70));
            int[][] stars = {{8, 12}, {22, 30}, {40, 9}, {63, 22}, {78, 14}, {90, 35}, {15, 55}, {85, 60}, {55, 45}};
            for (int[] s : stars) {
                int x = getWidth() * s[0] / 100;
                int y = getHeight() * s[1] / 100;
                g2.fillOval(x, y, 3, 3);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Small rounded label with a tinted background, e.g. "Restaurant" or "Voting open". */
    static class Chip extends JLabel {
        private final Color color;

        Chip(String text, Color color) {
            super(text);
            this.color = color;
            setFont(font(Font.BOLD, 12));
            setForeground(color);
            setBorder(BorderFactory.createEmptyBorder(4, 11, 4, 11));
            setOpaque(false);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            g2.setColor(alpha(color, 38));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), getHeight(), getHeight());
            g2.dispose();
            super.paintComponent(g);
        }
    }

    /** Circle with initials. */
    static class Avatar extends JComponent {
        private final String initials;
        private final Color color;
        private final int size;

        Avatar(String name, int size) {
            this.initials = Theme.initials(name);
            this.color = nameColor(name);
            this.size = size;
            Dimension d = new Dimension(size, size);
            setPreferredSize(d);
            setMinimumSize(d);
            setMaximumSize(d);
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            g2.setColor(alpha(color, 60));
            g2.fillOval(0, 0, size - 1, size - 1);
            g2.setColor(color);
            g2.setFont(font(Font.BOLD, size * 0.38f));
            var fm = g2.getFontMetrics();
            int x = (size - fm.stringWidth(initials)) / 2;
            int y = (size - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(initials, x, y);
            g2.dispose();
        }
    }

    /** Horizontal rounded progress bar used to show the share of votes. */
    static class Bar extends JComponent {
        private double fraction;
        private final Color color;

        Bar(double fraction, Color color) {
            this.fraction = Math.max(0, Math.min(1, fraction));
            this.color = color;
            setPreferredSize(new Dimension(120, 8));
        }

        @Override protected void paintComponent(Graphics g) {
            Graphics2D g2 = smooth(g);
            int h = getHeight();
            g2.setColor(SURFACE_2);
            g2.fillRoundRect(0, 0, getWidth(), h, h, h);
            int w = (int) Math.round(getWidth() * fraction);
            if (w > 0) {
                g2.setPaint(new GradientPaint(0, 0, ACCENT_2, getWidth(), 0, color));
                g2.fillRoundRect(0, 0, Math.max(w, h), h, h, h);
            }
            g2.dispose();
        }
    }

    /** The SAHRA logo: a crescent moon. */
    static class Moon implements Icon {
        private final int size;
        private final Color color;

        Moon(int size, Color color) {
            this.size = size;
            this.color = color;
        }

        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = smooth(g);
            Area moon = new Area(new Ellipse2D.Double(x, y, size, size));
            moon.subtract(new Area(new Ellipse2D.Double(x + size * 0.32, y - size * 0.12, size, size)));
            g2.setColor(color);
            g2.fill(moon);
            g2.dispose();
        }

        @Override public int getIconWidth()  { return size; }
        @Override public int getIconHeight() { return size; }
    }

    /** Wordmark: moon + "SAHRA". */
    static JLabel logo(float size) {
        JLabel l = text("SAHRA", Font.BOLD, size, TEXT);
        l.setIcon(new Moon(Math.round(size * 0.95f), GOLD));
        l.setIconTextGap(Math.round(size * 0.4f));
        l.setHorizontalTextPosition(SwingConstants.RIGHT);
        return l;
    }
}
