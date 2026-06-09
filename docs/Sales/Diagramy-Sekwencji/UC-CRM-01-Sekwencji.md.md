sequenceDiagram
autonumber
participant Inv as Inwentarz / RabbitMQ
participant Sub as LogisticsEventSubscriberAdapter
participant OrderSvc as OrderAppService
participant Repo as OrderRepository
participant Order as Order
participant Pub as EventPublisherPort
actor H as Handlowiec

    rect rgb(245,245,245)
    note right of Inv: Krok 1-2: pojazd gotowy fizycznie i finansowo
    Inv-->>Sub: handleVehicleReadyForHandover(VehicleReadyForHandoverEvent{eventId, orderId})
    Sub->>Sub: walidacja orderId
    Sub->>OrderSvc: markReadyForHandover(orderId)
    OrderSvc->>Repo: findById(OrderId)
    Repo-->>OrderSvc: Order
    OrderSvc->>Order: markAsReadyForHandover()
    Order->>Order: IN_PROGRESS -> READY_FOR_HANDOVER&#59; registerEvent(OrderReadyForHandoverEvent)
    OrderSvc->>Repo: save(Order)
    OrderSvc->>Pub: publish(OrderReadyForHandoverEvent)
    Pub-->>H: powiadomienie "Gotowe do odbioru"
    end

    note right of H: Krok 3: kontakt z klientem (tel./mail)
    H->>OrderSvc: scheduleHandover(ScheduleHandoverCommand{orderId, handoverDate})
    OrderSvc->>Repo: findById(OrderId)
    Repo-->>OrderSvc: Order
    OrderSvc->>Order: scheduleHandover(handoverDate)
    Order->>Order: READY_FOR_HANDOVER -> HANDOVER_SCHEDULED&#59; handoverDate = date
    OrderSvc->>Repo: save(Order)
    note over H,Order: A1 (odroczony odbiór)&#59; ta sama ścieżka z późniejszą datą — auto czeka na placu.