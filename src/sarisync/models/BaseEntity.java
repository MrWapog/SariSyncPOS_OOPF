package sarisync.models;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * ABSTRACTION + INHERITANCE foundation class.
 */
public abstract class BaseEntity {

    // ── Encapsulated fields ───────────────────────────────────────────────
    private final String id;
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Constructor ───────────────────────────────────────────────────────
    protected BaseEntity() {
        this.id        = UUID.randomUUID().toString();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        doValidate();  // Template Method — force subclass validation at construction
    }

    /** Constructor that accepts an explicit ID (useful for repository hydration). */
    protected BaseEntity(String id) {
        Objects.requireNonNull(id, "Entity ID must not be null");
        this.id        = id;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        doValidate();
    }

    // ── Template Method ───────────────────────────────────────────────────
    /**
     * Called once during construction. Delegates to the abstract
     * validate() method that each concrete subclass must implement.
     */
    private void doValidate() {
        validate();
    }

    /**
     * ABSTRACTION: Subclasses define their own field-level validation rules.
     * Example implementations: Product validates price > 0, Customer validates name not blank.
     */
    protected abstract void validate();

    // ── Accessors (Encapsulation) ─────────────────────────────────────────
    public String getId() {
        return id;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /** Called by services after mutating the entity to keep updatedAt current. */
    protected void touch() {
        this.updatedAt = LocalDateTime.now();
    }

    // ── Identity ──────────────────────────────────────────────────────────
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseEntity that)) return false;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id='" + id + "'}";
    }
}
