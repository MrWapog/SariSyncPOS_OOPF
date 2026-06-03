package sarisync.services;

import sarisync.enums.UserRole;
import sarisync.models.User;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ENCAPSULATION
 */
public class AuthService {

    // ── Private State (Encapsulation) ─────────────────────────────────────
    private final Map<String, User> userStore = new HashMap<>();
    private       User              currentSession;
    private final AuditService      auditService;

    // ── Constructors ──────────────────────────────────────────────────────
    public AuthService() {
        this(null);
    }

    public AuthService(AuditService auditService) {
        this.auditService = auditService;
        ensureDefaultAdmin();
    }

    /**
     * SAFETY NET — ensures the system can never be locked out of administration.
     * On startup (and on every login attempt), if no users exist, creates a default
     * admin account: username=admin, password=admin123.
     *
     * The default admin is flagged with:
     *   - isDefaultAdmin = true
     *   - mustChangePassword = true   (forces password change on first login)
     *   - firstLoginCompleted = false (triggers the first-login modal in the UI)
     *
     * This method is idempotent: if any user already exists, it does nothing.
     */
    public void ensureDefaultAdmin() {
        if (!userStore.isEmpty()) return;  // Skip if any user exists
        User defaultAdmin = new User("admin", "admin123", UserRole.ADMIN, "System Administrator");
        defaultAdmin.markAsDefaultAdmin();
        userStore.put("admin", defaultAdmin);
        System.out.println("[SariSync] Default admin created: username=admin password=admin123 (must change on first login)");
        log("user_created", defaultAdmin, "system");
    }

    // ── Authentication ────────────────────────────────────────────────────

    public Optional<User> login(String username, String plainTextPassword) {
        // Safety net — recreate default admin if all users got somehow deleted
        ensureDefaultAdmin();

        User user = userStore.get(username.toLowerCase().trim());
        if (user == null || !user.isActive() || !user.verifyPassword(plainTextPassword)) {
            return Optional.empty();
        }
        user.recordLogin();
        this.currentSession = user;
        return Optional.of(user);
    }

    public void completeFirstLogin(String userId, String newUsername, String newPassword) {
        User user = findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        // If the new username differs and is already taken → reject
        if (newUsername != null && !newUsername.isBlank()
                && !newUsername.equalsIgnoreCase(user.getUsername())) {
            String newKey = newUsername.toLowerCase().trim();
            if (userStore.containsKey(newKey))
                throw new IllegalArgumentException("Username already taken: " + newUsername);

            // Update the key in the map
            userStore.remove(user.getUsername().toLowerCase());
            user.completeFirstLogin(newUsername, newPassword);
            userStore.put(newKey, user);
        } else {
            user.completeFirstLogin(null, newPassword);
        }
        log("first_login_password_change", user, user.getUsername());
    }

    public void logout() { this.currentSession = null; }

    public Optional<User> getCurrentUser()  { return Optional.ofNullable(currentSession); }
    public boolean        isLoggedIn()      { return currentSession != null;             }
    public boolean        isAdmin()         { return isLoggedIn() && currentSession.isAdmin(); }

    // ── User Management (Admin operations) ────────────────────────────────

    /**
     * @throws IllegalArgumentException if the username is already taken.
     */
    public User createUser(String fullName, String username, String plainTextPassword,
                           UserRole role, boolean mustChange) {
        String key = username.toLowerCase().trim();
        if (userStore.containsKey(key))
            throw new IllegalArgumentException("Username already exists: " + username);
        if (plainTextPassword == null || plainTextPassword.length() < 6)
            throw new IllegalArgumentException("Password must be at least 6 characters");

        User user = new User(username, plainTextPassword, role, fullName);
        if (mustChange) user.resetPasswordTo(plainTextPassword);
        userStore.put(key, user);
        log("user_created", user, currentSessionUsername());
        return user;
    }

    /** Updates a user's full name, username, and role. */
    public User updateUser(String userId, String fullName, String newUsername, UserRole role) {
        User user = findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        if (fullName != null && !fullName.isBlank())
            user.setFullName(fullName);

        if (newUsername != null && !newUsername.isBlank()
                && !newUsername.equalsIgnoreCase(user.getUsername())) {
            String newKey = newUsername.toLowerCase().trim();
            if (userStore.containsKey(newKey))
                throw new IllegalArgumentException("Username already taken: " + newUsername);
            userStore.remove(user.getUsername().toLowerCase());
            user.setUsername(newUsername);
            userStore.put(newKey, user);
        }

        if (role != null) user.setRole(role);
        log("user_updated", user, currentSessionUsername());
        return user;
    }

    /** Resets the password for any user. They will be required to change it on next login. */
    public String resetPassword(String userId, String newPassword) {
        User user = findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        if (newPassword == null || newPassword.isBlank()) newPassword = generateRandomPassword();
        user.resetPasswordTo(newPassword);
        log("password_reset", user, currentSessionUsername());
        return newPassword;
    }

    public void deactivateUser(String userId) {
        User user = findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.deactivate();
        log("user_deactivated", user, currentSessionUsername());
    }

    public void activateUser(String userId) {
        User user = findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.activate();
        log("user_activated", user, currentSessionUsername());
    }

    // ── Read-only queries ────────────────────────────────────────────────
    public List<User> findAll() {
        return userStore.values().stream()
                .sorted(Comparator.comparing(User::getCreatedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<User> findActive()   { return userStore.values().stream().filter(User::isActive).collect(Collectors.toList()); }
    public List<User> findInactive() { return userStore.values().stream().filter(u -> !u.isActive()).collect(Collectors.toList()); }

    public Optional<User> findByUsername(String username) {
        return Optional.ofNullable(userStore.get(username.toLowerCase().trim()));
    }

    public Optional<User> findById(String userId) {
        return userStore.values().stream().filter(u -> u.getId().equals(userId)).findFirst();
    }

    public int totalUsers()  { return userStore.size(); }
    public int totalActive() { return (int) userStore.values().stream().filter(User::isActive).count(); }
    public int totalAdmins() { return (int) userStore.values().stream().filter(u -> u.isAdmin() && u.isActive()).count(); }

    // ── Utilities ────────────────────────────────────────────────────────
    public static String generateRandomPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz23456789";
        SecureRandom rng = new SecureRandom();
        StringBuilder pw = new StringBuilder(10);
        for (int i = 0; i < 10; i++) pw.append(chars.charAt(rng.nextInt(chars.length())));
        return pw.toString();
    }

    private String currentSessionUsername() {
        return currentSession == null ? "system" : currentSession.getUsername();
    }

    private void log(String eventType, User affected, String performedBy) {
        if (auditService == null) return;
        Map<String, Object> data = new HashMap<>();
        data.put("user_id",     affected.getId());
        data.put("username",    affected.getUsername());
        data.put("role",        affected.getRole().name());
        data.put("active",      affected.isActive());
        auditService.log(eventType, data, performedBy);
    }
}
