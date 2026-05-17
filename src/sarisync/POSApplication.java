package sarisync;

import sarisync.bootstrap.CatalogSeeder;
import sarisync.config.POSConfig;
import sarisync.enums.PaymentMethod;
import sarisync.interfaces.IPaymentProcessor;
import sarisync.models.*;
import sarisync.repositories.InMemoryProductRepository;
import sarisync.repositories.InMemoryTransactionRepository;
import sarisync.services.*;
import sarisync.ui.SariSyncApp;

import java.util.List;

/**
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║            S A R I S Y N C  –  P O S  A P P L I C A T I O N        ║
 * ║                    Java OOP Refactored Version                       ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * This class wires together all layers and demonstrates the four OOP pillars:
 *
 *  ┌──────────────────────────────────────────────────────────────────┐
 *  │ ENCAPSULATION  — All model fields private; controlled via getters │
 *  │                  and validated mutators. Passwords hashed, never   │
 *  │                  stored as plain text.                             │
 *  │                                                                    │
 *  │ INHERITANCE    — Product, CartItem, Customer, User, Transaction    │
 *  │                  all extend BaseEntity (UUID, timestamps, validate)│
 *  │                  Concrete processors extend AbstractPaymentProcessor│
 *  │                  Repositories implement IRepository<T,ID>          │
 *  │                                                                    │
 *  │ ABSTRACTION    — IPaymentProcessor, IRepository define contracts.  │
 *  │                  BaseEntity.validate() forces subclass rules.      │
 *  │                  AbstractPaymentProcessor.doProcess() is abstract. │
 *  │                                                                    │
 *  │ POLYMORPHISM   — TransactionService holds IPaymentProcessor refs.  │
 *  │                  Calling process() dispatches to Cash/Card/GCash   │
 *  │                  without the service knowing the concrete type.    │
 *  └──────────────────────────────────────────────────────────────────┘
 */
public class POSApplication {

    public static void main(String[] args) {
        boolean consoleDemo = args.length > 0 && "--demo".equals(args[0]);

        // ── 1. Bootstrap repositories ──────────────────────────────────────
        InMemoryProductRepository     productRepo = new InMemoryProductRepository();
        InMemoryTransactionRepository txnRepo     = new InMemoryTransactionRepository();

        // ── 2. Bootstrap services ──────────────────────────────────────────
        AuthService        authService    = new AuthService();
        ProductService     productService = new ProductService(productRepo);
        CartService        cartService    = new CartService();
        TransactionService txnService     = new TransactionService(txnRepo);

        // ── 3. Seed product catalogue ──────────────────────────────────────
        CatalogSeeder.seed(productService);

        if (!consoleDemo) {
            SariSyncApp.launch(productService, cartService);
            return;
        }

        System.out.println("=== " + POSConfig.APP_NAME + " v" + POSConfig.APP_VERSION + " (console demo) ===\n");
        System.out.println("Catalogue loaded: " + productService.findAll().size() + " products");
        System.out.println("Low-stock items : " + productService.findLowStock().size());
        System.out.println();

        Product nova     = productService.findByCategory("Snacks").stream()
            .filter(p -> p.getName().startsWith("Nova")).findFirst().orElseThrow();
        Product coke     = productService.findByCategory("Beverages").stream()
            .filter(p -> p.getName().contains("Coca")).findFirst().orElseThrow();
        Product candy    = productService.findByCategory("Snacks").stream()
            .filter(p -> p.getName().contains("Candy")).findFirst().orElseThrow();

        // ── 4. Demo: Authentication ────────────────────────────────────────
        System.out.println("--- Authentication ---");
        authService.login("admin", "wrong").ifPresentOrElse(
            u -> System.out.println("Logged in as: " + u.getDisplayName()),
            ()  -> System.out.println("Login failed (expected — wrong password)")
        );

        User cashierUser = authService.login("sarah", "1234").orElseThrow();
        System.out.println("Logged in as: " + cashierUser.getDisplayName()
                           + " [" + cashierUser.getRole() + "]");
        System.out.println();

        // ── 5. Demo: Build a cart ──────────────────────────────────────────
        System.out.println("--- Cart Operations ---");
        Customer customer = new Customer("Juan Dela Cruz", 120);

        cartService.addProduct(nova);
        cartService.addProduct(coke);
        cartService.addProduct(nova);  // increments quantity → Nova qty = 2
        cartService.addProduct(candy);
        cartService.setQuantity(candy.getId(), 3);

        System.out.println("Cart items   : " + cartService.getItemCount());
        System.out.printf ("Subtotal     : ₱%.2f%n", cartService.getSubtotal());
        System.out.printf ("Tax (12%%)    : ₱%.2f%n", cartService.getTax());
        System.out.printf ("Total        : ₱%.2f%n", cartService.getTotal());
        System.out.println();

        // ── 6. Demo: POLYMORPHISM — process via different payment methods ──
        System.out.println("--- Payment (POLYMORPHISM demo) ---");

        for (PaymentMethod method : new PaymentMethod[]{PaymentMethod.CASH, PaymentMethod.GCASH, PaymentMethod.CARD}) {
            IPaymentProcessor processor = PaymentProcessorFactory.get(method);
            double due = cartService.getTotal();
            IPaymentProcessor.PaymentResult result = processor.process(due, due + 50);  // overpay by ₱50 for cash demo
            System.out.printf("[%-7s] success=%-5b  paid=₱%.2f  change=₱%.2f  msg=%s%n",
                processor.getDisplayName(), result.success(),
                result.amountPaid(), result.change(), result.message());
        }
        System.out.println();

        // ── 7. Demo: Full transaction flow ────────────────────────────────
        System.out.println("--- Full Transaction ---");
        int pointsToRedeem = 100;
        double discount    = customer.calcDiscount(pointsToRedeem);  // ₱10

        Transaction txn = txnService.processPayment(
            cartService.getItems(),
            PaymentMethod.CASH,
            cartService.getFinalTotal(discount) + 100,  // pay extra ₱100
            discount,
            cashierUser.getDisplayName(),
            customer,
            pointsToRedeem
        );

        System.out.println("Transaction  : " + txn.getTransactionNumber());
        System.out.printf ("Final Total  : ₱%.2f%n",  txn.getFinalTotal());
        System.out.printf ("Amount Paid  : ₱%.2f%n",  txn.getAmountPaid());
        System.out.printf ("Change       : ₱%.2f%n",  txn.getChange());
        System.out.println("Status       : " + txn.getStatus());
        System.out.println("Points earned: " + txn.getPointsEarned());
        System.out.println("Customer pts : " + customer.getLoyaltyPoints());
        System.out.println();

        // ── 8. Demo: Void/Undo ────────────────────────────────────────────
        System.out.println("--- Undo Transaction ---");
        Transaction voided = txnService.undoLastTransaction(customer);
        System.out.println("Voided: " + voided.getTransactionNumber() + " → " + voided.getStatus());
        System.out.println("Customer pts after undo: " + customer.getLoyaltyPoints());
        System.out.println();

        System.out.println("=== Demo complete ===");
    }
}
