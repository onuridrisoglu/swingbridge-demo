package com.example.warehouse.startup;

import com.example.warehouse.AppSession;
import com.example.warehouse.dao.UserDAO;
import com.example.warehouse.model.User;
import com.example.warehouse.ui.util.UIConstants;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;

/**
 * Modal sign-in dialog — a faithful port of the Swing {@code LoginFrame}, including
 * the gradient banner, credential form, error label and footer hint. Shown over the
 * NetBeans main window during startup (and again on Logout).
 */
public final class LoginDialog extends JDialog {

    private final JTextField usernameField = new JTextField(20);
    private final JPasswordField passwordField = new JPasswordField(20);
    private final JLabel statusLabel = new JLabel(" ");
    private final UserDAO userDAO = new UserDAO();
    private boolean authenticated = false;

    private LoginDialog(Window owner) {
        super(owner, "WarehousePro 3.5 — Login", ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(Color.WHITE);
        setContentPane(root);

        root.add(buildBanner(), BorderLayout.NORTH);
        root.add(buildFormPanel(), BorderLayout.CENTER);
        root.add(buildFooter(), BorderLayout.SOUTH);

        pack();
        setMinimumSize(new Dimension(420, 380));
        setLocationRelativeTo(owner);
    }

    /**
     * Shows the dialog and blocks until the user signs in or cancels.
     *
     * @return {@code true} if a user was authenticated and stored in {@link AppSession}
     */
    public static boolean authenticate() {
        // Standalone owner-less modal dialog: shown during @OnStart it appears on its
        // own (centered on screen) before the main window is materialised, matching the
        // Swing LoginFrame → MainFrame flow.
        LoginDialog dialog = new LoginDialog(null);
        dialog.usernameField.requestFocusInWindow();
        dialog.setVisible(true); // blocks (modal)
        return dialog.authenticated;
    }

    private JPanel buildBanner() {
        JPanel banner = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setPaint(new GradientPaint(0, 0, UIConstants.PRIMARY, getWidth(), 0, UIConstants.PRIMARY_DARK));
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        banner.setPreferredSize(new Dimension(420, 90));
        banner.setLayout(new GridBagLayout());

        JLabel appName = new JLabel("WarehousePro™");
        appName.setFont(new Font("Tahoma", Font.BOLD, 26));
        appName.setForeground(Color.WHITE);

        JLabel subtitle = new JLabel("Inventory & Warehouse Management  v3.5");
        subtitle.setFont(new Font("Tahoma", Font.PLAIN, 11));
        subtitle.setForeground(new Color(190, 215, 240));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);
        textPanel.add(appName);
        textPanel.add(Box.createVerticalStrut(4));
        textPanel.add(subtitle);

        banner.add(textPanel);
        return banner;
    }

    private JPanel buildFormPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(Color.WHITE);
        outer.setBorder(new EmptyBorder(20, 40, 10, 40));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE);
        form.setBorder(UIConstants.sectionBorder("Sign In"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 8, 6, 8);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; c.anchor = GridBagConstraints.EAST; c.weightx = 0;
        form.add(UIConstants.fieldLabel("Username:"), c);

        c.gridx = 1; c.gridy = 0; c.anchor = GridBagConstraints.WEST; c.weightx = 1.0;
        usernameField.setFont(UIConstants.FONT_REGULAR);
        usernameField.setText("admin");
        form.add(usernameField, c);

        c.gridx = 0; c.gridy = 1; c.anchor = GridBagConstraints.EAST; c.weightx = 0;
        form.add(UIConstants.fieldLabel("Password:"), c);

        c.gridx = 1; c.gridy = 1; c.anchor = GridBagConstraints.WEST; c.weightx = 1.0;
        passwordField.setFont(UIConstants.FONT_REGULAR);
        passwordField.setText("admin");
        form.add(passwordField, c);

        c.gridx = 0; c.gridy = 2; c.gridwidth = 2; c.anchor = GridBagConstraints.CENTER;
        statusLabel.setFont(UIConstants.FONT_REGULAR);
        statusLabel.setForeground(UIConstants.DANGER);
        form.add(statusLabel, c);

        c.gridy = 3;
        JButton loginBtn = UIConstants.primaryButton("Sign In");
        loginBtn.setPreferredSize(new Dimension(120, 32));
        form.add(loginBtn, c);

        loginBtn.addActionListener(e -> doLogin());
        passwordField.addActionListener(e -> doLogin());
        usernameField.addActionListener(e -> passwordField.requestFocusInWindow());

        outer.add(form);
        return outer;
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(new Color(245, 247, 250));
        footer.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 210, 225)));

        JLabel hint = new JLabel("Default credentials:  admin / admin  |  manager / manager");
        hint.setFont(new Font("Tahoma", Font.ITALIC, 10));
        hint.setForeground(new Color(120, 130, 150));
        footer.add(hint);
        return footer;
    }

    private void doLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please enter username and password.");
            return;
        }

        User user = userDAO.authenticate(username, password);
        if (user == null) {
            statusLabel.setText("Invalid username or password.");
            passwordField.setText("");
            passwordField.requestFocusInWindow();
            return;
        }

        AppSession.login(user);
        authenticated = true;
        dispose();
    }
}
