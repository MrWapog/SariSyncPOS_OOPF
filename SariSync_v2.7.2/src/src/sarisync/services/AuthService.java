package sarisync.services;

import sarisync.enums.UserRole;
import sarisync.models.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * ENCAPSULATION
 *
 * AuthService owns all authentication logic.
 * The original TypeScript code kept a hardcoded users map with PLAIN-TEXT
 * passwords inside the React context — a critical security issue.
 *
 * This service:
 *   - Stores User objects (which hash passwords internally — see User.java)
 *   - Exposes only a login() method that returns an Optional<User>
 *   - Tracks the currently logged-in session separately from the user registry
 *   - Makes it easy to swap to a real user database (replace the in-memory map)
 */
public class AuthService {

    // ── Private State (Encapsulation) ─────────────────────────────────────
    private final Map<String, User> userStore = new HashMap<>();
    private       User              currentSession;

    // ── Constructor — seed default users ──────────────────────────────────
    public AuthService() {
        seedDefaultUsers();
    }

    private void seedDefaultUsers() {
        register("admin",   "admin123", UserRole.ADMIN);
        register("cashier", "cash123",  UserRole.CASHIER);
        register("sarah",   "1234",     UserRole.CASHIER);
    }

    // ── Public API ────────────────────────────────────────────────────────

    /**
     * Registers a new user in the system.
     * Password is hashed inside User's constructor — plain text is never stored.
     *
     * @throws IllegalArgumentException if the username is already taken.
     */
    public User register(String username, String plainTextPassword, UserRole role) {
        String key = username.toLowerCase();
        if (userStore.containsKey(key))
            throw new IllegalArgumentException("Username already exists: " + username);
        User user = new User(username, plainTextPassword, role);
        userStore.put(key, user);
        return user;
    }

    /**
     * Attempts to log in. Returns the authenticated User if successful, or empty.
     * On success, sets the current session.
     */
    public Optional<User> login(String username, String plainTextPassword) {
        User user = userStore.get(username.toLowerCase());
        if (user != null && user.isActive() && user.verifyPassword(plainTextPassword)) {
            this.currentSession = user;
            return Optional.of(user);
        }
        return Optional.empty();
    }

    /** Clears the current session. */
    public void logout() {
        this.currentSession = null;
    }

    /** Returns the currently logged-in user, or empty if no session. */
    public Optional<User> getCurrentUser() {
        return Optional.ofNullable(currentSession);
    }

    /** Returns true if there is an active session. */
    public boolean isLoggedIn() {
        return currentSession != null;
    }

    /** Returns true if the current session user has admin privileges. */
    public boolean isAdmin() {
        return isLoggedIn() && currentSession.isAdmin();
    }
}
