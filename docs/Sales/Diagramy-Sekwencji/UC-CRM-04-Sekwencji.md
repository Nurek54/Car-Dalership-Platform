sequenceDiagram
autonumber
participant Inv as Inwentarz / RabbitMQ
participant Sub as SalesEventSubscriberAdapter
participant Sales as SalesAppService<br/>«ScheduleHandoverUseCase»
participant Repo as OrderRepository
participant Order as Order<br/>«aggregate root»
participant Pub as EventPublisherPort
participant Notif as NotificationIntegrationPort<br/>«out port»
actor H as Handlowiec

    note over Inv,H: UC-CRM-04 — Obsługa zaproszenia klienta po odbiór
    rect rgb(245,245,245)
    note right of Inv: kroki 1-2: pojazd gotowy fizycznie i finansowo
    Inv-->>Sub: onVehicleReadyForHandover(VehicleReadyForHandoverEvent{eventId, vin, orderId})
    Sub->>Sales: markOrderAsReadyForHandover(OrderId)
    Sales->>Repo: findById(OrderId)
    Repo-->>Sales: Order
    Sales->>Order: markAsReadyForHandover()
    Order->>Order: IN_PROGRESS -> READY_FOR_HANDOVER ("Gotowe do odbioru")&#59; registerEvent(OrderReadyForHandoverEvent)
    Sales->>Repo: save(Order)
    Sales->>Pub: publishAll([OrderReadyForHandoverEvent])
    Pub->>Notif: (handler) sendAlertToSalesperson(orderId, "Zamówienie jest gotowe do wydania...")
    Notif-->>H: powiadomienie "Gotowe do odbioru — umów termin z klientem"
    end

    note right of H: krok 3: kontakt z klientem (tel./mail)
    H->>Sales: scheduleHandover(ScheduleHandoverCommand{orderId, handoverDate})
    Sales->>Repo: findById(OrderId)
    Repo-->>Sales: Order
    note over Sales: walidacja wejścia: data z przeszłości ->\nIllegalArgumentException "Handover date cannot be in the past"
    Sales->>Order: scheduleHandover(handoverDate)
    Order->>Order: READY_FOR_HANDOVER -> HANDOVER_SCHEDULED ("Umówiony na odbiór")&#59; handoverDate = date
    Sales->>Repo: save(Order)
    note over H,Order: A1 (odroczony odbiór): ta sama ścieżka z późniejszą datą — auto czeka na placu.
