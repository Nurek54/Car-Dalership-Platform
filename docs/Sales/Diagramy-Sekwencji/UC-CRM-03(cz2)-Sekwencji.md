sequenceDiagram
autonumber
participant MQ as RabbitMQ
participant Listener as SalesDepositListener
participant OrderSvc as OrderAppService
participant OrderRepo as OrderRepository
participant Order as Order
participant Pub as EventPublisherPort

    MQ-->>Listener: handle(event = DepositRegisteredEvent)
    Listener->>Listener: processedEventIds.add(eventId)
    alt duplikat (eventId już przetworzony)
        Listener-->>Listener: log + return
    else pierwsze wystąpienie
        Listener->>OrderSvc: activateOnDeposit(orderId)
        OrderSvc->>OrderRepo: findById(OrderId)
        OrderRepo-->>OrderSvc: Optional<Order>
        alt order empty
            OrderSvc-->>OrderSvc: log "deposit ignored" + return
        else
            OrderSvc->>Order: activate()
            Order->>Order: state = IN_PROGRESS&#59; registerEvent(OrderActivatedEvent)
            OrderSvc->>OrderRepo: save(Order)
            OrderSvc->>Pub: publish(OrderActivatedEvent)
        end
    end