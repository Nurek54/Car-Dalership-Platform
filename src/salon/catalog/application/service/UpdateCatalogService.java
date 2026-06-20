package salon.catalog.application.service;

import salon.catalog.application.port.in.UpdateCatalog;
import salon.catalog.application.port.out.CatalogImporterPort;
import salon.catalog.application.dto.ImportedCatalogData;
import salon.catalog.application.port.out.EventPublisher;
import salon.catalog.application.port.out.ProductCatalogRepository;
import salon.catalog.application.domain.exception.CatalogValidationException;
import salon.catalog.application.domain.model.catalog.ProductCatalogFactory;
import salon.catalog.application.domain.model.catalog.ProductCatalog;
import salon.catalog.application.domain.model.event.CatalogUpdateFailed;
import salon.catalog.application.domain.model.event.CatalogUpdated;
import salon.catalog.application.domain.service.RuleValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

/**
 * USŁUGA APLIKACJI (fasada przypadku użycia UC-KON-02) – realizacja portu
 * wejściowego {@link UpdateCatalog}.
 *
 * Orkiestracja: pobranie pakietu przez ACL → zbudowanie agregatu fabryką → walidacja
 * logiczna (usługa dziedziny) → archiwizacja bieżącego i zapis nowego katalogu w
 * jednej transakcji → emisja CatalogUpdated. W razie błędu walidacji/translacji
 * (scenariusz A1) odrzucenie pakietu i emisja CatalogUpdateFailed.
 */
@Service
public class UpdateCatalogService implements UpdateCatalog {

    private static final Logger log = LoggerFactory.getLogger(UpdateCatalogService.class);

    private final CatalogImporterPort catalogImporter;
    private final ProductCatalogRepository catalogRepository;
    private final RuleValidationService ruleValidationService;
    private final ProductCatalogFactory catalogFactory;
    private final EventPublisher eventPublisher;
    private final Clock clock;

    public UpdateCatalogService(CatalogImporterPort catalogImporter,
                                ProductCatalogRepository catalogRepository,
                                RuleValidationService ruleValidationService,
                                ProductCatalogFactory catalogFactory,
                                EventPublisher eventPublisher,
                                Clock clock) {
        this.catalogImporter = catalogImporter;
        this.catalogRepository = catalogRepository;
        this.ruleValidationService = ruleValidationService;
        this.catalogFactory = catalogFactory;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void update() {
        try {
            // Krok 1–2: pobranie i translacja pakietu (ACL realizuje tłumaczenie formatu).
            ImportedCatalogData data = catalogImporter.fetchLatestCatalog();

            // Krok 4 (przygotowanie): ustalenie kolejnej wersji względem bieżącego aktywnego katalogu.
            Optional<ProductCatalog> current = catalogRepository.findActiveByModelYear(data.modelYear());
            int nextVersion = current.map(c -> c.version() + 1).orElse(1);

            // Budowa agregatu fabryką – walidacja strukturalna (ceny, unikalność, istnienie opcji).
            ProductCatalog newCatalog = catalogFactory.createNextVersion(
                    data.modelYear(), nextVersion, data.options(), data.rules());

            // Krok 3: walidacja logiczna spójności reguł (usługa dziedziny).
            ruleValidationService.validateCatalogConsistency(newCatalog);

            // Krok 4: zapis nowego katalogu; poprzedni oznaczany jako archiwalny.
            current.ifPresent(c -> {
                c.archive();
                catalogRepository.save(c);
            });
            catalogRepository.save(newCatalog);

            // Krok 5: emisja zdarzenia na szynę danych.
            eventPublisher.publish(new CatalogUpdated(
                    newCatalog.id(), newCatalog.modelYear(), newCatalog.version(), Instant.now(clock)));

            log.info("Katalog zaktualizowany: {} (rocznik {}, wersja {})",
                    newCatalog.id(), newCatalog.modelYear(), newCatalog.version());

        } catch (CatalogValidationException | IllegalArgumentException e) {
            // Scenariusz A1: błąd krytyczny – odrzucenie pakietu, log dla wsparcia IT.
            log.error("Aktualizacja katalogu przerwana: {}", e.getMessage(), e);
            eventPublisher.publish(new CatalogUpdateFailed(e.getMessage(), Instant.now(clock)));
        }
    }
}
