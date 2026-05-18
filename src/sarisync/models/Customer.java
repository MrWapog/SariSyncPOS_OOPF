package sarisync.models;

import sarisync.config.POSConfig;

import java.util.Objects;

/**
 * ENCAPSULATION + INHERITANCE
 *
 * Business Rules (from POSConfig):
 *  - 1 point earned per ₱20 spent
 *  - 100 points = ₱10 discount
 *  - 200 points = ₱50 free-item credit
 *(Will be based/Change for next 
 */
public class Customer extends BaseEntity {

    // ── Private Fields ────────────────────────────────────────────────────
    private String name;
    private int    loyaltyPoints;

    // ── Constructors ──────────────────────────────────────────────────────
    public Customer(String name) {
        this(name, 0);
    }

    public Customer(String name, int loyaltyPoints) {
        super();
        this.name          = name;
        this.loyaltyPoints = loyaltyPoints;
    }

    // ── Validation ────────────────────────────────────────────────────────
    @Override
    protected void validate() {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Customer name must not be blank");
        if (loyaltyPoints < 0)
            throw new IllegalArgumentException("Loyalty points must be >= 0");
    }

    // ── Getters ───────────────────────────────────────────────────────────
    public String getName()          { return name;          }
    public int    getLoyaltyPoints() { return loyaltyPoints; }

    // ── Controlled Mutators (Encapsulation) ───────────────────────────────
    public void setName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Customer name must not be blank");
        this.name = name;
        touch();
    }

    /**
     * Awards points earned from a completed transaction.
     * Points formula: Math.floor(totalPaid / POSConfig.PESO_PER_POINT)
     */
    public void earnPoints(double totalPaid) {
        if (totalPaid < 0) throw new IllegalArgumentException("totalPaid must be >= 0");
        int earned = (int) Math.floor(totalPaid * POSConfig.POINTS_PER_PESO);
        this.loyaltyPoints += earned;
        touch();
    }

    /**
     * Deducts redeemed points after a successful payment.
     * Throws if the customer does not have enough points.
     */
    public void redeemPoints(int pointsToRedeem) {
        if (pointsToRedeem < 0)
            throw new IllegalArgumentException("Points to redeem must be >= 0");
        if (pointsToRedeem > loyaltyPoints)
            throw new IllegalStateException(
                "Insufficient loyalty points: has=" + loyaltyPoints + ", requested=" + pointsToRedeem
            );
        this.loyaltyPoints -= pointsToRedeem;
        touch();
    }

    /**
     * Reverses a loyalty transaction (used when voiding a sale).
     * Subtracts earned points, restores redeemed points.
     */
    public void reversePointsTransaction(int pointsEarned, int pointsRedeemed) {
        this.loyaltyPoints = Math.max(0, loyaltyPoints - pointsEarned) + pointsRedeemed;
        touch();
    }

    /**
     * Returns the maximum points the customer can redeem,
     * rounded down to the nearest 100-point block.
     */
    public int getMaxRedeemablePoints() {
        return (loyaltyPoints / 100) * 100;
    }

    /**
     * Calculates the peso discount for a given number of points to redeem.
     * Every 100 points = POSConfig.PESOS_PER_100_POINTS.
     */
    public double calcDiscount(int pointsToRedeem) {
        if (pointsToRedeem <= 0) return 0;
        return Math.floor(pointsToRedeem / 100.0) * POSConfig.PESOS_PER_100_POINTS;
    }

    /** Returns how many more points until the next ₱10 discount reward. */
    public int getPointsToNextReward() {
        return 100 - (loyaltyPoints % 100);
    }

    @Override
    public String toString() {
        return "Customer{id='" + getId() + "', name='" + name
               + "', points=" + loyaltyPoints + "}";
    }
}
