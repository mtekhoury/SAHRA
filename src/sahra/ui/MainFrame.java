package sahra.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ButtonGroup;
import javax.swing.JToggleButton;

import sahra.db.Models.User;

/** The application window. Shows the login screen first, then the main tabs. */
public class MainFrame extends JFrame {

    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private User currentUser;
    private JPanel home;

    /** Sets up colors, fonts and the FlatLaf look. Call before creating the window. */
    public static void installTheme() {
        Theme.install();
    }

    public MainFrame() {
        super("SAHRA");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1120, 740));
        root.setBackground(Theme.BG);
        root.add(new LoginPanel(this), "login");
        setContentPane(root);
        getRootPane().putClientProperty("JRootPane.titleBarBackground", Theme.BG);
        getRootPane().putClientProperty("JRootPane.titleBarForeground", Theme.TEXT);
        setLocationRelativeTo(null);
    }

    /** Top-bar navigation item: text that lights up with a coral underline when selected. */
    private static class NavTab extends JToggleButton {
        NavTab(String text) {
            super(text);
            setFont(Theme.font(Font.BOLD, 14));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setBorder(BorderFactory.createEmptyBorder(10, 14, 12, 14));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override protected void paintComponent(Graphics g) {
            setForeground(isSelected() ? Theme.TEXT : (getModel().isRollover() ? Theme.TEXT : Theme.MUTED));
            super.paintComponent(g);
            if (isSelected()) {
                Graphics2D g2 = Theme.smooth(g);
                g2.setColor(Theme.ACCENT);
                int w = getWidth() - 28;
                g2.fillRoundRect(14, getHeight() - 4, w, 3, 3, 3);
                g2.dispose();
            }
        }
    }

    User currentUser() {
        return currentUser;
    }

    /** Called by the login screen once the user is signed in. */
    void showHome(User user) {
        currentUser = user;
        if (home != null) root.remove(home);   // drop the previous user's screens

        home = new JPanel(new BorderLayout());
        home.setBackground(Theme.BG);

        // ----- top bar: logo on the left, user on the right -----
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(Theme.BG);
        top.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Theme.BORDER),
                BorderFactory.createEmptyBorder(14, 28, 14, 28)));

        JPanel who = Theme.transparent(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        JPanel names = Theme.transparent(null);
        names.setLayout(new BoxLayout(names, BoxLayout.Y_AXIS));
        JLabel name = Theme.text(user.fullName(), Font.BOLD, 14, Theme.TEXT);
        JLabel mail = Theme.muted(user.email());
        name.setAlignmentX(RIGHT_ALIGNMENT);
        mail.setAlignmentX(RIGHT_ALIGNMENT);
        names.add(name);
        names.add(mail);
        JButton logout = Theme.secondary("Log out");
        logout.addActionListener(e -> {
            currentUser = null;
            cards.show(root, "login");
        });
        who.add(names);
        who.add(new Theme.Avatar(user.fullName(), 40));
        who.add(logout);
        top.add(who, BorderLayout.EAST);

        // ----- navigation (in the top bar) + pages -----
        CardLayout pageCards = new CardLayout();
        JPanel pages = new JPanel(pageCards);
        pages.setBackground(Theme.BG);
        pages.add(new GroupsPanel(this), "groups");
        pages.add(new ActivitiesPanel(), "explore");

        JPanel nav = Theme.transparent(new FlowLayout(FlowLayout.LEFT, 6, 0));
        nav.setBorder(BorderFactory.createEmptyBorder(0, 40, 0, 0));
        ButtonGroup navGroup = new ButtonGroup();
        String[][] items = {{"Groups & SAHRAs", "groups"}, {"Explore places", "explore"}};
        for (String[] item : items) {
            NavTab tab = new NavTab(item[0]);
            tab.addActionListener(e -> pageCards.show(pages, item[1]));
            navGroup.add(tab);
            nav.add(tab);
            if (item[1].equals("groups")) tab.setSelected(true);
        }
        JPanel leftSide = Theme.transparent(new BorderLayout());
        leftSide.add(Theme.logo(24), BorderLayout.WEST);
        leftSide.add(nav, BorderLayout.CENTER);
        top.add(leftSide, BorderLayout.WEST);

        home.add(top, BorderLayout.NORTH);
        home.add(pages, BorderLayout.CENTER);

        root.add(home, "home");
        cards.show(root, "home");
    }
}
