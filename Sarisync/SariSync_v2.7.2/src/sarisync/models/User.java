package sarisync.models;

import sarisync.enums.UserRole;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * ENCAPSULATION + INHERITANCE
 *
 * User represents an authenticated staff member (cashier or admin).
 * Password is stored as a hash (never in plain text).
 * Role field controls which screens/operations are permitted.
 *
 * v2.7.2 additions:
 *   - fullName               — Display name shown in User Management
 *   - isDefaultAdmin         — Marks the system-seeded default admin (cannot be deactivated)
 *   - mustChangePassword     — Forces password change on next login
 *   - firstLoginCompleted    — Tracks whether initial setup is done
 *   - lastLogin              — Timestamp of last successful login
 */
public class User extends BaseEntity {

    // ── Private Fields ────────────────────────────────────────────────────
    private       String        username;
    private       String        passwordHash;
    private       String        fullName;
    private       UserRole      role;
    private       boolean       active;
    private       boolean       defaultAdmin;
    private       boolean       mustChangePassword;
    private       boolean       firstLoginCompleted;
    private       LocalDateTime lastLogin;

    // ── Constructor — standard user ───────────────────────────────────────
    public User(String username, String plainTextPassword, UserRole role, String fullName) {
        super();
        this.username             = username;
        this.passwordHash         = hash(plainTextPassword);
        this.fullName             = (fullName == null || fullName.isBlank()) ? capitalise(username) : fullName;
        this.role                 = role;
        this.active               = true;
        this.defaultAdmin         = false;
        this.mustChangePassword   = false;
        this.firstLoginCompleted  = true;
        validate();
    }

    /** Back-compat constructor without fullName. */
    public User(String username, String plainTextPassword, UserRole role) {
        this(username, plainTextPassword, role, null);
    }

    // ── Validation ────────────────────────────────────────────────────────
    @Override
    protected void validate() {
        Objects.requireNonNull(username, "Username must not be null");
        Objects.requireNonNull(role,     "UserRole must not be null");
        if (username.isBlank()) throw new IllegalArgumentException("Username must not be blank");
    }

    // ── Getters ───────────────────────────────────────────────────────────
    public String        getUsername()             { return username;             }
    public String        getFullName()             { return fullName;             }
    public UserRole      getRole()                 { return role;                 }
    public boolean       isActive()                { return active;               }
    public boolean       isDefaultAdmin()          { return defaultAdmin;         }
    public boolean       mustChangePassword()      { return mustChangePassword;   }
    public boolean       isFirstLoginCompleted()   { return firstLoginCompleted;  }
    public LocalDateTime getLastLogin()            { return lastLogin;            }

    public String getDisplayName() { return fullName == null ? capitalise(username) : fullName; }

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
        validateNewPassword(newPassword);
        this.passwordHash = hash(newPassword);
        touch();
    }

    /**
     * Forces a password reset (admin only).
     * Used by User Management → Reset Password.
     * Clears the new password and flags the user to change it on next login.
     */
    public void resetPasswordTo(String newPassword) {
        validateNewPassword(newPassword);
        this.passwordHash         = hash(newPassword);
        this.mustChangePassword   = true;
        this.firstLoginCompleted  = false;
        touch();
    }

    /**
     * Called after a successful first-login password change.
     * Clears the must-change flag and the default-admin flag.
     */
    public void completeFirstLogin(String newUsername, String newPassword) {
        validateNewPassword(newPassword);
        if (newUsername != null && !newUsername.isBlank())
            this.username = newUsername.toLowerCase();
        this.passwordHash         = hash(newPassword);
        this.mustChangePassword   = false;
        this.firstLoginCompleted  = true;
        this.defaultAdmin         = false;  // No longer the default admin once password is changed
        touch();
    }

    /** Records the timestamp of the most recent successful login. */
    public void recordLogin() {
        this.lastLogin = LocalDateTime.now();
        touch();
    }

    public void setFullName(String fullName) {
        if (fullName == null || fullName.isBlank()) throw new IllegalArgumentException("Full name required");
        this.fullName = fullName;
        touch();
    }

    public void setRole(UserRole role) {
        if (defaultAdmin && role != UserRole.ADMIN)
            throw new IllegalStateException("Default admin role cannot be changed");
        Objects.requireNonNull(role);
        this.role = role;
        touch();
    }

    public void setUsername(String newUsername) {
        if (newUsername == null || newUsername.isBlank())
            throw new IllegalArgumentException("Username cannot be blank");
        this.username = newUsername.toLowerCase().trim();
        touch();
    }

    public void deactivate() {
        if (defaultAdmin)
            throw new IllegalStateException("Default admin cannot be deactivated");
        this.active = false;
        touch();
    }

    public void activate() { this.active = true; touch(); }

    /** Internal-use only by AuthService for seeding. Marks this as the default admin. */
    public void markAsDefaultAdmin() {
        this.defaultAdmin         = true;
        this.mustChangePassword   = true;
        this.firstLoginCompleted  = false;
    }

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

    private static void validateNewPassword(String pw) {
        if (pw == null || pw.length() < 6)
            throw new IllegalArgumentException("New password must be at least 6 characters");
    }

    private static String capitalise(String s) {
        if (s == null || s.isEmpty()) return "";
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    @Override
    public String toString() {
        return "User{id='" + getId() + "', username='" + username
               + "', fullName='" + fullName + "', role=" + role
               + ", active=" + active + ", defaultAdmin=" + defaultAdmin + "}";
    }
}
