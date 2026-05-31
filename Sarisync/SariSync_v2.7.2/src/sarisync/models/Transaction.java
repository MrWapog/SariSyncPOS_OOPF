package sarisync.models;

import sarisync.enums.PaymentMethod;
import sarisync.enums.TransactionStatus;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * ENCAPSULATION + INHERITANCE
 *
 * Transaction is the central aggregate in the SariSync domain.
 *
 * v2.6.0 additions:
 *  - voidReason: optional reason text entered when voiding
 *  - voidedAt:   timestamp of when the transaction was voided
 *  - isToday():  helper used by dashboard to filter "Sales Today"
 *  - formatted display of transactionDate for UI/reports
 */
public class Transaction extends BaseEntity {

    private static final DateTimeFormatter DISPLAY_FMT =
        DateTimeFormatter.ofPattern("MMM dd, yyyy — hh:mm a");
    private static final DateTimeFormatter ID_FMT =
        DateTimeFormatter.ofPattern("yyyyMMdd");

    // ── Private Fields (Encapsulation) ────────────────────────────────────
    private final String           transactionNumber;
    private final List<CartItem>   items;
    private final double           subtotal;
    private final double           tax;
    private final double           total;
    private final double           loyaltyDiscount;
    private final double           finalTotal;
    private final double           amountPaid;
    private final double           change;
    private final PaymentMethod    paymentMethod;
    private final LocalDateTime    transactionDate;
    private final String           cashierName;
    private final String           customerName;
    private final int              pointsRedeemed;
    private final int              pointsEarned;
    private       TransactionStatus status;

    // ── New void fields (v2.6.0) ──────────────────────────────────────────
    private String        voidReason;
    private LocalDateTime voidedAt;

    // ── Constructor ───────────────────────────────────────────────────────
    public Transaction(String transactionNumber, List<CartItem> items,
                       double subtotal, double tax, double total,
                       double loyaltyDiscount, double finalTotal,
                       double amountPaid, double change,
                       PaymentMethod paymentMethod,
                       String cashierName, String customerName,
                       int pointsRedeemed, int pointsEarned) {
        super();
        this.transactionNumber = Objects.requireNonNull(transactionNumber);
        this.items             = Collections.unmodifiableList(items);
        this.subtotal          = subtotal;
        this.tax               = tax;
        this.total             = total;
        this.loyaltyDiscount   = loyaltyDiscount;
        this.finalTotal        = finalTotal;
        this.amountPaid        = amountPaid;
        this.change            = change;
        this.paymentMethod     = Objects.requireNonNull(paymentMethod);
        this.transactionDate   = LocalDateTime.now();
        this.cashierName       = cashierName;
        this.customerName      = customerName;
        this.pointsRedeemed    = pointsRedeemed;
        this.pointsEarned      = pointsEarned;
        this.status            = TransactionStatus.COMPLETED;
        validate();
    }

    @Override
    protected void validate() {
        // Validated by TransactionService before construction
    }

    // ── Read-Only Getters ─────────────────────────────────────────────────
    public String            getTransactionNumber() { return transactionNumber; }
    public List<CartItem>    getItems()             { return items;             }
    public double            getSubtotal()          { return subtotal;          }
    public double            getTax()               { return tax;               }
    public double            getTotal()             { return total;             }
    public double            getLoyaltyDiscount()   { return loyaltyDiscount;   }
    public double            getFinalTotal()        { return finalTotal;        }
    public double            getAmountPaid()        { return amountPaid;        }
    public double            getChange()            { return change;            }
    public PaymentMethod     getPaymentMethod()     { return paymentMethod;     }
    public LocalDateTime     getTransactionDate()   { return transactionDate;   }
    public String            getCashierName()       { return cashierName;       }
    public String            getCustomerName()      { return customerName;      }
    public int               getPointsRedeemed()    { return pointsRedeemed;    }
    public int               getPointsEarned()      { return pointsEarned;      }
    public TransactionStatus getStatus()            { return status;            }
    public String            getVoidReason()        { return voidReason;        }
    public LocalDateTime     getVoidedAt()          { return voidedAt;          }

    // ── Formatted Display ─────────────────────────────────────────────────

    /** Returns a human-readable timestamp: "May 19, 2026 — 09:42 AM" */
    public String getFormattedDate() {
        return transactionDate.format(DISPLAY_FMT);
    }

    /** Returns a human-readable void timestamp, or empty string if not voided. */
    public String getFormattedVoidedAt() {
        return voidedAt != null ? voidedAt.format(DISPLAY_FMT) : "";
    }

    // ── Domain Behaviour ──────────────────────────────────────────────────

    /**
     * Marks this transaction as VOIDED with an optional reason.
     * Records the exact void timestamp.
     * Only COMPLETED transactions can be voided.
     *
     * @param reason optional cashier/admin reason for voiding
     */
    public void void_(String reason) {
        if (status != TransactionStatus.COMPLETED)
            throw new IllegalStateException(
                "Only COMPLETED transactions can be voided. Current: " + status
            );
        this.status     = TransactionStatus.VOIDED;
        this.voidReason = (reason != null && !reason.isBlank()) ? reason.trim() : "No reason given";
        this.voidedAt   = LocalDateTime.now();
        touch();
    }

    /** Convenience overload — void without a reason. */
    public void void_() { void_("No reason given"); }

    /** Returns true only if status is COMPLETED (used for all revenue/analytics). */
    public boolean isCompleted() { return status == TransactionStatus.COMPLETED; }

    /** Returns true if this transaction was voided. */
    public boolean isVoided()    { return status == TransactionStatus.VOIDED;    }

    /**
     * Returns true if this transaction occurred today.
     * Used by the dashboard "Sales Today" filter.
     */
    public boolean isToday() {
        return transactionDate.toLocalDate()
                              .equals(java.time.LocalDate.now());
    }

    /** Total individual units in this transaction. */
    public int getTotalItemCount() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    @Override
    public String toString() {
        return "Transaction{num='" + transactionNumber + "', total=" + finalTotal
               + ", status=" + status + ", date=" + getFormattedDate() + "}";
    }
}
