package sarisync.config;

/**
 * ENCAPSULATION: Centralises all POS business constants.
 *
 */
public final class POSConfig {

    // ── Tax ───────────────────────────────────────────────────────────────
    public static final double VAT_RATE = 0.12;

    // ── Loyalty Programme ─────────────────────────────────────────────────
    /** ₱100 spent = 1 point → 0.01 pts per peso */
    public static final double POINTS_PER_PESO      = 1.0 / 100.0;

    /** Every 100 points = ₱10 discount */
    public static final double PESOS_PER_100_POINTS = 10.0;

    /** 200 points = ₱50 free-item credit */
    public static final int    FREE_ITEM_THRESHOLD_POINTS = 200;
    public static final double FREE_ITEM_VALUE_PESOS      = 50.0;

    // ── Inventory ─────────────────────────────────────────────────────────
    /** Products at or below this stock level are flagged as low stock */
    public static final int LOW_STOCK_THRESHOLD = 20;

    // ── App Metadata ──────────────────────────────────────────────────────
    public static final String APP_NAME    = "SariSync";
    public static final String APP_VERSION = "2.5.0";

    private POSConfig() { throw new UnsupportedOperationException("Utility class"); }
}
