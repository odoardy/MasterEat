package it.univaq.mastereat.controller.web;

import it.univaq.mastereat.dto.web.owner.OwnerOrdineResponse;
import it.univaq.mastereat.dto.common.RigaOrdineResponse;
import it.univaq.mastereat.dto.web.auth.WebUserSession;
import it.univaq.mastereat.model.StatoOrdine;
import it.univaq.mastereat.model.StoricoStatoOrdine;
import it.univaq.mastereat.service.OrdineService;
import it.univaq.mastereat.util.SessionUtils;
import it.univaq.mastereat.util.TemplateRenderer;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "OwnerOrderController", urlPatterns = {
        "/proprietario/ordini",
        "/proprietario/ordini/*"
})
public class OwnerOrderController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(OwnerOrderController.class.getName());

    private static final String ROLE_PROPRIETARIO = "PROPRIETARIO";
    private static final List<String> STATI_ORDINE = List.of(
            StatoOrdine.INSERITO.name(),
            StatoOrdine.IN_PREPARAZIONE.name(),
            StatoOrdine.PRONTO.name(),
            StatoOrdine.IN_CONSEGNA.name(),
            StatoOrdine.CONSEGNATO.name(),
            StatoOrdine.ANNULLATO.name()
    );

    private final OrdineService ordineService = new OrdineService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        WebUserSession currentUser = requireCurrentOwner(request, response);
        if (currentUser == null) {
            return;
        }

        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.isBlank() || "/".equals(pathInfo)) {
            renderOrders(request, response, currentUser);
            return;
        }

        renderOrderDetail(request, response, currentUser);
    }

    private void renderOrders(HttpServletRequest request,
                              HttpServletResponse response,
                              WebUserSession currentUser) throws ServletException, IOException {
        Map<String, String> filters = readFilters(request);
        Map<String, Object> model = new HashMap<>();
        model.put("filters", filters);
        model.put("statiOrdine", STATI_ORDINE);

        try {
            model.put("ordini", ordineService.getOrdiniProprietario(
                    currentUser.getIdUtente(),
                    filters.get("stato"),
                    filters.get("dal"),
                    filters.get("al")
            ));
        } catch (IllegalArgumentException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            model.put("ordini", List.of());
            model.put("errorMessage", exception.getMessage());
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
            return;
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante il rendering ordini proprietario", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            model.put("ordini", List.of());
            model.put("errorMessage", "Non è stato possibile caricare gli ordini in questo momento.");
        }

        TemplateRenderer.render(request, response, "owner/orders.ftl", model);
    }

    private void renderOrderDetail(HttpServletRequest request,
                                   HttpServletResponse response,
                                   WebUserSession currentUser) throws ServletException, IOException {
        Long idOrdine = parseOrderId(request.getPathInfo());
        if (idOrdine == null) {
            renderOrderNotFound(request, response);
            return;
        }

        try {
            OwnerOrdineResponse ordine = ordineService.getOrdineProprietario(currentUser.getIdUtente(), idOrdine);
            List<RigaOrdineResponse> righe =
                    ordineService.getRigheOrdineProprietario(currentUser.getIdUtente(), idOrdine);
            List<StoricoStatoOrdine> storico =
                    ordineService.getStoricoOrdineProprietario(currentUser.getIdUtente(), idOrdine);

            Map<String, Object> model = new HashMap<>();
            model.put("ordine", ordine);
            model.put("righe", righe);
            model.put("storico", storico);

            TemplateRenderer.render(request, response, "owner/order-detail.ftl", model);
        } catch (NoSuchElementException exception) {
            renderOrderNotFound(request, response);
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante il rendering dettaglio ordine proprietario", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            TemplateRenderer.render(request, response, "owner/order-detail.ftl", Map.of(
                    "errorMessage", "Non è stato possibile caricare il dettaglio ordine in questo momento."
            ));
        }
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

    private void renderOrderNotFound(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        TemplateRenderer.render(request, response, "owner/order-detail.ftl", Map.of(
                "errorMessage", "Ordine non trovato."
        ));
    }

    private Map<String, String> readFilters(HttpServletRequest request) {
        Map<String, String> filters = new HashMap<>();
        filters.put("stato", normalize(request.getParameter("stato")));
        filters.put("dal", normalize(request.getParameter("dal")));
        filters.put("al", normalize(request.getParameter("al")));
        return filters;
    }

    private Long parseOrderId(String pathInfo) {
        if (pathInfo == null || pathInfo.isBlank()) {
            return null;
        }

        String normalizedPath = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        String[] segments = normalizedPath.split("/");
        if (segments.length != 1) {
            return null;
        }

        try {
            long idOrdine = Long.parseLong(segments[0]);
            return idOrdine > 0 ? idOrdine : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim();
    }
}
