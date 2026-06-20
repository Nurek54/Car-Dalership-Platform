package salon.logistics.application.port.in;

/**
 * Port wejściowy UC-INW-01: weryfikacja dostępności i rezerwacja pojazdu z placu —
 * węzeł "ReserveVehicle" w docs/Inwentarz-Logistyka/LogisticsArchitecture.md.
 *
 * Wyzwalany asynchronicznie zdarzeniem FinancingApproved LUB BankTransferDeclared.
 * Kończy się emisją VehicleReservedFromStock (auto na placu) lub VehicleIsNotOnStock (A1).
 */
public interface ReserveVehicle {

    void reserveVehicleForOrder(String orderId);
}
