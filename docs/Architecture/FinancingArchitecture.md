flowchart TB
subgraph FINANCING["Financing & Leasing Validation"]
direction TB
FPort1["FinancingRequestUseCase"]
FApp["FinancingAppService"]
Financing[("FinancingApplication")]
FRepo[["FinancingRepository"]]
FAcl[["BankIntegrationAclPort"]]
FFactory["FinancingApplicationFactory"]
end
FPort1 --> FApp
FApp --> FRepo & FFactory & FAcl
FFactory -. creates .-> Financing
Financing -. generates .-> Event{{"DomainEvent Bus"}}

    FFactory@{ shape: cyl}
     FPort1:::port
     FApp:::app
     Financing:::domain
     FRepo:::repository
     FAcl:::repository
     FFactory:::factory
     Event:::domain
    classDef port fill:#FCF3CF,stroke:#7D6608,color:#000,stroke-width:2px
    classDef app fill:#D5F5E3,stroke:#145A32,color:#000,stroke-width:2px
    classDef domain fill:#FADBD8,stroke:#922B21,color:#000,stroke-width:2px
    classDef repository fill:#EBDEF0,stroke:#5B2C6F,color:#000,stroke-width:2px
    classDef factory fill:#D6EAF8,stroke:#1B4F72,color:#000,stroke-width:2px
    classDef entity fill:#FDEDEC,stroke:#E74C3C,color:#000,stroke-width:2px,stroke-dasharray: 5 5
