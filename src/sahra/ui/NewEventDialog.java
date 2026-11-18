package sahra.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SpinnerDateModel;
import javax.swing.SpinnerNumberModel;

import sahra.db.ActivityDao;
import sahra.db.EventDao;
import sahra.db.Models.Group;

/** "Plan a SAHRA": the group taps its choices (type, area, vibe) and sets the budget. */
class NewEventDialog extends JDialog {

    private final MainFrame frame;
    private final Group group;
    private Integer createdEventId;

    private final JTextField title = Theme.field(24, "e.g. Saturday SAHRA");
    private final JSpinner date = Theme.tall(new JSpinner(new SpinnerDateModel()));
    private final ButtonGroup type = new ButtonGroup();
    private final ButtonGroup area = new ButtonGroup();
    private final ButtonGroup atmosphere = new ButtonGroup();
    private final JSpinner minBudget = Theme.tall(new JSpinner(new SpinnerNumberModel(15.0, 0.0, 1000.0, 5.0)));
    private final JSpinner maxBudget = Theme.tall(new JSpinner(new SpinnerNumberModel(30.0, 0.0, 1000.0, 5.0)));

    NewEventDialog(MainFrame frame, Group group) {
        super(frame, "Plan a SAHRA", true);
        this.frame = frame;
        this.group = group;
        title.setText("Saturday SAHRA");
        date.setEditor(new JSpinner.DateEditor(date, "EEE, MMM d, yyyy"));

        JPanel body = new JPanel();
        body.setBackground(Theme.BG);
        body.setLayout(new BoxLayout(body, BoxLayout.Y_AXIS));
        body.setBorder(BorderFactory.createEmptyBorder(28, 32, 12, 32));

        add(body, Theme.h2("Plan a night out"));
        add(body, Theme.muted("For " + group.name() + ". Pick what you feel like, SAHRA finds the places."));
        body.add(Box.createVerticalStrut(22));

        JPanel row = Theme.transparent(new GridLayout(1, 2, 14, 0));
        row.add(labeled("Title", title));
        row.add(labeled("Date", date));
        add(body, row);
        body.add(Box.createVerticalStrut(18));

        add(body, labeled("What are you looking for?", choices(ActivityDao.TYPES, type, 4, true)));
        body.add(Box.createVerticalStrut(16));
        add(body, labeled("Where?", choices(ActivityDao.AREAS, area, 4, false)));
        body.add(Box.createVerticalStrut(16));
        add(body, labeled("What's the vibe?", choices(ActivityDao.ATMOSPHERES, atmosphere, 3, false)));
        body.add(Box.createVerticalStrut(18));

        JPanel budget = Theme.transparent(new GridLayout(1, 2, 14, 0));
        budget.add(labeled("Min budget per person ($)", minBudget));
        budget.add(labeled("Max budget per person ($)", maxBudget));
        add(body, budget);

        JButton find = Theme.primary("Find places");
        JButton cancel = Theme.secondary("Cancel");
        find.setPreferredSize(new Dimension(150, 42));
        cancel.setPreferredSize(new Dimension(110, 42));
        find.addActionListener(e -> create());
        cancel.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setBackground(Theme.BG);
        buttons.setBorder(BorderFactory.createEmptyBorder(14, 32, 26, 32));
        buttons.add(cancel);
        buttons.add(find);

        getContentPane().setBackground(Theme.BG);
        setLayout(new BorderLayout());
        getContentPane().add(body, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
        getRootPane().setDefaultButton(find);
        pack();
        setResizable(false);
        setLocationRelativeTo(frame);
    }

    private static void add(JPanel body, JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
        body.add(c);
    }

    private static JComponent labeled(String label, JComponent field) {
        JPanel p = Theme.transparent(new BorderLayout(0, 7));
        p.add(Theme.caps(label), BorderLayout.NORTH);
        p.add(field, BorderLayout.CENTER);
        return p;
    }

    /** A grid of pill-shaped toggle buttons; exactly one can be selected. */
    private static JComponent choices(String[] options, ButtonGroup group, int perRow, boolean colored) {
        int rows = (options.length + perRow - 1) / perRow;
        JPanel grid = Theme.transparent(new GridLayout(rows, perRow, 8, 8));
        for (int i = 0; i < options.length; i++) {
            String option = options[i];
            Color tint = colored ? Theme.typeColor(option) : Theme.ACCENT;
            JToggleButton b = new Theme.PillToggle(option, tint);
            b.setActionCommand(option);
            group.add(b);
            if (i == 0) b.setSelected(true);
            grid.add(b);
        }
        return grid;
    }

    Integer createdEventId() {
        return createdEventId;
    }

    private void create() {
        double min = ((Number) minBudget.getValue()).doubleValue();
        double max = ((Number) maxBudget.getValue()).doubleValue();
        if (title.getText().isBlank()) { Ui.info(this, "Please give your SAHRA a title."); return; }
        if (min > max) { Ui.info(this, "The minimum budget cannot be higher than the maximum."); return; }
        LocalDate day = ((Date) date.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        try {
            createdEventId = EventDao.create(group.id(), frame.currentUser().id(), title.getText(), day,
                    type.getSelection().getActionCommand(), area.getSelection().getActionCommand(),
                    atmosphere.getSelection().getActionCommand(), min, max);
            dispose();
        } catch (Exception ex) {
            Ui.error(this, ex);
        }
    }
}
