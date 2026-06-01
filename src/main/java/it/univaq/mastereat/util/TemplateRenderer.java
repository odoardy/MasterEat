package it.univaq.mastereat.util;

import freemarker.cache.ClassTemplateLoader;
import freemarker.core.HTMLOutputFormat;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import it.univaq.mastereat.dto.web.auth.WebUserSession;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.Writer;
import java.time.Year;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Punto unico di rendering FreeMarker per i controller della web application.
 *
 * Configura il motore con escaping HTML e arricchisce ogni model con dati
 * comuni di pagina, sessione utente e riepilogo carrello.
 */
public final class TemplateRenderer {

    private static final Configuration CONFIGURATION = createConfiguration();

    private TemplateRenderer() {
    }

    /**
     * Renderizza un template aggiungendo al model i dati condivisi usati dal
     * layout applicativo.
     */
    public static void render(HttpServletRequest request,
                              HttpServletResponse response,
                              String templateName,
                              Map<String, Object> model) throws IOException, ServletException {
        Map<String, Object> pageModel = new HashMap<>();
        if (model != null) {
            pageModel.putAll(model);
        }

        pageModel.putIfAbsent("contextPath", request.getContextPath());
        pageModel.putIfAbsent("requestUri", request.getRequestURI());
        pageModel.putIfAbsent("currentYear", String.valueOf(Year.now().getValue()));

        WebUserSession currentUser = SessionUtils.getCurrentUser(request);
        pageModel.putIfAbsent("currentUser", currentUser);
        pageModel.putIfAbsent("isAuthenticated", currentUser != null);
        pageModel.putIfAbsent("currentRole", currentUser != null ? currentUser.getRuolo() : "");
        pageModel.putIfAbsent("currentRoleLabel", currentUser != null ? formatRole(currentUser.getRuolo()) : "");
        pageModel.putIfAbsent("cartItemCount", getCartItemCount(request, currentUser));

        response.setContentType("text/html;charset=UTF-8");

        try {
            Template template = CONFIGURATION.getTemplate(templateName, Locale.ITALIAN);
            try (Writer writer = response.getWriter()) {
                template.process(pageModel, writer);
            }
        } catch (TemplateException exception) {
            throw new ServletException("Errore durante il rendering del template " + templateName, exception);
        }
    }

    private static Configuration createConfiguration() {
        Configuration configuration = new Configuration(Configuration.VERSION_2_3_34);
        configuration.setTemplateLoader(new ClassTemplateLoader(
                TemplateRenderer.class.getClassLoader(),
                "/templates"
        ));
        configuration.setDefaultEncoding("UTF-8");
        configuration.setOutputFormat(HTMLOutputFormat.INSTANCE);
        configuration.setRecognizeStandardFileExtensions(true);
        configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
        configuration.setLogTemplateExceptions(false);
        configuration.setWrapUncheckedExceptions(true);
        configuration.setLocalizedLookup(false);
        return configuration;
    }

    private static String formatRole(String role) {
        if ("CLIENTE".equals(role)) {
            return "Cliente";
        }
        if ("PERSONALE".equals(role)) {
            return "Personale";
        }
        if ("PROPRIETARIO".equals(role)) {
            return "Proprietario";
        }
        return role != null ? role : "";
    }

    private static int getCartItemCount(HttpServletRequest request, WebUserSession currentUser) {
        if (currentUser == null || !"CLIENTE".equals(currentUser.getRuolo())) {
            return 0;
        }

        var cart = CartSessionUtils.getExistingCart(request);
        return cart != null ? cart.getQuantitaTotale() : 0;
    }
}
