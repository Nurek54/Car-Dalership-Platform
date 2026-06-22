sequenceDiagram
autonumber
box lightblue Adapter wejściowy (zdarzeniowy)
participant Bus as Domain Event Bus<br/>(FinancingApproved LUB BankTransferDeclared)
participant Sub as SalesEventSubscriberAdapter /<br/>FinancingEventSubscriberAdapter
end
box lightgreen Warstwa aplikacji
participant App as InventoryManagementAppService<br/>«ReserveVehicleUseCase»
end
box lavender Adapter wyjściowy (ACL)
participant Spec as SpecificationIntegrationPort
end
box pink Warstwa dziedziny
participant Veh as InventoryVehicle
end
box lavender Adaptery wyjściowe (wewn.)
participant Repo as InventoryRepository
participant Pub as EventPublisherPort
end

    note over Bus,Pub: UC-INW-01 — Weryfikacja dostępności i rezerwacja pojazdu z placu
    Bus-->>Sub: FinancingApprovedEvent {orderId} LUB BankTransferDeclaredEvent {orderId}
    Sub->>Sub: walidacja (orderId niepusty)
    Sub->>App: reserveVehicleForOrder(orderId)

    App->>Spec: getSpecificationForOrder(orderId)
    note over Spec: ACL do Kontekstu Katalogu/Sprzedaży —<br/>kody wyposażenia NIE pochodzą "z powietrza"
    Spec-->>App: List~specCodes~ (zatwierdzona specyfikacja)

    App->>Repo: findAvailableVehicle(specCodes)
    alt Główny: pasujące auto jest na placu (status "Wolny")
        Repo-->>App: InventoryVehicle (ON_STOCK)
        App->>Veh: lockForOrder(orderId)
        note over Veh: ON_STOCK -> RESERVED (twarda blokada VIN, anty double-booking)<br/>registerEvent(VehicleReservedFromStockEvent)
        App->>Repo: save(vehicle)
        App->>Veh: pullDomainEvents()
        Veh-->>App: [VehicleReservedFromStockEvent]
        App->>Pub: publish(VehicleReservedFromStockEvent)
        note over Pub: Fakturowanie: trigger UC-FIR-02 (faktura końcowa)
    else A1: samochodu nie ma na placu
        Repo-->>App: Optional.empty()
        App->>Pub: publish(VehicleIsNotOnStockEvent)
        note over Pub: rezerwacja wstrzymana&#59; Fakturowanie: trigger UC-FIR-01 (prośba o zadatek)
    end
