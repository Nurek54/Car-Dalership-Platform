flowchart TB

%% =====================================================
%% STYLE DEFINITIONS
%% =====================================================
classDef port fill:#FCF3CF,stroke:#7D6608,color:#000,stroke-width:2px
classDef app fill:#D5F5E3,stroke:#145A32,color:#000,stroke-width:2px
classDef domain fill:#FADBD8,stroke:#922B21,color:#000,stroke-width:2px
classDef repository fill:#EBDEF0,stroke:#5B2C6F,color:#000,stroke-width:2px
classDef factory fill:#D6EAF8,stroke:#1B4F72,color:#000,stroke-width:2px
classDef entity fill:#FDEDEC,stroke:#E74C3C,color:#000,stroke-width:2px,stroke-dasharray: 5 5

%% =====================================================
%% SETTLEMENT FLOW (UC-FIR-03)
%% =====================================================
subgraph SETTLEMENT [Rozliczenia]
direction TB

    SPort2[ProcessPaymentUseCase]

    SApp[SettlementAppService]

    SetFactory[(SettlementFactory)]
    Settlement[(Settlement)]

    SRepo[[SettlementRepository]]

    SPort2 --> SApp

    SApp --> SetFactory
    SetFactory -. creates .-> Settlement

    SApp --> SRepo

end

%% =====================================================
%% DOCUMENT FLOW (UC-FIR-01 & UC-FIR-02)
%% =====================================================
subgraph DOCUMENT [Wystawianie Faktur]
direction TB

    DPort1[GenerateAdvanceUseCase]
    DPort2[GenerateInvoiceUseCase]

    DApp[DocumentAppService]

    DDomainSvc[InvoiceCalculationDomainService]

    DocFactory[(AccountingDocumentFactory)]
    Document[(AccountingDocument)]

    DRepo[[DocumentRepository]]
    DPdf[[PdfGeneratorPort]]
    DNotif[[NotificationPort]]

    %% DODANY PORT DO CRM
    CrmPort[[CrmIntegrationPort]]

    DPort1 --> DApp
    DPort2 --> DApp

    %% Usługa aplikacyjna jako Orkiestrator:
    DApp --> DDomainSvc
    DApp --> DocFactory
    DApp --> Document

    %% Relacje wewnątrz-domenowe:
    DDomainSvc -. uses .-> Settlement
    DocFactory -. creates .-> Document

    %% Wyjścia:
    DApp --> DRepo
    DApp --> DPdf
    DApp --> DNotif
    DApp --> CrmPort

end

%% =====================================================
%% DOMAIN EVENTS
%% =====================================================
Event{{DomainEvent Bus}}
Settlement -. generates .-> Event
Document -. generates .-> Event

%% =====================================================
%% NODE COLORS
%% =====================================================
class SPort1,SPort2,DPort1,DPort2 port
class SApp,DApp app
class Settlement,Document,Event,DDomainSvc domain
class PaymentEntity entity
class SRepo,DRepo,DPdf,DNotif,CrmPort repository
class SetFactory,DocFactory factory
