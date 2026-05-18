package sarisync.models;

import java.util.Objects;

/**
 * ENCAPSULATION + INHERITANCE
 *
 * Inherits identity and audit timestamps from BaseEntity.
 */
public class Product extends BaseEntity {

    // ── Private Fields (Encapsulation) ────────────────────────────────────
    private String name;
    private String category;
    private double price;
    private int    stock;
    private String imageUrl;
    private String description;

    // ── Constructor ───────────────────────────────────────────────────────
    public Product(String name, String category, double price, int stock,
                   String imageUrl, String description) {
        super();                          // assigns UUID + timestamps
        this.name        = name;
        this.category    = category;
        this.price       = price;
        this.stock       = stock;
        this.imageUrl    = imageUrl;
        this.description = description;
        // validate() is invoked by BaseEntity constructor via doValidate()
    }

    /** Hydration constructor: restores an existing product from a persistent store. */
    public Product(String id, String name, String category, double price,
                   int stock, String imageUrl, String description) {
        super(id);
        this.name        = name;
        this.category    = category;
        this.price       = price;
        this.stock       = stock;
        this.imageUrl    = imageUrl;
        this.description = description;
    }

    // ── Validation Contract (from BaseEntity) ─────────────────────────────
    @Override
    protected void validate() {
        Objects.requireNonNull(name,     "Product name must not be null");
        Objects.requireNonNull(category, "Product category must not be null");
        if (name.isBlank())             throw new IllegalArgumentException("Product name must not be blank");
        if (price < 0)                  throw new IllegalArgumentException("Price must be >= 0");
        if (stock < 0)                  throw new IllegalArgumentException("Stock must be >= 0");
    }

    // ── Getters (Encapsulation — read-only external access) ───────────────
    public String getName()        { return name;        }
    public String getCategory()    { return category;    }
    public double getPrice()       { return price;       }
    public int    getStock()       { return stock;       }
    public String getImageUrl()    { return imageUrl;    }
    public String getDescription() { return description; }

    // ── Controlled Mutators (Encapsulation — enforce business rules) ───────
    public void setName(String name) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name must not be blank");
        this.name = name;
        touch();
    }

    public void setCategory(String category) {
        Objects.requireNonNull(category, "Category must not be null");
        this.category = category;
        touch();
    }

    public void setPrice(double price) {
        if (price < 0) throw new IllegalArgumentException("Price must be >= 0, got: " + price);
        this.price = price;
        touch();
    }

    public void setStock(int stock) {
        if (stock < 0) throw new IllegalArgumentException("Stock must be >= 0, got: " + stock);
        this.stock = stock;
        touch();
    }

    public void setImageUrl(String imageUrl)       { this.imageUrl    = imageUrl; touch(); }
    public void setDescription(String description) { this.description = description; touch(); }

    // ── Domain Behaviour ──────────────────────────────────────────────────

    /**
     * Reduces stock by the given quantity.
     * Throws if there is insufficient inventory.
     */
    public void deductStock(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Deduct quantity must be > 0");
        if (quantity > stock) throw new IllegalStateException(
            "Insufficient stock for '" + name + "': requested=" + quantity + ", available=" + stock
        );
        this.stock -= quantity;
        touch();
    }

    /**
     * Restores stock (e.g., after a voided transaction).
     */
    public void restoreStock(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Restore quantity must be > 0");
        this.stock += quantity;
        touch();
    }

    /** Returns true if the product has at least one unit in stock. */
    public boolean isInStock()  { return stock > 0; }

    /** Returns true if stock is low (≤ 20 units) — threshold from business rules. */
    public boolean isLowStock() { return stock <= 20; }

    @Override
    public String toString() {
        return "Product{id='" + getId() + "', name='" + name + "', price=" + price
               + ", stock=" + stock + ", category='" + category + "'}";
    }
}
