package sarisync.ui.panels;

import sarisync.models.Product;
import sarisync.services.CartService;
import sarisync.services.ProductService;
import sarisync.ui.components.ProductCardPanel;
import sarisync.ui.components.ProductImageSlot;
import sarisync.ui.theme.AppTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * POS Dashboard — product grid with square image slots (uploaded image or emoji).
 */
public class POSDashboardPanel extends JPanel {

    private final ProductService productService;
    private final CartService cartService;
    private final Runnable onCartChanged;

    private final JTextField searchField = new JTextField(20);
    private final JComboBox<String> categoryFilter = new JComboBox<>();
    private final JPanel gridPanel = new JPanel();
    private final JLabel cartSummary = new JLabel();

    public POSDashboardPanel(ProductService productService, CartService cartService,
                             Runnable onCartChanged) {
        this.productService = productService;
        this.cartService = cartService;
        this.onCartChanged = onCartChanged;

        setLayout(new BorderLayout(0, 16));
        setBackground(AppTheme.BG);
        setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel title = new JLabel("POS Dashboard");
        title.setFont(AppTheme.TITLE);
        title.setForeground(AppTheme.TEXT);

        categoryFilter.addItem("All categories");
        productService.getCategories().forEach(c -> categoryFilter.addItem(c));

        searchField.setToolTipText("Search products…");
        searchField.getDocument().addDocumentListener(simpleChange(this::refreshGrid));
        categoryFilter.addActionListener(e -> refreshGrid());

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        filters.setOpaque(false);
        filters.add(searchField);
        filters.add(categoryFilter);

        gridPanel.setLayout(new GridLayout(0, 4, 16, 16));
        gridPanel.setOpaque(false);

        JScrollPane scroll = new JScrollPane(gridPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);

        cartSummary.setFont(AppTheme.BODY);
        cartSummary.setForeground(AppTheme.MUTED);
        updateCartSummary();

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.add(title, BorderLayout.NORTH);
        JPanel sub = new JPanel(new BorderLayout());
        sub.setOpaque(false);
        sub.add(filters, BorderLayout.WEST);
        sub.add(cartSummary, BorderLayout.EAST);
        header.add(sub, BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        refreshGrid();
    }

    public void refreshGrid() {
        gridPanel.removeAll();
        List<Product> products = filterProducts();
        int slotWidth = 140;
        for (Product p : products) {
            ProductCardPanel card = new ProductCardPanel(p,
                ProductImageSlot.AspectRatio.SQUARE, slotWidth);
            card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            card.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (!p.isInStock()) {
                        JOptionPane.showMessageDialog(POSDashboardPanel.this,
                            p.getName() + " is out of stock.", "Out of stock",
                            JOptionPane.INFORMATION_MESSAGE);
                        return;
                    }
                    cartService.addProduct(p);
                    updateCartSummary();
                    onCartChanged.run();
                }
            });
            gridPanel.add(card);
        }
        gridPanel.revalidate();
        gridPanel.repaint();
    }

    private List<Product> filterProducts() {
        String q = searchField.getText().trim();
        String cat = (String) categoryFilter.getSelectedItem();
        List<Product> list = q.isEmpty() ? productService.findAll() : productService.search(q);
        if (cat != null && !cat.equals("All categories")) {
            list = list.stream()
                .filter(p -> p.getCategory().equalsIgnoreCase(cat))
                .collect(Collectors.toList());
        }
        return list;
    }

    private void updateCartSummary() {
        cartSummary.setText(String.format("Cart: %d items · ₱%.2f",
            cartService.getItemCount(), cartService.getTotal()));
    }

    private static DocumentListener simpleChange(Runnable r) {
        return new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e)  { r.run(); }
            @Override public void removeUpdate(DocumentEvent e)  { r.run(); }
            @Override public void changedUpdate(DocumentEvent e) { r.run(); }
        };
    }
}
