sequenceDiagram
autonumber
actor Op as Handlowiec / CRM
box lightblue Adapter wejściowy
participant Rest as InventoryRestAdapter
end
box lightgreen Warstwa aplikacji
participant App as InventoryAppService
end
box lavender Adapter wyjściowy (ACL)
participant Acl as FactoryIntegrationAclPort
end
box pink Warstwa dziedziny
participant Fac as InventoryVehicleFactory
participant Veh as InventoryVehicle
end
box lavender Adaptery wyjściowe (wewn.)
participant Repo as VehicleRepository
participant Bus as EventPublisher
end

    Op->>Rest: POST /api/factory-orders {orderId, specCodes}
    Rest->>App: orderVehicleFromFactory(orderId, specCodes)
    App->>Acl: placeFactoryOrder(specCodes)
    alt A1: Fabryka odrzuca zlecenie (błąd API/połączenia)
        Acl-->>App: FactoryOrderException
        App->>Bus: publish(FactoryOrderFailed)
        App-->>Rest: 502 Bad Gateway
    else Główny: zlecenie przyjęte przez fabrykę
        Acl-->>App: factoryJobId
        App->>Fac: createInProduction(orderId, factoryJobId)
        note over Fac,Veh: wirtualny pojazd w stanie IN_PRODUCTION
        Fac-->>App: InventoryVehicle (IN_PRODUCTION)
        App->>Repo: save(vehicle)
        App-->>Rest: 202 Accepted (zamówiono w fabryce)
    end