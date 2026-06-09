package salon.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Korzeń kompozycji / punkt startowy Springa.
 *
 * Po przeniesieniu adapterów do warstw infrastruktury poszczególnych kontekstów
 * (salon.{kontekst}.infrastructure.*) cała aplikacja żyje już w pakiecie "salon".
 * Ten składnik leży w salon.bootstrap (Composition Root), który jest celowo wyłączony
 * z reguł ArchUnit (patrz HexagonalArchitectureTest.DoNotIncludeBootstrap).
 *
 * Ponieważ "salon.bootstrap" NIE jest pakietem nadrzędnym dla encji/repozytoriów
 * (leżą w salon.*.infrastructure.persistence), skanowanie JPA wskazujemy jawnie:
 *  - @EntityScan("salon")          — wykrywa @Entity w całym drzewie salon.*,
 *  - @EnableJpaRepositories("salon") — wykrywa repozytoria Spring Data,
 *  - scanBasePackages = "salon"    — component-scan adapterów (@Component/@RestController itd.).
 *
 * Kod kontekstów (domena/aplikacja) pozostaje nietknięty — to wyłącznie spinacz infrastruktury.
 */
@SpringBootApplication(scanBasePackages = "salon")
@EntityScan("salon")
@EnableJpaRepositories("salon")
public class SalonApplication {
    public static void main(String[] args) {
        SpringApplication.run(SalonApplication.class, args);
    }
}
