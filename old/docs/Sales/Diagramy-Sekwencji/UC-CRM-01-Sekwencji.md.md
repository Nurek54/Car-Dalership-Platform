sequenceDiagram
autonumber
actor H as Handlowiec
participant REST as SessionRestApiAdapter
participant Sales as SalesAppService<br/>«StartConfiguratorUseCase»
participant Pub as EventPublisherPort
participant Hnd as ConfiguratorSessionInitiatedEventHandler
participant Cat as CatalogIntegrationPort<br/>«out port»

    note over H,Cat: UC-CRM-01 — Uruchomienie sesji konfiguratora dla klienta
    H->>REST: POST /api/sales/sessions/initiate {customerId, salespersonId}
    REST->>Sales: startConfiguratorSession(StartConfiguratorSessionCommand)
    Sales->>Sales: sessionId = "CFG-" + UUID
    note right of Sales: bezstanowy wyzwalacz (PDF 3.3.3) —<br/>żaden agregat nie powstaje w bazie
    Sales->>Pub: publish(ConfiguratorSessionInitiatedEvent{sessionId, customerId, salespersonId})
    note over Pub: routing "configurator.session.initiated" (InitiateConfiguratorSession)
    Pub-->>Hnd: ConfiguratorSessionInitiatedEvent
    Hnd->>Cat: openConfiguratorInterface(sessionId, customerId, salespersonId)
    Cat-->>H: system otwiera interfejs konfiguratora pojazdów (warunek wstępny UC-KON-01)
    Sales-->>H: 200 OK {sessionId}
