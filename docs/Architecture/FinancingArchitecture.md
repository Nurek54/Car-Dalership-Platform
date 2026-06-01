flowchart TB

classDef port fill:#FCF3CF,stroke:#7D6608,color:#000,stroke-width:2px
classDef app fill:#D5F5E3,stroke:#145A32,color:#000,stroke-width:2px
classDef domain fill:#FADBD8,stroke:#922B21,color:#000,stroke-width:2px
classDef repository fill:#EBDEF0,stroke:#5B2C6F,color:#000,stroke-width:2px

%% =====================================================
%% FINANCING APPLICATION FLOW
%% =====================================================
subgraph FINANCING["🏦 Financing & Leasing Validation"]
direction TB

    FPort[ProcessFinancingUseCase]
    FApp[FinancingAppService]

    Financing[(FinancingApplication)]

    FRepo[[FinancingRepository]]
    FAclPort[[BankIntegrationAclPort]]
    FEventPort[[EventPublisher]]

    FPort --> FApp

    FApp --> Financing

    FApp --> FRepo
    FApp --> FAclPort
    FApp --> FEventPort

end

%% =====================================================
%% INSURANCE POLICY FLOW
%% =====================================================
subgraph INSURANCE["🛡️ Insurance & Residual Value"]
direction TB

    IPort[IssuePolicyUseCase]
    IApp[InsuranceAppService]

    IDomainSvc[ResidualValueCalculationService]
    Policy[(InsurancePolicy)]

    IRepo[[PolicyRepository]]
    IAclPort[[InsurerIntegrationAclPort]]

    IPort --> IApp

    IApp --> IDomainSvc
    IApp --> Policy
    IDomainSvc -. calculates residual value for .-> Policy

    IApp --> IRepo
    IApp --> IAclPort

end

%% =====================================================
%% DOMAIN EVENTS
%% =====================================================
Event{{DomainEvent}}
Financing -. generates .-> Event
Policy -. generates .-> Event

class FPort,IPort port
class FApp,IApp app
class Financing,Policy,IDomainSvc,Event domain
class FRepo,FAclPort,FEventPort,IRepo,IAclPort repository
