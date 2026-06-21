package salon.catalog.application.domain.model.catalog;

/**
 * Standard type (enumeration) – a value object indicating the catalog state.
 *  - ACTIVE   – the currently effective catalog,
 *  - ARCHIVED – a catalog replaced by a newer version (UC-KON-02, step 4).
 */
public enum CatalogState {
    ACTIVE,
    ARCHIVED
}
