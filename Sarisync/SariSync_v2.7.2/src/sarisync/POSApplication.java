package sarisync;

import sarisync.config.POSConfig;
import sarisync.enums.UserRole;
import sarisync.models.Shift;
import sarisync.models.User;
import sarisync.repositories.InMemoryCustomerRepository;
import sarisync.repositories.InMemoryProductRepository;
import sarisync.repositories.InMemoryTransactionRepository;
import sarisync.services.*;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * ╔══════════════════════════════════════════════════════════════════════╗
 * ║         S A R I S Y N C  –  P O S  A P P L I C A T I O N             ║
 * ║                    Java OOP Refactored v2.7.2                        ║
 * ╚══════════════════════════════════════════════════════════════════════╝
 *
 * CHANGE LOG v2.7.2:
 *  ✅ Removed Quick Sign In — no hardcoded demo logins
 *  ✅ User Management module — admin CRUD: create / edit / activate / deactivate / reset password
 *  ✅ Default admin auto-creation — system seeds admin/admin123 only if no users exist
 *  ✅ First-login password change required for default admin
 *  ✅ Shift Management — open/close with shortage/overage tracking
 *  ✅ Audit Service — logs user, shift, transaction and product events
 *  ✅ User model extended: fullName, isDefaultAdmin, mustChangePassword,
 *      firstLoginCompleted, lastLogin
 *
 *  Carried over from v2.5.0:
 *      Products and customers are added only via Admin Panel.
 *      Single shared repository instance across Admin and POS.
 */
public class POSApplication {

    public static void main(String[] args) {

        System.out.println("=== " + POSConfig.APP_NAME + " v" + POSConfig.APP_VERSION + " starting... ===\n");

        // ── Repositories (shared single instances) ─────────────────────────
        InMemoryProductRepository     productRepo  = new InMemoryProductRepository();
        InMemoryTransactionRepository txnRepo      = new InMemoryTransactionRepository();
        InMemoryCustomerRepository    customerRepo = new InMemoryCustomerRepository();

        // ── Services ───────────────────────────────────────────────────────
        AuditService       auditService    = new AuditService();
        AuthService        authService     = new AuthService(auditService);   // auto-creates default admin
        ShiftService       shiftService    = new ShiftService(auditService);
        ProductService     productService  = new ProductService(productRepo);
        CartService        cartService     = new CartService();
        TransactionService txnService      = new TransactionService(txnRepo, productRepo);
        CustomerService    customerService = new CustomerService(customerRepo);

        // ── Verify default admin is in place ───────────────────────────────
        printDefaultAdminStatus(authService);

        // ── Demonstrate v2.7.2 lifecycle ───────────────────────────────────
        demonstrateUserManagement(authService);
        demonstrateShiftLifecycle(authService, shiftService);

        // ── Final state ────────────────────────────────────────────────────
        System.out.println("\n─── System Ready ───");
        System.out.println("  Users      : " + authService.totalUsers()
                            + "  (active: " + authService.totalActive()
                            + ", admins: " + authService.totalAdmins() + ")");
        System.out.println("  Products   : " + productService.findAll().size() + " (add via Admin Panel)");
        System.out.println("  Customers  : " + customerService.findAll().size() + " (add via Admin Panel)");
        System.out.println("  Transactions: " + txnService.findAll().size());
        System.out.println("  Shifts     : " + shiftService.total()
                            + "  (open: " + (shiftService.getActiveShift().isPresent() ? 1 : 0)
                            + ", closed: " + shiftService.totalClosed() + ")");
        System.out.println("  Audit log  : " + auditService.totalEvents() + " events recorded");

        System.out.println("\nOpen SariSync_GUI_v2.7.2.html in your browser to use the system.");
        System.out.println("=== " + POSConfig.APP_NAME + " ready ===");
    }

    // ──────────────────────────────────────────────────────────────────────
    //  Helper demonstrations
    // ──────────────────────────────────────────────────────────────────────

    private static void printDefaultAdminStatus(AuthService authService) {
        Optional<User> defaultAdmin = authService.findByUsername("admin");
        if (defaultAdmin.isPresent()) {
            User a = defaultAdmin.get();
            System.out.println("\n[Bootstrap] Default admin status:");
            System.out.println("  username           : " + a.getUsername());
            System.out.println("  fullName           : " + a.getFullName());
            System.out.println("  role               : " + a.getRole());
            System.out.println("  isDefaultAdmin     : " + a.isDefaultAdmin());
            System.out.println("  mustChangePassword : " + a.mustChangePassword());
            System.out.println("  firstLoginCompleted: " + a.isFirstLoginCompleted());
        }
    }

    private static void demonstrateUserManagement(AuthService authService) {
        System.out.println("\n[Demo] User Management lifecycle:");

        // 1. Admin logs in for the first time
        Optional<User> session = authService.login("admin", "admin123");
        if (session.isEmpty()) { System.out.println("  ❌ Default admin login failed"); return; }
        User admin = session.get();
        System.out.println("  Admin logged in (mustChange=" + admin.mustChangePassword() + ")");

        // 2. First-login password change
        authService.completeFirstLogin(admin.getId(), "admin", "newAdminPw2026");
        System.out.println("  First-login flow completed. mustChange=" + admin.mustChangePassword());

        // 3. Admin creates a cashier
        User cashier = authService.createUser(
                "Maria Santos", "maria", "tempPass1", UserRole.CASHIER, true);
        System.out.println("  Created cashier: " + cashier.getUsername()
                            + " (mustChange=" + cashier.mustChangePassword() + ")");

        // 4. Admin resets the cashier's password
        String newPw = authService.resetPassword(cashier.getId(), null);  // null → auto-generated
        System.out.println("  Reset password for " + cashier.getUsername() + " → " + newPw);

        // 5. Admin deactivates and reactivates another user
        User temp = authService.createUser("Temp User", "tempuser", "temp1234", UserRole.CASHIER, false);
        authService.deactivateUser(temp.getId());
        System.out.println("  Deactivated: " + temp.getUsername() + "  active=" + temp.isActive());
        authService.activateUser(temp.getId());
        System.out.println("  Reactivated: " + temp.getUsername() + "  active=" + temp.isActive());

        authService.logout();
    }

    private static void demonstrateShiftLifecycle(AuthService authService, ShiftService shiftService) {
        System.out.println("\n[Demo] Shift Management lifecycle:");

        authService.login("admin", "newAdminPw2026");  // log back in

        // Open shift with ₱2000 starting cash
        Shift shift = shiftService.openShift("admin", new BigDecimal("2000.00"));
        System.out.println("  Opened shift: id=" + shift.getId().substring(0, 8) + "...");
        System.out.println("                openedBy=" + shift.getOpenedBy()
                            + ", startingAmount=₱" + shift.getStartingAmount());

        // Simulate cash sales of ₱1500
        BigDecimal cashSales = new BigDecimal("1500.00");
        // Actual drawer count: ₱3490 (₱10 shortage)
        BigDecimal actualCash = new BigDecimal("3490.00");

        Shift closed = shiftService.closeShift("admin", actualCash, cashSales, "End-of-day count");
        System.out.println("  Closed shift:");
        System.out.println("    expectedCash : ₱" + closed.getExpectedCash());
        System.out.println("    actualCash   : ₱" + closed.getActualCash());
        System.out.println("    shortage     : ₱" + closed.getShortageAmount());
        System.out.println("    overage      : ₱" + closed.getOverageAmount());
        System.out.println("    variance     : ₱" + closed.getCashVariance());

        authService.logout();
    }
}
