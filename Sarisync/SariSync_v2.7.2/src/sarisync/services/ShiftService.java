package sarisync.services;

import sarisync.enums.ShiftStatus;
import sarisync.models.Shift;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


public class ShiftService {

    private final Map<String, Shift> shifts = new HashMap<>();
    private final AuditService       auditService;

    public ShiftService()                          { this(null); }
    public ShiftService(AuditService auditService) { this.auditService = auditService; }

    /**
     * Returns the currently active (open) shift, if one exists.
     * Filters strictly on status=OPEN — closed shifts NEVER resume.
     */
    public Optional<Shift> getActiveShift() {
        return shifts.values().stream()
                .filter(Shift::isOpen)
                .findFirst();
    }

    /**
     * Opens a new shift. If an active shift already exists, returns it instead
     * (prevents duplicate open shifts — joining existing shift instead).
     */
    public Shift openShift(String openedBy, BigDecimal startingAmount) {
        Optional<Shift> existing = getActiveShift();
        if (existing.isPresent()) {
            // Multi-user join — return the existing open shift
            if (auditService != null)
                auditService.log("shift_resumed",
                        Map.of("shift_id", existing.get().getId(),
                               "opened_by", existing.get().getOpenedBy()),
                        openedBy);
            return existing.get();
        }
        Shift shift = new Shift(openedBy, startingAmount);
        shifts.put(shift.getId(), shift);
        if (auditService != null) {
            auditService.setCurrentShiftId(shift.getId());
            auditService.log("shift_opened",
                    Map.of("shift_id", shift.getId(),
                           "opening_cash", startingAmount.toPlainString(),
                           "opened_by", openedBy),
                    openedBy);
        }
        return shift;
    }

    /**
     * Closes the currently active shift with cash variance calculation.
     *
     * @param closedBy   username of the user performing the close
     * @param actualCash counted cash in the drawer
     * @param cashSales  total cash-method sales recorded during this shift
     * @param notes      optional notes
     * @return the now-closed Shift with shortage/overage populated
     * @throws IllegalStateException if no shift is currently open
     */
    public Shift closeShift(String closedBy, BigDecimal actualCash, BigDecimal cashSales, String notes) {
        Shift shift = getActiveShift()
                .orElseThrow(() -> new IllegalStateException("No active shift to close"));
        shift.closeShift(closedBy, actualCash, cashSales, notes);

        if (auditService != null) {
            Map<String,Object> data = new HashMap<>();
            data.put("shift_id",      shift.getId());
            data.put("opened_by",     shift.getOpenedBy());
            data.put("closed_by",     closedBy);
            data.put("opening_cash",  shift.getStartingAmount().toPlainString());
            data.put("expected_cash", shift.getExpectedCash().toPlainString());
            data.put("actual_cash",   shift.getActualCash().toPlainString());
            data.put("shortage",      shift.getShortageAmount().toPlainString());
            data.put("overage",       shift.getOverageAmount().toPlainString());
            data.put("variance",      shift.getCashVariance().toPlainString());
            auditService.log("shift_closed", data, closedBy);
            auditService.setCurrentShiftId(null);
        }
        return shift;
    }

    public List<Shift> findAll() {
        return shifts.values().stream()
                .sorted(Comparator.comparing(Shift::getStartedAt).reversed())
                .collect(Collectors.toList());
    }

    public List<Shift> findClosed() {
        return shifts.values().stream()
                .filter(s -> s.getStatus() == ShiftStatus.CLOSED)
                .sorted(Comparator.comparing(Shift::getClosedAt).reversed())
                .collect(Collectors.toList());
    }

    public Optional<Shift> findById(String shiftId) {
        return Optional.ofNullable(shifts.get(shiftId));
    }

    public int total()       { return shifts.size(); }
    public int totalClosed() { return (int) shifts.values().stream().filter(s -> s.getStatus() == ShiftStatus.CLOSED).count(); }
}
