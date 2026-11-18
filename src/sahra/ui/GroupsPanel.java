package sahra.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;

import sahra.db.EventDao;
import sahra.db.GroupDao;
import sahra.db.Models.Event;
import sahra.db.Models.Group;
import sahra.db.Models.Member;

/** Sidebar with my groups and their members; main area with the group's SAHRAs as cards. */
class GroupsPanel extends JPanel {

    static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.ENGLISH);

    private final MainFrame frame;
    private final DefaultListModel<Group> groupModel = new DefaultListModel<>();
    private final JList<Group> groupList = new JList<>(groupModel);
    private final JPanel memberBox = Theme.transparent(null);
    private final JLabel groupTitle = Theme.h1("");
    private final JLabel groupSubtitle = Theme.muted("");
    private final JPanel eventBox = Theme.transparent(null);
    private final JButton newEvent = Theme.primary("+  New SAHRA");

    GroupsPanel(MainFrame frame) {
        super(new BorderLayout());
        this.frame = frame;
        setBackground(Theme.BG);

        add(sidebar(), BorderLayout.WEST);
        add(main(), BorderLayout.CENTER);
        loadGroups();
    }

    // =================================================================
    //  Sidebar
    // =================================================================
    private JComponent sidebar() {
        JPanel side = new JPanel(new BorderLayout());
        side.setBackground(Theme.SURFACE);
        side.setPreferredSize(new Dimension(310, 0));
        side.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER),
                BorderFactory.createEmptyBorder(20, 16, 16, 16)));

        // groups header
        JPanel head = Theme.transparent(new BorderLayout());
        head.add(Theme.caps("Your groups"), BorderLayout.WEST);
        JButton add = Theme.link("+ New group");
        add.addActionListener(e -> newGroup());
        head.add(add, BorderLayout.EAST);
        head.setBorder(BorderFactory.createEmptyBorder(0, 4, 10, 0));

        groupList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        groupList.setBackground(Theme.SURFACE);
        groupList.setCellRenderer(new GroupRenderer());
        groupList.setFixedCellHeight(64);
        groupList.addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) loadSelectedGroup(); });

        JPanel groups = Theme.transparent(new BorderLayout());
        groups.add(head, BorderLayout.NORTH);
        groups.add(Theme.scroll(groupList), BorderLayout.CENTER);

        // members
        memberBox.setLayout(new BoxLayout(memberBox, BoxLayout.Y_AXIS));
        JPanel membersHead = Theme.transparent(new BorderLayout());
        membersHead.add(Theme.caps("Members"), BorderLayout.WEST);
        membersHead.setBorder(BorderFactory.createEmptyBorder(18, 4, 10, 0));

        JButton addMember = Theme.secondary("Add friend");
        JButton leave = Theme.danger("Leave");
        addMember.addActionListener(e -> addMember());
        leave.addActionListener(e -> leaveGroup());
        JPanel buttons = Theme.transparent(new GridLayout(1, 2, 8, 0));
        buttons.add(addMember);
        buttons.add(leave);
        buttons.setBorder(BorderFactory.createEmptyBorder(12, 0, 0, 0));

        JPanel members = Theme.transparent(new BorderLayout());
        members.add(membersHead, BorderLayout.NORTH);
        members.add(Theme.scroll(memberBox), BorderLayout.CENTER);
        members.add(buttons, BorderLayout.SOUTH);
        members.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Theme.BORDER));

        JPanel split = Theme.transparent(new GridLayout(2, 1, 0, 12));
        split.add(groups);
        split.add(members);
        side.add(split, BorderLayout.CENTER);
        return side;
    }

    /** One group row in the sidebar: avatar, name, member count. */
    private static class GroupRenderer implements ListCellRenderer<Group> {
        @Override public Component getListCellRendererComponent(JList<? extends Group> list, Group g,
                int index, boolean selected, boolean focus) {
            Theme.Card row = new Theme.Card(new BorderLayout(12, 0));
            row.fill(selected ? new Color(0x2A2140) : Theme.SURFACE)
               .stroke(selected ? Theme.alpha(Theme.ACCENT, 120) : null, 1.2f);
            row.arc = 16;
            row.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
            row.add(new Theme.Avatar(g.name(), 38), BorderLayout.WEST);
            JPanel text = Theme.transparent(new GridLayout(2, 1));
            text.add(Theme.text(g.name(), Font.BOLD, 14, Theme.TEXT));
            text.add(Theme.muted(g.memberCount() + (g.memberCount() == 1 ? " member" : " members")));
            row.add(text, BorderLayout.CENTER);
            JPanel wrap = Theme.transparent(new BorderLayout());
            wrap.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 4));
            wrap.add(row);
            return wrap;
        }
    }

    // =================================================================
    //  Main area
    // =================================================================
    private JComponent main() {
        JPanel main = new JPanel(new BorderLayout());
        main.setBackground(Theme.BG);
        main.setBorder(BorderFactory.createEmptyBorder(28, 32, 20, 32));

        JPanel head = Theme.transparent(new BorderLayout());
        JPanel titles = Theme.transparent(null);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        titles.add(groupTitle);
        titles.add(Box.createVerticalStrut(4));
        titles.add(groupSubtitle);
        head.add(titles, BorderLayout.WEST);
        JPanel right = Theme.transparent(new FlowLayout(FlowLayout.RIGHT, 0, 8));
        newEvent.setPreferredSize(new Dimension(170, 44));
        newEvent.addActionListener(e -> newEvent());
        right.add(newEvent);
        head.add(right, BorderLayout.EAST);
        head.setBorder(BorderFactory.createEmptyBorder(0, 0, 22, 0));

        eventBox.setLayout(new BoxLayout(eventBox, BoxLayout.Y_AXIS));
        JPanel holder = Theme.transparent(new BorderLayout());
        holder.add(eventBox, BorderLayout.NORTH);   // cards stay at the top

        main.add(head, BorderLayout.NORTH);
        main.add(Theme.scroll(holder), BorderLayout.CENTER);
        return main;
    }

    /** A SAHRA shown as a card: title, date, filter chips, status, actions. */
    private JComponent eventCard(Event ev) {
        boolean voting = "VOTING".equals(ev.status());
        boolean mine = ev.createdBy() == frame.currentUser().id();

        Theme.Card card = new Theme.Card(new BorderLayout(16, 0));
        card.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // left: date block
        JPanel dateBlock = new Theme.Card(new GridLayout(2, 1)).fill(Theme.SURFACE_2).stroke(null, 0);
        ((Theme.Card) dateBlock).arc = 14;
        dateBlock.setBorder(BorderFactory.createEmptyBorder(6, 12, 6, 12));
        dateBlock.setPreferredSize(new Dimension(64, 64));
        JLabel month = Theme.text(ev.plannedDate().getMonth().getDisplayName(java.time.format.TextStyle.SHORT,
                Locale.ENGLISH).toUpperCase(), Font.BOLD, 11, Theme.ACCENT);
        JLabel day = Theme.text(String.valueOf(ev.plannedDate().getDayOfMonth()), Font.BOLD, 22, Theme.TEXT);
        month.setHorizontalAlignment(JLabel.CENTER);
        day.setHorizontalAlignment(JLabel.CENTER);
        dateBlock.add(month);
        dateBlock.add(day);
        card.add(dateBlock, BorderLayout.WEST);

        // middle: title + chips
        JPanel middle = Theme.transparent(null);
        middle.setLayout(new BoxLayout(middle, BoxLayout.Y_AXIS));
        JLabel title = Theme.h3(ev.title());
        JLabel date = Theme.muted(ev.plannedDate().format(DATE)
                + (ev.fallback() ? "   ·   closest options (no exact match)" : ""));
        JPanel chips = Theme.transparent(new FlowLayout(FlowLayout.LEFT, 6, 0));
        chips.add(new Theme.Chip(ev.type(), Theme.typeColor(ev.type())));
        chips.add(new Theme.Chip(ev.area(), Theme.MUTED));
        chips.add(new Theme.Chip(ev.atmosphere(), Theme.MUTED));
        chips.add(new Theme.Chip(Ui.money(ev.minBudget()) + " – " + Ui.money(ev.maxBudget()), Theme.ACCENT_2));
        for (JComponent c : new JComponent[]{title, date, chips}) c.setAlignmentX(LEFT_ALIGNMENT);
        chips.setBorder(BorderFactory.createEmptyBorder(10, -6, 0, 0));
        middle.add(title);
        middle.add(Box.createVerticalStrut(3));
        middle.add(date);
        middle.add(chips);
        card.add(middle, BorderLayout.CENTER);

        // right: status + actions
        JPanel right = Theme.transparent(new FlowLayout(FlowLayout.RIGHT, 8, 14));
        right.add(voting ? new Theme.Chip("Voting open", Theme.WARNING)
                         : new Theme.Chip("Going to " + ev.finalName(), Theme.SUCCESS));
        JButton open = voting ? Theme.primary("Vote") : Theme.secondary("View");
        open.addActionListener(e -> openEvent(ev.id()));
        right.add(open);
        if (mine) {
            JButton delete = Theme.danger("Delete");
            delete.addActionListener(e -> deleteEvent(ev));
            right.add(delete);
        }
        card.add(right, BorderLayout.EAST);

        // whole card is clickable, with a hover glow
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { openEvent(ev.id()); }
            @Override public void mouseEntered(MouseEvent e) { card.stroke(Theme.alpha(Theme.ACCENT, 140), 1.4f); card.repaint(); }
            @Override public void mouseExited(MouseEvent e)  { card.stroke(Theme.BORDER, 1f); card.repaint(); }
        });

        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
        return card;
    }

    private JComponent emptyState(String title, String text) {
        Theme.Card card = new Theme.Card(null);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.fill(Theme.BG).stroke(Theme.BORDER, 1.5f);
        card.setBorder(BorderFactory.createEmptyBorder(48, 40, 48, 40));
        JLabel icon = new JLabel(new Theme.Moon(46, Theme.GOLD));
        JLabel t = Theme.h3(title);
        JLabel s = Theme.muted(text);
        for (JComponent c : new JComponent[]{icon, t, s}) c.setAlignmentX(CENTER_ALIGNMENT);
        card.add(icon);
        card.add(Box.createVerticalStrut(14));
        card.add(t);
        card.add(Box.createVerticalStrut(6));
        card.add(s);
        card.setAlignmentX(LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, card.getPreferredSize().height));
        return card;
    }

    // =================================================================
    //  Data
    // =================================================================
    private Group selectedGroup() {
        return groupList.getSelectedValue();
    }

    private void loadGroups() {
        try {
            Group keep = selectedGroup();
            groupModel.clear();
            List<Group> groups = GroupDao.listForUser(frame.currentUser().id());
            for (Group g : groups) groupModel.addElement(g);
            if (keep != null) {
                for (int i = 0; i < groupModel.size(); i++) {
                    if (groupModel.get(i).id() == keep.id()) groupList.setSelectedIndex(i);
                }
            }
            if (groupList.getSelectedIndex() < 0 && !groupModel.isEmpty()) groupList.setSelectedIndex(0);
            if (groupModel.isEmpty()) loadSelectedGroup();
        } catch (Exception ex) {
            Ui.error(this, ex);
        }
    }

    private void loadSelectedGroup() {
        memberBox.removeAll();
        eventBox.removeAll();
        Group g = selectedGroup();
        newEvent.setEnabled(g != null);
        if (g == null) {
            groupTitle.setText("No group yet");
            groupSubtitle.setText("Create a group in the sidebar and invite your friends.");
            eventBox.add(emptyState("Start a group", "Click \"+ New group\" on the left to begin."));
            refresh();
            return;
        }
        try {
            List<Member> members = GroupDao.members(g.id());
            for (Member m : members) memberBox.add(memberRow(m));

            List<Event> events = EventDao.listForGroup(g.id());
            groupTitle.setText(g.name());
            groupSubtitle.setText(members.size() + (members.size() == 1 ? " member" : " members")
                    + "   ·   " + events.size() + (events.size() == 1 ? " SAHRA" : " SAHRAs"));
            if (events.isEmpty()) {
                eventBox.add(emptyState("No SAHRAs yet", "Plan your first night out with \"+ New SAHRA\"."));
            }
            for (Event ev : events) {
                eventBox.add(eventCard(ev));
                eventBox.add(Box.createVerticalStrut(12));
            }
        } catch (Exception ex) {
            Ui.error(this, ex);
        }
        refresh();
    }

    private void refresh() {
        memberBox.revalidate();
        memberBox.repaint();
        eventBox.revalidate();
        eventBox.repaint();
    }

    private JComponent memberRow(Member m) {
        JPanel row = Theme.transparent(new BorderLayout(10, 0));
        row.setBorder(BorderFactory.createEmptyBorder(6, 4, 6, 4));
        row.add(new Theme.Avatar(m.fullName(), 32), BorderLayout.WEST);
        JPanel text = Theme.transparent(new GridLayout(2, 1));
        boolean me = m.id() == frame.currentUser().id();
        text.add(Theme.text(m.fullName() + (me ? "  (you)" : ""), Font.BOLD, 13, Theme.TEXT));
        text.add(Theme.text(m.email(), Font.PLAIN, 12, Theme.MUTED));
        row.add(text, BorderLayout.CENTER);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        return row;
    }

    // =================================================================
    //  Actions
    // =================================================================
    private void newGroup() {
        String name = Ui.input(this, "New group", "Give your group a name your friends will recognize.", "e.g. LAU Squad");
        if (name == null || name.isBlank()) return;
        try {
            GroupDao.create(name, frame.currentUser().id());
            groupList.clearSelection();
            loadGroups();
            for (int i = 0; i < groupModel.size(); i++) {
                if (groupModel.get(i).name().equals(name.trim())) groupList.setSelectedIndex(i);
            }
        } catch (Exception ex) {
            Ui.error(this, ex);
        }
    }

    private void addMember() {
        Group g = selectedGroup();
        if (g == null) { Ui.info(this, "Select a group first."); return; }
        String email = Ui.input(this, "Add a friend", "Enter the email your friend signed up with to add them to "
                + g.name() + ".", "friend@example.com");
        if (email == null || email.isBlank()) return;
        try {
            String added = GroupDao.addMemberByEmail(g.id(), email);
            if (added == null) Ui.info(this, "No SAHRA account uses that email yet.");
            else loadGroups();
        } catch (Exception ex) {
            Ui.error(this, ex);
        }
    }

    private void leaveGroup() {
        Group g = selectedGroup();
        if (g == null) return;
        if (!Ui.confirm(this, "Leave " + g.name() + "?")) return;
        try {
            GroupDao.leave(g.id(), frame.currentUser().id());
            groupList.clearSelection();
            loadGroups();
        } catch (Exception ex) {
            Ui.error(this, ex);
        }
    }

    private void newEvent() {
        Group g = selectedGroup();
        if (g == null) return;
        NewEventDialog dialog = new NewEventDialog(frame, g);
        dialog.setVisible(true);
        Integer created = dialog.createdEventId();
        if (created != null) new EventDialog(frame, created).setVisible(true);
        loadSelectedGroup();
    }

    private void openEvent(int id) {
        new EventDialog(frame, id).setVisible(true);
        loadSelectedGroup();
    }

    private void deleteEvent(Event ev) {
        if (!Ui.confirm(this, "Delete \"" + ev.title() + "\" and all its votes?")) return;
        try {
            EventDao.delete(ev.id());
            loadSelectedGroup();
        } catch (Exception ex) {
            Ui.error(this, ex);
        }
    }
}
