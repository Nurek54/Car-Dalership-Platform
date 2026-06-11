sequenceDiagram
autonumber
participant Ext as System zewnętrzny<br/>producenta/importera (Blackbox)
participant Cron as CatalogUpdateCronJobAdapter /<br/>webhook «driving adapter»
participant API as CatalogAppService<br/>(UpdateCatalogUseCase)
participant Acl as ImporterApiPort<br/>«out port, ACL»
participant Cat as ProductCatalog<br/>(Aggregate Root)
participant Old as ProductCatalog (poprzednia wersja)
participant CatRepo as CatalogRepository
participant Bus as EventPublisherPort

    Note over Ext,Bus: UC-KON-02 — Automatyczna aktualizacja cennika i katalogu

    Ext-->>Cron: nowy pakiet danych katalogowych (webhook / szyna / API)
    Cron->>API: publishNewCatalogVersion(modelYear, previousCatalogId)

    API->>Acl: fetchCurrentOptions(modelYear)
    Note right of Acl: warstwa translacji (ACL): zagnieżdżone struktury<br/>zewnętrzne -> CatalogOption / CatalogRule (krok 2)
    alt A1: błąd translacji lub walidacji danych (np. brak cen, zły format)
        Acl--xAPI: wyjątek translacji / walidacja odrzuca pakiet
        API->>Bus: publish(CatalogUpdateFailedEvent {modelYear, reason})
        Note right of Bus: aktualizacja przerwana, pakiet odrzucony,<br/>szczegóły w logu dla wsparcia IT
    else Główny: pakiet poprawny
        Acl-->>API: List~CatalogOption~ (+ reguły)
        API->>API: walidacja strukturalna i logiczna pakietu (krok 3)

        API->>CatRepo: findById(previousCatalogId)
        CatRepo-->>API: ProductCatalog (ACTIVE)
        API->>Old: archive()
        Note right of Old: ACTIVE -> ARCHIVED — stare wersje niemutowalne,<br/>historyczne specyfikacje zachowują spójność
        API->>CatRepo: save(stary katalog)

        API->>Cat: ProductCatalog.createActive(modelYear) + addOption/addRule
        Note right of Cat: NOWA instancja agregatu z podbitą wersją<br/>registerEvent(CatalogUpdatedEvent)
        API->>CatRepo: save(nowy katalog)

        API->>Bus: publish(CatalogUpdatedEvent)
        Note right of Bus: Sprzedaż/Konfigurator odświeżają widoki (krok 5)
    end
