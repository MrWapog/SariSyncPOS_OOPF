package sarisync.models;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * ABSTRACTION + INHERITANCE foundation class.
 *
 * BaseEntity is an abstract class that provides:
 *  - A universally unique identifier (UUID) generated at creation
 *  - Audit timestamps (createdAt, updatedAt)
 *  - Abstract validation contract that all subclasses must honour
 *
 * Every domain model in SariSync inherits from this class, ensuring
 * consistent identity and traceability across the system.
 *
 * Design pattern: Template Method — validate() is called by the
 * constructor via doValidate(), subclasses fill in the rules.
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
        // Subclasses MUST call validate() at the END of their own constructor —
        // we cannot call it here because subclass fields are not yet assigned
        // (Effective Java Item 19: never call overridable methods from constructors)
    }

    /** Constructor that accepts an explicit ID (useful for repository hydration). */
    protected BaseEntity(String id) {
        Objects.requireNonNull(id, "Entity ID must not be null");
        this.id        = id;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        // Subclasses MUST call validate() at the END of their own constructor.
    }

    /**
     * ABSTRACTION: Subclasses define their own field-level validation rules.
     * Subclasses must call this method at the end of their constructor
     * after all fields are initialised.
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
