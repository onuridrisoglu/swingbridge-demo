package com.example.warehouse.bridge;

import com.example.warehouse.AppSession;
import com.example.warehouse.ui.products.ProductsPanel;
import com.example.warehouse.ui.util.StatusBar;

import javax.swing.*;

public class ProductsBridge {

    public static void main(String[] args) {
        BridgeUtil.init();
        SwingUtilities.invokeLater(() -> {
            StatusBar statusBar = new StatusBar(AppSession.getUsername());
            JFrame frame = new JFrame();
            frame.setUndecorated(true);
            frame.setSize(1200, 700);
            frame.setContentPane(new ProductsPanel(statusBar));
            frame.setVisible(true);
        });
    }
}
