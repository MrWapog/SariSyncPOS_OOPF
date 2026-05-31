package sarisync.enums;

/**
 * Lifecycle states for a Shift session.
 * Only ONE shift can be OPEN at a time across the entire system.
 */
public enum ShiftStatus {
    OPEN,
    CLOSED
}
