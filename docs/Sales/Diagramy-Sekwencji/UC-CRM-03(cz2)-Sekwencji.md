sequenceDiagram
autonumber
participant MQ as RabbitMQ
participant Listener as SalesDepositListener
participant Sales as SalesAppService<br/>«ActivateOrderOnDepositUseCase»
participant OrderRepo as OrderRepository
participant Order as Order<br/>«aggregate root»
participant Pub as EventPublisherPort

    note over MQ,Pub: UC-CRM-03 (cz.2) — Aktywacja po zadatku (Rys. 19/20 PDF)
    MQ-->>Listener: handle(event = PaymentRegisteredEvent {eventId, orderId})
    Listener->>Listener: processedEventIds.add(eventId)
    alt duplikat (eventId już przetworzony)
        Listener-->>Listener: log + return (idempotencja po stronie subskrybenta)
    else pierwsze wystąpienie
        Listener->>Sales: activateOnDeposit(orderId)
        Sales->>OrderRepo: findById(OrderId)
        OrderRepo-->>Sales: Optional<Order>
        alt order empty
            Sales-->>Sales: log "deposit ignored" + return
        else stan != DRAFT_CREATED/DRAFT
            Sales-->>Sales: log "already active" + return (kolejna wpłata)
        else
            Sales->>Order: activate()
            Order->>Order: DRAFT -> IN_PROGRESS ("W realizacji")&#59; registerEvent(OrderActivatedEvent)
            Sales->>OrderRepo: save(Order)
            Sales->>Pub: publishAll([OrderActivatedEvent])
            note over Pub: handler OrderActivatedEventHandler -><br/>ManufacturingIntegrationPort.startVehicleRealization (realizacja rusza)
        end
    end
