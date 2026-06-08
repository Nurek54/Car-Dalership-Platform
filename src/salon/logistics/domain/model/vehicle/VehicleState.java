package salon.logistics.domain.model.vehicle;

public enum VehicleState {
    IN_TRANSIT,       // w drodze z fabryki
    ON_YARD,          // na placu salonu
    RESERVED,         // zablokowany pod konkretne zamówienie
    HANDED_OVER,      // wydany klientowi
    TRANSPORT_DAMAGE  // szkoda logistyczna (UC-INW-01 A1)
}
