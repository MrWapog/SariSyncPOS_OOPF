package sarisync.services;

import sarisync.enums.AuditEventType;
import sarisync.models.AuditEvent;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Records auditable events such as user changes, shift open/close,
 * transactions, and product changes.
 *
 * Used as the system-wide event log.
 * Mirrors the audit_logs table in the Supabase database.
 */
public class AuditService {

    private final List<AuditEvent> events = new ArrayList<>();
    private       String           currentShiftId;   // nullable — set by ShiftService

    /** Stub-compatible: accepts a free-text event type. */
    public AuditEvent log(String eventTypeRaw, Map<String,Object> data, String performedBy) {
        AuditEventType type = parseEventType(eventTypeRaw);
        return logTyped(type, data, performedBy);
    }

    public AuditEvent logTyped(AuditEventType type, Map<String,Object> data, String performedBy) {
        AuditEvent ev = new AuditEvent(type, data, performedBy, currentShiftId);
        events.add(ev);
        System.out.println("[Audit] " + ev);
        return ev;
    }

    /** Allows ShiftService to set the current shift_id so audit entries are tagged. */
    public void setCurrentShiftId(String shiftId) { this.currentShiftId = shiftId; }

    public List<AuditEvent> findAll()                       { return new ArrayList<>(events); }
    public List<AuditEvent> findByType(AuditEventType type) {
        return events.stream().filter(e -> e.getEventType() == type).collect(Collectors.toList());
    }
    public List<AuditEvent> findByUser(String username) {
        return events.stream().filter(e -> e.getPerformedBy().equals(username)).collect(Collectors.toList());
    }
    public int totalEvents() { return events.size(); }

    private static AuditEventType parseEventType(String raw) {
        for (AuditEventType t : AuditEventType.values())
            if (t.getDbValue().equals(raw)) return t;
        throw new IllegalArgumentException("Unknown audit event type: " + raw);
    }
}
