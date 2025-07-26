// START: src/main/java/de/lazycarbs/calculator/config/WebMvcConfig.java
package de.lazycarbs.calculator.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

/**
 * Spring MVC Konfigurationsklasse zum Registrieren des ApiKeyAuthInterceptor.
 * Definiert, für welche URL-Pfade der Interceptor aktiv sein soll.
 * Diese Version schützt nur PUT-Anfragen für Faktor-Endpunkte.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final ApiKeyAuthInterceptor apiKeyAuthInterceptor;

    // Spring injiziert den ApiKeyAuthInterceptor automatisch
    public WebMvcConfig(ApiKeyAuthInterceptor apiKeyAuthInterceptor) {
        this.apiKeyAuthInterceptor = apiKeyAuthInterceptor;
    }

    /**
     * Registriert den ApiKeyAuthInterceptor für bestimmte URL-Pfade.
     *
     * @param registry Das InterceptorRegistry-Objekt, das zur Registrierung von Interceptoren verwendet wird.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Registriert den Interceptor für alle PUT-Anfragen an /api/bolus-factors/**
        // und /api/calorie-factors/**.
        // GET-Anfragen an diese Pfade sind NICHT durch diesen Interceptor geschützt.
        registry.addInterceptor(apiKeyAuthInterceptor)
                .addPathPatterns("/api/bolus-factors/**", "/api/calorie-factors/**");
        // Der Interceptor prüft intern, ob es sich um eine PUT-Anfrage handelt.
        // POST /api/calculate wird im CalculatorController selbst behandelt.
    }
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                        "http://localhost:5173",
                        "https://iridescent-semifreddo-01e3ae.netlify.app",
                        "https://lazycarbs.netlify.app",
                        "https://lazycarbs-production.up.railway.app"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
