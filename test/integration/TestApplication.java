package integration;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;

/**
 * Kotwica konfiguracji dla testów wycinkowych (@DataJpaTest, @WebMvcTest).
 *
 * Produkcyjny korzeń kompozycji (salon.bootstrap.SalonApplication) leży poza drzewem
 * pakietów testów integracyjnych (integration.*), więc Spring Boot jej nie znajdzie
 * idąc w górę pakietu testowego. Ta klasa pełni rolę kotwicy dla testów wycinkowych.
 *
 * @ComponentScan("salon") pozwala @WebMvcTest wykryć kontrolery i @ControllerAdvice
 * w drzewie salon.*. Wycinki (@WebMvcTest, @DataJpaTest) stosują własne filtry typów,
 * więc nie ładują całego kontekstu — @WebMvcTest bierze tylko kontrolery i beany webowe,
 * @DataJpaTest tylko encje i repozytoria.
 *
 * excludeFilters wyklucza SalonApplication (@SpringBootApplication) ze skanowania,
 * aby uniknąć podwójnej rejestracji @EnableJpaRepositories w testach wycinkowych.
 */
@SpringBootConfiguration
@ComponentScan(
    value = "salon",
    excludeFilters = @ComponentScan.Filter(
        type = FilterType.ANNOTATION,
        classes = SpringBootApplication.class
    )
)
public class TestApplication {
}
