package sarisync.ui.components;

import sarisync.models.Product;
import sarisync.ui.theme.AppTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Product card with image slot, name, price, and optional action row (Admin).
 */
public class ProductCardPanel extends JPanel {

    private final ProductImageSlot imageSlot;
    private final JLabel nameLabel = new JLabel();
    private final JLabel metaLabel = new JLabel();

    public ProductCardPanel(Product product, ProductImageSlot.AspectRatio ratio, int slotWidth) {
        setLayout(new BorderLayout(0, 8));
        setBackground(AppTheme.CARD_BG);
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(AppTheme.BORDER),
            new EmptyBorder(12, 12, 12, 12)));

        imageSlot = new ProductImageSlot(product, ratio, slotWidth);
        nameLabel.setText(product.getName());
        nameLabel.setFont(AppTheme.HEADING);
        nameLabel.setForeground(AppTheme.TEXT);

        metaLabel.setFont(AppTheme.SMALL);
        metaLabel.setForeground(AppTheme.MUTED);
        String stockHint = product.isLowStock() ? " · Low stock" : "";
        metaLabel.setText(String.format("₱%.2f · %d in stock%s",
            product.getPrice(), product.getStock(), stockHint));

        JPanel body = new JPanel(new BorderLayout(0, 4));
        body.setOpaque(false);
        body.add(imageSlot, BorderLayout.NORTH);
        body.add(nameLabel, BorderLayout.CENTER);
        body.add(metaLabel, BorderLayout.SOUTH);

        add(body, BorderLayout.CENTER);
    }

    public ProductImageSlot getImageSlot() {
        return imageSlot;
    }

    public void refresh(Product product) {
        imageSlot.refresh();
        nameLabel.setText(product.getName());
        String stockHint = product.isLowStock() ? " · Low stock" : "";
        metaLabel.setText(String.format("₱%.2f · %d in stock%s",
            product.getPrice(), product.getStock(), stockHint));
    }
}
