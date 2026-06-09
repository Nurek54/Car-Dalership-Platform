package salon.sales.application.port.in;

/**
 * Port wejściowy dla UC-CRM-01 (Uruchomienie sesji konfiguratora dla klienta).
 * Zwraca identyfikator utworzonej sesji konfiguratora.
 */
public interface StartConfiguratorSessionUseCase {
    String startConfiguratorSession(StartConfiguratorSessionCommand command);
}
