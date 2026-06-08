package salon.logistics.domain.model.vehicle;

import salon.logistics.domain.event.VehicleReadyForHandoverEvent;
import salon.logistics.domain.event.VehicleReceivedInYardEvent;
import salon.logistics.domain.exceptions.InvalidVehicleStateException;
import salon.shared.event.AbstractAggregateRoot;
import salon.shared.model.OrderId;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Aggregate Root: fizyczny egzemplarz pojazdu — "cyfrowy bliźniak" auta na placu (UC-INW-01..06).
 *
 * Cykl: IN_TRANSIT -> ON_YARD -> (RESERVED) -> HANDED_OVER. PDI: PENDING -> APPROVED -> (EXPIRED).
 * Rezerwacja to mutex: lockForOrder zdejmuje auto z puli sprzedaży, releaseReservation je zwraca.
 *
 * ZALOZENIE (do potwierdzenia testem): konstruktor tworzy auto IN_TRANSIT, STOCK, PDI PENDING.
 */
public class InventoryVehicle extends AbstractAggregateRoot {

    private static final int PDI_VALIDITY_DAYS = 90; // UC-INW-05

    private final VinNumber vin;
    private VehicleRole role;
    private VehicleState state;
    private PdiStatus pdiStatus;
    private LocalDate pdiApprovalDate;
    private LocalDate yardEntryDate;
    private OrderId lockedForOrder; // null, dopóki nie zarezerwowano

    public InventoryVehicle(VinNumber vin) {
        if (vin == null) {
            throw new IllegalArgumentException("vin must not be null.");
        }
        this.vin = vin;
        this.role = VehicleRole.STOCK;
        this.state = VehicleState.IN_TRANSIT;
        this.pdiStatus = PdiStatus.PENDING;
        this.pdiApprovalDate = null;
        this.yardEntryDate = null;
        this.lockedForOrder = null;
    }

    // UC-INW-01: przyjęcie na plac po weryfikacji tożsamości przez ACL Importera.
    public void receiveOnYard(ImporterData data) {
        if (data == null) {
            throw new IllegalArgumentException("data must not be null.");
        }
        if (!this.vin.value().equals(data.vin())) {
            // Obcy VIN (UC-INW-01 A2) — odrzucamy przyjęcie.
            throw new InvalidVehicleStateException(
                    "VIN mismatch: vehicle " + this.vin.value() + " vs importer " + data.vin());
        }
        if (this.state != VehicleState.IN_TRANSIT) {
            throw new InvalidVehicleStateException(
                    "Only an IN_TRANSIT vehicle can be received on yard, was: " + this.state);
        }
        this.state = VehicleState.ON_YARD;
        this.yardEntryDate = LocalDate.now();
        registerEvent(new VehicleReceivedInYardEvent(UUID.randomUUID(), this.vin.value(), Instant.now()));
    }

    // UC-INW-02: zatwierdzenie przeglądu zerowego PDI -> gotowość do wydania.
    public void approvePdi() {
        if (this.state != VehicleState.ON_YARD && this.state != VehicleState.RESERVED) {
            throw new InvalidVehicleStateException(
                    "PDI can only be approved for a vehicle on yard/reserved, was: " + this.state);
        }
        this.pdiStatus = PdiStatus.APPROVED;
        this.pdiApprovalDate = LocalDate.now();
        registerEvent(new VehicleReadyForHandoverEvent(UUID.randomUUID(), this.vin.value(), Instant.now()));
    }

    // UC-INW-05: Cron unieważnia PDI po przekroczeniu limitu dni przestoju.
    public void expirePdiValidity() {
        if (this.pdiStatus != PdiStatus.APPROVED) {
            return; // tylko zatwierdzone PDI może wygasnąć — idempotentnie
        }
        if (this.pdiApprovalDate == null) {
            return;
        }
        long days = java.time.temporal.ChronoUnit.DAYS.between(this.pdiApprovalDate, LocalDate.now());
        if (days > PDI_VALIDITY_DAYS) {
            this.pdiStatus = PdiStatus.EXPIRED;
        }
    }

    // UC-INW-03: nadanie roli auta demonstracyjnego.
    public void markAsDemo(int currentMileage) {
        if (currentMileage < 0) {
            throw new IllegalArgumentException("currentMileage must not be negative.");
        }
        if (this.state == VehicleState.RESERVED || this.state == VehicleState.HANDED_OVER) {
            throw new InvalidVehicleStateException(
                    "A reserved/handed-over vehicle cannot become DEMO, was: " + this.state);
        }
        this.role = VehicleRole.DEMO;
    }

    // UC-INW-07: twarda blokada (mutex) pod konkretne zamówienie.
    public void lockForOrder(OrderId orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null.");
        }
        if (this.state != VehicleState.ON_YARD) {
            throw new InvalidVehicleStateException(
                    "Only an ON_YARD vehicle can be locked for an order, was: " + this.state);
        }
        this.lockedForOrder = orderId;
        this.state = VehicleState.RESERVED;
    }

    // UC-INW-06: zwolnienie rezerwacji po anulowaniu zamówienia.
    public void releaseReservation() {
        if (this.state != VehicleState.RESERVED) {
            throw new InvalidVehicleStateException(
                    "Only a RESERVED vehicle can be released, was: " + this.state);
        }
        this.lockedForOrder = null;
        this.state = VehicleState.ON_YARD;
    }

    // Pomocnicze: czy auto można przydzielić do zamówienia (Fast Track).
    public boolean isAvailableForOrder() {
        return this.role == VehicleRole.STOCK && this.state == VehicleState.ON_YARD;
    }

    public VinNumber getVin() {
        return this.vin;
    }

    public VehicleRole getRole() {
        return this.role;
    }

    public VehicleState getState() {
        return this.state;
    }

    public PdiStatus getPdiStatus() {
        return this.pdiStatus;
    }

    public LocalDate getPdiApprovalDate() {
        return this.pdiApprovalDate;
    }

    public LocalDate getYardEntryDate() {
        return this.yardEntryDate;
    }

    public OrderId getLockedForOrder() {
        return this.lockedForOrder;
    }
}
