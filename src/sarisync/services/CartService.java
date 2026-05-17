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
 *
 * CartService manages the active shopping cart for a single sales session.
 * All cart mutation goes through controlled methods that enforce business rules.
 *
 * Priority fix from original codebase:
 *   Tax (0.12) and totals were computed inline in BOTH POSMainScreen.tsx AND
 *   PaymentScreen.tsx — a DRY violation. This service centralises all cart
 *   calculations in one place.
 */
public class CartService {

    // ── Private State ─────────────────────────────────────────────────────
    private final List<CartItem> items = new ArrayList<>();
    private       CartItem       lastRemovedItem;

    // ── Cart Mutations ────────────────────────────────────────────────────

    /**
     * Adds a product to the cart.
     * If it already exists, increments quantity by 1.
     */
    public CartItem addProduct(Product product) {
        Optional<CartItem> existing = findByProductId(product.getId());
        if (existing.isPresent()) {
            existing.get().increment();
            return existing.get();
        }
        CartItem item = new CartItem(product, 1);
        items.add(item);
        return item;
    }

    /**
     * Removes a product entirely from the cart.
     * Stores the removed item for potential undo.
     *
     * @throws IllegalArgumentException if the product is not in the cart.
     */
    public void removeProduct(String productId) {
        CartItem toRemove = findByProductId(productId).orElseThrow(() ->
            new IllegalArgumentException("Product not in cart: " + productId)
        );
        lastRemovedItem = toRemove;
        items.remove(toRemove);
    }

    /**
     * Sets the quantity of a cart item.
     * If qty == 0 the item is removed (same behaviour as removeProduct).
     *
     * @throws IllegalArgumentException if qty is negative.
     */
    public void setQuantity(String productId, int qty) {
        if (qty < 0) throw new IllegalArgumentException("Quantity must be >= 0, got: " + qty);
        if (qty == 0) {
            removeProduct(productId);
            return;
        }
        findByProductId(productId).orElseThrow(() ->
            new IllegalArgumentException("Product not in cart: " + productId)
        ).setQuantity(qty);
    }

    /** Empties the cart entirely. */
    public void clear() {
        items.clear();
    }

    // ── Calculations (single source of truth) ────────────────────────────

    /** Returns the pre-tax subtotal of all items. */
    public double getSubtotal() {
        return items.stream().mapToDouble(CartItem::getLineSubtotal).sum();
    }

    /** Returns the VAT amount (subtotal × POSConfig.VAT_RATE). */
    public double getTax() {
        return getSubtotal() * POSConfig.VAT_RATE;
    }

    /** Returns subtotal + tax. */
    public double getTotal() {
        return getSubtotal() + getTax();
    }

    /**
     * Returns the final total after applying a loyalty discount.
     * @param loyaltyDiscount peso value of the discount (must be >= 0)
     */
    public double getFinalTotal(double loyaltyDiscount) {
        return Math.max(0, getTotal() - loyaltyDiscount);
    }

    /** Returns the total number of individual units in the cart. */
    public int getItemCount() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    /** Returns true if the cart has no items. */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    // ── Reads ─────────────────────────────────────────────────────────────

    /** Returns an unmodifiable view of the cart. */
    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    /** Returns the last item removed (for undo support). */
    public Optional<CartItem> getLastRemovedItem() {
        return Optional.ofNullable(lastRemovedItem);
    }

    private Optional<CartItem> findByProductId(String productId) {
        return items.stream()
            .filter(i -> i.getProduct().getId().equals(productId))
            .findFirst();
    }
}
