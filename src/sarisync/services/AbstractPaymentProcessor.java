package sarisync.services;

import sarisync.interfaces.IPaymentProcessor;

/**
 * ABSTRACTION + INHERITANCE (Template Method Pattern)
 *
 * Concrete classes extend this and implement:
 *   - doProcess()         → channel-specific logic
 *   - getPaymentMethod()  → enum constant
 *   - getDisplayName()    → UI label
 *
 * Subclasses:
 *   CashPaymentProcessor   – validates cash tendered, calculates change
 *   CardPaymentProcessor   – simulates card authorisation (no change)
 *   GCashPaymentProcessor  – simulates e-wallet authorisation
 *   PayMayaPaymentProcessor – simulates PayMaya authorisation
 */
public abstract class AbstractPaymentProcessor implements IPaymentProcessor {

    // ── Template Method (Inheritance + Abstraction) ───────────────────────

    /**
     * Final process() — cannot be overridden.
     * Applies shared pre-conditions, then calls the channel-specific doProcess().
     */
    @Override
    public final PaymentResult process(double amountDue, double amountPaid) {
        if (amountDue <= 0) throw new IllegalArgumentException("Amount due must be > 0, got: " + amountDue);
        return doProcess(amountDue, amountPaid);
    }

    
    protected abstract PaymentResult doProcess(double amountDue, double amountPaid);

    // ── Shared Utility ────────────────────────────────────────────────────

    protected String formatPeso(double amount) {
        return String.format("₱%.2f", amount);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{method=" + getPaymentMethod() + "}";
    }
}
