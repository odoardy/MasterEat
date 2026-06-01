package it.univaq.mastereat.controller.web;

import it.univaq.mastereat.dto.web.owner.OwnerStatisticheResponse;
import it.univaq.mastereat.dto.web.auth.WebUserSession;
import it.univaq.mastereat.service.StatisticheService;
import it.univaq.mastereat.util.SessionUtils;
import it.univaq.mastereat.util.TemplateRenderer;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "OwnerStatisticheController", urlPatterns = "/proprietario/statistiche")
public class OwnerStatisticheController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(OwnerStatisticheController.class.getName());

    private static final String ROLE_PROPRIETARIO = "PROPRIETARIO";

    private final StatisticheService statisticheService = new StatisticheService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        WebUserSession currentUser = requireCurrentOwner(request, response);
        if (currentUser == null) {
            return;
        }

        LocalDate dataSelezionata = LocalDate.now();
        String dataParam = normalize(request.getParameter("data"));
        String validationError = null;

        if (dataParam != null) {
            try {
                dataSelezionata = LocalDate.parse(dataParam);
            } catch (DateTimeParseException exception) {
                validationError = "Data non valida: usare il formato YYYY-MM-DD. Sono mostrate le statistiche di oggi.";
            }
        }

        Map<String, Object> model = new HashMap<>();
        model.put("dataFiltro", dataSelezionata.toString());
        if (validationError != null) {
            model.put("errorMessage", validationError);
        }

        try {
            model.put("statistiche", statisticheService.getStatisticheProprietario(
                    currentUser.getIdUtente(),
                    dataSelezionata
            ));
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
            return;
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante il rendering statistiche proprietario", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            model.put("statistiche", OwnerStatisticheResponse.empty(dataSelezionata));
            model.put("errorMessage", "Non è stato possibile caricare le statistiche in questo momento.");
        }

        TemplateRenderer.render(request, response, "owner/statistics.ftl", model);
    }

    private WebUserSession requireCurrentOwner(HttpServletRequest request,
                                               HttpServletResponse response) throws IOException {
        WebUserSession currentUser = SessionUtils.getCurrentUser(request);
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }

        if (!ROLE_PROPRIETARIO.equals(currentUser.getRuolo())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
            return null;
        }

        return currentUser;
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
