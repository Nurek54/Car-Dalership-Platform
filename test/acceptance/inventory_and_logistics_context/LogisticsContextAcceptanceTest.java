package acceptance.inventory_and_logistics_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import salon.common.application.EventPublisher;
import salon.common.event.DomainEvent;
import salon.common.model.OrderId;
import salon.logistics.application.domain.event.FactoryOrderPlacedEvent;
import salon.logistics.application.domain.event.VehicleDeliveredToStockEvent;
import salon.logistics.application.domain.event.VehicleInventoryReleasedEvent;
import salon.logistics.application.domain.event.VehicleIsNotOnStockEvent;
import salon.logistics.application.domain.event.VehicleReadyForHandoverEvent;
import salon.logistics.application.domain.event.VehicleReservationCancelledEvent;
import salon.logistics.application.domain.event.VehicleReservedFromStockEvent;
import salon.logistics.application.domain.model.vehicle.ImporterData;
import salon.logistics.application.domain.model.vehicle.InventoryVehicleFactory;
import salon.logistics.application.domain.model.vehicle.SpecificationId;
import salon.logistics.application.domain.model.vehicle.VehicleState;
import salon.logistics.application.domain.model.vehicle.VinNumber;
import salon.logistics.application.port.in.PrepareForHandover;
import salon.logistics.application.port.in.ReceiveVehicle;
import salon.logistics.application.port.in.ReleaseVehicle;
import salon.logistics.application.port.in.ReserveVehicle;
import salon.logistics.application.port.out.CatalogIntegration;
import salon.logistics.application.port.out.ImporterACL;
import salon.logistics.application.port.out.VehicleDatabaseRepository;
import salon.logistics.application.service.InventoryManagementService;
import salon.logistics.infrastructure.in.messaging.BillingEventSubscriberAdapter;
import salon.logistics.infrastructure.in.messaging.CatalogEventSubscriberAdapter;
import salon.logistics.infrastructure.in.messaging.FinancingEventSubscriberAdapter;
import salon.logistics.infrastructure.in.messaging.SalesEventSubscriberAdapter;
import salon.logistics.infrastructure.in.web.VehicleArrivalRestAdapter;
import salon.logistics.infrastructure.out.mock.InMemoryInventoryRepository;
import salon.logistics.infrastructure.out.mock.InMemorySpecificationReadModelAdapter;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testy akceptacyjne kontekstu Inwentarza i Logistyki — pełne przypadki użycia end-to-end
 * sterowane adapterami wejściowymi. Kontekst jest złożony przez Springa (wstrzykiwanie zależności),
 * z prawdziwą persystencją w pamięci i nasłuchującym EventPublisherem zbierającym wyemitowane zdarzenia.
 *
 * Każda metoda dostaje świeży kontekst (@DirtiesContext), aby stan magazynu i lista zdarzeń się nie nakładały.
 */
@SpringBootTest(classes = LogisticsContextAcceptanceTest.LogisticsTestConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class LogisticsContextAcceptanceTest {

    @Autowired private CatalogEventSubscriberAdapter catalogSubscriber;
    @Autowired private SalesEventSubscriberAdapter salesSubscriber;
    @Autowired private FinancingEventSubscriberAdapter financingSubscriber;
    @Autowired private BillingEventSubscriberAdapter billingSubscriber;
    @Autowired private VehicleArrivalRestAdapter vehicleArrival;
    @Autowired private VehicleDatabaseRepository vehicleRepository;
    @Autowired private RecordingEventPublisher eventPublisher;

    private final InventoryVehicleFactory factory = new InventoryVehicleFactory();

    /** Dostawia na plac wolny pojazd o danej specyfikacji (auto fizycznie obecne w salonie). */
    private void seedStockVehicle(String vin, String specificationId) {
        vehicleRepository.save(factory.createStockArrival(
                new ImporterData(new VinNumber(vin), new SpecificationId(specificationId), List.of())));
    }

    // ===================================================================================
    // UC-INW-01: Weryfikacja dostępności i rezerwacja pojazdu z placu
    // ===================================================================================
    @Test
    void uc01_shouldReserveVehicleFromStock() {
        // Na placu stoi wolny pojazd o specyfikacji SPEC-1, a zamówienie jest z nią powiązane
        seedStockVehicle("VIN-1", "SPEC-1");
        salesSubscriber.handleOrderPlaced(new SalesEventSubscriberAdapter.OrderPlaced("ORD-1", "SPEC-1"));

        // Zatwierdzone finansowanie inicjuje proces realizacji
        financingSubscriber.handleFinancingApproved(new FinancingEventSubscriberAdapter.FinancingApproved("ORD-1"));

        // Pojazd zostaje zarezerwowany, emitowane jest VehicleReservedFromStock
        assertThat(vehicleRepository.findByOrderId(new OrderId("ORD-1")).orElseThrow().state())
                .isEqualTo(VehicleState.RESERVED);
        assertThat(eventPublisher.has(VehicleReservedFromStockEvent.class)).isTrue();
    }

    @Test
    void uc01_a1_shouldEmitNotOnStockWhenNoVehicle() {
        // Brak wolnego pojazdu o wymaganej specyfikacji
        salesSubscriber.handleOrderPlaced(new SalesEventSubscriberAdapter.OrderPlaced("ORD-2", "SPEC-2"));

        financingSubscriber.handleBankTransferDeclared(
                new FinancingEventSubscriberAdapter.BankTransferDeclared("ORD-2"));

        // Rezerwacja wstrzymana — emisja VehicleNotOnStock
        assertThat(eventPublisher.has(VehicleIsNotOnStockEvent.class)).isTrue();
    }

    // ===================================================================================
    // UC-INW-02 + UC-INW-03: Zlecenie produkcji i przyjęcie pojazdu na plac
    // ===================================================================================
    @Test
    void uc02_uc03_shouldOrderFromFactoryThenReceiveOntoYard() {
        // Specyfikacja zamówienia jest znana (zdarzenie z Katalogu), zamówienie powiązane
        catalogSubscriber.handleSpecificationCompleted(
                new CatalogEventSubscriberAdapter.SpecificationCompleted("SPEC-3", List.of("B2", "C1")));
        salesSubscriber.handleOrderPlaced(new SalesEventSubscriberAdapter.OrderPlaced("ORD-3", "SPEC-3"));

        // UC-INW-02: zaksięgowany zadatek wyzwala zlecenie produkcji do fabryki
        billingSubscriber.handleAdvancePaymentRegistered(
                new BillingEventSubscriberAdapter.AdvancePaymentRegistered("ORD-3"));
        assertThat(eventPublisher.has(FactoryOrderPlacedEvent.class)).isTrue();
        assertThat(vehicleRepository.findByOrderId(new OrderId("ORD-3")).orElseThrow().state())
                .isEqualTo(VehicleState.IN_PRODUCTION);

        // UC-INW-03: auto zjeżdża z lawety (skan VIN nadanego przez fabrykę) i trafia na plac
        vehicleArrival.onVehicleArrival(new VehicleArrivalRestAdapter.VehicleArrivalRequest("VIN-FAC-ORD-3"));
        assertThat(vehicleRepository.findByOrderId(new OrderId("ORD-3")).orElseThrow().state())
                .isEqualTo(VehicleState.RESERVED);
        assertThat(eventPublisher.has(VehicleDeliveredToStockEvent.class)).isTrue();
    }

    // ===================================================================================
    // UC-INW-04: Zwolnienie blokady pojazdu po upływie terminu płatności
    // ===================================================================================
    @Test
    void uc04_shouldReleaseReservationOnPaymentDeadline() {
        // Pojazd jest zarezerwowany dla zamówienia ORD-4
        seedStockVehicle("VIN-4", "SPEC-4");
        salesSubscriber.handleOrderPlaced(new SalesEventSubscriberAdapter.OrderPlaced("ORD-4", "SPEC-4"));
        financingSubscriber.handleFinancingApproved(new FinancingEventSubscriberAdapter.FinancingApproved("ORD-4"));

        // Upływa termin płatności końcowej
        billingSubscriber.handlePaymentDeadlineExpired(
                new BillingEventSubscriberAdapter.PaymentDeadlineExpired("ORD-4"));

        // Pojazd wraca na plac, emisja VehicleReservationCancelled
        assertThat(vehicleRepository.findByVin(new VinNumber("VIN-4")).orElseThrow().state())
                .isEqualTo(VehicleState.ON_STOCK);
        assertThat(eventPublisher.has(VehicleReservationCancelledEvent.class)).isTrue();
    }

    // ===================================================================================
    // UC-INW-05 + UC-INW-06: Przygotowanie do wydania i fizyczne wydanie pojazdu
    // ===================================================================================
    @Test
    void uc05_uc06_shouldPrepareAndHandOverVehicle() {
        // Pojazd zarezerwowany dla zamówienia ORD-6
        seedStockVehicle("VIN-6", "SPEC-6");
        salesSubscriber.handleOrderPlaced(new SalesEventSubscriberAdapter.OrderPlaced("ORD-6", "SPEC-6"));
        financingSubscriber.handleFinancingApproved(new FinancingEventSubscriberAdapter.FinancingApproved("ORD-6"));

        // UC-INW-05: pełne rozliczenie -> pojazd gotowy do wydania
        billingSubscriber.handleSettlementCompleted(
                new BillingEventSubscriberAdapter.SettlementCompleted("ORD-6"));
        assertThat(vehicleRepository.findByOrderId(new OrderId("ORD-6")).orElseThrow().state())
                .isEqualTo(VehicleState.READY_FOR_HANDOVER);
        assertThat(eventPublisher.has(VehicleReadyForHandoverEvent.class)).isTrue();

        // UC-INW-06: komenda wydania z modułu Sprzedaży -> pojazd wydany
        salesSubscriber.handleReleaseVehicle(new SalesEventSubscriberAdapter.ReleaseVehicleCommand("ORD-6"));
        assertThat(vehicleRepository.findByOrderId(new OrderId("ORD-6")).orElseThrow().state())
                .isEqualTo(VehicleState.HANDED_OVER);
        assertThat(eventPublisher.has(VehicleInventoryReleasedEvent.class)).isTrue();
    }

    // ===================================================================================
    // Złożenie kontekstu (wstrzykiwanie zależności) — odpowiednik korzenia kompozycji w testach
    // ===================================================================================
    @Configuration
    static class LogisticsTestConfig {

        @Bean
        RecordingEventPublisher eventPublisher() {
            return new RecordingEventPublisher();
        }

        @Bean
        VehicleDatabaseRepository vehicleRepository() {
            return new InMemoryInventoryRepository();
        }

        @Bean
        CatalogIntegration catalogIntegration() {
            return new InMemorySpecificationReadModelAdapter();
        }

        /** Deterministyczny adapter fabryki — nadaje VIN zależny od numeru zamówienia. */
        @Bean
        ImporterACL importerAcl() {
            return (orderId, optionCodes) -> "VIN-FAC-" + orderId;
        }

        @Bean
        InventoryManagementService inventoryManagementService(VehicleDatabaseRepository repository,
                                                              CatalogIntegration catalogIntegration,
                                                              ImporterACL importerAcl,
                                                              EventPublisher eventPublisher) {
            return new InventoryManagementService(repository, catalogIntegration, importerAcl, eventPublisher);
        }

        @Bean
        CatalogEventSubscriberAdapter catalogSubscriber(CatalogIntegration catalogIntegration) {
            return new CatalogEventSubscriberAdapter(catalogIntegration);
        }

        @Bean
        SalesEventSubscriberAdapter salesSubscriber(CatalogIntegration catalogIntegration, ReleaseVehicle releaseVehicle) {
            return new SalesEventSubscriberAdapter(catalogIntegration, releaseVehicle);
        }

        @Bean
        FinancingEventSubscriberAdapter financingSubscriber(ReserveVehicle reserveVehicle) {
            return new FinancingEventSubscriberAdapter(reserveVehicle);
        }

        @Bean
        BillingEventSubscriberAdapter billingSubscriber(ReserveVehicle reserveVehicle,
                                                        PrepareForHandover prepareForHandover,
                                                        ReleaseVehicle releaseVehicle) {
            return new BillingEventSubscriberAdapter(reserveVehicle, prepareForHandover, releaseVehicle);
        }

        @Bean
        VehicleArrivalRestAdapter vehicleArrival(ReceiveVehicle receiveVehicle) {
            return new VehicleArrivalRestAdapter(receiveVehicle);
        }
    }

    /** Nasłuchujący adapter zdarzeń — zbiera wyemitowane zdarzenia dziedzinowe na potrzeby asercji. */
    static final class RecordingEventPublisher implements EventPublisher {
        private final List<DomainEvent> events = new ArrayList<>();

        @Override
        public void publish(DomainEvent event) {
            events.add(event);
        }

        boolean has(Class<? extends DomainEvent> type) {
            return events.stream().anyMatch(type::isInstance);
        }
    }
}
