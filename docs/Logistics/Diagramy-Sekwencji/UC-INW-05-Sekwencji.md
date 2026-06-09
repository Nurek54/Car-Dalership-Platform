sequenceDiagram
autonumber
box lightblue Adapter wejściowy (zdarzeniowy)
participant Listener as InventoryEventListener
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

    Listener->>App: on(SettlementCompleted {orderId})
    App->>Repo: findByOrderId(orderId)
    Repo-->>App: InventoryVehicle (RESERVED)
    note over App: weryfikacja aktywnej rezerwacji dla VIN
    App->>Veh: markReadyForHandover()
    note over Veh: aktywna rezerwacja (RESERVED)<br/>registerEvent(VehicleReadyForHandoverEvent)
    App->>Repo: save(vehicle)
    App->>Veh: pullDomainEvents()
    Veh-->>App: [VehicleReadyForHandoverEvent]
    App->>Bus: publish(VehicleReadyForHandoverEvent)