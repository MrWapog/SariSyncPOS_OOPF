package sarisync.models;

import java.util.Objects;

/**
 * ENCAPSULATION + INHERITANCE
 */
public class CartItem extends BaseEntity {

    // ── Private Fields ────────────────────────────────────────────────────
    private final Product product;   
    private int           quantity;

    // ── Constructor ───────────────────────────────────────────────────────
    public CartItem(Product product, int quantity) {
        super();
        this.product  = product;
        this.quantity = quantity;
        validate();
    }

    // ── Validation ────────────────────────────────────────────────────────
    @Override
    protected void validate() {
        Objects.requireNonNull(product, "CartItem product must not be null");
        if (quantity < 1) throw new IllegalArgumentException("Cart quantity must be >= 1, got: " + quantity);
    }

    // ── Getters ───────────────────────────────────────────────────────────
    public Product getProduct()  { return product;  }
    public int     getQuantity() { return quantity; }

    // ── Domain Behaviour ──────────────────────────────────────────────────

    public double getLineSubtotal() {
        return product.getPrice() * quantity;
    }

    /** Increments quantity by 1. */
    public void increment() {
        this.quantity++;
        touch();
    }

    /** Decrements quantity. Throws if it would drop below 1. */
    public void decrement() {
        if (quantity <= 1) throw new IllegalStateException(
            "Cannot decrement below 1. Remove the item from the cart instead."
        );
        this.quantity--;
        touch();
    }

    /** Directly sets quantity. Throws if <= 0. */
    public void setQuantity(int quantity) {
        if (quantity < 1) throw new IllegalArgumentException("Quantity must be >= 1, got: " + quantity);
        this.quantity = quantity;
        touch();
    }

    @Override
    public String toString() {
        return "CartItem{product='" + product.getName() + "', qty=" + quantity
               + ", lineTotal=" + getLineSubtotal() + "}";
    }
}
