package sarisync.models;

import sarisync.enums.ShiftStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

public class Shift extends BaseEntity {

    // ── Required fields ──────────────────────────────────────────────────
    private final String        openedBy;          // username who opened the shift
    private final LocalDateTime startedAt;
    private final BigDecimal    startingAmount;
    private       ShiftStatus   status;

    // ── Fields populated on close ────────────────────────────────────────
    private String        closedBy;
    private LocalDateTime closedAt;
    private BigDecimal    expectedCash;
    private BigDecimal    actualCash;
    private BigDecimal    shortageAmount;
    private BigDecimal    overageAmount;
    private BigDecimal    cashVariance;
    private String        notes;

    // ── Constructor ──────────────────────────────────────────────────────
    public Shift(String openedBy, BigDecimal startingAmount) {
        super();
        this.openedBy       = openedBy;
        this.startedAt      = LocalDateTime.now();
        this.startingAmount = startingAmount == null ? BigDecimal.ZERO : startingAmount;
        this.status         = ShiftStatus.OPEN;
        validate();
    }

    @Override
    protected void validate() {
        Objects.requireNonNull(openedBy, "openedBy is required");
        if (openedBy.isBlank()) throw new IllegalArgumentException("openedBy cannot be blank");
    }

    /**
     * Closes the shift and computes the cash variance.
     *
     * @param closedBy   username of the user closing the shift
     * @param actualCash counted cash in the drawer at close
     * @param cashSales  total of all CASH-method completed transactions during this shift
     * @param notes      optional notes about the shift close
     */
    public void closeShift(String closedBy, BigDecimal actualCash, BigDecimal cashSales, String notes) {
        if (status != ShiftStatus.OPEN)
            throw new IllegalStateException("Shift is already closed");
        Objects.requireNonNull(closedBy, "closedBy is required");
        Objects.requireNonNull(actualCash, "actualCash is required");

        BigDecimal sales = cashSales == null ? BigDecimal.ZERO : cashSales;
        this.expectedCash   = startingAmount.add(sales).setScale(2, RoundingMode.HALF_UP);
        this.actualCash     = actualCash.setScale(2, RoundingMode.HALF_UP);
        this.cashVariance   = this.actualCash.subtract(this.expectedCash).setScale(2, RoundingMode.HALF_UP);
        this.shortageAmount = this.cashVariance.signum() < 0 ? this.cashVariance.abs() : BigDecimal.ZERO;
        this.overageAmount  = this.cashVariance.signum() > 0 ? this.cashVariance       : BigDecimal.ZERO;

        this.closedBy = closedBy;
        this.closedAt = LocalDateTime.now();
        this.notes    = notes;
        this.status   = ShiftStatus.CLOSED;
        touch();
    }

    // ── Getters ──────────────────────────────────────────────────────────
    public String        getOpenedBy()       { return openedBy;       }
    public String        getClosedBy()       { return closedBy;       }
    public LocalDateTime getStartedAt()      { return startedAt;      }
    public LocalDateTime getClosedAt()       { return closedAt;       }
    public BigDecimal    getStartingAmount() { return startingAmount; }
    public BigDecimal    getExpectedCash()   { return expectedCash;   }
    public BigDecimal    getActualCash()     { return actualCash;     }
    public BigDecimal    getShortageAmount() { return shortageAmount; }
    public BigDecimal    getOverageAmount()  { return overageAmount;  }
    public BigDecimal    getCashVariance()   { return cashVariance;   }
    public String        getNotes()          { return notes;          }
    public ShiftStatus   getStatus()         { return status;         }
    public boolean       isOpen()            { return status == ShiftStatus.OPEN; }

    @Override
    public String toString() {
        return "Shift{id='" + getId() + "', openedBy='" + openedBy
             + "', status=" + status + ", startingAmount=" + startingAmount
             + (status == ShiftStatus.CLOSED
                 ? ", shortage=" + shortageAmount + ", overage=" + overageAmount
                 : "")
             + "}";
    }
}
