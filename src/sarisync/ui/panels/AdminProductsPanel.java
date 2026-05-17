package sarisync.ui.panels;

import sarisync.models.Product;
import sarisync.services.ProductService;
import sarisync.ui.components.ProductCardPanel;
import sarisync.ui.components.ProductImageSlot;
import sarisync.ui.dialogs.ProductFormDialog;
import sarisync.ui.theme.AppTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/**
 * Admin → Products — wider 16:10 image slots, add/edit/delete.
 */
public class AdminProductsPanel extends JPanel {

    private final ProductService productService;
    private final Frame owner;
    private final JPanel gridPanel = new JPanel();

    public AdminProductsPanel(Frame owner, ProductService productService) {
        this.owner = owner;
        this.productService = productService;

        setLayout(new BorderLayout(0, 16));
        setBackground(AppTheme.BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("Admin → Products");
        title.setFont(AppTheme.TITLE);
        title.setForeground(AppTheme.TEXT);

        JButton addBtn = new JButton("+ Add Product");
        addBtn.setBackground(AppTheme.PRIMARY);
        addBtn.setForeground(Color.WHITE);
        addBtn.setFocusPainted(false);
        addBtn.setBorder(new EmptyBorder(10, 18, 10, 18));
        addBtn.setOpaque(true);
        addBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        addBtn.addActionListener(e -> openForm(null));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.WEST);
        header.add(addBtn, BorderLayout.EAST);

        gridPanel.setLayout(new GridLayout(0, 3, 20, 20));
        gridPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(gridPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        refreshGrid();
    }

    public void refreshGrid() {
        gridPanel.removeAll();
        int slotWidth = 220;
        for (Product p : productService.findAll()) {
            ProductCardPanel card = new ProductCardPanel(p,
                ProductImageSlot.AspectRatio.WIDE_16_10, slotWidth);

            JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
            actions.setOpaque(false);
            JButton edit = linkButton("Edit");
            JButton del = linkButton("Delete");
            edit.addActionListener(e -> openForm(p));
            del.addActionListener(e -> confirmDelete(p));
            actions.add(edit);
            actions.add(del);

            card.add(actions, BorderLayout.SOUTH);
            gridPanel.add(card);
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private void openForm(Product existing) {
        ProductFormDialog dialog = new ProductFormDialog(owner, productService, existing, p -> {
            refreshGrid();
            firePropertyChange("productsChanged", false, true);
        });
        dialog.setVisible(true);
    }

    private void confirmDelete(Product p) {
        int ok = JOptionPane.showConfirmDialog(this,
            "Delete \"" + p.getName() + "\"?", "Delete product",
            JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;
        try {
            productService.deleteProduct(p.getId());
            refreshGrid();
            firePropertyChange("productsChanged", false, true);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private static JButton linkButton(String text) {
        JButton b = new JButton(text);
        b.setForeground(AppTheme.PRIMARY);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setFont(AppTheme.SMALL);
        return b;
    }
}
