flowchart TB

%% =====================================================
%% STYLE DEFINITIONS (Tylko warstwy wewnętrzne)
%% =====================================================
classDef port fill:#FCF3CF,stroke:#7D6608,color:#000,stroke-width:2px
classDef app fill:#D5F5E3,stroke:#145A32,color:#000,stroke-width:2px
classDef domain fill:#FADBD8,stroke:#922B21,color:#000,stroke-width:2px
classDef repository fill:#EBDEF0,stroke:#5B2C6F,color:#000,stroke-width:2px
classDef factory fill:#D6EAF8,stroke:#1B4F72,color:#000,stroke-width:2px

%% =====================================================
%% PAYMENT FLOW
%% =====================================================
subgraph PAYMENT["💳 Payment Processing"]
direction TB

    PPort[RegisterPaymentUseCase]
    PApp[PaymentAppService]

    PDomainSvc[PaymentClassificationService]
    POrderPort[[OrderInformationPort]]

    Payment[(Payment)]

    PRepo[[PaymentRepository]]
    PEvent[[EventPublisher]]

    PPort --> PApp

    PApp --> PDomainSvc
    PApp --> Payment

    PDomainSvc -. queries rules .-> POrderPort
    PDomainSvc -. verifies amount .-> Payment

    PApp --> PRepo
    PApp --> PEvent

end

%% =====================================================
%% DOCUMENT FLOW
%% =====================================================
subgraph DOCUMENT["📄 Document Issuing"]
direction TB

    DPort[IssueDocumentUseCase]
    DApp[DocumentAppService]
    Document[(AccountingDocument)]
    DocumentFactory[(AccountingDocumentFactory)]
    DRepo[[DocumentRepository]]

    DPort --> DApp
    DApp --> DocumentFactory
    DocumentFactory --> Document
    DApp --> DRepo

end

%% =====================================================
%% SETTLEMENT FLOW
%% =====================================================
subgraph SETTLEMENT["🧾 Settlement Calculation"]
direction TB

    SPort[CalculateSettlementUseCase]
    SApp[SettlementAppService]

    SDomainSvc[SettlementCalculationService]
    SFinPort[[FinancingIntegrationPort]]

    Settlement[(OrderSettlement)]
    OrderSettlementFactory[(OrderSettlementFactory)]

    SRepo[[SettlementRepository]]

    SPort --> SApp

    SApp --> SDomainSvc
    SApp --> OrderSettlementFactory
    OrderSettlementFactory --> Settlement

    SDomainSvc -. fetches leasing values .-> SFinPort
    SDomainSvc -. calculates balance for .-> Settlement

    SApp --> SRepo

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
class PPort,DPort,SPort port
class PApp,DApp,SApp app
class PDomainSvc,SDomainSvc,Payment,Document,Settlement,Event domain
class PRepo,DRepo,SRepo,PEvent,POrderPort,SFinPort repository
class OrderSettlementFactory,DocumentFactory factory
