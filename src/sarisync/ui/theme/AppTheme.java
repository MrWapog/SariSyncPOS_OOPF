package sarisync.ui.theme;

import java.awt.*;

/** Shared colours and fonts for the Swing UI. */
public final class AppTheme {

    public static final Color BG          = new Color(0xF5, 0xF6, 0xFA);
    public static final Color CARD_BG     = Color.WHITE;
    public static final Color BORDER      = new Color(0xE2, 0xE8, 0xF0);
    public static final Color TEXT        = new Color(0x1E, 0x29, 0x3B);
    public static final Color MUTED       = new Color(0x64, 0x74, 0x8B);
    public static final Color PRIMARY     = new Color(0x25, 0x63, 0xEB);
    public static final Color PRIMARY_HOVER = new Color(0x1D, 0x4E, 0xD8);
    public static final Color DANGER      = new Color(0xDC, 0x26, 0x26);
    public static final Color SLOT_BG     = new Color(0xF1, 0xF5, 0xF9);
    public static final Color OVERLAY     = new Color(0, 0, 0, 140);

    public static final Font TITLE   = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font HEADING = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font BODY    = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font EMOJI   = new Font("Segoe UI Emoji", Font.PLAIN, 36);

    private AppTheme() {}
}
