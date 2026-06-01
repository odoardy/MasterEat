package it.univaq.mastereat.controller.web;

import it.univaq.mastereat.dto.web.staff.StaffOrdineResponse;
import it.univaq.mastereat.dto.web.staff.StaffRigaOrdineResponse;
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

@WebServlet(name = "StaffOrderController", urlPatterns = {
        "/staff/ordini",
        "/staff/ordini/*"
})
public class StaffOrderController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(StaffOrderController.class.getName());

    private static final String ROLE_PERSONALE = "PERSONALE";
    private static final String FLASH_STAFF_SUCCESS_ATTRIBUTE = "staffSuccessMessage";
    private static final String FLASH_STAFF_ERROR_ATTRIBUTE = "staffErrorMessage";
    private static final List<String> STATI_ORDINE = List.of(
            StatoOrdine.INSERITO.name(),
            StatoOrdine.IN_PREPARAZIONE.name(),
            StatoOrdine.PRONTO.name(),
            StatoOrdine.IN_CONSEGNA.name()
    );

    private final OrdineService ordineService = new OrdineService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        WebUserSession currentUser = requireCurrentStaff(request, response);
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

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        WebUserSession currentUser = requireCurrentStaff(request, response);
        if (currentUser == null) {
            return;
        }

        processAdvanceState(request, response, currentUser);
    }

    private void renderOrders(HttpServletRequest request,
                              HttpServletResponse response,
                              WebUserSession currentUser) throws ServletException, IOException {
        Map<String, String> filters = readFilters(request);
        Map<String, Object> model = new HashMap<>();
        model.put("filters", filters);
        model.put("statiOrdine", STATI_ORDINE);
        addStaffFlashMessages(request, model);

        try {
            model.put("ordini", ordineService.getOrdiniStaff(currentUser.getIdUtente(), filters.get("stato")));
        } catch (IllegalArgumentException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            model.put("ordini", List.of());
            model.put("errorMessage", exception.getMessage());
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
            return;
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante il rendering ordini staff", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            model.put("ordini", List.of());
            model.put("errorMessage", "Non è stato possibile caricare gli ordini staff in questo momento.");
        }

        TemplateRenderer.render(request, response, "staff/orders.ftl", model);
    }

    private void renderOrderDetail(HttpServletRequest request,
                                   HttpServletResponse response,
                                   WebUserSession currentUser) throws ServletException, IOException {
        Long idOrdine = parseOrderId(request.getPathInfo(), false);
        if (idOrdine == null) {
            renderOrderNotFound(request, response);
            return;
        }

        try {
            StaffOrdineResponse ordine = ordineService.getOrdineStaff(currentUser.getIdUtente(), idOrdine);
            List<StaffRigaOrdineResponse> righe =
                    ordineService.getRigheOrdineStaff(currentUser.getIdUtente(), idOrdine);
            List<StoricoStatoOrdine> storico =
                    ordineService.getStoricoOrdineStaff(currentUser.getIdUtente(), idOrdine);

            Map<String, Object> model = new HashMap<>();
            model.put("ordine", ordine);
            model.put("righe", righe);
            model.put("storico", storico);
            model.put("canAdvance", ordine.getProssimoStato() != null);
            addStaffFlashMessages(request, model);

            TemplateRenderer.render(request, response, "staff/order-detail.ftl", model);
        } catch (NoSuchElementException exception) {
            renderOrderNotFound(request, response);
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante il rendering dettaglio ordine staff", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            TemplateRenderer.render(request, response, "staff/order-detail.ftl", Map.of(
                    "errorMessage", "Non è stato possibile caricare il dettaglio ordine in questo momento."
            ));
        }
    }

    private void processAdvanceState(HttpServletRequest request,
                                     HttpServletResponse response,
                                     WebUserSession currentUser) throws ServletException, IOException {
        Long idOrdine = parseOrderId(request.getPathInfo(), true);
        if (idOrdine == null) {
            renderOrderNotFound(request, response);
            return;
        }

        try {
            ordineService.avanzaStatoOrdineStaff(currentUser.getIdUtente(), idOrdine);
            SessionUtils.setFlashMessage(
                    request,
                    FLASH_STAFF_SUCCESS_ATTRIBUTE,
                    "Stato ordine aggiornato correttamente."
            );
            redirectToOrderDetail(request, response, idOrdine);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            SessionUtils.setFlashMessage(request, FLASH_STAFF_ERROR_ATTRIBUTE, exception.getMessage());
            redirectToOrderDetail(request, response, idOrdine);
        } catch (NoSuchElementException exception) {
            renderOrderNotFound(request, response);
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante avanzamento stato ordine staff", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            TemplateRenderer.render(request, response, "staff/order-detail.ftl", Map.of(
                    "errorMessage", "Non è stato possibile aggiornare lo stato ordine in questo momento."
            ));
        }
    }

    private WebUserSession requireCurrentStaff(HttpServletRequest request,
                                               HttpServletResponse response) throws IOException {
        WebUserSession currentUser = SessionUtils.getCurrentUser(request);
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }

        if (!ROLE_PERSONALE.equals(currentUser.getRuolo())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
            return null;
        }

        return currentUser;
    }

    private void renderOrderNotFound(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        TemplateRenderer.render(request, response, "staff/order-detail.ftl", Map.of(
                "errorMessage", "Ordine non trovato."
        ));
    }

    private Map<String, String> readFilters(HttpServletRequest request) {
        Map<String, String> filters = new HashMap<>();
        filters.put("stato", normalize(request.getParameter("stato")));
        return filters;
    }

    private Long parseOrderId(String pathInfo, boolean statePath) {
        if (pathInfo == null || pathInfo.isBlank()) {
            return null;
        }

        String normalizedPath = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        String[] segments = normalizedPath.split("/");
        if (statePath) {
            if (segments.length != 2 || !"stato".equals(segments[1])) {
                return null;
            }
        } else if (segments.length != 1) {
            return null;
        }

        try {
            long idOrdine = Long.parseLong(segments[0]);
            return idOrdine > 0 ? idOrdine : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private void addStaffFlashMessages(HttpServletRequest request, Map<String, Object> model) {
        String successMessage = SessionUtils.consumeFlashMessage(request, FLASH_STAFF_SUCCESS_ATTRIBUTE);
        if (successMessage != null) {
            model.put("successMessage", successMessage);
        }

        String errorMessage = SessionUtils.consumeFlashMessage(request, FLASH_STAFF_ERROR_ATTRIBUTE);
        if (errorMessage != null) {
            model.put("errorMessage", errorMessage);
        }
    }

    private void redirectToOrderDetail(HttpServletRequest request,
                                       HttpServletResponse response,
                                       long idOrdine) throws IOException {
        response.sendRedirect(request.getContextPath() + "/staff/ordini/" + idOrdine);
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim();
    }
}
