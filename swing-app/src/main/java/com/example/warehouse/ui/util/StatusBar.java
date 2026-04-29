package com.example.warehouse.ui.util;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class StatusBar extends JPanel {

    private final JLabel messageLabel;
    private final JLabel timeLabel;
    private final JLabel userLabel;
    private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public StatusBar(String username) {
        setLayout(new BorderLayout());
        setBackground(new Color(220, 228, 240));
        setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(160, 175, 200)),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)));

        messageLabel = new JLabel("Ready");
        messageLabel.setFont(UIConstants.FONT_REGULAR);

        userLabel = new JLabel("User: " + username);
        userLabel.setFont(UIConstants.FONT_REGULAR);

        timeLabel = new JLabel(LocalDateTime.now().format(fmt));
        timeLabel.setFont(UIConstants.FONT_REGULAR);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        right.setOpaque(false);
        right.add(userLabel);
        right.add(new JSeparator(SwingConstants.VERTICAL));
        right.add(timeLabel);

        add(messageLabel, BorderLayout.WEST);
        add(right, BorderLayout.EAST);

        Timer clock = new Timer(30_000, e -> timeLabel.setText(LocalDateTime.now().format(fmt)));
        clock.start();
    }

    public void setMessage(String message) {
        messageLabel.setText(message);
    }

    public void setRowCount(int count, String entity) {
        messageLabel.setText(count + " " + entity + " record" + (count != 1 ? "s" : "") + " loaded");
    }
}
