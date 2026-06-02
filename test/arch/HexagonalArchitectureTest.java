package arch;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

@AnalyzeClasses(packages = "salon", importOptions = ImportOption.DoNotIncludeTests.class)
class HexagonalArchitectureTest {

    @ArchTest
    static final ArchRule strictlyEnforceHexagonalArchitecture = layeredArchitecture()
            .consideringAllDependencies()
            // Zapis z dwiema kropkami "..domain.." to magia ArchUnit!
            // Sprawdzi on automatycznie: salon.billing.domain, salon.sales.domain itd.
            .layer("Domain").definedBy("..domain..")
            .layer("Application").definedBy("..application..")
            .layer("Infrastructure").definedBy("..infrastructure..")

            // Definiowanie kierunku zależności
            .whereLayer("Infrastructure").mayOnlyBeAccessedByLayers("Infrastructure")
            .whereLayer("Application").mayOnlyBeAccessedByLayers("Infrastructure", "Application")
            .whereLayer("Domain").mayOnlyBeAccessedByLayers("Application", "Infrastructure", "Domain");

    @ArchTest
    static final ArchRule domainShouldNotDependOnExternalFrameworks = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAPackage("org.springframework..")
            .orShould().dependOnClassesThat().resideInAPackage("jakarta.persistence..")
            .because("In Domain Layer there shouldn't be framework code!");

    @ArchTest
    static final ArchRule allContextsShouldBeIndependent = slices()
            .matching("salon.(*).domain..")
            .should().notDependOnEachOther()
            .because("Contexts should be isolated. We using Domain Events!");
}