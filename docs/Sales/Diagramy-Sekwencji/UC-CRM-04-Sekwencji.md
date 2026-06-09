sequenceDiagram
autonumber
actor H as Handlowiec
participant Sales as SalesAppService
participant Cfg as ConfiguratorAppService
participant Pub as EventPublisherPort
participant Cat as Katalog (subskrybent)

    H->>Sales: startConfiguratorSession(StartConfiguratorSessionCommand{customerId, salespersonId})
    Sales->>Cfg: startConfiguratorSession(command)
    Cfg->>Cfg: sessionId = "CFG-" + UUID
    Cfg->>Pub: publish(ConfiguratorSessionInitiatedEvent{sessionId, customerId, salespersonId})
    Pub-->>Cat: ConfiguratorSessionInitiatedEvent
    Cat-->>H: otwiera interfejs konfiguratora pojazdów
    Cfg-->>Sales: sessionId
    Sales-->>H: sessionId