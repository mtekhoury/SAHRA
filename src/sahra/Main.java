package sahra;

import javax.swing.SwingUtilities;

import sahra.ui.MainFrame;

/** Starts the SAHRA app. In VS Code, click "Run" just above the main method below. */
public class Main {

    public static void main(String[] args) {
        MainFrame.installTheme();   // modern dark look (uses FlatLaf if it is in the lib folder)
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
            // bring the window in front of VS Code when it opens
            frame.setAlwaysOnTop(true);
            frame.toFront();
            frame.requestFocus();
            frame.setAlwaysOnTop(false);
        });
    }
}
