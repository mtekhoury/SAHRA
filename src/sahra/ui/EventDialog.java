package sahra.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import sahra.db.EventDao;
import sahra.db.Models.Event;
import sahra.db.Models.ResultRow;
import sahra.db.Models.Voter;

/** One SAHRA: banner with the filters, the matching places as vote cards, and who voted. */
class EventDialog extends JDialog {

    private final MainFrame frame;
    private final int eventId;

    EventDialog(MainFrame frame, int eventId) {
        super(frame, "SAHRA", true);
        this.frame = frame;
        this.eventId = eventId;
        getContentPane().setBackground(Theme.BG);
        setSize(new Dimension(1060, 700));
        setLocationRelativeTo(frame);
        reload();
    }

    /** Rebuilds the whole window from the database (after every vote). */
    private void reload() {
        try {
            Event ev = EventDao.get(eventId);
            if (ev == null) { dispose(); return; }
            List<ResultRow> rows = EventDao.results(eventId);
            List<Voter> voters = EventDao.voters(eventId);
            Integer mine = EventDao.myVote(eventId, frame.currentUser().id());

            setTitle("SAHRA – " + ev.title());
            JPanel content = new JPanel(new BorderLayout());
            content.setBackground(Theme.BG);
            content.add(banner(ev), BorderLayout.NORTH);
            content.add(body(ev, rows, voters, mine), BorderLayout.CENTER);
            content.add(footer(ev), BorderLayout.SOUTH);
            setContentPane(content);
            revalidate();
            repaint();
        } catch (Exception ex) {
            Ui.error(this, ex);
        }
    }

    // ---------------- banner ----------------
    private JComponent banner(Event ev) {
        JPanel wrap = new JPanel(new BorderLayout());
        wrap.setBackground(Theme.BG);
        wrap.setBorder(BorderFactory.createEmptyBorder(18, 18, 0, 18));

        Theme.Glow glow = new Theme.Glow(new BorderLayout(), 22);
        glow.setBorder(BorderFactory.createEmptyBorder(22, 28, 22, 28));

        JPanel left = Theme.transparent(null);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        JLabel title = Theme.text(ev.title(), Font.BOLD, 28, Color.WHITE);
        JLabel date = Theme.text(ev.plannedDate().format(GroupsPanel.DATE), Font.PLAIN, 14,
                new Color(255, 255, 255, 215));
        JPanel chips = Theme.transparent(new FlowLayout(FlowLayout.LEFT, 6, 0));
        for (String s : new String[]{ev.type(), ev.area(), ev.atmosphere(),
                Ui.money(ev.minBudget()) + " – " + Ui.money(ev.maxBudget()) + " / person"}) {
            chips.add(new Theme.Chip(s, Color.WHITE));
        }
        chips.setBorder(BorderFactory.createEmptyBorder(12, -6, 0, 0));
        for (JComponent c : new JComponent[]{title, date, chips}) c.setAlignmentX(Component.LEFT_ALIGNMENT);
        left.add(title);
        left.add(Box.createVerticalStrut(2));
        left.add(date);
        left.add(chips);
        glow.add(left, BorderLayout.CENTER);

        boolean voting = "VOTING".equals(ev.status());
        JPanel right = Theme.transparent(new FlowLayout(FlowLayout.RIGHT, 0, 6));
        right.add(new Theme.Chip(voting ? "Voting open" : "Decided", voting ? Theme.WARNING : Theme.SUCCESS));
        glow.add(right, BorderLayout.EAST);

        wrap.add(glow);
        return wrap;
    }

    // ---------------- body ----------------
    private JComponent body(Event ev, List<ResultRow> rows, List<Voter> voters, Integer mine) {
        boolean voting = "VOTING".equals(ev.status());
        int members = Math.max(1, voters.size());
        int maxVotes = rows.stream().mapToInt(ResultRow::votes).max().orElse(0);

        JPanel body = new JPanel(new BorderLayout(18, 0));
        body.setBackground(Theme.BG);
        body.setBorder(BorderFactory.createEmptyBorder(18, 18, 0, 18));

        // left column: notice + result cards
        JPanel list = Theme.transparent(null);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));

        JComponent notice = notice(ev, rows);
        if (notice != null) {
            list.add(notice);
            list.add(Box.createVerticalStrut(14));
        }
        for (ResultRow r : rows) {
            list.add(resultCard(ev, r, members, maxVotes, voting, mine));
            list.add(Box.createVerticalStrut(10));
        }
        JPanel holder = Theme.transparent(new BorderLayout());
        holder.add(list, BorderLayout.NORTH);
        body.add(Theme.scroll(holder), BorderLayout.CENTER);

        // right column: who voted
        body.add(votersCard(voters), BorderLayout.EAST);
        return body;
    }

    private JComponent notice(Event ev, List<ResultRow> rows) {
        String text;
        Color color;
        if (!"VOTING".equals(ev.status())) {
            text = "It's decided. The group is going to " + ev.finalName() + ".";
            color = Theme.SUCCESS;
        } else if (rows.isEmpty()) {
            text = "No places of this type exist yet. Add some in the Explore places tab.";
            color = Theme.DANGER;
        } else if (ev.fallback()) {
            text = "No exact match, so here are the closest places, starting from your minimum budget ("
                    + Ui.money(ev.minBudget()) + ").";
            color = Theme.WARNING;
        } else {
            return null;
        }
        Theme.Card card = new Theme.Card(new BorderLayout());
        card.fill(Theme.alpha(color, 28)).stroke(Theme.alpha(color, 90), 1f);
        card.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));
        card.add(Theme.text(text, Font.BOLD, 13, color));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
        return card;
    }

    private JComponent resultCard(Event ev, ResultRow r, int members, int maxVotes, boolean voting, Integer mine) {
        boolean isMine = mine != null && mine == r.activityId();
        boolean winner = !voting && ev.finalActivityId() != null && ev.finalActivityId() == r.activityId();
        boolean leading = voting && r.votes() > 0 && r.votes() == maxVotes;

        Theme.Card card = new Theme.Card(new BorderLayout(16, 0));
        card.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        if (winner) card.stroke(Theme.GOLD, 2f).fill(new Color(0x221F2A));
        else if (isMine) card.stroke(Theme.alpha(Theme.ACCENT, 160), 1.5f);

        // rank badge
        JLabel rank = new JLabel(String.valueOf(r.rank()), JLabel.CENTER) {
            @Override protected void paintComponent(java.awt.Graphics g) {
                var g2 = Theme.smooth(g);
                g2.setColor(winner ? Theme.alpha(Theme.GOLD, 60) : Theme.SURFACE_2);
                g2.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        rank.setFont(Theme.font(Font.BOLD, 15));
        rank.setForeground(winner ? Theme.GOLD : Theme.TEXT);
        rank.setPreferredSize(new Dimension(40, 40));
        JPanel rankWrap = Theme.transparent(new FlowLayout(FlowLayout.CENTER, 0, 4));
        rankWrap.add(rank);
        card.add(rankWrap, BorderLayout.WEST);

        // name, details, vote bar
        JPanel mid = Theme.transparent(null);
        mid.setLayout(new BoxLayout(mid, BoxLayout.Y_AXIS));
        JLabel name = Theme.h3(r.name());
        JLabel info = Theme.muted(r.area() + "   ·   " + r.atmosphere() + "   ·   " + Ui.money(r.price()) + " / person");
        JPanel barRow = Theme.transparent(new BorderLayout(10, 0));
        Theme.Bar bar = new Theme.Bar((double) r.votes() / members, winner ? Theme.GOLD : Theme.ACCENT);
        barRow.add(bar, BorderLayout.CENTER);
        barRow.add(Theme.text(r.votes() + (r.votes() == 1 ? " vote" : " votes"), Font.BOLD, 12, Theme.MUTED),
                BorderLayout.EAST);
        barRow.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        barRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));
        for (JComponent c : new JComponent[]{name, info, barRow}) c.setAlignmentX(Component.LEFT_ALIGNMENT);
        mid.add(name);
        mid.add(Box.createVerticalStrut(3));
        mid.add(info);
        mid.add(barRow);
        card.add(mid, BorderLayout.CENTER);

        // chips + buttons
        JPanel right = Theme.transparent(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        if (winner) right.add(new Theme.Chip("Winner", Theme.GOLD));
        else if (leading) right.add(new Theme.Chip("Leading", Theme.ACCENT_2));
        right.add("EXACT".equals(r.matchType()) ? new Theme.Chip("Perfect match", Theme.SUCCESS)
                                                : new Theme.Chip("Closest option", Theme.WARNING));
        JButton details = Theme.secondary("Details");
        details.addActionListener(e -> ActivityDialog.showDetails(this, r.activityId()));
        right.add(details);
        if (voting) {
            if (isMine) {
                right.add(new Theme.Chip("Your pick  ✓", Theme.ACCENT));
            } else {
                JButton vote = Theme.primary(mine == null ? "Vote" : "Switch vote");
                vote.addActionListener(e -> vote(r.activityId()));
                right.add(vote);
            }
        }
        card.add(right, BorderLayout.EAST);

        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
        return card;
    }

    private JComponent votersCard(List<Voter> voters) {
        long voted = voters.stream().filter(v -> v.choice() != null).count();

        Theme.Card card = new Theme.Card(new BorderLayout(0, 12));
        card.setPreferredSize(new Dimension(290, 0));
        card.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

        JPanel head = Theme.transparent(new BorderLayout(0, 8));
        head.add(Theme.caps("Who voted"), BorderLayout.NORTH);
        JPanel progress = Theme.transparent(new BorderLayout(10, 0));
        progress.add(new Theme.Bar(voters.isEmpty() ? 0 : (double) voted / voters.size(), Theme.SUCCESS),
                BorderLayout.CENTER);
        progress.add(Theme.text(voted + " / " + voters.size(), Font.BOLD, 12, Theme.MUTED), BorderLayout.EAST);
        head.add(progress, BorderLayout.CENTER);
        card.add(head, BorderLayout.NORTH);

        JPanel list = Theme.transparent(null);
        list.setLayout(new BoxLayout(list, BoxLayout.Y_AXIS));
        for (Voter v : voters) {
            JPanel row = Theme.transparent(new BorderLayout(10, 0));
            row.setBorder(BorderFactory.createEmptyBorder(7, 0, 7, 0));
            row.add(new Theme.Avatar(v.name(), 32), BorderLayout.WEST);
            JPanel text = Theme.transparent(new GridLayout(2, 1));
            text.add(Theme.text(v.name(), Font.BOLD, 13, Theme.TEXT));
            text.add(v.choice() == null ? Theme.text("Hasn't voted yet", Font.ITALIC, 12, Theme.MUTED)
                                        : Theme.text(v.choice(), Font.PLAIN, 12, Theme.ACCENT));
            row.add(text, BorderLayout.CENTER);
            row.setAlignmentX(Component.LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
            list.add(row);
        }
        JPanel holder = Theme.transparent(new BorderLayout());
        holder.add(list, BorderLayout.NORTH);
        card.add(Theme.scroll(holder), BorderLayout.CENTER);
        return card;
    }

    // ---------------- footer ----------------
    private JComponent footer(Event ev) {
        JPanel footer = new JPanel(new BorderLayout());
        footer.setBackground(Theme.BG);
        footer.setBorder(BorderFactory.createEmptyBorder(14, 18, 18, 18));

        boolean voting = "VOTING".equals(ev.status());
        boolean creator = ev.createdBy() == frame.currentUser().id();
        JLabel hint = Theme.muted(voting
                ? (creator ? "You created this SAHRA. Close voting when everyone has voted."
                           : "You can change your vote until the creator closes voting.")
                : "Voting is closed.");
        footer.add(hint, BorderLayout.WEST);

        JPanel buttons = Theme.transparent(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton close = Theme.secondary("Close");
        close.addActionListener(e -> dispose());
        buttons.add(close);
        if (voting && creator) {
            JButton decide = Theme.primary("Close voting & pick winner");
            decide.addActionListener(e -> closeVoting());
            buttons.add(decide);
        }
        footer.add(buttons, BorderLayout.EAST);
        return footer;
    }

    // ---------------- actions ----------------
    private void vote(int activityId) {
        try {
            EventDao.vote(eventId, activityId, frame.currentUser().id());
            reload();
        } catch (Exception ex) {
            Ui.error(this, ex);
        }
    }

    private void closeVoting() {
        if (!Ui.confirm(this, "Close voting now? The place with the most votes will be chosen.")) return;
        try {
            String winner = EventDao.close(eventId);
            reload();
            Ui.info(this, "Your SAHRA is decided: " + winner + "!");
        } catch (Exception ex) {
            Ui.error(this, ex);
        }
    }
}
