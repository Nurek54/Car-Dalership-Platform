package salon.catalog.domain.model.catalog;

public enum CatalogState {
    SCHEDULED, // zaplanowany do aktywacji w przyszłości (aktywowany przez Cron, UC-KAT-02)
    ACTIVE,    // aktualny cennik
    ARCHIVED   // zarchiwizowany (po wydaniu nowej wersji)
}
