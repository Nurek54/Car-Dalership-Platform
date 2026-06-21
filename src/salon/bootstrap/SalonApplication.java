package salon.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Composition root / Spring entry point.
 *
 * After moving the adapters into the infrastructure layers of the individual contexts
 * (salon.{context}.infrastructure.*) the whole application now lives in the "salon" package.
 * This component resides in salon.bootstrap (Composition Root), which is intentionally excluded
 * from the ArchUnit rules (see HexagonalArchitectureTest.DoNotIncludeBootstrap).
 *
 * Because "salon.bootstrap" is NOT a parent package for entities/repositories
 * (they reside in salon.*.infrastructure.persistence), we specify the JPA scan explicitly:
 *  - @EntityScan("salon")          — detects @Entity across the whole salon.* tree,
 *  - @EnableJpaRepositories("salon") — detects Spring Data repositories,
 *  - scanBasePackages = "salon"    — component-scan of adapters (@Component/@RestController etc.).
 *
 * The context code (domain/application) stays untouched — this is purely an infrastructure connector.
 */
@SpringBootApplication(scanBasePackages = "salon")
@EntityScan("salon")
@EnableJpaRepositories("salon")
public class SalonApplication {
    public static void main(String[] args) {
        SpringApplication.run(SalonApplication.class, args);
    }
}
