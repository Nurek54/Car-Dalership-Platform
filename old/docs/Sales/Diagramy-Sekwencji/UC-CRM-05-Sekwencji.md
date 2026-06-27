sequenceDiagram
autonumber
actor H as Handlowiec
participant REST as OrderRestApiAdapter
participant Sales as SalesAppService<br/>«ReleaseVehicleUseCase»
participant Repo as OrderRepository
participant Order as Order<br/>«aggregate root»
participant InvPort as InventoryIntegrationPort<br/>«out port»
participant BilPort as BillingIntegrationPort<br/>«out port»
participant Pub as EventPublisherPort
participant Sub as LogisticsEventSubscriberAdapter

    note over H,Sub: UC-CRM-05 — Rejestracja fizycznego wydania pojazdu
    note over H,Order: warunek wstępny: zamówienie READY_FOR_HANDOVER lub HANDOVER_SCHEDULED
    H->>REST: POST /api/sales/orders/{orderId}/handover
    REST->>Sales: confirmHandover(OrderId)
    Sales->>Repo: findById(OrderId)
    Repo-->>Sales: Order
    Sales->>Order: confirmHandover()
    Order->>Order: -> COMPLETED ("Zrealizowane")&#59;<br/>registerEvent(OrderCompletedEvent + VehicleHandedOverEvent)

    Sales->>InvPort: releasePhysicalVehicle(order.vehicleId)
    note over InvPort: komenda ReleaseVehicle do Inwentarza (krok 3, UC-INW-06)&#59;<br/>blokada magazynowa -> InventoryLockedException przerywa proces PRZED zapisem
    Sales->>BilPort: closeOrderBalance(orderId)
    note over BilPort: Rozliczenia domykają saldo końcowe (PUT /api/billing/accounts/{id}/close)

    Sales->>Repo: save(Order)
    Sales->>Pub: publishAll([OrderCompletedEvent, VehicleHandedOverEvent])
    note over Pub: routing: "order.completed", "vehicle.handed_over"<br/>(obsługa posprzedażowa + Rozliczenia)
    REST-->>H: 200 OK

    alt A1 — Odmowa Inwentarza (blokada magazynowa)
        Sub-->>Sales: revertHandoverOnInventoryError(orderId)<br/>(trigger: VehicleInventoryReleasedError{orderId, reason})
        Sales->>Repo: findById(OrderId)
        Repo-->>Sales: Order
        Sales->>Order: revertToReadyForHandover()
        Order->>Order: COMPLETED -> READY_FOR_HANDOVER (kompensata / saga)&#59; handoverDate = null
        Sales->>Repo: save(Order)
    else Sukces: pojazd zwolniony (VehicleInventoryReleased)
        Sub-->>Sub: zamówienie pozostaje COMPLETED ("Zrealizowane")
    end
