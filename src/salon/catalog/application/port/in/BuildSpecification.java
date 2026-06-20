package salon.catalog.application.port.in;

import salon.catalog.application.command.AddOptionCommand;
import salon.catalog.application.command.FinalizeSpecificationCommand;
import salon.catalog.application.command.InitiateConfiguratorSessionCommand;
import salon.catalog.application.command.RemoveOptionCommand;
import salon.catalog.application.dto.SpecificationView;

/**
 * PORT WEJŚCIOWY (kontrakt) – „BuildSpecification” z diagramu portów i adapterów.
 *
 * Publikuje usługi świadczone przez kontekst dla aktorów inicjujących UC-KON-01
 * (zdarzenie InitiateConfiguratorSession z CRM oraz interfejs użytkownika).
 * Należy do warstwy aplikacji; implementowany przez usługę aplikacji.
 *
 * Zgodnie z zasadą segregacji interfejsów (I z SOLID) port ma jedno przeznaczenie:
 * opracowanie i zatwierdzenie specyfikacji pojazdu.
 */
public interface BuildSpecification {

    /** Krok 1: otwarcie sesji konfiguratora dla aktywnego katalogu danego rocznika. */
    SpecificationView initiate(InitiateConfiguratorSessionCommand command);

    /** Kroki 2–4: dobranie opcji z bieżącą weryfikacją reguł wykluczających. */
    SpecificationView addOption(AddOptionCommand command);

    /** Zmiana wyboru: usunięcie wcześniej dobranej opcji. */
    SpecificationView removeOption(RemoveOptionCommand command);

    /** Kroki 5–7: zatwierdzenie i emisja zdarzenia SpecificationCompleted. */
    SpecificationView finalizeSpecification(FinalizeSpecificationCommand command);
}
