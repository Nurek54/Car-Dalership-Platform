package arch;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.core.importer.Location;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noFields;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(
        packages = "salon",
        importOptions = {ImportOption.DoNotIncludeTests.class, HexagonalArchitectureTest.DoNotIncludeBootstrap.class})
class HexagonalArchitectureTest {

    /**
     * Korzeń kompozycji (salon.bootstrap.* — uruchamiane mainy: SalonDemo/MessagingDemo) celowo
     * spina ze sobą wszystkie warstwy (to jest zadanie "Composition Root"/wiring). Nie podlega on
     * regułom warstw heksagonalnych, więc wykluczamy go z analizy — zgodnie z dokumentacją architektury.
     */
    static final class DoNotIncludeBootstrap implements ImportOption {
        @Override
        public boolean includes(Location location) {
            return !location.contains("salon/bootstrap/") && !location.contains("salon\\bootstrap\\");
        }
    }

    @ArchTest
    static final ArchRule strictlyEnforceHexagonalArchitecture = layeredArchitecture()
            .consideringAllDependencies()
            // Sprawdzi on automatycznie: salon.billing.domain, salon.sales.domain itd.
            .layer("Domain").definedBy("..domain..")
            .layer("Application").definedBy("..application..")
            .layer("Infrastructure").definedBy("..infrastructure..")

            // Definiowanie kierunku zależności
            .whereLayer("Infrastructure").mayOnlyBeAccessedByLayers("Infrastructure")
            .whereLayer("Application").mayOnlyBeAccessedByLayers("Infrastructure", "Application")
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure", "Domain");

//            // SERWIS DOMENOWY (..domain.service..) sam pobiera agregat z repozytorium (port wyjściowy w
//            // warstwie aplikacji), aby walidacja reguł (Fail-fast) działała w domenie.
//            .ignoreDependency(resideInAPackage("..domain.service.."), resideInAPackage("..application.."));

    @ArchTest
    static final ArchRule domainShouldNotDependOnExternalFrameworks = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAPackage("org.springframework..")
            .orShould().dependOnClassesThat().resideInAPackage("jakarta.persistence..")
            .because("W warstwie domeny nie powinno być kodu frameworków!");

    @ArchTest
    static final ArchRule allContextsShouldBeIndependent = slices()
            .matching("salon.(*).application.domain..")
            .should().notDependOnEachOther()
            .because("Konteksty powinny być izolowane. Używamy do tego Zdarzeń Domenowych!");

    @ArchTest       // porty muszą być interfejsami
    static final ArchRule portsShouldBeInterfaces = classes()
            .that().resideInAPackage("..application.port..")
            .should().beInterfaces()
            .because("Porty w architekturze heksagonalnej muszą być interfejsami!");

    @ArchTest       // zdarzenia domenowe muszą być rekordami
    static final ArchRule domainEventsShouldBeRecords = classes()
            .that().resideInAPackage("..domain.event..")
            .and().areTopLevelClasses()
            .should().beRecords()
            .because("Zdarzenia domenowe są niemutowalne!");

    @ArchTest       // komendy muszą być rekordami
    static final ArchRule commandsShouldBeRecords = classes()
            .that().resideInAPackage("..application.command..")
            .and().areTopLevelClasses()
            .should().beRecords()
            .because("Komendy (Command) to niemutowalne obiekty przenoszące intencję — powinny być rekordami!")
            .allowEmptyShould(true);

    @ArchTest       // wstrzykiwanie zależności tylko przez konstruktor
    static final ArchRule noFieldInjection = noFields()
            .should().beAnnotatedWith("org.springframework.beans.factory.annotation.Autowired")
            .because("Zabronione jest wstrzykiwanie przez pole — używamy wstrzykiwania przez konstruktor!")
            .allowEmptyShould(true);

    @ArchTest       // adaptery @Repository należą do infrastruktury
    static final ArchRule springRepositoriesShouldResideInInfrastructure = classes()
            .that().areAnnotatedWith("org.springframework.stereotype.Repository")
            .should().resideInAPackage("..infrastructure..")
            .because("Adaptery bazodanowe (@Repository) to szczegóły infrastruktury!")
            .allowEmptyShould(true);

    @ArchTest       // adaptery @Component należą do infrastruktury
    static final ArchRule springComponentsShouldResideInInfrastructure = classes()
            .that().areAnnotatedWith("org.springframework.stereotype.Component")
            .should().resideInAPackage("..infrastructure..")
            .because("Adaptery (@Component) to szczegóły infrastruktury, nie domeny ani aplikacji!")
            .allowEmptyShould(true);

    @ArchTest       // kontrolery REST to adaptery wejściowe (driving) w infrastrukturze
    static final ArchRule restControllersShouldResideInInfrastructureWeb = classes()
            .that().areAnnotatedWith("org.springframework.web.bind.annotation.RestController")
            .should().resideInAPackage("..infrastructure.in.web..")
            .because("Kontrolery REST to adaptery wejściowe — ich miejsce to infrastructure.in.web!")
            .allowEmptyShould(true);

    @ArchTest       // serwisy aplikacyjne (@Service) żyją w warstwie aplikacji
    static final ArchRule applicationServicesShouldResideInApplicationLayer = classes()
            .that().areAnnotatedWith("org.springframework.stereotype.Service")
            .should().resideInAPackage("..application..")
            .because("Serwisy aplikacyjne (@Service) należą do warstwy aplikacji!")
            .allowEmptyShould(true);

    @ArchTest       // domena nie może zależeć od infrastruktury
    static final ArchRule domainShouldNotDependOnInfrastructure = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
            .because("Domena musi być całkowicie niezależna od warstwy infrastruktury!");

    @ArchTest       // wyjątki domenowe muszą rozszerzać RuntimeException
    static final ArchRule domainExceptionsShouldBeUnchecked = classes()
            .that().resideInAPackage("..domain.exception..")
            .and().areTopLevelClasses()
            .should().beAssignableTo(RuntimeException.class)
            .because("Wyjątki domenowe powinny być niesprawdzane (RuntimeException), aby nie zaśmiecać sygnatur!")
            .allowEmptyShould(true);
}
