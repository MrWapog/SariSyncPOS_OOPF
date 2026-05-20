package sarisync.services;

import sarisync.enums.PaymentMethod;

// ══════════════════════════════════════════════════════════════════════════
//  POLYMORPHISM — All three classes extend AbstractPaymentProcessor and
//  override doProcess() with channel-specific behaviour. The TransactionService
//  never knows which concrete type it is calling; it calls process() on the
//  IPaymentProcessor reference and polymorphism selects the right implementation.
// ══════════════════════════════════════════════════════════════════════════

// ─── Cash Payment ─────────────────────────────────────────────────────────

/**
 * Handles cash transactions.
 * Validates that enough cash was tendered and calculates change.
 */
class CashPaymentProcessor extends AbstractPaymentProcessor {

    @Override
    protected PaymentResult doProcess(double amountDue, double amountPaid) {
        if (amountPaid < amountDue) {
            return PaymentResult.fail(
                "Insufficient cash. Due: " + formatPeso(amountDue)
                + ", Paid: " + formatPeso(amountPaid)
            );
        }
        double change = amountPaid - amountDue;
        return PaymentResult.ok(amountPaid, change);
    }

    @Override public PaymentMethod getPaymentMethod()        { return PaymentMethod.CASH;  }
    @Override public String        getDisplayName()          { return "Cash";               }
    @Override public boolean       requiresChangeCalculation() { return true;              }
}

// ─── Card Payment ─────────────────────────────────────────────────────────

/**
 * Handles credit/debit card transactions.
 * In a production system this would call an acquiring bank API.
 * Amount paid equals amount due; no change is returned.
 */
class CardPaymentProcessor extends AbstractPaymentProcessor {

    @Override
    protected PaymentResult doProcess(double amountDue, double amountPaid) {
        // Simulate card authorisation (always succeeds in prototype)
        return PaymentResult.ok(amountDue, 0.0);
    }

    @Override public PaymentMethod getPaymentMethod()        { return PaymentMethod.CARD;  }
    @Override public String        getDisplayName()          { return "Card";               }
    @Override public boolean       requiresChangeCalculation() { return false;             }
}

// ─── GCash Payment ────────────────────────────────────────────────────────

/**
 * Handles GCash e-wallet transactions.
 * In production this would integrate with GCash's merchant API.
 */
class GCashPaymentProcessor extends AbstractPaymentProcessor {

    @Override
    protected PaymentResult doProcess(double amountDue, double amountPaid) {
        return PaymentResult.ok(amountDue, 0.0);
    }

    @Override public PaymentMethod getPaymentMethod()        { return PaymentMethod.GCASH;   }
    @Override public String        getDisplayName()          { return "GCash";                }
    @Override public boolean       requiresChangeCalculation() { return false;               }
}

// ─── PayMaya Payment ──────────────────────────────────────────────────────

/**
 * Handles PayMaya (Maya) e-wallet transactions.
 */
class PayMayaPaymentProcessor extends AbstractPaymentProcessor {

    @Override
    protected PaymentResult doProcess(double amountDue, double amountPaid) {
        return PaymentResult.ok(amountDue, 0.0);
    }

    @Override public PaymentMethod getPaymentMethod()        { return PaymentMethod.PAYMAYA; }
    @Override public String        getDisplayName()          { return "PayMaya";              }
    @Override public boolean       requiresChangeCalculation() { return false;               }
}
