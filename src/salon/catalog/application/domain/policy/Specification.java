package salon.catalog.application.domain.policy;

/**
 * Wzorzec Specyfikacja (PDF, rozdz. 4): obiekt wartości warstwy dziedziny będący
 * predykatem – oddzielną regułą biznesową zwracającą wartość logiczną.
 *
 * Implementuje regułę będącą odpowiedzialnością więcej niż jednego typu
 * (tu: zależności między wybranymi opcjami a regułami katalogu).
 *
 * @param <T> typ obiektu sprawdzanego przez specyfikację
 */
public interface Specification<T> {

    boolean isSatisfiedBy(T candidate);
}
