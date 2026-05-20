package sarisync;

import sarisync.config.POSConfig;
import sarisync.repositories.InMemoryCustomerRepository;
import sarisync.repositories.InMemoryProductRepository;
import sarisync.repositories.InMemoryTransactionRepository;
import sarisync.services.*;

/**
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║         S A R I S Y N C  –  P O S  A P P L I C A T I O N          ║
 * ║                    Java OOP Refactored v2.5.0                        ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * CHANGE LOG v2.5.0:
 *  ✅ Removed all hardcoded demo products — products come from Admin Panel only
 *  ✅ Removed all demo transactions — history starts empty
 *  ✅ Stock deduction wired up in TransactionService.processPayment()
 *  ✅ Loyalty points updated to ₱100 = 1 point
 *  ✅ CustomerService added for full Admin CRUD + POS search
 *  ✅ Single shared repository instance across Admin and POS
 */
public class POSApplication {

    public static void main(String[] args) {

        System.out.println("=== " + POSConfig.APP_NAME + " v" + POSConfig.APP_VERSION + " starting... ===\n");

        // ── Repositories (shared single instances) ─────────────────────────
        InMemoryProductRepository     productRepo  = new InMemoryProductRepository();
        InMemoryTransactionRepository txnRepo      = new InMemoryTransactionRepository();
        InMemoryCustomerRepository    customerRepo = new InMemoryCustomerRepository();

        // ── Services ───────────────────────────────────────────────────────
        AuthService        authService     = new AuthService();
        ProductService     productService  = new ProductService(productRepo);
        CartService        cartService     = new CartService();
        TransactionService txnService      = new TransactionService(txnRepo, productRepo);
        CustomerService    customerService = new CustomerService(customerRepo);

        /*
         * NOTE: No products are seeded here.
         * All products must be added through the Admin Panel → Products section.
         * This ensures POS always reflects the current Admin-managed catalogue.
         *
         * Example of how to add a product via service (Admin Panel calls this):
         *   productService.addProduct("Nova (BBQ)", "Snacks", 25.0, 100, null, "Crispy chips");
         *
         * Likewise, no customers are seeded.
         * Register customers via Admin Panel → Customer Management.
         */

        System.out.println("System ready.");
        System.out.println("  Products : " + productService.findAll().size() + " (add via Admin Panel)");
        System.out.println("  Customers: " + customerService.findAll().size() + " (add via Admin Panel)");
        System.out.println("  Transactions: " + txnService.findAll().size() + " (none yet)");
        System.out.println("\nOpen SariSync_GUI.html in your browser to use the system.\n");
        System.out.println("=== " + POSConfig.APP_NAME + " ready ===");
    }
}
