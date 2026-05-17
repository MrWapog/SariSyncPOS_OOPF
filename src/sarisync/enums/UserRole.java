package sarisync.enums;

/**
 * Defines the roles available in the SariSync POS system.
 * Used for role-based access control (RBAC).
 */
public enum UserRole {
    CASHIER("Cashier"),
    ADMIN("Admin");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
