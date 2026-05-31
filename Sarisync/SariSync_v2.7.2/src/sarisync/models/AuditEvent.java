package sarisync.models;

import sarisync.enums.AuditEventType;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Immutable record of an auditable system event.
 * Stored in AuditService for later inspection by admins.
 */
public class AuditEvent extends BaseEntity {

    private final AuditEventType    eventType;
    private final Map<String,Object> eventData;
    private final String            performedBy;
    private final String            shiftId;       // nullable — null if no active shift
    private final LocalDateTime     occurredAt;

    public AuditEvent(AuditEventType eventType, Map<String,Object> eventData,
                      String performedBy, String shiftId) {
        super();
        this.eventType   = eventType;
        this.eventData   = eventData == null ? new HashMap<>() : new HashMap<>(eventData);
        this.performedBy = performedBy == null ? "system" : performedBy;
        this.shiftId     = shiftId;
        this.occurredAt  = LocalDateTime.now();
        validate();
    }

    @Override
    protected void validate() {
        if (eventType == null) throw new IllegalArgumentException("eventType is required");
    }

    public AuditEventType    getEventType()   { return eventType;   }
    public Map<String,Object> getEventData()  { return new HashMap<>(eventData); }
    public String            getPerformedBy() { return performedBy; }
    public String            getShiftId()     { return shiftId;     }
    public LocalDateTime     getOccurredAt()  { return occurredAt;  }

    @Override
    public String toString() {
        return "[" + occurredAt + "] " + eventType.getDbValue()
             + " by " + performedBy + " — " + eventData;
    }
}
