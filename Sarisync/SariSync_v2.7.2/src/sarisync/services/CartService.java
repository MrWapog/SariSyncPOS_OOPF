package sarisync.services;

import sarisync.config.POSConfig;
import sarisync.models.CartItem;
import sarisync.models.Product;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * ENCAPSULATION
 */
public class CartService {

    private final List<CartItem> items          = new ArrayList<>();
    private       CartItem       lastRemovedItem;

    // ── Cart Mutations ────────────────────────────────────────────────────

    /**
     * @throws IllegalStateException if product is out of stock.
     */
    public CartItem addProduct(Product product) {
        if (product.getStock() == 0)
            throw new IllegalStateException("'" + product.getName() + "' is out of stock.");
        Optional<CartItem> existing = findByProductId(product.getId());
        if (existing.isPresent()) {
            CartItem item = existing.get();
            if (item.getQuantity() >= product.getStock())
                throw new IllegalStateException(
                    "Cannot add more '" + product.getName()
                    + "'. Only " + product.getStock() + " in stock."
                );
            item.increment();
            return item;
        }
        CartItem item = new CartItem(product, 1);
        items.add(item);
        return item;
    }

    public void removeProduct(String productId) {
        CartItem toRemove = findByProductId(productId).orElseThrow(() ->
            new IllegalArgumentException("Product not in cart: " + productId)
        );
        lastRemovedItem = toRemove;
        items.remove(toRemove);
    }

    /**
     * @param productId target product
     * @param qty       desired quantity (1 or more)
     * @param product   product reference (needed for stock validation)
     */
    public void updateQuantity(String productId, int qty, Product product) {
        if (qty < 0) throw new IllegalArgumentException("Quantity cannot be negative.");
        if (qty == 0) {
            removeProduct(productId);
            return;
        }
        if (qty > product.getStock()) {
            throw new IllegalStateException(
                "Insufficient stock for '" + product.getName()
                + "'. Available: " + product.getStock() + ", Requested: " + qty
            );
        }
        CartItem item = findByProductId(productId).orElseThrow(() ->
            new IllegalArgumentException("Product not in cart: " + productId)
        );
        item.setQuantity(qty);
    }

    /** Sets quantity via cart item directly (no stock validation — used internally). */
    public void setQuantity(String productId, int qty) {
        if (qty < 0) throw new IllegalArgumentException("Quantity must be >= 0");
        if (qty == 0) { removeProduct(productId); return; }
        findByProductId(productId).orElseThrow(() ->
            new IllegalArgumentException("Product not in cart: " + productId)
        ).setQuantity(qty);
    }

    /** Empties the cart. */
    public void clear() { items.clear(); }

    // ── Calculations ──────────────────────────────────────────────────────

    public double getSubtotal() {
        return items.stream().mapToDouble(CartItem::getLineSubtotal).sum();
    }

    public double getTax() { return getSubtotal() * POSConfig.VAT_RATE; }

    public double getTotal() { return getSubtotal() + getTax(); }

    public double getFinalTotal(double loyaltyDiscount) {
        return Math.max(0, getTotal() - loyaltyDiscount);
    }

    public int     getItemCount() { return items.stream().mapToInt(CartItem::getQuantity).sum(); }
    public boolean isEmpty()      { return items.isEmpty(); }

    // ── Reads ─────────────────────────────────────────────────────────────
    public List<CartItem>    getItems()          { return Collections.unmodifiableList(items); }
    public Optional<CartItem> getLastRemovedItem(){ return Optional.ofNullable(lastRemovedItem); }

    private Optional<CartItem> findByProductId(String productId) {
        return items.stream()
            .filter(i -> i.getProduct().getId().equals(productId))
            .findFirst();
    }
}
