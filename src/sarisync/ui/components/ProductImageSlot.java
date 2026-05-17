package sarisync.ui.components;

import sarisync.models.Product;
import sarisync.services.ImageService;
import sarisync.ui.theme.AppTheme;

import javax.swing.*;
import java.awt.*;

/**
 * Renders a product image or falls back to the emoji icon.
 * Supports square (1:1) slots for POS and wide (16:10) slots for Admin.
 */
public class ProductImageSlot extends JPanel {

    public enum AspectRatio {
        SQUARE(1, 1),
        WIDE_16_10(16, 10);

        private final int w;
        private final int h;

        AspectRatio(int w, int h) {
            this.w = w;
            this.h = h;
        }

        public Dimension sizeForWidth(int width) {
            int height = Math.max(1, width * h / w);
            return new Dimension(width, height);
        }
    }

    private final Product product;
    private final AspectRatio aspectRatio;
    private final JLabel imageLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel emojiLabel = new JLabel("", SwingConstants.CENTER);

    public ProductImageSlot(Product product, AspectRatio aspectRatio, int slotWidth) {
        this.product = product;
        this.aspectRatio = aspectRatio;
        Dimension size = aspectRatio.sizeForWidth(slotWidth);
        setPreferredSize(size);
        setMinimumSize(size);
        setMaximumSize(new Dimension(Integer.MAX_VALUE, size.height));
        setLayout(new BorderLayout());
        setBackground(AppTheme.SLOT_BG);
        setOpaque(true);

        emojiLabel.setFont(new Font("Segoe UI Emoji", Font.PLAIN, slotWidth > 120 ? 42 : 32));
        emojiLabel.setText(product.getFallbackIcon());
        emojiLabel.setOpaque(false);

        imageLabel.setOpaque(false);
        refresh();
    }

    public void refresh() {
        removeAll();
        if (product.hasImage()) {
            Dimension d = getPreferredSize();
            ImageIcon icon = ImageService.scaledIcon(product.getImageUrl(), d.width, d.height);
            if (icon != null) {
                imageLabel.setIcon(icon);
                imageLabel.setText("");
                add(imageLabel, BorderLayout.CENTER);
                return;
            }
        }
        add(emojiLabel, BorderLayout.CENTER);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(AppTheme.BORDER);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
        g2.dispose();
    }
}
