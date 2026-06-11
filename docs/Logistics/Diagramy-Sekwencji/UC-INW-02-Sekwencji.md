sequenceDiagram
autonumber
box lightblue Adapter wejściowy (zdarzeniowy)
participant Bus as Domain Event Bus<br/>(AdvancePaymentRegistered z Fakturowania)
participant Sub as BillingEventSubscriberAdapter
end
box lightgreen Warstwa aplikacji
participant App as InventoryManagementAppService<br/>«OrderFactoryVehicleUseCase»
end
box lavender Adaptery wyjściowe (ACL)
participant Spec as SpecificationIntegrationPort
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

    note over Bus,Pub: UC-INW-02 — Zlecenie produkcji pojazdu w fabryce
    Bus-->>Sub: AdvancePaymentRegisteredEvent {orderId} (opłacony zadatek)
    Sub->>App: orderVehicleFromFactory(orderId)

    App->>Repo: findByOrderId(orderId)
    note over App: idempotencja: istniejący pojazd dla zamówienia -> zlecenie pomijane
    Repo-->>App: Optional.empty()

    App->>Spec: getSpecificationForOrder(orderId)
    note over Spec: synchroniczne pobranie kodów wyposażenia<br/>(silnik, opcje, kolor) na podstawie OrderId (PDF 3.5.3)
    Spec-->>App: List~specCodes~

    App->>Acl: placeFactoryOrder(orderId, specCodes)
    note over Acl: komenda PlaceFactoryOrder — ACL tłumaczy żądanie<br/>na format API producenta/importera
    alt Główny: FactoryOrderAcknowledged
        Acl-->>App: VinNumber (przydzielony przez fabrykę)
        App->>Fac: createOrderedFromFactory(vin, orderId)
        Fac->>Veh: new InventoryVehicle(vin) + assignToOrder(orderId)
        note over Veh: wirtualna instancja auta w stanie IN_PRODUCTION<br/>("W produkcji"), przypisana do zamówienia
        Fac-->>App: InventoryVehicle
        App->>Repo: save(vehicle)
        App->>Pub: publish(FactoryOrderPlacedEvent {orderId, vin})
    else A1: fabryka odrzuca zlecenie (np. problem z połączeniem)
        Acl-->>App: FactoryOrderRejectedException
        App->>Pub: publish(FactoryOrderFailedEvent {orderId, reason})
    end
