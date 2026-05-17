package sarisync.ui.dialogs;

import sarisync.config.POSConfig;
import sarisync.models.Product;
import sarisync.services.ProductService;
import sarisync.ui.components.ImageUploadPanel;
import sarisync.ui.theme.AppTheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.function.Consumer;

/**
 * Add / Edit Product modal with image upload, preview, and fallback-icon dropdown.
 */
public class ProductFormDialog extends JDialog {

    private final ProductService productService;
    private final Product existing;
    private final Consumer<Product> onSaved;

    private final JTextField nameField = new JTextField(24);
    private final JTextField categoryField = new JTextField(24);
    private final JSpinner priceSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 999_999.0, 1.0));
    private final JSpinner stockSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 999_999, 1));
    private final JTextArea descriptionArea = new JTextArea(3, 24);
    private final ImageUploadPanel imageUpload = new ImageUploadPanel();
    private final JComboBox<String> iconCombo = new JComboBox<>(POSConfig.FALLBACK_ICONS);

    public ProductFormDialog(Frame owner, ProductService productService,
                           Product existing, Consumer<Product> onSaved) {
        super(owner, existing == null ? "Add Product" : "Edit Product", true);
        this.productService = productService;
        this.existing = existing;
        this.onSaved = onSaved;

        setMinimumSize(new Dimension(420, 520));
        setLocationRelativeTo(owner);
        buildUi();
        if (existing != null) populate(existing);
        pack();
    }

    private void buildUi() {
        JPanel root = new JPanel(new BorderLayout(0, 16));
        root.setBorder(new EmptyBorder(20, 24, 20, 24));
        root.setBackground(Color.WHITE);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 0, 6, 12);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1;

        addRow(form, gbc, "Product image", imageUpload);
        gbc.gridy++;
        gbc.insets = new Insets(12, 0, 6, 12);
        addRow(form, gbc, "Fallback icon", iconCombo);
        iconCombo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 18));
        iconCombo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                Component c = super.getListCellRendererComponent(list, value, index,
                    isSelected, cellHasFocus);
                setFont(new Font("Segoe UI Emoji", Font.PLAIN, 20));
                return c;
            }
        });

        gbc.gridy++;
        addRow(form, gbc, "Name", nameField);
        gbc.gridy++;
        addRow(form, gbc, "Category", categoryField);
        gbc.gridy++;
        addRow(form, gbc, "Price (₱)", priceSpinner);
        gbc.gridy++;
        addRow(form, gbc, "Stock", stockSpinner);
        gbc.gridy++;
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        addRow(form, gbc, "Description", new JScrollPane(descriptionArea));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actions.setOpaque(false);
        JButton cancel = styledSecondary("Cancel");
        JButton save = styledPrimary(existing == null ? "Add Product" : "Save Changes");
        cancel.addActionListener(e -> dispose());
        save.addActionListener(e -> saveProduct());
        actions.add(cancel);
        actions.add(save);

        root.add(form, BorderLayout.CENTER);
        root.add(actions, BorderLayout.SOUTH);
        setContentPane(root);
    }

    private void addRow(JPanel form, GridBagConstraints gbc, String label, JComponent field) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(AppTheme.SMALL);
        lbl.setForeground(AppTheme.MUTED);
        gbc.gridx = 0;
        gbc.weightx = 0;
        form.add(lbl, gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        form.add(field, gbc);
        gbc.gridy++;
        gbc.gridx = 0;
    }

    private void populate(Product p) {
        nameField.setText(p.getName());
        categoryField.setText(p.getCategory());
        priceSpinner.setValue(p.getPrice());
        stockSpinner.setValue(p.getStock());
        descriptionArea.setText(p.getDescription() != null ? p.getDescription() : "");
        imageUpload.setImageDataUri(p.getImageUrl());
        iconCombo.setSelectedItem(p.getFallbackIcon());
    }

    private void saveProduct() {
        String name = nameField.getText().trim();
        String category = categoryField.getText().trim();
        if (name.isEmpty() || category.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Name and category are required.",
                "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }
        double price = ((Number) priceSpinner.getValue()).doubleValue();
        int stock = ((Number) stockSpinner.getValue()).intValue();
        String image = imageUpload.getImageDataUri();
        String icon = (String) iconCombo.getSelectedItem();
        String desc = descriptionArea.getText().trim();

        try {
            Product saved;
            if (existing == null) {
                saved = productService.addProduct(name, category, price, stock, image, icon, desc);
            } else {
                saved = productService.updateProduct(existing.getId(), name, category,
                    price, stock, image, icon, desc);
            }
            onSaved.accept(saved);
            dispose();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private static JButton styledPrimary(String text) {
        JButton b = new JButton(text);
        b.setBackground(AppTheme.PRIMARY);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(8, 16, 8, 16));
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private static JButton styledSecondary(String text) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setBorder(new EmptyBorder(8, 16, 8, 16));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }
}
