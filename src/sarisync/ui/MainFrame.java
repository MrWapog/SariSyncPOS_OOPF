package sarisync.ui;

import sarisync.config.POSConfig;
import sarisync.services.CartService;
import sarisync.services.ProductService;
import sarisync.ui.panels.AdminProductsPanel;
import sarisync.ui.panels.POSDashboardPanel;
import sarisync.ui.theme.AppTheme;

import javax.swing.*;
import java.awt.*;

/**
 * Main window with POS Dashboard and Admin → Products tabs.
 */
public class MainFrame extends JFrame {

    private final POSDashboardPanel posPanel;
    private final AdminProductsPanel adminPanel;

    public MainFrame(ProductService productService, CartService cartService) {
        super(POSConfig.APP_NAME + " — " + POSConfig.APP_VERSION);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(960, 640));
        getContentPane().setBackground(AppTheme.BG);

        posPanel = new POSDashboardPanel(productService, cartService, () -> {});
        adminPanel = new AdminProductsPanel(this, productService);
        adminPanel.addPropertyChangeListener("productsChanged",
            e -> posPanel.refreshGrid());

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(AppTheme.BODY);
        tabs.addTab("POS Dashboard", posPanel);
        tabs.addTab("Admin → Products", adminPanel);

        add(tabs, BorderLayout.CENTER);
        setLocationRelativeTo(null);
    }
}
