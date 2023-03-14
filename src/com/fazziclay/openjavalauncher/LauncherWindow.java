package com.fazziclay.openjavalauncher;

import javax.swing.*;
import java.awt.*;
import java.awt.event.InputMethodEvent;
import java.awt.event.InputMethodListener;

public class LauncherWindow {
    private static final String WINDOW_TITLE = "OpenJavaLauncher";


    private final JFrame frame;
    private JLabel latest;

    public LauncherWindow(int startHeight, int startWidth) {
        frame = new JFrame(WINDOW_TITLE);
        frame.setBackground(Color.BLACK);
        frame.getRootPane().setBackground(Color.BLACK);
        frame.getContentPane().setBackground(Color.BLACK);
        frame.setAutoRequestFocus(true);
        frame.setSize(startWidth, startHeight);
        Dimension center = getScreenCenter();
        frame.setLocation(center.width - (startWidth / 2), center.height - (startHeight / 2));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    }

    public void run() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menuFile = new JMenu("FILE");
        JMenuItem m11 = new JMenuItem("Open");
        JMenuItem m22 = new JMenuItem("Save as...");
        menuFile.add(m11);
        menuFile.add(m22);
        menuBar.add(menuFile);
        JMenu menuHelp = new JMenu("HELP");
        menuBar.add(menuHelp);
        frame.getContentPane().add(BorderLayout.NORTH, menuBar);


        JPanel panel = new JPanel();
        JTextField nickname = new JTextField("Nickname", 16);

        nickname.addInputMethodListener(new InputMethodListener() {
            @Override
            public void inputMethodTextChanged(InputMethodEvent event) {
                latest.setText(event.getText().toString());
            }

            @Override
            public void caretPositionChanged(InputMethodEvent event) {
                latest.setText(event.getText().toString());
            }
        });

        panel.add(nickname);

        // Добавление компонентов в рамку.
        latest = new JLabel("[?] release: " + "?????" + " snapshot: " + "?????");
        latest.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 20));
        latest.setForeground(Color.WHITE);
        frame.getContentPane().add(BorderLayout.SOUTH, latest);
        frame.getContentPane().add(panel);

        frame.setVisible(true);
    }

    private Dimension getScreenCenter() {
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        screenSize.setSize(screenSize.width / 2, screenSize.height / 2);
        return screenSize;
    }

    public boolean isExists() {
        return frame.isShowing();
    }

    public void setLatest(boolean isFresh, String release, String snapshot) {
        latest.setText((isFresh ? "[*]" : "[ ]") + " release: " + release + " snapshot: " + snapshot);
    }
}
