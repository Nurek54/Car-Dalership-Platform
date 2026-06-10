sequenceDiagram
autonumber
box lightblue Adapter wejściowy (zdarzeniowy)
participant Listener as InventoryEventListener
end
box lightgreen Warstwa aplikacji
participant App as AllocationAppService<br/>«HandleOrderEventsUseCase»
end
box pink Warstwa dziedziny
participant Veh as InventoryVehicle
end
box lavender Adaptery wyjściowe (wewn.)
participant Repo as VehicleRepository
participant Bus as EventPublisher
end

    Listener->>App: releaseReservationForOrder(orderId)<br/>(trigger: PaymentDeadlineExpired)
    App->>Repo: findByOrderId(orderId)
    alt A1: Pojazd nie istnieje w rezerwacjach (wydany/usunięty)
        Repo-->>App: Optional.empty()
        note over App: brak akcji (obsługa idempotentna)
    else Główny: znaleziono zablokowany VIN
        Repo-->>App: InventoryVehicle (RESERVED)
        App->>Veh: releaseReservation()
        note over Veh: RESERVED → ON_STOCK, czyści OrderId<br/>registerEvent(VehicleReservationCancelledEvent)
        App->>Repo: save(vehicle)
        App->>Veh: pullDomainEvents()
        Veh-->>App: [VehicleReservationCancelledEvent]
        App->>Bus: publish(VehicleReservationCancelledEvent)
    end