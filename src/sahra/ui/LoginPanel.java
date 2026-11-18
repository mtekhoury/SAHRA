package sahra.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagLayout;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import sahra.db.Models.User;
import sahra.db.UserDao;

/** Split screen: a glowing brand panel on the left, the log-in / sign-up card on the right. */
class LoginPanel extends JPanel {

    private final MainFrame frame;
    private boolean signUpMode;

    private final JLabel heading = Theme.h2("Welcome back");
    private final JLabel subheading = Theme.muted("Log in to plan your next night out.");
    private final JTextField name = Theme.field(24, "Full name");
    private final JTextField email = Theme.field(24, "you@example.com");
    private final JPasswordField password = Theme.passwordField(24, "Password");
    private final JLabel nameLabel = Theme.caps("Full name");
    private final JButton submit = Theme.primary("Log in");
    private final JLabel switchText = Theme.muted("New to SAHRA?");
    private final JButton switchMode = Theme.link("Create an account");
    private final JLabel hint = Theme.muted("Test account: maria@sahra.com  /  1234");

    LoginPanel(MainFrame frame) {
        super(new BorderLayout());
        this.frame = frame;
        setBackground(Theme.BG);

        add(brandPanel(), BorderLayout.WEST);
        add(formPanel(), BorderLayout.CENTER);

        submit.addActionListener(e -> submit());
        password.addActionListener(e -> submit());
        switchMode.addActionListener(e -> toggleMode());
        applyMode();
    }

    // ---------------- left: brand ----------------
    private JComponent brandPanel() {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setBackground(Theme.BG);
        outer.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 0));
        outer.setPreferredSize(new Dimension(470, 0));

        Theme.Glow glow = new Theme.Glow(null, 28);
        glow.setLayout(new BoxLayout(glow, BoxLayout.Y_AXIS));
        glow.setBorder(BorderFactory.createEmptyBorder(44, 44, 44, 44));

        JLabel logo = Theme.logo(30);
        JLabel headline = Theme.text("<html>Plan the night.<br>Not the group chat.</html>",
                Font.BOLD, 34, Color.WHITE);
        JLabel tagline = Theme.text("<html>Pick your vibe and budget, let SAHRA find the places, "
                + "and let your friends vote.</html>", Font.PLAIN, 15, new Color(255, 255, 255, 210));

        glow.add(left(logo));
        glow.add(Box.createVerticalGlue());
        glow.add(left(headline));
        glow.add(Box.createVerticalStrut(14));
        glow.add(left(tagline));
        glow.add(Box.createVerticalStrut(34));
        glow.add(left(step("1", "Choose type, area, atmosphere and budget")));
        glow.add(Box.createVerticalStrut(12));
        glow.add(left(step("2", "Everyone votes on the matching places")));
        glow.add(Box.createVerticalStrut(12));
        glow.add(left(step("3", "The most-voted place wins. Go out!")));
        glow.add(Box.createVerticalGlue());

        outer.add(glow, BorderLayout.CENTER);
        return outer;
    }

    private static JComponent step(String number, String text) {
        JPanel row = Theme.transparent(new FlowLayout(FlowLayout.LEFT, 12, 0));
        JLabel n = new JLabel(number, JLabel.CENTER) {
            @Override protected void paintComponent(java.awt.Graphics g) {
                var g2 = Theme.smooth(g);
                g2.setColor(new Color(255, 255, 255, 45));
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        n.setFont(Theme.font(Font.BOLD, 13));
        n.setForeground(Color.WHITE);
        n.setPreferredSize(new Dimension(28, 28));
        row.add(n);
        row.add(Theme.text(text, Font.PLAIN, 14, Color.WHITE));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, row.getPreferredSize().height));
        return row;
    }

    private static JComponent left(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        return c;
    }

    // ---------------- right: form card ----------------
    private JComponent formPanel() {
        JPanel center = new JPanel(new GridBagLayout());
        center.setBackground(Theme.BG);

        Theme.Card card = new Theme.Card(null);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(36, 40, 32, 40));
        card.setPreferredSize(new Dimension(440, 520));

        card.add(left(heading));
        card.add(Box.createVerticalStrut(6));
        card.add(left(subheading));
        card.add(Box.createVerticalStrut(26));
        card.add(left(nameLabel));
        card.add(Box.createVerticalStrut(6));
        card.add(left(stretch(name)));
        card.add(Box.createVerticalStrut(16));
        card.add(left(Theme.caps("Email")));
        card.add(Box.createVerticalStrut(6));
        card.add(left(stretch(email)));
        card.add(Box.createVerticalStrut(16));
        card.add(left(Theme.caps("Password")));
        card.add(Box.createVerticalStrut(6));
        card.add(left(stretch(password)));
        card.add(Box.createVerticalStrut(24));
        submit.setPreferredSize(new Dimension(360, 44));
        card.add(left(stretch(submit)));
        card.add(Box.createVerticalStrut(14));

        JPanel switchRow = Theme.transparent(new FlowLayout(FlowLayout.LEFT, 4, 0));
        switchRow.add(switchText);
        switchRow.add(switchMode);
        card.add(left(switchRow));
        card.add(Box.createVerticalGlue());
        card.add(left(hint));

        center.add(card);
        return center;
    }

    private static <T extends JComponent> T stretch(T c) {
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
        return c;
    }

    private void toggleMode() {
        signUpMode = !signUpMode;
        applyMode();
    }

    private void applyMode() {
        name.setVisible(signUpMode);
        nameLabel.setVisible(signUpMode);
        heading.setText(signUpMode ? "Create your account" : "Welcome back");
        subheading.setText(signUpMode ? "Join your friends and start planning." : "Log in to plan your next night out.");
        submit.setText(signUpMode ? "Create account" : "Log in");
        switchText.setText(signUpMode ? "Already have an account?" : "New to SAHRA?");
        switchMode.setText(signUpMode ? "Log in" : "Create an account");
        hint.setVisible(!signUpMode);
        revalidate();
        repaint();
    }

    private void submit() {
        String pass = new String(password.getPassword());
        try {
            if (signUpMode) {
                if (name.getText().isBlank() || email.getText().isBlank() || pass.length() < 4) {
                    Ui.info(this, "Please fill in every field (password: 4+ characters).");
                    return;
                }
                User u = UserDao.register(name.getText(), email.getText(), pass);
                password.setText("");
                frame.showHome(u);
            } else {
                User u = UserDao.login(email.getText(), pass);
                if (u == null) {
                    Ui.info(this, "Wrong email or password.");
                    return;
                }
                password.setText("");
                frame.showHome(u);
            }
        } catch (Exception ex) {
            Ui.error(this, ex);
        }
    }
}
