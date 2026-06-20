package salon.catalog.application.domain.model.catalog;

/**
 * Typ standardowy (enumeracja) – obiekt wartości wskazujący stan katalogu.
 *  - ACTIVE   – katalog aktualnie obowiązujący,
 *  - ARCHIVED – katalog zastąpiony przez nowszą wersję (UC-KON-02, krok 4).
 */
public enum CatalogState {
    ACTIVE,
    ARCHIVED
}
