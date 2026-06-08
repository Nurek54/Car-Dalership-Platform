package salon.logistics.domain.model.vehicle;

public enum PdiStatus {
    PENDING,         // przegląd zerowy jeszcze nie wykonany
    APPROVED,        // PDI zatwierdzone — gotowe do wydania
    EXPIRED,         // ważność PDI wygasła (UC-INW-05)
    PRE_SALE_DEFECT  // usterka przedsprzedażowa (UC-INW-02 A1)
}
