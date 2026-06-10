sequenceDiagram
autonumber
box lightblue Adapter wejściowy
participant Bus as Domain Event Bus<br/>(OrderActivatedEvent ze Sprzedaży)
participant Sub as SalesEventSubscriberAdapter
end
box lightgreen Warstwa aplikacji
participant App as AllocationAppService<br/>«HandleOrderEventsUseCase»
end
box lavender Adapter wyjściowy (ACL)
participant Spec as SpecificationIntegrationAdapter<br/>«SpecificationIntegrationPort»
end
box pink Warstwa dziedziny
participant Alloc as VehicleAllocationDomainService
participant Veh as InventoryVehicle
participant Slot as ProductionSlot
end
box lavender Adaptery wyjściowe (wewn.)
participant VRepo as VehicleRepository
participant SRepo as ProductionSlotRepository
participant Pub as EventPublisherPort
end

    Bus->>Sub: OrderActivatedEvent {orderId, specCodes?}
    Sub->>Sub: walidacja (orderId niepusty)
    Sub->>App: allocateVehicleForOrder(orderId, specCodes)

    opt Zdarzenie nie niesie kodów wyposażenia
        App->>Spec: getSpecificationForOrder(orderId)
        note over Spec: ACL do Kontekstu Katalogu/Sprzedaży —<br/>kody wyposażenia (silnik, kolor, opcje)<br/>NIE pochodzą "z powietrza"
        Spec-->>App: List~specCodes~ (zatwierdzona specyfikacja)
    end

    App->>VRepo: findAll()
    VRepo-->>App: List~InventoryVehicle~

    App->>Alloc: tryLockExistingVehicle(orderId, vehicles, specCodes)

    alt Fast Track: pasujące auto jest na placu
        Alloc->>Veh: lockForOrder(orderId)
        note over Veh: ON_STOCK → RESERVED<br/>registerEvent(VehicleReservedFromStockEvent)
        Alloc-->>App: true
        App->>VRepo: save(vehicle)
        App->>Pub: publish(VehicleReservedFromStockEvent)
        note over Pub: trigger UC-FIR-02 (faktura końcowa)
    else Long Track: brak auta — slot produkcyjny
        Alloc-->>App: false
        App->>Alloc: createProductionSlot(orderId, specCodes)
        Alloc->>Slot: createForOrder(orderId, specCodes)
        note over Slot: zlecenie produkcyjne z kompletem kodów —<br/>fabryka wie, co wyprodukować
        Alloc-->>App: ProductionSlot
        App->>SRepo: save(slot)
        App->>Pub: publish(VehicleIsNotOnStockEvent)
        note over Pub: trigger UC-FIR-01 (prośba o zadatek)
    end
