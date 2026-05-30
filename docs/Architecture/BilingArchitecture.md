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

    Payment[(Payment Aggregate)]

    PRepo[[PaymentRepository]]
    PEvent[[EventPublisher]]

    PDb[(DatabaseAdapter)]
    PBus[(EventBusAdapter)]

    PRest --> PPort
    PPort --> PApp

    PApp --> PDomainSvc
    PApp --> Payment

    PApp --> PRepo
    PApp --> PEvent

    PDb -. implements .-> PRepo
    PBus -. implements .-> PEvent

end

%% =====================================================
%% DOCUMENT FLOW
%% =====================================================

subgraph DOCUMENT["📄 Document Issuing"]
direction TB

    DRest[BillingRestAdapter]
    DPort[IssueDocumentUseCase]

    DApp[DocumentAppService]

    Document[(AccountingDocument)]

    DRepo[[DocumentRepository]]

    DDb[(DatabaseAdapter)]

    DRest --> DPort
    DPort --> DApp

    DApp --> Document
    DApp --> DRepo

    DDb -. implements .-> DRepo

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

    Settlement[(OrderSettlement)]

    SRepo[[SettlementRepository]]
    SExternal[[ExternalIntegrationPort]]

    SDb[(DatabaseAdapter)]
    SIntegration[(SystemIntegrationAdapter)]

    SRest --> SPort
    SPort --> SApp

    SApp --> SDomainSvc
    SApp --> Settlement

    SApp --> SRepo
    SApp --> SExternal

    SDb -. implements .-> SRepo
    SIntegration -. implements .-> SExternal

end

%% =====================================================
%% DOMAIN EVENTS
%% =====================================================

Event{{DomainEvent}}

Payment -. generates .-> Event
Document -. generates .-> Event

%% =====================================================
%% NODE COLORS
%% =====================================================

class PRest,DRest,SRest inbound

class PPort,DPort,SPort port

class PApp,DApp,SApp app

class PDomainSvc,SDomainSvc,Payment,Document,Settlement,Event domain

class PRepo,DRepo,SRepo,PEvent,SExternal repository

class PDb,PBus,DDb,SDb,SIntegration adapter
