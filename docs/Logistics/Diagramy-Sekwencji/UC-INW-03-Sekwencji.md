sequenceDiagram
autonumber
actor Op as Pracownik placu
box lightblue Adapter wejściowy
participant Rest as InventoryRestAdapter
end
box lightgreen Warstwa aplikacji
participant App as InventoryAppService
end
box pink Warstwa dziedziny
participant Veh as InventoryVehicle
end
box lavender Adaptery wyjściowe (wewn.)
participant Repo as VehicleRepository
participant Bus as EventPublisher
end

    Op->>Rest: POST /api/stock/deliver {vin}
    Rest->>App: registerDelivery(vin)
    App->>Repo: findByVin(vin)
    Repo-->>App: InventoryVehicle (ON_STOCK)
    App->>Repo: findPendingOrderForSpec(vin)
    alt A1: Auto nieprzypisane (zamówione „na stock")
        Repo-->>App: Optional.empty()
        note over Veh: pojazd pozostaje ON_STOCK (Wolny)
        App->>Repo: save(vehicle)
        App-->>Rest: 200 OK (bez rezerwacji, bez zdarzenia)
    else Główny: znaleziono oczekujące zamówienie
        Repo-->>App: OrderId
        App->>Veh: lockForOrder(orderId)
        note over Veh: ON_STOCK → RESERVED (referencja OrderId)<br/>registerEvent(VehicleDeliveredToStock)
        App->>Repo: save(vehicle)
        App->>Veh: pullDomainEvents()
        Veh-->>App: [VehicleDeliveredToStock]
        App->>Bus: publish(VehicleDeliveredToStock)
        App-->>Rest: 200 OK (Zarezerwowany)
    end