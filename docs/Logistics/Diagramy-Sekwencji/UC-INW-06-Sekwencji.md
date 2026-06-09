sequenceDiagram
autonumber
box lightblue Adapter wejściowy (komenda ze Sprzedaży/CRM)
participant Cmd as InventoryCommandAdapter
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

    Cmd->>App: releaseVehicle(ReleaseVehicle {vin})
    App->>Repo: findByVin(vin)
    Repo-->>App: InventoryVehicle
    App->>Veh: handOver()
    alt A1: Niewłaściwy status (≠ gotowy do wydania)
        Veh-->>App: InvalidVehicleStateException
        App->>Bus: publish(VehicleInventoryReleasedError)
        App-->>Cmd: błąd komendy
    else Główny: pojazd gotowy do wydania
        note over Veh: → HANDED_OVER<br/>registerEvent(VehicleInventoryReleased)
        App->>Repo: save(vehicle)
        App->>Veh: pullDomainEvents()
        Veh-->>App: [VehicleInventoryReleased]
        App->>Bus: publish(VehicleInventoryReleased)
        App-->>Cmd: OK (Wydany)
    end