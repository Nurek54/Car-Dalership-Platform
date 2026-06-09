sequenceDiagram
autonumber
actor H as Handlowiec
participant REST as OrderRestApiAdapter
participant OrderSvc as OrderAppService
participant Repo as OrderRepository
participant Order as Order
participant Pub as EventPublisherPort
participant Inv as Inwentarz (subskrybent)
participant Sub as LogisticsEventSubscriberAdapter

    note over H,Order: Warunek wstępny: zamówienie w stanie HANDOVER_SCHEDULED ("Umówiony na odbiór")
    H->>REST: POST /api/sales/orders/{orderId}/handover
    REST->>OrderSvc: completeHandover(orderId)
    OrderSvc->>Repo: findById(OrderId)
    Repo-->>OrderSvc: Order
    OrderSvc->>Order: completeHandover()
    Order->>Order: HANDOVER_SCHEDULED -> COMPLETED&#59; registerEvent(VehicleHandedOverEvent)
    OrderSvc->>Repo: save(Order)
    OrderSvc->>Pub: publish(VehicleHandedOverEvent)
    Pub-->>Inv: VehicleHandedOverEvent  [komenda ReleaseVehicle — zwolnienie pojazdu]
    REST-->>H: 204 No Content

    alt Inwentarz odmawia zwolnienia (blokada magazynowa)
        Inv-->>Sub: handleVehicleInventoryReleasedError(VehicleInventoryReleasedError{orderId, reason})
        Sub->>Sub: walidacja + powiadomienie Handlowca o blokadzie
        Sub->>OrderSvc: revertHandoverOnInventoryError(orderId)
        OrderSvc->>Repo: findById(OrderId)
        Repo-->>OrderSvc: Order
        OrderSvc->>Order: revertToReadyForHandover()
        Order->>Order: COMPLETED -> READY_FOR_HANDOVER
        OrderSvc->>Repo: save(Order)
    else Sukces: pojazd zwolniony
        Inv-->>Inv: zamówienie pozostaje COMPLETED ("Zrealizowane")
    end