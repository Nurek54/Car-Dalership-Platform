package salon.catalog.infrastructure.config;

import salon.catalog.application.domain.model.catalog.ProductCatalogFactory;
import salon.catalog.application.domain.model.specification.VehicleSpecificationFactory;
import salon.catalog.application.domain.service.RuleValidationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Warstwa konfiguracji (infrastruktura) – neutralny komponent składający system
 * z części przez wstrzykiwanie zależności.
 *
 * Model dziedziny (fabryki, usługa dziedziny) jest WOLNY od zależności frameworkowych
 * (brak adnotacji Spring), dlatego jego egzemplarze tworzymy tutaj i udostępniamy jako
 * ziarna. Klasa należy do warstwy infrastruktury (salon.catalog.infrastructure.config),
 * która – zgodnie z architekturą heksagonalną – może odwoływać się do warstwy aplikacji
 * i dziedziny. Adaptery i usługi aplikacji są wykrywane przez skanowanie komponentów.
 */
@Configuration
public class DomainBeansConfiguration {

    @Bean
    public ProductCatalogFactory productCatalogFactory() {
        return new ProductCatalogFactory();
    }

    @Bean
    public VehicleSpecificationFactory vehicleSpecificationFactory() {
        return new VehicleSpecificationFactory();
    }

    @Bean
    public RuleValidationService ruleValidationService() {
        return new RuleValidationService();
    }

    @Bean
    public Clock systemClock() {
        return Clock.systemUTC();
    }
}
