flowchart LR

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
%% GENERIC HEXAGONAL ARCHITECTURE
%% =====================================================
subgraph HEXAGON["Ogólny Wzorzec Architektury Heksagonalnej (Dla każdego Kontekstu)"]
direction LR

    subgraph DRIVING["Adaptery Wejściowe"]
        direction LR
        UI["RestApiAdapter<br/>Spring @RestController"]:::inbound
        MQ["EventSubscriberAdapter<br/>RabbitMQ Listener"]:::inbound
        CR["CronJobAdapter<br/>Spring @Scheduled"]:::inbound
    end

    subgraph CORE["Jądro Systemu"]
        direction TB

        IPort(("Port Wejściowy<br/>Use Case / Inbound")):::port
        AppSvc["Usługa Aplikacji<br/>AppService"]:::app
        Dom["Model Dziedziny<br/>Agregaty, Usługi Dziedziny"]:::domain
        OPort(("Port Wyjściowy<br/>Repository / Outbound")):::repository

        IPort --> AppSvc
        AppSvc --> Dom
        AppSvc --> OPort
    end

    subgraph DRIVEN["Adaptery Wyjściowe"]
        direction LR
        DB["DatabaseAdapter<br/>PostgreSQL / JPA"]:::adapter
        EB["EventBusAdapter<br/>RabbitMQ Publisher"]:::adapter
        EXT["ExternalApiAdapter<br/>REST Client / ACL"]:::adapter
    end

    DRIVING -->|Wywołują| IPort
    OPort -.->|Implementowane przez| DRIVEN

end
