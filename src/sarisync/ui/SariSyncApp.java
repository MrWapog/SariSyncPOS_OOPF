package sarisync.ui;

import sarisync.repositories.InMemoryProductRepository;
import sarisync.services.CartService;
import sarisync.services.ProductService;
import javax.swing.*;

/**
 * Launches the SariSync Swing GUI (POS + Admin product management).
 */
public final class SariSyncApp {

    private SariSyncApp() {}

    public static void launch(ProductService productService, CartService cartService) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(productService, cartService);
            frame.setSize(1100, 720);
            frame.setVisible(true);
        });
    }
}
