package salon.sales.application.port.in;

import salon.sales.application.command.StartConfiguratorSessionCommand;

/**
 * Port wejściowy dla UC-CRM-01 (uruchomienie sesji konfiguratora) —
 * węzeł "StartConfiguratorUseCase" w docs/Architecture/SalesArchitecture.md (PDF rozdz. 3.3.3).
 *
 * Bezstanowy wyzwalacz: nie tworzy żadnych agregatów — emituje jedynie zdarzenie
 * inicjujące sesję w Kontekście Katalogu i Konfiguratora.
 */
public interface StartConfiguratorUseCase {

    /** Zwraca identyfikator nowo otwartej sesji konfiguratora. */
    String startConfiguratorSession(StartConfiguratorSessionCommand command);
}
