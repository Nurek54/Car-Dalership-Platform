sequenceDiagram
autonumber
box lightblue Adapter wejściowy (ACL banku, async)
participant Bank as BankIntegrationAclPort
end
box lightgreen Warstwa aplikacji
participant App as FinancingAppService
end
box pink Warstwa dziedziny
participant Appl as FinancingApplication
end
box lavender Adaptery wyjściowe (wewn.)
participant Repo as FinancingRepository
participant Bus as EventPublisher
end

    Bank->>App: on(FinancingDecisionReceivedFromBank {appId})
    App->>Repo: findById(appId)
    Repo-->>App: FinancingApplication (PENDING)
    App->>Bank: isApproved(appId)
    alt Główny: decyzja pozytywna
        Bank-->>App: true
        App->>Appl: approve()
        note over Appl: PENDING → APPROVED<br/>registerEvent(FinancingApprovedEvent)
        App->>Repo: save(application)
        App->>Appl: pullDomainEvents()
        Appl-->>App: [FinancingApprovedEvent]
        App->>Bus: publish(FinancingApprovedEvent)
    else A1: decyzja odmowna
        Bank-->>App: false
        App->>Appl: reject()
        note over Appl: PENDING → REJECTED<br/>registerEvent(FinancingRejectedEvent)
        App->>Repo: save(application)
        App->>Appl: pullDomainEvents()
        Appl-->>App: [FinancingRejectedEvent]
        App->>Bus: publish(FinancingRejectedEvent)
    end
    note over Appl: Podwójny webhook → approve()/reject() z innego stanu niż PENDING<br/>zostaje odrzucone (ochrona przed duplikacją zdarzeń)