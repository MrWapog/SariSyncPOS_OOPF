package sarisync.services;

import sarisync.config.POSConfig;
import sarisync.models.Product;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;

/**
 * Validates product image uploads and converts files to embedded data URIs
 * stored on {@link Product#getImageUrl()}.
 */
public final class ImageService {

    private ImageService() {}

    public static void validateImageFile(File file) throws IOException {
        if (file == null || !file.isFile()) {
            throw new IOException("Please select a valid image file.");
        }
        long size = Files.size(file.toPath());
        if (size > POSConfig.MAX_IMAGE_BYTES) {
            throw new IOException("Image must be 5 MB or smaller.");
        }
        String ext = extension(file.getName());
        if (!isAllowedExtension(ext)) {
            throw new IOException("Only JPG, PNG, and WEBP images are supported.");
        }
        BufferedImage img = ImageIO.read(file);
        if (img == null) {
            throw new IOException("Could not read image. Use JPG, PNG, or WEBP.");
        }
    }

    public static String fileToDataUri(File file) throws IOException {
        validateImageFile(file);
        String ext = extension(file.getName());
        String mime = mimeType(ext);
        byte[] bytes = Files.readAllBytes(file.toPath());
        String encoded = Base64.getEncoder().encodeToString(bytes);
        return "data:" + mime + ";base64," + encoded;
    }

    public static Optional<Image> loadImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return Optional.empty();
        }
        try {
            if (imageUrl.startsWith("data:")) {
                int comma = imageUrl.indexOf(',');
                if (comma < 0) return Optional.empty();
                byte[] bytes = Base64.getDecoder().decode(imageUrl.substring(comma + 1));
                return Optional.of(ImageIO.read(new java.io.ByteArrayInputStream(bytes)));
            }
            File file = new File(imageUrl);
            if (file.isFile()) {
                return Optional.of(ImageIO.read(file));
            }
        } catch (Exception ignored) {
            // fall through
        }
        return Optional.empty();
    }

    public static ImageIcon scaledIcon(String imageUrl, int width, int height) {
        return loadImage(imageUrl)
            .map(img -> {
                Image scaled = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            })
            .orElse(null);
    }

    public static boolean isAllowedExtension(String ext) {
        if (ext == null) return false;
        String lower = ext.toLowerCase(Locale.ROOT);
        return Arrays.stream(POSConfig.ALLOWED_IMAGE_EXTENSIONS)
            .anyMatch(e -> e.equals(lower));
    }

    private static String extension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1).toLowerCase(Locale.ROOT) : "";
    }

    private static String mimeType(String ext) {
        return switch (ext) {
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            default -> "image/jpeg";
        };
    }
}
