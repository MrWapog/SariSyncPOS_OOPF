package sarisync.interfaces;

import sarisync.enums.PaymentMethod;

/**
 * ABSTRACTION + POLYMORPHISM
 *
 * IPaymentProcessor defines the contract that every payment channel must fulfil.
 * The TransactionService works exclusively with this interface — it never
 * references CashPaymentProcessor, CardPaymentProcessor, etc. directly.
 *
 * This enables POLYMORPHISM: the service calls process() on any IPaymentProcessor
 * reference and the correct subclass behaviour executes automatically.
 *
 * To add a new payment method (e.g. Maya, ShopeePay):
 *   1. Implement this interface (or extend AbstractPaymentProcessor).
 *   2. Register the new processor in PaymentProcessorFactory.
 *   No other class needs to change.
 */
public interface IPaymentProcessor {

    /**
     * Attempts to process the payment.
     *
     * @param amountDue  Total amount the customer owes.
     * @param amountPaid Amount tendered by the customer (ignored for digital payments).
     * @return PaymentResult carrying success flag, change, and a status message.
     * @throws IllegalArgumentException if amountDue <= 0
     * @throws IllegalStateException    if amountPaid < amountDue for cash payments
     */
    PaymentResult process(double amountDue, double amountPaid);

    /** Returns the PaymentMethod enum constant this processor handles. */
    PaymentMethod getPaymentMethod();

    /** Human-readable name for UI display. */
    String getDisplayName();

    /**
     * Returns true if this processor requires the cashier to handle physical cash
     * (i.e., needs change calculation displayed on screen).
     */
    boolean requiresChangeCalculation();

    // ── Nested Result Record ──────────────────────────────────────────────

    /**
     * Immutable value object returned by process().
     * Encapsulates the outcome of a payment attempt.
     */
    record PaymentResult(boolean success,
                         double  amountPaid,
                         double  change,
                         String  message) {

        public static PaymentResult ok(double amountPaid, double change) {
            return new PaymentResult(true, amountPaid, change, "Payment successful");
        }

        public static PaymentResult fail(String reason) {
            return new PaymentResult(false, 0, 0, reason);
        }
    }
}
