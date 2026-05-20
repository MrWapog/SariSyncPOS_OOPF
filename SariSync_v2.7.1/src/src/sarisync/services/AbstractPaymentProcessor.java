package sarisync.services;

import sarisync.interfaces.IPaymentProcessor;

/**
 * ABSTRACTION + INHERITANCE (Template Method Pattern)
 *
 * AbstractPaymentProcessor provides the shared skeleton for all payment
 * processors. It handles common validation (amountDue > 0) and delegates
 * the channel-specific logic to the abstract doProcess() method.
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

    /**
     * ABSTRACTION: Channel-specific payment logic.
     * Each subclass defines how its payment channel works.
     */
    protected abstract PaymentResult doProcess(double amountDue, double amountPaid);

    // ── Shared Utility ────────────────────────────────────────────────────

    /**
     * Formats a peso amount for log/receipt display.
     */
    protected String formatPeso(double amount) {
        return String.format("₱%.2f", amount);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{method=" + getPaymentMethod() + "}";
    }
}
