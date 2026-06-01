package salon.shared.infrastructure.messaging;

import java.util.HashMap;
import java.util.Map;

/**
 * Maleńki, "idiotoodporny" serializator/deserializator JSON dla naszych zdarzeń.
 *
 * Świadomie NIE używamy Jacksona ani innej biblioteki — nasze zdarzenia to płaskie rekordy
 * (same Stringi/UUID/Instant zapisane jako tekst), więc wystarczy płaska mapa klucz->wartość.
 * Dzięki temu cały moduł messaging zależy tylko od jednego JAR-a (amqp-client).
 *
 * Format na drucie:  {"type":"DepositRegisteredEvent","eventId":"...","orderId":"ORD-1", ...}
 */
public final class EventJson {

    private EventJson() {
    }

    // Budowa JSON-a z płaskiej mapy (wszystkie wartości traktujemy jako tekst).
    public static String write(Map<String, String> fields) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        boolean first = true;
        for (Map.Entry<String, String> entry : fields.entrySet()) {
            if (!first) {
                sb.append(",");
            }
            first = false;
            sb.append("\"").append(escape(entry.getKey())).append("\":");
            if (entry.getValue() == null) {
                sb.append("null");
            } else {
                sb.append("\"").append(escape(entry.getValue())).append("\"");
            }
        }
        sb.append("}");
        return sb.toString();
    }

    // Bardzo prosty parser płaskiego JSON-a do mapy. Zakłada format, który sami produkujemy.
    public static Map<String, String> read(String json) {
        Map<String, String> result = new HashMap<>();
        if (json == null) {
            return result;
        }
        String body = json.trim();
        if (body.startsWith("{")) {
            body = body.substring(1);
        }
        if (body.endsWith("}")) {
            body = body.substring(0, body.length() - 1);
        }
        // Dzielimy po przecinkach najwyższego poziomu (nasze wartości nie zawierają przecinków).
        String[] pairs = body.split(",");
        for (int i = 0; i < pairs.length; i++) {
            String pair = pairs[i].trim();
            if (pair.isEmpty()) {
                continue;
            }
            int colon = pair.indexOf(':');
            if (colon < 0) {
                continue;
            }
            String key = unquote(pair.substring(0, colon).trim());
            String rawValue = pair.substring(colon + 1).trim();
            if (rawValue.equals("null")) {
                result.put(key, null);
            } else {
                result.put(key, unquote(rawValue));
            }
        }
        return result;
    }

    private static String unquote(String text) {
        String t = text;
        if (t.startsWith("\"")) {
            t = t.substring(1);
        }
        if (t.endsWith("\"")) {
            t = t.substring(0, t.length() - 1);
        }
        return t.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private static String escape(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
