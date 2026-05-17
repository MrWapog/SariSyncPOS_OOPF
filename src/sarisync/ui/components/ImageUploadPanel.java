package sarisync.ui.components;

import sarisync.services.ImageService;
import sarisync.ui.theme.AppTheme;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

/**
 * Image picker with live preview, hover "Change Image" overlay, and Remove.
 */
public class ImageUploadPanel extends JPanel {

    private static final int PREVIEW_W = 280;
    private static final int PREVIEW_H = 175; // ~16:10

    private String imageDataUri = "";
    private final JPanel previewStack = new JPanel(null);
    private final JLabel previewImage = new JLabel();
    private final JLabel placeholder = new JLabel("<html><center>Click to upload<br>"
        + "<span style='color:#64748B;font-size:11px'>JPG, PNG, WEBP · max 5MB</span></center>",
        SwingConstants.CENTER);
    private final JPanel hoverOverlay = new JPanel(new GridBagLayout());
    private final JButton removeBtn = new JButton("Remove");
    private final JFileChooser fileChooser = new JFileChooser();

    public ImageUploadPanel() {
        setLayout(new BorderLayout(0, 8));
        setOpaque(false);

        fileChooser.setFileFilter(new FileNameExtensionFilter(
            "Images (JPG, PNG, WEBP)", "jpg", "jpeg", "png", "webp"));

        previewStack.setPreferredSize(new Dimension(PREVIEW_W, PREVIEW_H));
        previewStack.setBackground(AppTheme.SLOT_BG);
        previewStack.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createDashedBorder(AppTheme.BORDER, 2f, 4f),
            BorderFactory.createEmptyBorder(4, 4, 4, 4)));
        previewStack.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        placeholder.setFont(AppTheme.BODY);
        placeholder.setForeground(AppTheme.MUTED);
        placeholder.setBounds(0, 0, PREVIEW_W, PREVIEW_H);

        previewImage.setHorizontalAlignment(SwingConstants.CENTER);
        previewImage.setVerticalAlignment(SwingConstants.CENTER);
        previewImage.setBounds(0, 0, PREVIEW_W, PREVIEW_H);

        JLabel changeLabel = new JLabel("Change Image", SwingConstants.CENTER);
        changeLabel.setForeground(Color.WHITE);
        changeLabel.setFont(AppTheme.HEADING);
        hoverOverlay.setBackground(AppTheme.OVERLAY);
        hoverOverlay.setBounds(0, 0, PREVIEW_W, PREVIEW_H);
        hoverOverlay.setVisible(false);
        hoverOverlay.add(changeLabel);
        hoverOverlay.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        removeBtn.setVisible(false);
        removeBtn.setForeground(AppTheme.DANGER);
        removeBtn.setFocusPainted(false);
        removeBtn.setBorderPainted(false);
        removeBtn.setContentAreaFilled(false);
        removeBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        removeBtn.addActionListener(e -> clearImage());

        previewStack.add(placeholder);
        previewStack.add(previewImage);
        previewStack.add(hoverOverlay);

        MouseAdapter openPicker = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                pickImage();
            }
        };
        previewStack.addMouseListener(openPicker);
        hoverOverlay.addMouseListener(openPicker);
        placeholder.addMouseListener(openPicker);

        previewStack.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!imageDataUri.isBlank()) hoverOverlay.setVisible(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoverOverlay.setVisible(false);
            }
        });

        JPanel south = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        south.setOpaque(false);
        south.add(removeBtn);

        add(previewStack, BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
        showPlaceholder();
    }

    public void setImageDataUri(String uri) {
        this.imageDataUri = uri != null ? uri : "";
        if (imageDataUri.isBlank()) {
            clearImage();
        } else {
            applyPreview(imageDataUri);
        }
    }

    public String getImageDataUri() {
        return imageDataUri;
    }

    public boolean hasImage() {
        return imageDataUri != null && !imageDataUri.isBlank();
    }

    private void pickImage() {
        if (fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = fileChooser.getSelectedFile();
        try {
            String uri = ImageService.fileToDataUri(file);
            applyPreview(uri);
            imageDataUri = uri;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Invalid image",
                JOptionPane.WARNING_MESSAGE);
        }
    }

    private void applyPreview(String uri) {
        ImageIcon icon = ImageService.scaledIcon(uri, PREVIEW_W, PREVIEW_H);
        previewImage.setIcon(icon);
        placeholder.setVisible(false);
        previewImage.setVisible(true);
        hoverOverlay.setVisible(false);
        removeBtn.setVisible(true);
        previewStack.setBorder(BorderFactory.createLineBorder(AppTheme.BORDER));
    }

    private void showPlaceholder() {
        placeholder.setVisible(true);
        previewImage.setVisible(false);
        previewImage.setIcon(null);
        hoverOverlay.setVisible(false);
        removeBtn.setVisible(false);
        previewStack.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createDashedBorder(AppTheme.BORDER, 2f, 4f),
            BorderFactory.createEmptyBorder(4, 4, 4, 4)));
    }

    private void clearImage() {
        imageDataUri = "";
        showPlaceholder();
    }
}
