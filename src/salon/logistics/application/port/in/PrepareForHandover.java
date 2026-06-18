package salon.logistics.application.port.in;

/**
 * Port wejściowy UC-INW-05: przygotowanie pojazdu do wydania po rozliczeniu —
 * węzeł "PrepareForHandover" w docs/Inwentarz-Logistyka/LogisticsArchitecture.md.
 *
 * Wyzwalany zdarzeniem SettlementCompleted (saldo = 0). Pojazd (RESERVED) zostaje
 * oznaczony jako "Gotowy do wydania", kontekst emituje VehicleReadyForHandoverEvent.
 */
public interface PrepareForHandover {

    void prepareVehicleForHandover(String orderId);
}
