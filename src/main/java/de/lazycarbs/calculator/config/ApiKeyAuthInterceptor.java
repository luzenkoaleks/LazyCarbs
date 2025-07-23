// START: src/main/java/de/lazycarbs/calculator/config/ApiKeyAuthInterceptor.java
package de.lazycarbs.calculator.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Ein Spring-Interceptor zur Authentifizierung von Anfragen mittels API-Key.
 * Überprüft, ob der 'X-API-Key'-Header in der Anfrage vorhanden und gültig ist.
 * Diese Version schützt nur PUT-Anfragen für Faktor-Endpunkte.
 */
@Component
public class ApiKeyAuthInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(ApiKeyAuthInterceptor.class);

    // Der API-Key wird aus den Anwendungseigenschaften oder Umgebungsvariablen geladen
    @Value("${api.key.secret}") // Diesen Wert müssen wir in application.properties setzen
    private String apiSecretKey;

    // Der Name des Headers, in dem der API-Key erwartet wird
    private static final String API_KEY_HEADER = "X-API-Key";

    /**
     * Diese Methode wird vor der eigentlichen Handler-Methode (Controller-Methode) aufgerufen.
     * Sie überprüft den API-Key nur für bestimmte geschützte Operationen (z.B. PUT für Faktoren).
     * @param request Das aktuelle HTTP-Request-Objekt.
     * @param response Das aktuelle HTTP-Response-Objekt.
     * @param handler Der Handler (Controller-Methode), der ausgeführt werden soll.
     * @throws Exception bei Fehlern.
     * @return true, wenn die Anfrage fortgesetzt werden soll (API-Key gültig oder nicht benötigt); false, wenn die Anfrage abgelehnt werden soll.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // CORS Preflight-Anfragen (OPTIONS) sollen immer durchgelassen werden, da sie keine Authentifizierung enthalten
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            logger.debug("OPTIONS-Anfrage durchgelassen: {}", request.getRequestURI());
            return true;
        }

        String requestUri = request.getRequestURI();
        String httpMethod = request.getMethod();
        String remoteAddr = request.getRemoteAddr();

        logger.info("API-Key Interceptor: Bearbeite Anfrage {} {} von {}", httpMethod, requestUri, remoteAddr);
        logger.info("Konfigurierter apiSecretKey im Interceptor: {}", apiSecretKey != null && !apiSecretKey.isEmpty() ? "[SET]" : "[NOT SET]");


        // Prüfe, ob der API-Key für diese Anfrage erforderlich ist
        // API-Key ist erforderlich für PUT-Anfragen an Bolus-Faktoren und Kalorien-Faktoren
        boolean isProtectedPutRequest = ("PUT".equalsIgnoreCase(httpMethod) &&
                (requestUri.startsWith("/api/bolus-factors") || requestUri.startsWith("/api/calorie-factors")));

        // GET-Anfragen an /api/bolus-factors und /api/calorie-factors sind UNGESCHÜTZT.
        // POST /api/calculate wird im CalculatorController selbst geprüft (enableDatabaseStorage).

        if (isProtectedPutRequest) {
            logger.info("Geschützte PUT-Anfrage erkannt: {}", requestUri);

            // Stelle sicher, dass der geheime API-Key im Backend konfiguriert ist
            if (apiSecretKey == null || apiSecretKey.isEmpty()) {
                logger.error("API Secret Key ist im Backend nicht konfiguriert. Kann API-Key-Validierung nicht durchführen.");
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write("Server Error: API Key nicht konfiguriert.");
                return false;
            }

            String requestApiKey = request.getHeader(API_KEY_HEADER);
            logger.info("Empfangener API-Key im Header '{}': {}", API_KEY_HEADER, requestApiKey != null && !requestApiKey.isEmpty() ? "[SET]" : "[EMPTY/NOT SET]");

            if (requestApiKey == null || !requestApiKey.equals(apiSecretKey)) {
                logger.warn("Ungültiger oder fehlender API-Key für geschützte Anfrage ({} {}). Erwartet: [GEHEIM], Erhalten: {}. Von IP: {}",
                        httpMethod, requestUri, requestApiKey != null && !requestApiKey.isEmpty() ? "[SET]" : "[EMPTY/NOT SET]", remoteAddr);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // HTTP 401 Unauthorized
                response.getWriter().write("Unauthorized: Ungültiger oder fehlender API-Key für diese Operation.");
                return false; // Anfrage ablehnen
            }
            logger.info("API-Key Authentifizierung erfolgreich für geschützte Anfrage ({} {}) von IP: {}", httpMethod, requestUri, remoteAddr);
        } else {
            logger.info("Anfrage {} {} ist nicht durch den Interceptor geschützt.", httpMethod, requestUri);
        }

        // Wenn die Anfrage nicht geschützt ist oder die Authentifizierung erfolgreich war, fortfahren
        return true;
    }
}
