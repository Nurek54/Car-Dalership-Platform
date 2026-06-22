sequenceDiagram
autonumber
box lightblue Adapter wejściowy (zdarzeniowy)
participant Bus as Domain Event Bus<br/>(PaymentDeadlineExpired z Fakturowania)
participant Sub as BillingEventSubscriberAdapter
end
box lightgreen Warstwa aplikacji
participant App as InventoryManagementAppService<br/>«ReleaseReservationUseCase»
end
box pink Warstwa dziedziny
participant Veh as InventoryVehicle
end
box lavender Adaptery wyjściowe (wewn.)
participant Repo as InventoryRepository
participant Pub as EventPublisherPort
end

    note over Bus,Pub: UC-INW-04 — Zwolnienie blokady pojazdu (timeout płatności)
    note over Bus: kontekst Inwentarza nie ma własnych timerów —<br/>polega na sygnale PaymentDeadlineExpired z Fakturowania (kanwa: Assumptions)
    Bus-->>Sub: PaymentDeadlineExpiredEvent {orderId}
    Sub->>App: releaseReservationForOrder(orderId)
    App->>Repo: findByOrderId(orderId)
    alt A1: pojazd nie istnieje w rezerwacjach (wydany/usunięty)
        Repo-->>App: Optional.empty()
        note over App: brak akcji (obsługa idempotentna)
    else Główny: znaleziono zablokowany VIN
        Repo-->>App: InventoryVehicle (RESERVED)
        App->>Veh: releaseReservation()
        note over Veh: RESERVED -> ON_STOCK ("Wolny"), czyści referencję OrderId<br/>registerEvent(VehicleReservationCancelledEvent)
        App->>Repo: save(vehicle)
        App->>Veh: pullDomainEvents()
        Veh-->>App: [VehicleReservationCancelledEvent]
        App->>Pub: publish(VehicleReservationCancelledEvent)
    end
