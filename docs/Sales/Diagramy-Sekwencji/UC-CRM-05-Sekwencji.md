sequenceDiagram
autonumber
participant Order as Order

    Note over Order: W kodzie istnieje wyłącznie metoda domenowa Order.completeHandover()&#59;<br/>żaden serwis aplikacyjny ani adapter jej nie wywołuje.
    Order->>Order: completeHandover()
    alt state == CANCELLED
        Order-->>Order: IllegalStateException
    else state == COMPLETED
        Order-->>Order: IllegalStateException
    else
        Order->>Order: state = COMPLETED&#59; registerEvent(VehicleHandedOverEvent)
    end