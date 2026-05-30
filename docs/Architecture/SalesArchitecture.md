flowchart TB

%% =====================================================
%% STYLE DEFINITIONS
%% =====================================================
classDef inbound fill:#D6EAF8,stroke:#1B4F72,color:#000,stroke-width:2px
classDef port fill:#FCF3CF,stroke:#7D6608,color:#000,stroke-width:2px
classDef app fill:#D5F5E3,stroke:#145A32,color:#000,stroke-width:2px
classDef domain fill:#FADBD8,stroke:#922B21,color:#000,stroke-width:2px
classDef repository fill:#EBDEF0,stroke:#5B2C6F,color:#000,stroke-width:2px
classDef adapter fill:#EAECEE,stroke:#424949,color:#000,stroke-width:2px

%% =====================================================
%% PAYMENT FLOW
%% =====================================================
subgraph PAYMENT["💳 Payment Processing"]
direction TB

    PRest[BillingRestAdapter]
    PPort[RegisterPaymentUseCase]
    PApp[PaymentAppService]

    PDomainSvc[PaymentClassificationService]
    POrderPort[[OrderInformationPort]]

    Payment[(Payment)]

    PRepo[[PaymentRepository]]
    PEvent[[EventPublisher]]

    PDb[(DatabaseAdapter)]
    PBus[(EventBusAdapter)]
    PSalesApi[(SalesModuleRestAdapter)]

    PRest --> PPort
    PPort --> PApp

    PApp --> PDomainSvc
    PApp --> Payment

    %% Relacje Usługi Dziedziny (po angielsku)
    PDomainSvc -. queries rules .-> POrderPort
    PDomainSvc -. verifies amount .-> Payment

    PApp --> PRepo
    PApp --> PEvent

    PDb -. implements .-> PRepo
    PBus -. implements .-> PEvent
    PSalesApi -. implements .-> POrderPort

end

%% =====================================================
%% SETTLEMENT FLOW
%% =====================================================
subgraph SETTLEMENT["🧾 Settlement Calculation"]
direction TB

    SRest[BillingRestAdapter]
    SPort[CalculateSettlementUseCase]
    SApp[SettlementAppService]

    SDomainSvc[SettlementCalculationService]
    SFinPort[[FinancingIntegrationPort]]

    Settlement[(OrderSettlement)]

    SRepo[[SettlementRepository]]

    SDb[(DatabaseAdapter)]
    SFinApi[(FinancingACLAdapter)]

    SRest --> SPort
    SPort --> SApp

    SApp --> SDomainSvc
    SApp --> Settlement

    %% Relacje Usługi Dziedziny (po angielsku)
    SDomainSvc -. fetches leasing values .-> SFinPort
    SDomainSvc -. calculates balance for .-> Settlement

    SApp --> SRepo

    SDb -. implements .-> SRepo
    SFinApi -. implements .-> SFinPort

end

%% =====================================================
%% NODE COLORS
%% =====================================================
class PRest,SRest inbound
class PPort,SPort port
class PApp,SApp app
class PDomainSvc,SDomainSvc,Payment,Settlement domain
class PRepo,SRepo,PEvent,POrderPort,SFinPort repository
class PDb,PBus,SDb,PSalesApi,SFinApi adapter
