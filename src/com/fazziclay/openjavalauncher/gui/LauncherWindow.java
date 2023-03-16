package com.fazziclay.openjavalauncher.gui;

import com.fazziclay.openjavalauncher.OpenJavaLauncher;
import com.fazziclay.openjavalauncher.launcher.GameProfile;
import com.fazziclay.openjavalauncher.launcher.UserProfile;
import com.fazziclay.openjavalauncher.launcher.VersionManifest;
import com.fazziclay.openjavalauncher.operation.Operation;
import com.fazziclay.openjavalauncher.util.Lang;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static com.fazziclay.openjavalauncher.util.Lang.t;

public class LauncherWindow {
    private static final String WINDOW_TITLE = "OpenJavaLauncher v" + OpenJavaLauncher.VERSION_NAME + " ("+OpenJavaLauncher.VERSION_BUILD+")";
    private static final Color BACKGROUND_COLOR = Color.BLACK;


    private JFrame frame;
    private JLabel latest;
    private DefaultListModel<String> pop;
    private OpenJavaLauncher.WindowListener windowListener;
    private JPanel operations;
    private final Dimension start;
    private boolean created = false;
    private JPanel root;
    private MainComponent mainComponent;

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
        frame.getContentPane().add(BorderLayout.SOUTH, latest);

        operations = new JPanel();
        operations.setBackground(Color.gray);
        operations.setLayout(new BoxLayout(operations, BoxLayout.Y_AXIS));
        frame.getContentPane().add(BorderLayout.EAST, operations);


        mainComponent = new MainComponent();
        root = new JPanel();
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.add(mainComponent);
        frame.getContentPane().add(root);
    }

    private void setScreen(JComponent c) {
        root.removeAll();
        root.add(c);
    }

    public void languageChanged() {
        frame.repaint();
    }

    private class MainComponent extends JPanel {
        public MainComponent() {
            setLayout(new GridBagLayout());

            JComboBox<String> userProfile = new JComboBox<>();
            userProfile.setModel(new UserProfileModel(userProfile, windowListener.getSelectedUserProfile()));
            userProfile.setPreferredSize(new Dimension(150, 30));

            JComboBox<String> gameProfile = new JComboBox<>();
            gameProfile.setModel(new GameProfileModel(gameProfile, windowListener.getSelectedGameProfile()));
            gameProfile.setPreferredSize(new Dimension(150, 30));


            JButton start = new JButton(t("main.start"));
            start.addActionListener(e -> windowListener.startClicked());

            GridBagConstraints c = new GridBagConstraints();
            c.gridx = 0;
            c.gridy = 0;
            c.gridwidth = 5;
            c.gridheight = 2;
            add(userProfile, c);

            c.gridx = 0;
            c.gridy = 5;
            c.gridwidth = 5;
            c.gridheight = 2;
            add(gameProfile, c);

            c.gridx = 0;
            c.gridy = 7;
            c.gridwidth = 10;
            c.gridheight = 2;
            add(start, c);
        }

        private class GameProfileModel extends AbstractListModel<String> implements ComboBoxModel<String> {
            private final JComboBox<String> comboBox;
            private String selected;

            public GameProfileModel(JComboBox<String> comboBox, GameProfile gameProfile) {
                this.comboBox = comboBox;
                selected = gameProfile == null ? null : gameProfile.getName();
            }

            @Override
            public void setSelectedItem(Object anItem) {
                this.selected = (String) anItem;
                GameProfile selected = windowListener.getGameProfiles()[comboBox.getSelectedIndex()];
                windowListener.selectGameProfile(selected);
            }

            @Override
            public Object getSelectedItem() {
                return selected;
            }

            @Override
            public int getSize() {
                return windowListener.getGameProfiles().length;
            }

            @Override
            public String getElementAt(int index) {
                return windowListener.getGameProfiles()[index].getName();
            }
        }

        private class UserProfileModel extends AbstractListModel<String> implements ComboBoxModel<String> {
            private final JComboBox<String> comboBox;
            private String selected;

            public UserProfileModel(JComboBox<String> comboBox, UserProfile userProfile) {
                this.comboBox = comboBox;
                selected = userProfile == null ? null : userProfile.getName();
            }

            @Override
            public void setSelectedItem(Object anItem) {
                this.selected = (String) anItem;
                UserProfile selected = windowListener.getUserProfiles()[comboBox.getSelectedIndex()];
                windowListener.selectUserProfile(selected);
            }

            @Override
            public Object getSelectedItem() {
                return selected;
            }

            @Override
            public int getSize() {
                return windowListener.getUserProfiles().length;
            }

            @Override
            public String getElementAt(int index) {
                return windowListener.getUserProfiles()[index].getName();
            }
        }
    }


    public void updateUserProfiles() {

    }

    public void updateGameProfiles() {

    }

    private void setupMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File
        JMenu menuFile = new JMenu(t("menu.file"));
        JMenuItem settings = new JMenuItem(t("menu.file.language"));
        menuFile.add(settings);

        settings.addActionListener(e -> {
            ButtonGroup group = new ButtonGroup();
            JFrame f = new JFrame();
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            JPanel panel = new JPanel();
            f.getContentPane().add(panel);

            for (String language : Lang.getLanguages()) {
                try {
                    JRadioButton b = new JRadioButton(Lang.getLanguageName(language));
                    group.add(b);
                    panel.add(b);
                    b.addActionListener(ee -> windowListener.selectLanguage(language));
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }

            f.setType(Window.Type.NORMAL);
            f.setBounds(frame.getX(), frame.getY(), 150, 150);
            f.setVisible(true);
            f.revalidate();
        });


        JMenu menuProfiles = new JMenu(t("menu.profiles"));
        JMenuItem addGame = new JMenuItem(t("menu.profiles.addGameProfile"));
        addGame.addActionListener(e -> windowListener.addGameProfileClicked());
        JMenuItem addUser = new JMenuItem(t("menu.profiles.addUserProfile"));
        addUser.addActionListener(e -> windowListener.addUserProfileClicked());
        menuProfiles.add(addGame);
        menuProfiles.add(addUser);


        JMenu menuDebug = new JMenu("DEBUG");
        JMenuItem addFakeOperation = new JMenuItem("Add fake operation");
        addFakeOperation.addActionListener(e -> windowListener.addFakeOperationClicked());
        menuDebug.add(addFakeOperation);

        JMenuItem clearOperations = new JMenuItem("Clear operations");
        clearOperations.addActionListener(e -> windowListener.clearOperationsClicked());
        menuDebug.add(clearOperations);

        JMenuItem updateVersionManifest = new JMenuItem("Update version_manifest_v2.json");
        updateVersionManifest.addActionListener(e -> windowListener.updateVersionManifestClicked());
        menuDebug.add(updateVersionManifest);



        menuBar.add(menuFile);
        menuBar.add(menuProfiles);
        menuBar.add(menuDebug);
        frame.setJMenuBar(menuBar);
    }


    public void updateVersionManifest(VersionManifest manifest) {
        if (manifest == null) return;
        latest.setText(t("main.latestStatus", manifest.isFresh() ? "*" : " ", manifest.getLatest().getRelease(), manifest.getLatest().getSnapshot()));
    }

    public void updateOperations(List<Operation> operations) {
        this.operations.removeAll();
        JLabel t = new JLabel(t("currentOperations.title"));
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
                JButton cancel = new JButton(t("currentOperations.operation.cancel"));
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
