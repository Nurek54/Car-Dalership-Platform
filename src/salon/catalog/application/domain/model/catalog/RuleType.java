package salon.catalog.application.domain.model.catalog;

/**
 * Typ standardowy (enumeracja) – rodzaj reguły zależności między opcjami.
 *  - REQUIRES – wybór opcji źródłowej wymaga obecności opcji docelowej,
 *  - EXCLUDES – wybór opcji źródłowej wyklucza opcję docelową
 *               (np. silnik B2 nie może być wybrany ze skrzynią C1 – UC-KON-01 / A1).
 */
public enum RuleType {
    REQUIRES,
    EXCLUDES
}
