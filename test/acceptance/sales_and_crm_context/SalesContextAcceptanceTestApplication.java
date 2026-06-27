package acceptance.sales_and_crm_context;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import salon.sales.application.domain.model.offer.OfferFactory;
import salon.sales.application.domain.model.order.OrderFactory;

/**
 * Kotwica konfiguracji Springa dla testów akceptacyjnych kontekstu Sprzedaży
 * (pełny wycinek Sprzedaży: REST + JPA + szyna zdarzeń, z WireMockiem dla wychodzącego HTTP i zamockowanym brokerem).
 *
 * WAŻNE: skanowanie komponentów jest ograniczone WYŁĄCZNIE do {@code salon.sales}. Pozostałe konteksty
 * (Rozliczenia, Finansowanie, Logistyka, Katalog) są spinane w {@code salon.bootstrap.SalonWiringConfiguration}
 * i ciągną za sobą współpracowników nieistotnych tutaj; przeskanowanie całego drzewa {@code salon} uruchomiłoby
 * także tamte beany i kaskadowo wyrzucało niepowiązane błędy. Dlatego ładujemy tylko beany Sprzedaży i sami dostarczamy
 * dwie wolne od frameworka fabryki domenowe.
 */
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = "salon.sales")
@EntityScan(basePackages = "salon.sales.infrastructure.out.persistence")
@EnableJpaRepositories(basePackages = "salon.sales.infrastructure.out.persistence")
public class SalesContextAcceptanceTestApplication {

    @Bean
    public OfferFactory offerFactory() {
        return new OfferFactory();
    }

    @Bean
    public OrderFactory orderFactory() {
        return new OrderFactory();
    }
}
