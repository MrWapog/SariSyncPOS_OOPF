package sarisync.enums;


public enum PaymentMethod {
    CASH("Cash"),
    CARD("Card"),
    GCASH("GCash"),
    PAYMAYA("PayMaya");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Returns true if this payment method requires physical cash handling.
     */
    public boolean requiresCashHandling() {
        return this == CASH;
    }

    /**
     * Returns true if this method is a digital/electronic payment.
     */
    public boolean isDigital() {
        return this == GCASH || this == PAYMAYA || this == CARD;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
