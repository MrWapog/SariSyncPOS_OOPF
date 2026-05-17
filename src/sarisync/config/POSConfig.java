package sarisync.config;

/**
 * ENCAPSULATION: Centralises all POS business constants.
 *
 * Priority fix from the original codebase:
 *   In the TypeScript version, magic numbers (0.12, 100, 10, 50, 200)
 *   were scattered across store.tsx, POSMainScreen, and PaymentScreen.
 *   This class eliminates all magic numbers and serves as the single
 *   source of truth for every business rule.
 */
public final class POSConfig {

    // ── Tax ───────────────────────────────────────────────────────────────
    /** Philippine VAT rate applied to all sales. */
    public static final double VAT_RATE = 0.12;

    // ── Loyalty Programme ─────────────────────────────────────────────────
    /** Points earned per peso spent (1 point per ₱20 spent → 0.05 pts/₱). */
    public static final double POINTS_PER_PESO  = 1.0 / 20.0;

    /** Pesos of discount for every 100 loyalty points redeemed. */
    public static final double PESOS_PER_100_POINTS = 10.0;

    /** Points threshold required to unlock a free-item credit. */
    public static final int    FREE_ITEM_THRESHOLD_POINTS = 200;

    /** Peso value of the free-item credit awarded at FREE_ITEM_THRESHOLD_POINTS. */
    public static final double FREE_ITEM_VALUE_PESOS = 50.0;

    // ── Inventory ─────────────────────────────────────────────────────────
    /** Products at or below this stock level are flagged as "low stock". */
    public static final int LOW_STOCK_THRESHOLD = 20;

    // ── Product Images ──────────────────────────────────────────────────────
    /** Maximum upload size in bytes (5 MB). */
    public static final long MAX_IMAGE_BYTES = 5L * 1024 * 1024;

    /** Allowed image extensions (lowercase, without dot). */
    public static final String[] ALLOWED_IMAGE_EXTENSIONS = {"jpg", "jpeg", "png", "webp"};

    /** Default emoji when no custom fallback icon is chosen. */
    public static final String DEFAULT_FALLBACK_ICON = "📦";

    /** Emoji options for the fallback-icon dropdown in Add/Edit Product. */
    public static final String[] FALLBACK_ICONS = {
        "📦", "🍿", "🥤", "🍜", "🍬", "🧴", "🐟", "💧", "🍪",
        "🍚", "☕", "🧃", "🍫", "🧼", "🥫", "🍞", "🥚", "🧻"
    };

    // ── Application Metadata ──────────────────────────────────────────────
    public static final String APP_NAME    = "SariSync";
    public static final String APP_VERSION = "2.4.1";

    /** Utility class — prevent instantiation. */
    private POSConfig() {
        throw new UnsupportedOperationException("POSConfig is a utility class");
    }
}
