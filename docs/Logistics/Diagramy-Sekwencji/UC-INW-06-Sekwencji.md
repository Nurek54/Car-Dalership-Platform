sequenceDiagram
autonumber
box lightblue Adapter wejściowy (komenda ze Sprzedaży/CRM)
participant Cmd as InventoryCommandAdapter<br/>(InventoryIntegrationPort z CRM)
end
box lightgreen Warstwa aplikacji
participant App as InventoryManagementAppService<br/>«ReleaseInventoryUseCase»
end
box pink Warstwa dziedziny
participant Veh as InventoryVehicle
end
box lavender Adaptery wyjściowe (wewn.)
participant Repo as InventoryRepository
participant Pub as EventPublisherPort
end

    note over Cmd,Pub: UC-INW-06 — Zdjęcie pojazdu ze stanu magazynowego
    Cmd->>App: releaseVehicle(orderId) (komenda ReleaseVehicle, UC-CRM-05 krok 3)
    App->>Repo: findByOrderId(orderId)
    Repo-->>App: InventoryVehicle
    App->>Veh: handOver()
    alt A1: niewłaściwy status (≠ "Gotowy do wydania")
        Veh-->>App: InvalidVehicleStateException
        App->>Pub: publish(VehicleInventoryReleasedError {orderId, reason})
        note over Pub: Sprzedaż/CRM: kompensata — powrót zamówienia<br/>do "Gotowe do odbioru" (UC-CRM-05, A1)
    else Główny: pojazd gotowy do wydania
        note over Veh: RESERVED -> HANDED_OVER ("Wydany")<br/>registerEvent(VehicleInventoryReleasedEvent)
        App->>Repo: save(vehicle)
        App->>Veh: pullDomainEvents()
        Veh-->>App: [VehicleInventoryReleasedEvent]
        App->>Pub: publish(VehicleInventoryReleasedEvent)
        App-->>Cmd: OK (Wydany)
    end
