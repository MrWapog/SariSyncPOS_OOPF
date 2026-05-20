package sarisync.models;

import sarisync.config.POSConfig;

import java.time.LocalDate;
import java.util.Objects;

/**
 * ENCAPSULATION + INHERITANCE
 *
 * Business Rules (from POSConfig):
 *  - ₱100 spent = 1 loyalty point
 *  - 100 points = ₱10 discount
 */
public class Customer extends BaseEntity {

    private String    name;
    private String    contactNumber;
    private int       loyaltyPoints;
    private final LocalDate dateRegistered;

    // ── Constructors 
    public Customer(String name, String contactNumber) {
        super();
        this.name           = name;
        this.contactNumber  = contactNumber;
        this.loyaltyPoints  = 0;
        this.dateRegistered = LocalDate.now();
    }

    public Customer(String name, String contactNumber, int loyaltyPoints) {
        super();
        this.name           = name;
        this.contactNumber  = contactNumber;
        this.loyaltyPoints  = loyaltyPoints;
        this.dateRegistered = LocalDate.now();
    }

    @Override
    protected void validate() {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Customer name must not be blank");
        if (loyaltyPoints < 0)
            throw new IllegalArgumentException("Loyalty points must be >= 0");
    }

    // ── Getters 
    public String    getName()           { return name;           }
    public String    getContactNumber()  { return contactNumber;  }
    public int       getLoyaltyPoints()  { return loyaltyPoints;  }
    public LocalDate getDateRegistered() { return dateRegistered; }

    // ── Mutators 
    public void setName(String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Customer name must not be blank");
        this.name = name; touch();
    }

    public void setContactNumber(String contactNumber) {
        Objects.requireNonNull(contactNumber, "Contact number must not be null");
        this.contactNumber = contactNumber; touch();
    }

    // ── Loyalty Logic 
    /** Rule: ₱100 spent = 1 point */
    public void earnPoints(double totalPaid) {
        if (totalPaid < 0) throw new IllegalArgumentException("totalPaid must be >= 0");
        this.loyaltyPoints += (int) Math.floor(totalPaid * POSConfig.POINTS_PER_PESO);
        touch();
    }

    public void redeemPoints(int pointsToRedeem) {
        if (pointsToRedeem < 0) throw new IllegalArgumentException("Points must be >= 0");
        if (pointsToRedeem > loyaltyPoints)
            throw new IllegalStateException("Insufficient points: has=" + loyaltyPoints);
        this.loyaltyPoints -= pointsToRedeem; touch();
    }

    public void reversePointsTransaction(int pointsEarned, int pointsRedeemed) {
        this.loyaltyPoints = Math.max(0, loyaltyPoints - pointsEarned) + pointsRedeemed;
        touch();
    }

    public int    getMaxRedeemablePoints() { return (loyaltyPoints / 100) * 100; }
    public double calcDiscount(int pts)    { return pts <= 0 ? 0 : Math.floor(pts / 100.0) * POSConfig.PESOS_PER_100_POINTS; }
    public int    getPointsToNextReward()  { return 100 - (loyaltyPoints % 100); }

    @Override
    public String toString() {
        return "Customer{id='" + getId() + "', name='" + name + "', contact='" + contactNumber + "', points=" + loyaltyPoints + "}";
    }
}
