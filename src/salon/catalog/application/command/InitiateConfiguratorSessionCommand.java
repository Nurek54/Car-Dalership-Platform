package salon.catalog.application.command;

/**
 * Model danych wejściowych (polecenie) dla otwarcia sesji konfiguratora.
 *
 * Weryfikacja SYNTAKTYCZNA (niebiznesowa) odbywa się w konstruktorze – to
 * odpowiedzialność warstwy aplikacji, nie dziedziny (PDF, rozdz. 4 i 5).
 */
public record InitiateConfiguratorSessionCommand(int modelYear) {

    public InitiateConfiguratorSessionCommand {
        if (modelYear <= 0) {
            throw new IllegalArgumentException("modelYear musi być dodatni");
        }
    }
}
