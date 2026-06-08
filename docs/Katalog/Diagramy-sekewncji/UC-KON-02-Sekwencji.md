sequenceDiagram
autonumber
actor Klient
participant API as SpecificationAppService<br/>(BuildSpecificationUseCase)
participant RVS as RuleValidationDomainService
participant Spec as VehicleSpecification<br/>(Aggregate Root)
participant CatRepo as CatalogRepository
participant SpecRepo as SpecificationRepository
participant Bus as EventPublisherPort

    Note over Klient,Bus: Warunek wstępny: sesja zainicjowana (InitiateConfiguratorSession)

    Klient->>API: startSpecification(catalogId)
    API->>Spec: new VehicleSpecification(id, CatalogId)
    Note right of Spec: stan = DRAFT
    API->>SpecRepo: save(specification)
    API-->>Klient: SpecificationId

    loop Wybór pakietu / silnika / skrzyni / koloru
        Klient->>API: addOption(specId, catalogId, optionCode)
        API->>SpecRepo: findById(specId)
        SpecRepo-->>API: VehicleSpecification
        API->>RVS: validateAndAddOption(spec, OptionCode)
        RVS->>CatRepo: findById(catalogId)
        CatRepo-->>RVS: ProductCatalog
        RVS->>Spec: addOption(option, catalog)
        Note right of Spec: findOption + checkExclusions (EXCLUDES, Fail-fast)<br/>stan = IN_PROGRESS
        alt Kombinacja dozwolona
            Spec-->>RVS: ok
            RVS-->>API: ok
            API->>SpecRepo: save(specification)
            API-->>Klient: 200 OK
        else A1: Niedozwolona kombinacja (EXCLUDES)
            Spec--xRVS: RuleViolationException
            RVS--xAPI: RuleViolationException
            API-->>Klient: blad - zmien silnik/skrzynie/pakiet
            Note right of Spec: brak zapisu, brak zdarzenia
        end
    end

    alt Klient zatwierdza
        Klient->>API: finalizeSpecification(specId)
        API->>SpecRepo: findById(specId)
        SpecRepo-->>API: VehicleSpecification
        API->>RVS: assertComplete(spec)
        RVS->>CatRepo: findById(catalogId)
        CatRepo-->>RVS: ProductCatalog
        Note right of RVS: weryfikacja regul REQUIRES (kompletnosc)
        alt Konfiguracja kompletna
            RVS-->>API: ok
            API->>Spec: finalizeSpecification()
            Note right of Spec: stan = FINAL<br/>registerEvent(SpecificationCompletedEvent)
            API->>SpecRepo: save(specification)
            API->>Bus: publish(SpecificationCompletedEvent)
        else Brak wymaganej opcji (REQUIRES)
            RVS--xAPI: RuleViolationException
            API-->>Klient: blad - uzupelnij wymagane opcje
            Note right of Spec: pozostaje IN_PROGRESS
        end
    else A2: Przerwanie sesji
        Klient->>API: (opuszcza konfigurator)
        Note over Spec: pozostaje w stanie DRAFT (wersja robocza)
    end