package sarisync.services;

import sarisync.config.POSConfig;
import sarisync.enums.PaymentMethod;
import sarisync.interfaces.IPaymentProcessor;
import sarisync.models.CartItem;
import sarisync.models.Customer;
import sarisync.models.Transaction;
import sarisync.repositories.InMemoryTransactionRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ENCAPSULATION + POLYMORPHISM
 *
 * TransactionService orchestrates the payment flow.
 * It uses POLYMORPHISM through IPaymentProcessor — it never calls
 * CashPaymentProcessor or GCashPaymentProcessor by name; it only
 * calls process() on the interface reference returned by the factory.
 *
 * Responsibilities:
 *   - Building and persisting Transaction records
 *   - Delegating payment to the appropriate IPaymentProcessor
 *   - Voiding the last transaction (undo)
 *   - Generating transaction numbers
 *   - Reporting queries
 */
public class TransactionService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    // ── Dependencies (injected for testability) ───────────────────────────
    private final InMemoryTransactionRepository repository;
    private       int                           sequenceCounter;

    // ── Constructor ───────────────────────────────────────────────────────
    public TransactionService(InMemoryTransactionRepository repository) {
        this.repository      = repository;
        this.sequenceCounter = repository.count();
    }

    // ── Core Operations ───────────────────────────────────────────────────

    /**
     * POLYMORPHISM in action:
     * Accepts any PaymentMethod, retrieves the correct IPaymentProcessor
     * from the factory, processes the payment, and records the transaction.
     *
     * @param cartItems        Snapshot of the current cart
     * @param paymentMethod    Selected payment method
     * @param amountPaid       Cash tendered (ignored for digital payments)
     * @param loyaltyDiscount  Peso value of loyalty points redeemed
     * @param cashierName      Logged-in cashier's display name
     * @param customer         Customer for loyalty point updates
     * @param pointsRedeemed   Points the customer chose to redeem
     * @return Persisted Transaction
     */
    public Transaction processPayment(List<CartItem> cartItems,
                                      PaymentMethod  paymentMethod,
                                      double         amountPaid,
                                      double         loyaltyDiscount,
                                      String         cashierName,
                                      Customer       customer,
                                      int            pointsRedeemed) {

        if (cartItems.isEmpty()) throw new IllegalStateException("Cannot process an empty cart");

        // ── 1. Calculate totals ──────────────────────────────────────────
        double subtotal   = cartItems.stream().mapToDouble(CartItem::getLineSubtotal).sum();
        double tax        = subtotal * POSConfig.VAT_RATE;
        double total      = subtotal + tax;
        double finalTotal = Math.max(0, total - loyaltyDiscount);

        // ── 2. Run payment processor (POLYMORPHISM) ──────────────────────
        IPaymentProcessor processor = PaymentProcessorFactory.get(paymentMethod);
        IPaymentProcessor.PaymentResult result = processor.process(finalTotal, amountPaid);

        if (!result.success()) {
            throw new IllegalStateException("Payment failed: " + result.message());
        }

        // ── 3. Calculate loyalty points earned ───────────────────────────
        int pointsEarned = (int) Math.floor(finalTotal * POSConfig.POINTS_PER_PESO);

        // ── 4. Build and persist transaction ─────────────────────────────
        String txnNumber = generateTransactionNumber();
        Transaction txn  = new Transaction(
            txnNumber, cartItems,
            subtotal, tax, total,
            loyaltyDiscount, finalTotal,
            result.amountPaid(), result.change(),
            paymentMethod,
            cashierName, customer.getName(),
            pointsRedeemed, pointsEarned
        );

        repository.save(txn);

        // ── 5. Update customer loyalty ────────────────────────────────────
        customer.redeemPoints(pointsRedeemed);
        customer.earnPoints(finalTotal);

        return txn;
    }

    /**
     * Voids the most recent COMPLETED transaction.
     * Reverses loyalty points on the customer's account.
     *
     * @throws IllegalStateException if there is no completed transaction to void.
     */
    public Transaction undoLastTransaction(Customer customer) {
        Transaction last = repository.findLastCompleted().orElseThrow(() ->
            new IllegalStateException("No completed transaction to undo")
        );
        last.void_();
        repository.update(last);
        customer.reversePointsTransaction(last.getPointsEarned(), last.getPointsRedeemed());
        return last;
    }

    // ── Queries ───────────────────────────────────────────────────────────

    public List<Transaction> findAll()            { return repository.findAll();              }
    public Optional<Transaction> findLast()        { return repository.findLastCompleted();    }

    public List<Transaction> findCompleted() {
        return repository.findAll().stream()
            .filter(Transaction::isCompleted)
            .collect(Collectors.toList());
    }

    public double getTotalRevenue() {
        return findCompleted().stream().mapToDouble(Transaction::getFinalTotal).sum();
    }

    public long getTotalTransactionCount() {
        return findCompleted().size();
    }

    // ── Private Utilities ─────────────────────────────────────────────────

    private String generateTransactionNumber() {
        return "TXN-" + LocalDate.now().format(DATE_FMT)
               + "-" + String.format("%03d", ++sequenceCounter);
    }
}
