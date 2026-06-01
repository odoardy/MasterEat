package it.univaq.mastereat.controller.web;

import it.univaq.mastereat.dto.web.customer.ClienteProfileUpdateRequest;
import it.univaq.mastereat.dto.common.OrdineResponse;
import it.univaq.mastereat.dto.common.ProdottiOrdineResponse;
import it.univaq.mastereat.dto.web.auth.WebUserSession;
import it.univaq.mastereat.model.StatoOrdine;
import it.univaq.mastereat.model.StoricoStatoOrdine;
import it.univaq.mastereat.model.Utente;
import it.univaq.mastereat.service.OrdineService;
import it.univaq.mastereat.service.UtenteService;
import it.univaq.mastereat.util.SessionUtils;
import it.univaq.mastereat.util.TemplateRenderer;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@WebServlet(name = "CustomerAccountController", urlPatterns = {
        "/cliente/account",
        "/cliente/profilo",
        "/cliente/ordini",
        "/cliente/ordini/*"
})
public class CustomerAccountController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(CustomerAccountController.class.getName());

    private static final String FLASH_ACCOUNT_SUCCESS_ATTRIBUTE = "accountSuccessMessage";
    private static final String FLASH_ACCOUNT_ERROR_ATTRIBUTE = "accountErrorMessage";
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+0-9 .()\\-]{6,30}$");
    private static final List<String> STATI_ORDINE = Arrays.stream(StatoOrdine.values())
            .map(Enum::name)
            .toList();

    private final UtenteService utenteService = new UtenteService();
    private final OrdineService ordineService = new OrdineService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String servletPath = request.getServletPath();

        if ("/cliente/account".equals(servletPath)) {
            renderAccount(request, response);
            return;
        }
        if ("/cliente/profilo".equals(servletPath)) {
            renderProfilePage(request, response);
            return;
        }
        if ("/cliente/ordini".equals(servletPath)) {
            if (request.getPathInfo() == null || request.getPathInfo().isBlank()) {
                renderOrders(request, response);
            } else {
                renderOrderDetail(request, response);
            }
            return;
        }

        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        String servletPath = request.getServletPath();
        if ("/cliente/profilo".equals(servletPath)) {
            processProfileUpdate(request, response);
            return;
        }
        if ("/cliente/ordini".equals(servletPath)) {
            processOrderCancel(request, response);
            return;
        }

        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    private void renderAccount(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            long idCliente = getCurrentClienteId(request);
            Utente cliente = requireCurrentCliente(request);
            List<OrdineResponse> ordini = ordineService.getOrdiniCliente(idCliente, null, null, null);
            List<OrdineResponse> ultimiOrdini = ordini.subList(0, Math.min(3, ordini.size()));

            Map<String, Object> model = new HashMap<>();
            model.put("cliente", cliente);
            model.put("ultimiOrdini", ultimiOrdini);
            addAccountFlashMessages(request, model);

            TemplateRenderer.render(request, response, "customer/account.ftl", model);
        } catch (NoSuchElementException exception) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            TemplateRenderer.render(request, response, "customer/account.ftl", Map.of(
                    "errorMessage", "Cliente non trovato."
            ));
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante il rendering account cliente", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            TemplateRenderer.render(request, response, "customer/account.ftl", Map.of(
                    "errorMessage", "Non e stato possibile caricare l'account in questo momento."
            ));
        }
    }

    private void renderProfilePage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Utente cliente = requireCurrentCliente(request);
            Map<String, Object> model = new HashMap<>();
            model.put("cliente", cliente);
            model.put("form", formFromCliente(cliente));
            model.put("errors", List.of());
            addAccountFlashMessages(request, model);

            TemplateRenderer.render(request, response, "customer/profile.ftl", model);
        } catch (NoSuchElementException exception) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            TemplateRenderer.render(request, response, "customer/profile.ftl", Map.of(
                    "errorMessage", "Cliente non trovato.",
                    "form", emptyProfileForm(),
                    "errors", List.of()
            ));
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante il rendering profilo cliente", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            TemplateRenderer.render(request, response, "customer/profile.ftl", Map.of(
                    "errorMessage", "Non e stato possibile caricare il profilo in questo momento.",
                    "form", emptyProfileForm(),
                    "errors", List.of()
            ));
        }
    }

    private void processProfileUpdate(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Map<String, String> form = readProfileForm(request);
        List<String> errors = validateProfileForm(form);
        if (!errors.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            renderProfileForm(request, response, form, errors);
            return;
        }

        ClienteProfileUpdateRequest updateRequest = new ClienteProfileUpdateRequest(
                form.get("nome"),
                form.get("cognome"),
                form.get("email"),
                form.get("telefono"),
                form.get("indirizzo"),
                form.get("citta"),
                form.get("cap")
        );

        try {
            Utente updated = utenteService.aggiornaProfiloCliente(getCurrentClienteId(request), updateRequest);
            SessionUtils.updateCurrentUser(request, WebUserSession.fromUtente(updated));
            SessionUtils.setFlashMessage(
                    request,
                    FLASH_ACCOUNT_SUCCESS_ATTRIBUTE,
                    "Profilo aggiornato correttamente."
            );
            response.sendRedirect(request.getContextPath() + "/cliente/account");
        } catch (IllegalArgumentException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            renderProfileForm(request, response, form, List.of(exception.getMessage()));
        } catch (NoSuchElementException exception) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            renderProfileForm(request, response, form, List.of("Cliente non trovato."));
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante l'aggiornamento profilo cliente", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            renderProfileForm(
                    request,
                    response,
                    form,
                    List.of("Non e stato possibile aggiornare il profilo in questo momento.")
            );
        }
    }

    private void renderOrders(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Map<String, String> filters = readOrderFilters(request);
        Map<String, Object> model = new HashMap<>();
        model.put("filters", filters);
        model.put("statiOrdine", STATI_ORDINE);
        addAccountFlashMessages(request, model);

        try {
            List<OrdineResponse> ordini = ordineService.getOrdiniCliente(
                    getCurrentClienteId(request),
                    filters.get("stato"),
                    filters.get("dataDa"),
                    filters.get("dataA")
            );
            model.put("ordini", ordini);
        } catch (IllegalArgumentException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            model.put("ordini", List.of());
            model.put("errorMessage", exception.getMessage());
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante il rendering storico ordini cliente", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            model.put("ordini", List.of());
            model.put("errorMessage", "Non e stato possibile caricare gli ordini in questo momento.");
        }

        TemplateRenderer.render(request, response, "customer/orders.ftl", model);
    }

    private void renderOrderDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Long idOrdine = parseOrderId(request.getPathInfo(), false);
        if (idOrdine == null) {
            renderOrderNotFound(request, response);
            return;
        }

        try {
            long idCliente = getCurrentClienteId(request);
            OrdineResponse ordine = ordineService.getOrdineCliente(idCliente, idOrdine);
            ProdottiOrdineResponse prodotti = ordineService.getProdottiOrdine(idCliente, idOrdine);
            List<StoricoStatoOrdine> storico = ordineService.getStoricoOrdineCliente(idCliente, idOrdine);

            Map<String, Object> model = new HashMap<>();
            model.put("ordine", ordine);
            model.put("righe", prodotti.getProdotti());
            model.put("storico", storico);
            model.put(
                    "orarioConsegnaRichiesto",
                    valueOrEmpty(ordineService.getOrarioConsegnaRichiestoCliente(idCliente, idOrdine))
            );
            model.put("canCancel", isOrderCancelable(ordine));
            addAccountFlashMessages(request, model);

            TemplateRenderer.render(request, response, "customer/order-detail.ftl", model);
        } catch (NoSuchElementException | SecurityException exception) {
            renderOrderNotFound(request, response);
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante il rendering dettaglio ordine cliente", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            TemplateRenderer.render(request, response, "customer/order-detail.ftl", Map.of(
                    "errorMessage", "Non e stato possibile caricare il dettaglio ordine in questo momento."
            ));
        }
    }

    private void processOrderCancel(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Long idOrdine = parseOrderId(request.getPathInfo(), true);
        if (idOrdine == null) {
            renderOrderNotFound(request, response);
            return;
        }

        try {
            ordineService.annullaOrdine(getCurrentClienteId(request), idOrdine);
            SessionUtils.setFlashMessage(
                    request,
                    FLASH_ACCOUNT_SUCCESS_ATTRIBUTE,
                    "Ordine annullato correttamente."
            );
            response.sendRedirect(request.getContextPath() + "/cliente/ordini/" + idOrdine);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            SessionUtils.setFlashMessage(request, FLASH_ACCOUNT_ERROR_ATTRIBUTE, exception.getMessage());
            response.sendRedirect(request.getContextPath() + "/cliente/ordini/" + idOrdine);
        } catch (NoSuchElementException | SecurityException exception) {
            renderOrderNotFound(request, response);
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante annullamento ordine cliente", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            TemplateRenderer.render(request, response, "customer/order-detail.ftl", Map.of(
                    "errorMessage", "Non e stato possibile annullare l'ordine in questo momento."
            ));
        }
    }

    private void renderProfileForm(HttpServletRequest request,
                                   HttpServletResponse response,
                                   Map<String, String> form,
                                   List<String> errors) throws ServletException, IOException {
        Map<String, Object> model = new HashMap<>();
        try {
            model.put("cliente", requireCurrentCliente(request));
        } catch (NoSuchElementException ignored) {
            // Il messaggio di errore del form spiega gia il problema all'utente.
        }
        model.put("form", form);
        model.put("errors", errors);
        TemplateRenderer.render(request, response, "customer/profile.ftl", model);
    }

    private void renderOrderNotFound(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        TemplateRenderer.render(request, response, "customer/order-detail.ftl", Map.of(
                "errorMessage", "Ordine non trovato."
        ));
    }

    private Utente requireCurrentCliente(HttpServletRequest request) {
        long idCliente = getCurrentClienteId(request);
        return utenteService.getUtenteById(idCliente)
                .orElseThrow(() -> new NoSuchElementException("Cliente non trovato."));
    }

    private long getCurrentClienteId(HttpServletRequest request) {
        WebUserSession currentUser = SessionUtils.getCurrentUser(request);
        if (currentUser == null) {
            throw new SecurityException("Utente non autenticato.");
        }

        return currentUser.getIdUtente();
    }

    private Map<String, String> readProfileForm(HttpServletRequest request) {
        Map<String, String> form = emptyProfileForm();
        form.put("nome", normalize(request.getParameter("nome")));
        form.put("cognome", normalize(request.getParameter("cognome")));
        form.put("email", normalize(request.getParameter("email")));
        form.put("telefono", normalize(request.getParameter("telefono")));
        form.put("indirizzo", normalize(request.getParameter("indirizzo")));
        form.put("citta", normalize(request.getParameter("citta")));
        form.put("cap", normalize(request.getParameter("cap")));
        return form;
    }

    private Map<String, String> formFromCliente(Utente cliente) {
        Map<String, String> form = emptyProfileForm();
        form.put("nome", valueOrEmpty(cliente.getNome()));
        form.put("cognome", valueOrEmpty(cliente.getCognome()));
        form.put("email", valueOrEmpty(cliente.getEmail()));
        form.put("telefono", valueOrEmpty(cliente.getTelefono()));
        form.put("indirizzo", valueOrEmpty(cliente.getIndirizzo()));
        form.put("citta", valueOrEmpty(cliente.getCitta()));
        form.put("cap", valueOrEmpty(cliente.getCap()));
        return form;
    }

    private Map<String, String> emptyProfileForm() {
        Map<String, String> form = new HashMap<>();
        form.put("nome", "");
        form.put("cognome", "");
        form.put("email", "");
        form.put("telefono", "");
        form.put("indirizzo", "");
        form.put("citta", "");
        form.put("cap", "");
        return form;
    }

    private List<String> validateProfileForm(Map<String, String> form) {
        List<String> errors = new ArrayList<>();

        validateRequiredText(errors, form.get("nome"), "Nome", 1, 80);
        validateRequiredText(errors, form.get("cognome"), "Cognome", 1, 80);
        validateEmail(errors, form.get("email"));
        validatePhone(errors, form.get("telefono"));
        validateRequiredText(errors, form.get("indirizzo"), "Indirizzo", 1, 255);
        validateRequiredText(errors, form.get("citta"), "Citta", 1, 100);
        validateOptionalText(errors, form.get("cap"), "CAP", 20);

        return errors;
    }

    private void validateEmail(List<String> errors, String email) {
        validateRequiredText(errors, email, "Email", 1, 255);
        if (!isBlank(email) && !EMAIL_PATTERN.matcher(email).matches()) {
            errors.add("Email non valida.");
        }
    }

    private void validatePhone(List<String> errors, String telefono) {
        validateRequiredText(errors, telefono, "Telefono", 6, 30);
        if (!isBlank(telefono) && !PHONE_PATTERN.matcher(telefono).matches()) {
            errors.add("Telefono non valido.");
        }
    }

    private void validateRequiredText(List<String> errors,
                                      String value,
                                      String label,
                                      int minLength,
                                      int maxLength) {
        if (isBlank(value)) {
            errors.add(label + " obbligatorio.");
            return;
        }

        if (value.length() < minLength) {
            errors.add(label + " deve contenere almeno " + minLength + " caratteri.");
        }
        if (value.length() > maxLength) {
            errors.add(label + " deve contenere al massimo " + maxLength + " caratteri.");
        }
    }

    private void validateOptionalText(List<String> errors, String value, String label, int maxLength) {
        if (!isBlank(value) && value.length() > maxLength) {
            errors.add(label + " deve contenere al massimo " + maxLength + " caratteri.");
        }
    }

    private Map<String, String> readOrderFilters(HttpServletRequest request) {
        Map<String, String> filters = new HashMap<>();
        filters.put("stato", normalize(request.getParameter("stato")));
        filters.put("dataDa", normalize(request.getParameter("dataDa")));
        filters.put("dataA", normalize(request.getParameter("dataA")));
        return filters;
    }

    private Long parseOrderId(String pathInfo, boolean cancelPath) {
        if (pathInfo == null || pathInfo.isBlank()) {
            return null;
        }

        String normalizedPath = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        String[] segments = normalizedPath.split("/");
        if (cancelPath) {
            if (segments.length != 2 || !"annulla".equals(segments[1])) {
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

    private boolean isOrderCancelable(OrdineResponse ordine) {
        return ordine != null
                && ("BOZZA".equals(ordine.getStato()) || "INSERITO".equals(ordine.getStato()));
    }

    private void addAccountFlashMessages(HttpServletRequest request, Map<String, Object> model) {
        String successMessage = SessionUtils.consumeFlashMessage(request, FLASH_ACCOUNT_SUCCESS_ATTRIBUTE);
        if (successMessage != null) {
            model.put("successMessage", successMessage);
        }

        String errorMessage = SessionUtils.consumeFlashMessage(request, FLASH_ACCOUNT_ERROR_ATTRIBUTE);
        if (errorMessage != null) {
            model.put("errorMessage", errorMessage);
        }
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim();
    }

    private String valueOrEmpty(String value) {
        return value != null ? value : "";
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
