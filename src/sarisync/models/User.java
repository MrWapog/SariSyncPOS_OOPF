package sarisync.models;

import sarisync.enums.UserRole;

import java.util.Objects;

/**
 * ENCAPSULATION + INHERITANCE
 *
 * User represents an authenticated staff member (cashier or admin).
 * Password is stored as a hash (never in plain text).
 * Role field controls which screens/operations are permitted.
 *
 * NOTE: In the original TypeScript code, credentials were stored in
 * plain text inside the React context — a security issue. This class
 * addresses that by keeping the raw password private and only exposing
 * a password-verification method.
 */
public class User extends BaseEntity {

    // ── Private Fields ────────────────────────────────────────────────────
    private final String   username;
    private       String   passwordHash;     // never stored or exposed as plain text
    private final UserRole role;
    private       boolean  active;

    // ── Constructor ───────────────────────────────────────────────────────
    public User(String username, String plainTextPassword, UserRole role) {
        super();
        this.username     = username;
        this.passwordHash = hash(plainTextPassword);
        this.role         = role;
        this.active       = true;
    }

    // ── Validation ────────────────────────────────────────────────────────
    @Override
    protected void validate() {
        Objects.requireNonNull(username, "Username must not be null");
        Objects.requireNonNull(role,     "UserRole must not be null");
        if (username.isBlank()) throw new IllegalArgumentException("Username must not be blank");
    }

    // ── Getters ───────────────────────────────────────────────────────────
    public String   getUsername() { return username; }
    public UserRole getRole()     { return role;     }
    public boolean  isActive()    { return active;   }

    /** Returns the display name (capitalised username). */
    public String getDisplayName() {
        if (username.isEmpty()) return "";
        return Character.toUpperCase(username.charAt(0)) + username.substring(1);
    }

    // ── Controlled Behaviour (Encapsulation) ──────────────────────────────

    /**
     * Verifies a plain-text password against the stored hash.
     * External code never touches the hash directly.
     */
    public boolean verifyPassword(String plainTextPassword) {
        return this.passwordHash.equals(hash(plainTextPassword));
    }

    /** Updates the password. Requires the current password for verification. */
    public void changePassword(String currentPassword, String newPassword) {
        if (!verifyPassword(currentPassword))
            throw new SecurityException("Current password is incorrect");
        if (newPassword == null || newPassword.length() < 6)
            throw new IllegalArgumentException("New password must be at least 6 characters");
        this.passwordHash = hash(newPassword);
        touch();
    }

    public void deactivate() { this.active = false; touch(); }
    public void activate()   { this.active = true;  touch(); }

    /** Returns true if this user has admin-level access. */
    public boolean isAdmin()   { return role == UserRole.ADMIN;   }
    public boolean isCashier() { return role == UserRole.CASHIER; }

    // ── Private Utilities ─────────────────────────────────────────────────

    /**
     * Simple hashing placeholder.
     * Production: replace with BCrypt.hashpw(plain, BCrypt.gensalt(12))
     */
    private static String hash(String plain) {
        return Integer.toHexString(Objects.hash(plain, "sarisync_salt_v1"));
    }

    @Override
    public String toString() {
        return "User{id='" + getId() + "', username='" + username
               + "', role=" + role + ", active=" + active + "}";
    }
}
