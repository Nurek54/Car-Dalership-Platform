package acceptance.catalog_and_configurator_context;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Kotwica konfiguracji Springa dla testów akceptacyjnych kontekstu Katalogu i Konfiguratora
 * (pełny wycinek salon.catalog: serwisy aplikacyjne + adaptery, z zamockowanymi granicami
 * zewnętrznymi — ImporterServiceClient i MessageBroker).
 *
 * WAŻNE: skanowanie komponentów jest ograniczone WYŁĄCZNIE do {@code salon.catalog}. Pozostałe
 * konteksty są spinane w {@code salon.bootstrap.SalonWiringConfiguration} i ciągną za sobą
 * współpracowników nieistotnych tutaj; przeskanowanie całego drzewa {@code salon} uruchomiłoby
 * także tamte beany i kaskadowo wyrzucało niepowiązane błędy.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = "salon.catalog")
public class CatalogContextAcceptanceTestApplication {
}
