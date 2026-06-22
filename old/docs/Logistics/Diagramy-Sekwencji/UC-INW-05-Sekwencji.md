sequenceDiagram
autonumber
box lightblue Adapter wejściowy (zdarzeniowy)
participant Bus as Domain Event Bus<br/>(SettlementCompleted z Fakturowania)
participant Sub as BillingEventSubscriberAdapter
end
box lightgreen Warstwa aplikacji
participant App as InventoryManagementAppService<br/>«PrepareForHandoverUseCase»
end
box pink Warstwa dziedziny
participant Veh as InventoryVehicle
end
box lavender Adaptery wyjściowe (wewn.)
participant Repo as InventoryRepository
participant Pub as EventPublisherPort
end

    note over Bus,Pub: UC-INW-05 — Przygotowanie pojazdu do wydania po rozliczeniu
    Bus-->>Sub: SettlementCompletedEvent {orderId} (saldo = 0 PLN)
    Sub->>App: prepareVehicleForHandover(orderId)
    App->>Repo: findByOrderId(orderId)
    Repo-->>App: InventoryVehicle (RESERVED)
    note over App: weryfikacja aktywnej rezerwacji dla VIN (krok 2)
    App->>Veh: markReadyForHandover()
    note over Veh: pojazd (RESERVED) oznaczony "Gotowy do wydania"<br/>registerEvent(VehicleReadyForHandoverEvent)
    App->>Repo: save(vehicle)
    App->>Veh: pullDomainEvents()
    Veh-->>App: [VehicleReadyForHandoverEvent]
    App->>Pub: publish(VehicleReadyForHandoverEvent)
    note over Pub: Sprzedaż/CRM: trigger UC-CRM-04 (zaproszenie klienta po odbiór)
