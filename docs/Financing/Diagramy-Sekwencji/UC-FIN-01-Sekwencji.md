sequenceDiagram
autonumber
box lightblue Adapter wejściowy (zdarzeniowy)
participant Listener as FinancingEventListener
end
box lightgreen Warstwa aplikacji
participant App as FinancingAppService
end
box pink Warstwa dziedziny
participant Fac as FinancingApplicationFactory
participant Appl as FinancingApplication
end
box lavender Adapter wyjściowy (ACL banku)
participant Bank as BankIntegrationAclPort
end
box lavender Adaptery wyjściowe (wewn.)
participant Repo as FinancingRepository
participant Bus as EventPublisher
end

    Listener->>App: on(FinancingRequestedEvent {orderId, customerId})
    App->>Fac: createFor(orderId, customerId)
    note over Fac: walidacja parametrów wejściowych
    Fac-->>App: FinancingApplication (DRAFT)
    App->>Appl: submitApplication()
    note over Appl: DRAFT → PENDING
    App->>Repo: save(application)
    App->>Bank: submitApplication(appId, customerId)
    note over Bank: ACL tłumaczy dane na format API banku (JSON/XML)
    alt A1: Błąd komunikacji z bankiem
        Bank-->>App: BankUnavailableException
        note over App: zawieszenie procesu i ponowienie próby
    else A2: Bank odrzuca walidację (np. błędny NIP)
        Bank-->>App: ValidationError
        App->>Bus: publish(FinancingApplicationFailed)
    else Główny: żądanie przyjęte do weryfikacji
        Bank-->>App: ACK (oczekiwanie na decyzję)
        App-->>Listener: wniosek w stanie PENDING
    end