package salon.logistics.application;

import salon.logistics.application.port.in.OrderFactoryVehicleUseCase;
import salon.logistics.application.port.in.PrepareForHandoverUseCase;
import salon.logistics.application.port.in.ReceiveVehicleUseCase;
import salon.logistics.application.port.in.ReleaseInventoryUseCase;
import salon.logistics.application.port.in.ReleaseReservationUseCase;
import salon.logistics.application.port.in.ReserveVehicleUseCase;
import salon.logistics.application.port.in.SynchronizeSpecificationUseCase;
import salon.logistics.application.port.out.FactoryIntegrationAclPort;
import salon.logistics.application.FactoryOrderRejectedException;
import salon.logistics.application.port.out.InventoryRepository;
import salon.logistics.application.port.out.SpecificationReadModelPort;
import salon.logistics.domain.event.FactoryOrderFailedEvent;
import salon.logistics.domain.event.FactoryOrderPlacedEvent;
import salon.logistics.domain.event.VehicleInventoryReleasedError;
import salon.logistics.domain.event.VehicleIsNotOnStockEvent;
import salon.logistics.domain.exceptions.InvalidVehicleStateException;
import salon.logistics.domain.model.vehicle.ImporterData;
import salon.logistics.domain.model.vehicle.InventoryVehicle;
import salon.logistics.domain.model.vehicle.InventoryVehicleFactory;
import salon.logistics.domain.model.vehicle.VinNumber;
import salon.shared.application.EventPublisherPort;
import salon.shared.event.DomainEvent;
import salon.shared.model.OrderId;
import salon.shared.model.SpecificationId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Scentralizowana usługa aplikacyjna Kontekstu Inwentarza i Logistyki —
 * węzeł "InventoryManagementAppService" w docs/Inwentarz-Logistyka/LogisticsArchitecture.md
 * (PDF rozdz. 3.5.3 "Scentralizowana Orkiestracja i Izolacja Logiki").
 *
 * Wszystkie przypadki użycia (UC-INW-01..06) mają własne dedykowane porty wejściowe,
 * ale ich wykonanie jest delegowane do tej jednej usługi (redukcja Class Explosion).
 * Wyłączna odpowiedzialność: orkiestracja — pobranie/utworzenie agregatu InventoryVehicle,
 * wywołanie mutacji stanu, utrwalenie wyników i publikacja zdarzeń WYGENEROWANYCH
 * PRZEZ AGREGAT. Zdarzenia procesowe bez agregatu (VehicleIsNotOnStock, FactoryOrderPlaced,
 * FactoryOrderFailed, VehicleInventoryReleasedError) emituje usługa.
 */
public class InventoryManagementAppService
        implements ReserveVehicleUseCase, OrderFactoryVehicleUseCase, ReceiveVehicleUseCase,
        ReleaseReservationUseCase, PrepareForHandoverUseCase, ReleaseInventoryUseCase,
        SynchronizeSpecificationUseCase {

    private final InventoryRepository inventoryRepository;
    private final InventoryVehicleFactory vehicleFactory;
    private final SpecificationReadModelPort specificationReadModel;
    private final FactoryIntegrationAclPort factoryAcl;
    private final EventPublisherPort eventPublisher;

    public InventoryManagementAppService(InventoryRepository inventoryRepository,
                                         SpecificationReadModelPort specificationReadModel,
                                         FactoryIntegrationAclPort factoryAcl,
                                         EventPublisherPort eventPublisher) {
        this(inventoryRepository, new InventoryVehicleFactory(),
                specificationReadModel, factoryAcl, eventPublisher);
    }

    public InventoryManagementAppService(InventoryRepository inventoryRepository,
                                         InventoryVehicleFactory vehicleFactory,
                                         SpecificationReadModelPort specificationReadModel,
                                         FactoryIntegrationAclPort factoryAcl,
                                         EventPublisherPort eventPublisher) {
        if (inventoryRepository == null) {
            throw new IllegalArgumentException("inventoryRepository must not be null.");
        }
        if (vehicleFactory == null) {
            throw new IllegalArgumentException("vehicleFactory must not be null.");
        }
        if (specificationReadModel == null) {
            throw new IllegalArgumentException("specificationReadModel must not be null.");
        }
        if (factoryAcl == null) {
            throw new IllegalArgumentException("factoryAcl must not be null.");
        }
        if (eventPublisher == null) {
            throw new IllegalArgumentException("eventPublisher must not be null.");
        }
        this.inventoryRepository = inventoryRepository;
        this.vehicleFactory = vehicleFactory;
        this.specificationReadModel = specificationReadModel;
        this.factoryAcl = factoryAcl;
        this.eventPublisher = eventPublisher;
    }

    /** Zdarzenie SpecificationCompleted (Katalog) -> aktualizacja lokalnej kopii specyfikacji. */
    @Override
    public void registerSpecification(String specificationId, List<String> optionCodes) {
        if (specificationId == null || specificationId.isBlank()) {
            throw new IllegalArgumentException("specificationId must not be blank.");
        }
        this.specificationReadModel.saveSpecification(
                new SpecificationId(specificationId), optionCodes);
    }

    /** Zdarzenie OrderPlaced (Sprzedaż) -> powiązanie zamówienia ze specyfikacją. */
    @Override
    public void linkOrderToSpecification(String orderId, String specificationId) {
        OrderId id = requireOrderId(orderId);
        if (specificationId == null || specificationId.isBlank()) {
            throw new IllegalArgumentException("specificationId must not be blank.");
        }
        this.specificationReadModel.linkOrderToSpecification(
                id, new SpecificationId(specificationId));
    }

    /**
     * UC-INW-01: weryfikacja dostępności i rezerwacja pojazdu z placu.
     * Trigger: FinancingApproved LUB BankTransferDeclared. Kody wyposażenia czytane
     * z lokalnego read modelu (zasilonego zdarzeniami SpecificationCompleted i OrderPlaced)
     * — bez synchronicznego odpytywania innych kontekstów.
     */
    @Override
    public void reserveVehicleForOrder(String orderId) {
        OrderId id = requireOrderId(orderId);
        List<String> specCodes = requireSpecCodes(id);

        Optional<InventoryVehicle> found = this.inventoryRepository.findAvailableVehicle(specCodes);
        if (found.isEmpty()) {
            // A1: samochodu nie ma na placu -> wstrzymujemy rezerwację, emitujemy VehicleIsNotOnStock
            // (w Fakturowaniu wyzwala UC-FIR-01 — prośbę o zadatek).
            this.eventPublisher.publish(new VehicleIsNotOnStockEvent(
                    UUID.randomUUID(), id.value(), Instant.now()));
            return;
        }
        InventoryVehicle vehicle = found.get();
        vehicle.lockForOrder(id);
        this.inventoryRepository.save(vehicle);
        publishEventsOf(vehicle); // VehicleReservedFromStockEvent (w Fakturowaniu: UC-FIR-02)
    }

    /**
     * UC-INW-02: zlecenie produkcji pojazdu w fabryce. Trigger: AdvancePaymentRegistered.
     * Kody wyposażenia (silnik, opcje, kolor) czytane z lokalnego read modelu specyfikacji;
     * FactoryIntegrationAclPort (ACL) komunikuje się z API producenta — to jedyna
     * synchroniczna integracja tego przypadku użycia (system zewnętrzny, zgodnie z PDF).
     */
    @Override
    public void orderVehicleFromFactory(String orderId) {
        OrderId id = requireOrderId(orderId);

        // Idempotencja: jeśli dla zamówienia istnieje już pojazd (zarezerwowany/w produkcji), nie zlecamy.
        if (this.inventoryRepository.findByOrderId(id).isPresent()) {
            System.out.println("[InventoryManagementAppService] Order " + orderId
                    + " already has a vehicle — factory order skipped.");
            return;
        }

        List<String> specCodes = requireSpecCodes(id);
        try {
            VinNumber vin = this.factoryAcl.placeFactoryOrder(id, specCodes);
            // Wirtualna instancja auta (IN_PRODUCTION) przypisana do zamówienia klienta.
            InventoryVehicle vehicle = this.vehicleFactory.createOrderedFromFactory(vin, id);
            this.inventoryRepository.save(vehicle);
            this.eventPublisher.publish(new FactoryOrderPlacedEvent(
                    UUID.randomUUID(), id.value(), vin.value(), Instant.now()));
        } catch (FactoryOrderRejectedException e) {
            // A1: fabryka odrzuca zlecenie (np. problem z połączeniem).
            this.eventPublisher.publish(new FactoryOrderFailedEvent(
                    UUID.randomUUID(), id.value(), e.getMessage(), Instant.now()));
        }
    }

    /**
     * UC-INW-03: przyjęcie pojazdu na stan magazynowy (Pracownik Placu skanuje VIN).
     * Tożsamość pojazdu weryfikuje warstwa ACL importera. Auto z kartoteką "W produkcji"
     * zostaje sparowane z oczekującym zamówieniem (VehicleDeliveredToStock); auto bez
     * zamówienia (A1) dostaje status "Wolny" — bez zdarzenia końcowego.
     */
    @Override
    public void receiveVehicle(String vin) {
        if (vin == null || vin.isBlank()) {
            throw new IllegalArgumentException("vin must not be blank.");
        }
        VinNumber vinNumber = new VinNumber(vin);
        ImporterData data = this.factoryAcl.fetchVehicleData(vinNumber);

        InventoryVehicle vehicle = this.inventoryRepository.findByVin(vinNumber)
                .orElseGet(() -> this.vehicleFactory.createUnassigned(vinNumber)); // A1: nowa kartoteka

        vehicle.receiveOnYard(data);
        this.inventoryRepository.save(vehicle);
        publishEventsOf(vehicle); // VehicleDeliveredToStockEvent (tylko gdy sparowano z zamówieniem)
    }

    /**
     * UC-INW-04: zwolnienie blokady pojazdu. Trigger: PaymentDeadlineExpired.
     * Operacja idempotentna — A1: pojazd nie istnieje w rezerwacjach -> brak akcji.
     */
    @Override
    public void releaseReservationForOrder(String orderId) {
        OrderId id = requireOrderId(orderId);
        Optional<InventoryVehicle> found = this.inventoryRepository.findByOrderId(id);
        if (found.isEmpty()) {
            System.out.println("[InventoryManagementAppService] No reserved vehicle for order "
                    + orderId + " — release skipped (idempotent).");
            return;
        }
        InventoryVehicle vehicle = found.get();
        vehicle.releaseReservation();
        this.inventoryRepository.save(vehicle);
        publishEventsOf(vehicle); // VehicleReservationCancelledEvent
    }

    /**
     * UC-INW-05: przygotowanie pojazdu do wydania po rozliczeniu. Trigger: SettlementCompleted.
     */
    @Override
    public void prepareVehicleForHandover(String orderId) {
        OrderId id = requireOrderId(orderId);
        InventoryVehicle vehicle = this.inventoryRepository.findByOrderId(id)
                .orElseThrow(() -> new IllegalStateException(
                        "No reserved vehicle for order " + orderId));
        vehicle.markReadyForHandover();
        this.inventoryRepository.save(vehicle);
        publishEventsOf(vehicle); // VehicleReadyForHandoverEvent (w CRM: UC-CRM-04)
    }

    /**
     * UC-INW-06: zdjęcie pojazdu ze stanu magazynowego. Trigger: komenda ReleaseVehicle z CRM.
     * A1: niewłaściwy status pojazdu -> komenda odrzucona, emitowane VehicleInventoryReleasedError.
     */
    @Override
    public void releaseVehicle(String orderId) {
        OrderId id = requireOrderId(orderId);
        Optional<InventoryVehicle> found = this.inventoryRepository.findByOrderId(id);
        if (found.isEmpty()) {
            this.eventPublisher.publish(new VehicleInventoryReleasedError(
                    UUID.randomUUID(), id.value(), "No vehicle assigned to this order.",
                    Instant.now()));
            return;
        }
        InventoryVehicle vehicle = found.get();
        try {
            vehicle.handOver();
        } catch (InvalidVehicleStateException e) {
            this.eventPublisher.publish(new VehicleInventoryReleasedError(
                    UUID.randomUUID(), id.value(), e.getMessage(), Instant.now()));
            return;
        }
        this.inventoryRepository.save(vehicle);
        publishEventsOf(vehicle); // VehicleInventoryReleasedEvent
    }

    // --- pomocnicze ---

    private OrderId requireOrderId(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank.");
        }
        return new OrderId(orderId);
    }

    /**
     * Kody wyposażenia z lokalnego read modelu. Brak danych = naruszenie spójności
     * ostatecznej (zdarzenia SpecificationCompleted/OrderPlaced jeszcze nie dotarły
     * albo zostały zgubione) — zgłaszamy jawnie, zamiast rezerwować w ciemno.
     */
    private List<String> requireSpecCodes(OrderId orderId) {
        return this.specificationReadModel.findCodesForOrder(orderId)
                .orElseThrow(() -> new IllegalStateException(
                        "No specification known for order " + orderId.value()
                        + " — SpecificationCompleted/OrderPlaced events not received yet."));
    }

    private void publishEventsOf(InventoryVehicle vehicle) {
        for (DomainEvent event : vehicle.pullDomainEvents()) {
            this.eventPublisher.publish(event);
        }
    }
}
