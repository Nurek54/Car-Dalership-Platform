sequenceDiagram
autonumber
actor Op as Pracownik placu
box lightblue Adapter wejściowy
participant Rest as InventoryRestAdapter
end
box lightgreen Warstwa aplikacji
participant App as YardManagementAppService<br/>«ManageYardUseCase»
end
box lavender Adapter wyjściowy (ACL)
participant Acl as ImporterIdentityAclPort
end
box pink Warstwa dziedziny
participant Veh as InventoryVehicle
end
box lavender Adaptery wyjściowe (wewn.)
participant Repo as VehicleRepository
participant Bus as EventPublisher
end

    Op->>Rest: POST /api/yard/receive {vin}
    Rest->>App: receiveVehicle(vin)
    App->>Acl: verifyVin(vin)
    alt A2: Obcy VIN (nieprzypisany do salonu)
        Acl-->>App: ForeignVinException
        App-->>Rest: 403 Forbidden (błąd tożsamości)
    else Główny: rozpoznano pojazd
        Acl-->>App: ImporterData
        App->>Repo: findByVin(vin)
        Repo-->>App: InventoryVehicle (IN_PRODUCTION)
        App->>Veh: receiveOnYard(data)
        note over Veh: IN_PRODUCTION → ON_STOCK<br/>registerEvent(VehicleReceivedOnYardEvent)
        App->>Repo: save(vehicle)
        App->>Veh: pullDomainEvents()
        Veh-->>App: [VehicleReceivedOnYardEvent]
        App->>Bus: publish(VehicleReceivedOnYardEvent)
        App-->>Rest: 200 OK (Pojazd na placu)
    end