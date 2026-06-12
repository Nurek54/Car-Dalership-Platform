package salon.logistics.application.port.in;

/**
 * Port wejściowy UC-INW-02: zlecenie produkcji pojazdu w fabryce —
 * węzeł "OrderFactoryVehicleUseCase" w docs/Inwentarz-Logistyka/LogisticsArchitecture.md.
 *
 * Wyzwalany zdarzeniem AdvancePaymentRegistered (opłacony zadatek). Kody wyposażenia
 * czytane są z lokalnego read modelu specyfikacji (SpecificationReadModelPort, zasilanego
 * zdarzeniami SpecificationCompleted/OrderPlaced), zlecenie wysyła FactoryIntegrationAclPort.
 * Kończy się emisją FactoryOrderPlaced lub FactoryOrderFailed (A1).
 */
public interface OrderFactoryVehicleUseCase {

    void orderVehicleFromFactory(String orderId);
}
