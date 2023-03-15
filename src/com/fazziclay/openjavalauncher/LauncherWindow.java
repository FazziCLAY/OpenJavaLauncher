package com.fazziclay.openjavalauncher;

import com.fazziclay.openjavalauncher.operation.Operation;
import com.fazziclay.openjavalauncher.util.JScrollPopupMenu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import static com.fazziclay.openjavalauncher.util.Lang.t;

public class LauncherWindow {
    private static final String WINDOW_TITLE = "OpenJavaLauncher";
    private static final Color BACKGROUND_COLOR = Color.BLACK;


    private JFrame frame;
    private JLabel latest;
    private JScrollPopupMenu pop;
    private OpenJavaLauncher.WindowListener windowListener;
    private JPanel operations;
    private Dimension start;
    private boolean created = false;

    public LauncherWindow(int startHeight, int startWidth) {
        start = new Dimension(startWidth, startHeight);
    }

    public void run(OpenJavaLauncher.WindowListener windowListener) {
        this.windowListener = windowListener;

        frame = new JFrame(WINDOW_TITLE);
        frame.setBackground(BACKGROUND_COLOR);
        frame.getRootPane().setBackground(BACKGROUND_COLOR);
        frame.getContentPane().setBackground(BACKGROUND_COLOR);
        frame.setAutoRequestFocus(true);
        frame.setSize(start);
        Dimension center = getScreenCenter();
        frame.setLocation(center.width - (start.width / 2), center.height - (start.height / 2));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        setupLayout();
        frame.setVisible(true);

        created = true;
    }

    private void setupLayout() {
        setupMenuBar();
        latest = new JLabel(t("main.latestStatus", "?", "?.?", "??w??a"));
        latest.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 18));
        latest.setForeground(Color.WHITE);

        operations = new JPanel();
        operations.setBackground(Color.gray);
        operations.setLayout(new BoxLayout(operations, BoxLayout.Y_AXIS));
        frame.getContentPane().add(BorderLayout.SOUTH, latest);
        frame.getContentPane().add(BorderLayout.EAST, operations);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(Color.RED);
        frame.getContentPane().add(p);

        JButton userProfile = new JButton("User Profile: "+windowListener.getCurrentUserProfile());
        JButton gameProfile = new JButton("Game Profile: "+windowListener.getCurrentGameProfile());
        JButton start = new JButton("Start!");
        start.addActionListener(e -> windowListener.startClicked());
        p.add(userProfile);
        p.add(gameProfile);
        p.add(start);
    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File
        JMenu menuFile = new JMenu("File");
        JMenuItem m11 = new JMenuItem("Open");
        m11.addActionListener(e -> windowListener.fileOpenClicked());
        JMenuItem m22 = new JMenuItem("Save as...");
        menuFile.add(m11);
        menuFile.add(m22);

        JMenu menuHelp = new JMenu("Help");


        JMenu menuDebug = new JMenu("DEBUG");
        JMenuItem addFakeOperation = new JMenuItem("Add fake operation");
        addFakeOperation.addActionListener(e -> windowListener.addFakeOperationClicked());
        menuDebug.add(addFakeOperation);

        JMenuItem clearOperations = new JMenuItem("Clear operations");
        clearOperations.addActionListener(e -> windowListener.clearOperations());
        menuDebug.add(clearOperations);

        JMenuItem updateVersionManifest = new JMenuItem("Update version_manifest_v2.json");
        updateVersionManifest.addActionListener(e -> windowListener.updateVersionManifest());
        menuDebug.add(updateVersionManifest);

        menuBar.add(menuFile);
        menuBar.add(menuHelp);
        menuBar.add(menuDebug);
        frame.getContentPane().add(BorderLayout.NORTH, menuBar);
    }


    public void updateVersionManifest(VersionManifest manifest) {
        if (manifest == null) return;
        latest.setText(t("main.latestStatus", manifest.isFresh() ? "*" : " ", manifest.getLatest().getRelease(), manifest.getLatest().getSnapshot()));
        pop = new JScrollPopupMenu("Versions");
        for (VersionManifest.Version version : manifest.getVersions()) {
            pop.add(new JMenuItem(version.getId()));
        }
    }

    public void updateOperations(List<Operation> operations) {
        this.operations.removeAll();
        JLabel t = new JLabel(" ~ Current operations ~ ");
        t.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 21));
        this.operations.add(t);
        for (Operation operation : operations) {
            JPanel p = new JPanel();
            p.setBackground(Color.PINK);
            p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

            JLabel title = new JLabel(operation.getTitle());
            title.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
            p.add(title);

            JLabel desc = new JLabel(operation.getDescription());
            desc.setFont(new Font(Font.SERIF, Font.PLAIN, 13));
            p.add(desc);

            if (operation.isCancelable()) {
                JButton cancel = new JButton("Cancel");
                cancel.addActionListener(e -> operation.cancel());
                p.add(cancel);
            }
            this.operations.add(p);
        }
    }

    public void tick() {
        operations.updateUI();
    }


    private Dimension getScreenCenter() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenSize.setSize(screenSize.width / 2, screenSize.height / 2);
        return screenSize;
    }

    public boolean isExists() {
        return frame == null || !created || frame.isShowing();
    }
}
