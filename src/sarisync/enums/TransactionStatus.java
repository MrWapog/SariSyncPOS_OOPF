package sarisync.enums;

/**
 * Represents the lifecycle states of a POS transaction.
 */
public enum TransactionStatus {
    PENDING("Pending"),
    COMPLETED("Completed"),
    VOIDED("Voided"),
    REFUNDED("Refunded");

    private final String displayName;

    TransactionStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
