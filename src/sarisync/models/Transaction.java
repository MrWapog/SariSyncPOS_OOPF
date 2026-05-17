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
 * All monetary fields are computed and stored at creation — making
 * the transaction a reliable audit record that is immutable after completion.
 *
 * Fields are private and exposed only as read-only getters, enforcing
 * the rule that a completed transaction cannot be retroactively altered
 * (it can only be voided).
 */
public class Transaction extends BaseEntity {

    private static final DateTimeFormatter ID_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    // ── Private Fields (Encapsulation) ────────────────────────────────────
    private final String              transactionNumber;   // e.g. TXN-20260513-001
    private final List<CartItem>      items;               // unmodifiable snapshot
    private final double              subtotal;
    private final double              tax;
    private final double              total;               // subtotal + tax
    private final double              loyaltyDiscount;
    private final double              finalTotal;          // total - loyaltyDiscount
    private final double              amountPaid;
    private final double              change;
    private final PaymentMethod       paymentMethod;
    private final LocalDateTime       transactionDate;
    private final String              cashierName;
    private final String              customerName;
    private final int                 pointsRedeemed;
    private final int                 pointsEarned;
    private       TransactionStatus   status;

    // ── Constructor (called by TransactionService after computing all values) ──
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
    }

    // ── Validation ────────────────────────────────────────────────────────
    @Override
    protected void validate() {
        // Fields are set after super() call; deep validation happens in TransactionService
    }

    // ── Read-Only Getters (Encapsulation) ─────────────────────────────────
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

    // ── Domain Behaviour ──────────────────────────────────────────────────

    /** Marks this transaction as voided. Can only be applied to COMPLETED transactions. */
    public void void_() {
        if (status != TransactionStatus.COMPLETED)
            throw new IllegalStateException("Only COMPLETED transactions can be voided. Current status: " + status);
        this.status = TransactionStatus.VOIDED;
        touch();
    }

    public boolean isCompleted() { return status == TransactionStatus.COMPLETED; }
    public boolean isVoided()    { return status == TransactionStatus.VOIDED;    }

    /** Returns the total number of individual units sold in this transaction. */
    public int getTotalItemCount() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    /** Formats the transaction number for display/receipts. */
    public String getFormattedId() {
        return transactionNumber;
    }

    @Override
    public String toString() {
        return "Transaction{num='" + transactionNumber + "', total=" + finalTotal
               + ", method=" + paymentMethod + ", status=" + status + "}";
    }
}
