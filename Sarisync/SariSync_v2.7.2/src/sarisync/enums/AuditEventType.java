package sarisync.enums;

public enum AuditEventType {
    USER_CREATED            ("user_created"),
    USER_UPDATED            ("user_updated"),
    USER_ACTIVATED          ("user_activated"),
    USER_DEACTIVATED        ("user_deactivated"),
    PASSWORD_RESET          ("password_reset"),
    FIRST_LOGIN_PW_CHANGE   ("first_login_password_change"),
    SHIFT_OPENED            ("shift_opened"),
    SHIFT_RESUMED           ("shift_resumed"),
    SHIFT_CLOSED            ("shift_closed"),
    TRANSACTION_CREATED     ("transaction_created"),
    TRANSACTION_VOIDED      ("transaction_voided"),
    PRODUCT_CREATED         ("product_created"),
    PRODUCT_UPDATED         ("product_updated");

    private final String dbValue;
    AuditEventType(String dbValue) { this.dbValue = dbValue; }
    public String getDbValue() { return dbValue; }
}
