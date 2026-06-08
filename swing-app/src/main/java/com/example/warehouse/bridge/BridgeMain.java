package com.example.warehouse.bridge;

import com.example.warehouse.ui.MainFrame;

import javax.swing.*;
import java.awt.*;

public class BridgeMain {

    public static void main(String[] args) {
        BridgeUtil.init();
        SwingUtilities.invokeLater(() -> {
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            new MainFrame(ge.getDefaultScreenDevice().getDefaultConfiguration(), true);
        });
    }
}
