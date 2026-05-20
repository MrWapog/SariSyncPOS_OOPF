package sarisync.services;

import sarisync.config.POSConfig;
import sarisync.enums.PaymentMethod;
import sarisync.enums.TransactionStatus;
import sarisync.interfaces.IPaymentProcessor;
import sarisync.models.CartItem;
import sarisync.models.Customer;
import sarisync.models.Transaction;
import sarisync.repositories.InMemoryProductRepository;
import sarisync.repositories.InMemoryTransactionRepository;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ENCAPSULATION + POLYMORPHISM
 *
 */
public class TransactionService {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final InMemoryTransactionRepository transactionRepository;
    private final InMemoryProductRepository     productRepository;
    private int sequenceCounter;

    public TransactionService(InMemoryTransactionRepository transactionRepository,
                              InMemoryProductRepository     productRepository) {
        this.transactionRepository = transactionRepository;
        this.productRepository     = productRepository;
        this.sequenceCounter       = transactionRepository.count();
    }

    // ── Core Payment Flow 

    /**
     * Processes a full payment transaction.
     * Validates stock → charges payment → deducts stock → awards loyalty → persists.
     */
    public Transaction processPayment(List<CartItem> cartItems,
                                      PaymentMethod  paymentMethod,
                                      double         amountPaid,
                                      double         loyaltyDiscount,
                                      String         cashierName,
                                      Customer       customer,
                                      int            pointsRedeemed) {

        if (cartItems == null || cartItems.isEmpty())
            throw new IllegalStateException("Cannot process an empty cart.");

        // 1. Totals
        double subtotal   = cartItems.stream().mapToDouble(CartItem::getLineSubtotal).sum();
        double tax        = subtotal * POSConfig.VAT_RATE;
        double total      = subtotal + tax;
        double finalTotal = Math.max(0, total - loyaltyDiscount);

        // 2. Stock validation — fail fast
        for (CartItem item : cartItems) {
            if (item.getProduct().getStock() < item.getQuantity())
                throw new IllegalStateException(
                    "Insufficient stock for '" + item.getProduct().getName()
                    + "': needed=" + item.getQuantity()
                    + ", available=" + item.getProduct().getStock()
                );
        }

        // 3. Payment processor (POLYMORPHISM)
        IPaymentProcessor processor = PaymentProcessorFactory.get(paymentMethod);
        IPaymentProcessor.PaymentResult result = processor.process(finalTotal, amountPaid);
        if (!result.success())
            throw new IllegalStateException("Payment failed: " + result.message());

        // 4. ★ Deduct stock
        for (CartItem item : cartItems) {
            item.getProduct().deductStock(item.getQuantity());
            productRepository.update(item.getProduct());
        }

        // 5. Loyalty points
        int pointsEarned = (int) Math.floor(finalTotal * POSConfig.POINTS_PER_PESO);
        customer.redeemPoints(pointsRedeemed);
        customer.earnPoints(finalTotal);

        // 6. Persist transaction
        Transaction txn = new Transaction(
            generateTransactionNumber(), cartItems,
            subtotal, tax, total,
            loyaltyDiscount, finalTotal,
            result.amountPaid(), result.change(),
            paymentMethod, cashierName, customer.getName(),
            pointsRedeemed, pointsEarned
        );
        transactionRepository.save(txn);
        return txn;
    }

    // ── Void Transaction 

    /**
     * @param transactionNumber the TXN-XXXXXXXX-NNN identifier
     * @param reason            cashier/admin reason (may be blank)
     * @param customer          customer to reverse points on (may be null for walk-in)
     * @return the updated voided transaction
     */
    public Transaction voidTransaction(String transactionNumber, String reason, Customer customer) {
        Transaction txn = transactionRepository.findByNumber(transactionNumber)
            .orElseThrow(() -> new IllegalArgumentException(
                "Transaction not found: " + transactionNumber
            ));

        // Records status, voidReason, voidedAt
        txn.void_(reason);

        // ★ Restore stock for every purchased item
        for (CartItem item : txn.getItems()) {
            item.getProduct().restoreStock(item.getQuantity());
            productRepository.update(item.getProduct());
        }

        // Reverse loyalty points if customer is provided
        if (customer != null) {
            customer.reversePointsTransaction(txn.getPointsEarned(), txn.getPointsRedeemed());
        }

        transactionRepository.update(txn);
        return txn;
    }

    /**
     * Convenience overload — voids the MOST RECENT completed transaction.
     * Used by the "Undo Last Transaction" button.
     */
    public Transaction voidLastTransaction(String reason, Customer customer) {
        Transaction last = transactionRepository.findLastCompleted()
            .orElseThrow(() -> new IllegalStateException("No completed transaction to void."));
        return voidTransaction(last.getTransactionNumber(), reason, customer);
    }

    // ── Queries 

    public List<Transaction> findAll() { return transactionRepository.findAll(); }

    /**
     * Returns only COMPLETED transactions.
     * Used for all revenue/analytics calculations.
     */
    public List<Transaction> findCompleted() {
        return transactionRepository.findAll().stream()
            .filter(Transaction::isCompleted)
            .collect(Collectors.toList());
    }

    /**
     * Returns COMPLETED transactions from TODAY only.
     * Powers the "Sales Today" dashboard card.
     */
    public List<Transaction> findTodayCompleted() {
        return transactionRepository.findAll().stream()
            .filter(t -> t.isCompleted() && t.isToday())
            .collect(Collectors.toList());
    }

    /** Total revenue from COMPLETED transactions only. */
    public double getTotalRevenue() {
        return findCompleted().stream().mapToDouble(Transaction::getFinalTotal).sum();
    }

    /** Total revenue from TODAY's COMPLETED transactions only. */
    public double getTodayRevenue() {
        return findTodayCompleted().stream().mapToDouble(Transaction::getFinalTotal).sum();
    }

    public Optional<Transaction> findLastCompleted() {
        return transactionRepository.findLastCompleted();
    }

    // ── Utility 
    private String generateTransactionNumber() {
        return "TXN-" + LocalDate.now().format(DATE_FMT)
               + "-" + String.format("%04d", ++sequenceCounter);
    }
}
