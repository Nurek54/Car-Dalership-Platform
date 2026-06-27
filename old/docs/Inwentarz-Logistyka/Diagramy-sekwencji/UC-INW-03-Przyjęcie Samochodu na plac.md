sequenceDiagram
autonumber
actor Op as Pracownik placu
box lightblue Adapter wejściowy
participant Rest as InventoryRestAdapter
end
box lightgreen Warstwa aplikacji
participant App as InventoryManagementAppService<br/>«ReceiveVehicleUseCase»
end
box lavender Adapter wyjściowy (ACL)
participant Acl as FactoryIntegrationAclPort
end
box pink Warstwa dziedziny
participant Fac as InventoryVehicleFactory
participant Veh as InventoryVehicle
end
box lavender Adaptery wyjściowe (wewn.)
participant Repo as InventoryRepository
participant Pub as EventPublisherPort
end

    note over Op,Pub: UC-INW-03 — Przyjęcie pojazdu na stan magazynowy
    Op->>Rest: POST /api/yard/receive {vin} (skan VIN przy zjeździe z lawety)
    Rest->>App: receiveVehicle(vin)
    App->>Acl: fetchVehicleData(vin)
    note over Acl: weryfikacja tożsamości pojazdu w bazie importera
    Acl-->>App: ImporterData

    App->>Repo: findByVin(vin)
    alt Główny: kartoteka istnieje (auto zamówione pod klienta, IN_PRODUCTION)
        Repo-->>App: InventoryVehicle (IN_PRODUCTION, order = OrderId)
        App->>Veh: receiveOnYard(data)
        note over Veh: automatyczne sparowanie VIN z oczekującym zamówieniem:<br/>IN_PRODUCTION -> RESERVED ("Zarezerwowany")<br/>registerEvent(VehicleDeliveredToStockEvent)
        App->>Repo: save(vehicle)
        App->>Veh: pullDomainEvents()
        Veh-->>App: [VehicleDeliveredToStockEvent]
        App->>Pub: publish(VehicleDeliveredToStockEvent)
        App-->>Rest: 200 OK (Zarezerwowany)
    else A1: auto nieprzypisane (zamówione "na stock")
        Repo-->>App: Optional.empty()
        App->>Fac: createUnassigned(vin)
        Fac-->>App: InventoryVehicle (nowa kartoteka, IN_PRODUCTION)
        App->>Veh: receiveOnYard(data)
        note over Veh: brak zamówienia -> IN_PRODUCTION -> ON_STOCK ("Wolny")<br/>zdarzenie końcowe NIE jest emitowane
        App->>Repo: save(vehicle)
        App-->>Rest: 200 OK (Wolny, bez rezerwacji, bez zdarzenia)
    end
