package it.univaq.mastereat.controller.web;

import it.univaq.mastereat.dto.common.OrdineResponse;
import it.univaq.mastereat.dto.common.ProdottoPubblicoResponse;
import it.univaq.mastereat.dto.web.cart.WebCart;
import it.univaq.mastereat.dto.web.cart.WebCartItem;
import it.univaq.mastereat.dto.web.cart.WebCartItemCharacteristic;
import it.univaq.mastereat.dto.web.auth.WebUserSession;
import it.univaq.mastereat.model.Caratteristica;
import it.univaq.mastereat.model.Prodotto;
import it.univaq.mastereat.model.Utente;
import it.univaq.mastereat.service.CaratteristicaService;
import it.univaq.mastereat.service.OrdineService;
import it.univaq.mastereat.service.ProdottoService;
import it.univaq.mastereat.service.UtenteService;
import it.univaq.mastereat.util.CartSessionUtils;
import it.univaq.mastereat.util.SessionUtils;
import it.univaq.mastereat.util.TemplateRenderer;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller MVC dell'area cliente per carrello e checkout.
 *
 * Mantiene il carrello nella sessione HTTP, ricostruisce le configurazioni di
 * prodotto lato server e delega a {@link OrdineService} la conferma con orario
 * di consegna richiesto.
 */
@WebServlet(name = "CustomerCartController", urlPatterns = {
        "/cliente/carrello",
        "/cliente/carrello/aggiungi",
        "/cliente/carrello/decrementa",
        "/cliente/carrello/incrementa",
        "/cliente/carrello/rimuovi",
        "/cliente/carrello/svuota",
        "/cliente/checkout",
        "/cliente/ordine-confermato"
})
public class CustomerCartController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(CustomerCartController.class.getName());
    private static final String PARAM_ORARIO_CONSEGNA_RICHIESTO = "orarioConsegnaRichiesto";
    private static final DateTimeFormatter DATETIME_LOCAL_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private final ProdottoService prodottoService = new ProdottoService();
    private final CaratteristicaService caratteristicaService = new CaratteristicaService();
    private final OrdineService ordineService = new OrdineService();
    private final UtenteService utenteService = new UtenteService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        switch (request.getServletPath()) {
            case "/cliente/carrello" -> renderCart(request, response);
            case "/cliente/checkout" -> renderCheckoutPage(request, response);
            case "/cliente/ordine-confermato" -> renderOrderConfirmation(request, response);
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        switch (request.getServletPath()) {
            case "/cliente/carrello/aggiungi" -> processAddToCart(request, response);
            case "/cliente/carrello/decrementa" -> processDecrementCartItem(request, response);
            case "/cliente/carrello/incrementa" -> processIncrementCartItem(request, response);
            case "/cliente/carrello/rimuovi" -> processRemoveFromCart(request, response);
            case "/cliente/carrello/svuota" -> processClearCart(request, response);
            case "/cliente/checkout" -> processCheckout(request, response);
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void renderCart(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Map<String, Object> model = new HashMap<>();
        model.put("cart", getDisplayCart(request));
        addCartFlashMessages(request, model);

        TemplateRenderer.render(request, response, "customer/cart.ftl", model);
    }

    private void renderCheckoutPage(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        WebCart cart = CartSessionUtils.getExistingCart(request);
        if (cart == null || cart.isEmpty()) {
            CartSessionUtils.setErrorMessage(
                    request,
                    "Il carrello e vuoto: aggiungi almeno un prodotto prima del checkout."
            );
            response.sendRedirect(request.getContextPath() + "/cliente/carrello");
            return;
        }

        renderCheckout(request, response, cart, null);
    }

    private void renderCheckout(HttpServletRequest request,
                                HttpServletResponse response,
                                WebCart cart,
                                String errorMessage) throws ServletException, IOException {
        renderCheckout(request, response, cart, errorMessage, null);
    }

    private void renderCheckout(HttpServletRequest request,
                                HttpServletResponse response,
                                WebCart cart,
                                String errorMessage,
                                String selectedDeliveryDateTime) throws ServletException, IOException {
        Map<String, Object> model = new HashMap<>();
        WebCart displayCart = cart != null ? cart : new WebCart();
        LocalDateTime minDeliveryDateTime =
                ordineService.getOrarioConsegnaMinimoWeb(displayCart.getTempoPreparazioneStimato());
        String minDeliveryDateTimeValue = toDateTimeLocalValue(minDeliveryDateTime);
        model.put("cart", displayCart);
        model.put("cliente", loadCurrentCliente(request).orElse(null));
        model.put("minDeliveryDateTime", minDeliveryDateTimeValue);
        model.put("minDeliveryDateTimeDisplay", minDeliveryDateTimeValue);
        model.put("selectedDeliveryDateTime", selectedDeliveryDateTime != null ? selectedDeliveryDateTime : "");
        if (errorMessage != null) {
            model.put("errorMessage", errorMessage);
        } else {
            String flashError = CartSessionUtils.consumeErrorMessage(request);
            if (flashError != null) {
                model.put("errorMessage", flashError);
            }
        }

        TemplateRenderer.render(request, response, "customer/checkout.ftl", model);
    }

    private void renderOrderConfirmation(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Long idOrdine = parseOptionalLong(request.getParameter("idOrdine"));
        if (idOrdine == null || idOrdine <= 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            TemplateRenderer.render(request, response, "customer/order-confirmation.ftl", Map.of(
                    "errorMessage", "Ordine non valido."
            ));
            return;
        }

        WebUserSession currentUser = SessionUtils.getCurrentUser(request);
        try {
            OrdineResponse ordine = ordineService.getOrdineCliente(currentUser.getIdUtente(), idOrdine);
            Map<String, Object> model = new HashMap<>();
            model.put("ordine", ordine);
            model.put(
                    "orarioConsegnaRichiesto",
                    valueOrEmpty(ordineService.getOrarioConsegnaRichiestoCliente(currentUser.getIdUtente(), idOrdine))
            );
            TemplateRenderer.render(request, response, "customer/order-confirmation.ftl", model);
        } catch (NoSuchElementException exception) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            TemplateRenderer.render(request, response, "customer/order-confirmation.ftl", Map.of(
                    "errorMessage", "Ordine non trovato."
            ));
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante il rendering della conferma ordine web", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            TemplateRenderer.render(request, response, "customer/order-confirmation.ftl", Map.of(
                    "errorMessage", "Non e stato possibile caricare la conferma ordine in questo momento."
            ));
        }
    }

    private void processAddToCart(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String idProdottoParam = request.getParameter("idProdotto");

        try {
            long idProdotto = parseRequiredLong(idProdottoParam, "Prodotto non valido.");
            int quantita = parseRequiredInt(request.getParameter("quantita"), "Quantita non valida.");
            if (quantita <= 0) {
                throw new IllegalArgumentException("La quantita deve essere maggiore di zero.");
            }

            WebCartItem item = buildCartItem(idProdotto, quantita, readSelectedCharacteristicIds(request));
            CartSessionUtils.getCart(request).addItem(item);
            CartSessionUtils.setSuccessMessage(request, "Prodotto aggiunto al carrello.");
            response.sendRedirect(request.getContextPath() + "/cliente/carrello");
        } catch (IllegalArgumentException | NoSuchElementException exception) {
            CartSessionUtils.setErrorMessage(request, exception.getMessage());
            redirectToProductOrMenu(request, response, idProdottoParam);
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante l'aggiunta prodotto al carrello web", exception);
            CartSessionUtils.setErrorMessage(request, "Non e stato possibile aggiungere il prodotto al carrello.");
            redirectToProductOrMenu(request, response, idProdottoParam);
        }
    }

    private void processRemoveFromCart(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String itemKey = request.getParameter("itemKey");
        WebCart cart = CartSessionUtils.getExistingCart(request);
        if (cart != null && cart.removeItem(itemKey)) {
            CartSessionUtils.setSuccessMessage(request, "Configurazione rimossa dal carrello.");
        } else {
            CartSessionUtils.setErrorMessage(request, "Elemento del carrello non trovato.");
        }

        response.sendRedirect(request.getContextPath() + "/cliente/carrello");
    }

    private void processDecrementCartItem(HttpServletRequest request, HttpServletResponse response) throws IOException {
        updateCartItemQuantity(request, response, false);
    }

    private void processIncrementCartItem(HttpServletRequest request, HttpServletResponse response) throws IOException {
        updateCartItemQuantity(request, response, true);
    }

    private void updateCartItemQuantity(HttpServletRequest request,
                                        HttpServletResponse response,
                                        boolean increment) throws IOException {
        String itemKey = request.getParameter("itemKey");
        WebCart cart = CartSessionUtils.getExistingCart(request);
        boolean updated = cart != null
                && (increment ? cart.incrementItem(itemKey) : cart.decrementItem(itemKey));

        if (updated) {
            CartSessionUtils.setSuccessMessage(request, "Quantita aggiornata.");
        } else {
            CartSessionUtils.setErrorMessage(request, "Elemento del carrello non trovato.");
        }

        response.sendRedirect(request.getContextPath() + "/cliente/carrello");
    }

    private void processClearCart(HttpServletRequest request, HttpServletResponse response) throws IOException {
        WebCart cart = CartSessionUtils.getExistingCart(request);
        if (cart != null && !cart.isEmpty()) {
            cart.clear();
            CartSessionUtils.setSuccessMessage(request, "Carrello svuotato.");
        }

        response.sendRedirect(request.getContextPath() + "/cliente/carrello");
    }

    /**
     * Conferma il carrello corrente dopo la validazione server-side dell'orario
     * di consegna e delle righe convertite in richieste ordine.
     */
    private void processCheckout(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        WebCart cart = CartSessionUtils.getExistingCart(request);
        if (cart == null || cart.isEmpty()) {
            CartSessionUtils.setErrorMessage(
                    request,
                    "Il carrello e vuoto: aggiungi almeno un prodotto prima del checkout."
            );
            response.sendRedirect(request.getContextPath() + "/cliente/carrello");
            return;
        }

        WebUserSession currentUser = SessionUtils.getCurrentUser(request);
        String requestedDeliveryTimeValue = normalize(request.getParameter(PARAM_ORARIO_CONSEGNA_RICHIESTO));
        try {
            LocalDateTime requestedDeliveryTime = parseRequestedDeliveryTime(requestedDeliveryTimeValue);
            OrdineResponse ordine = ordineService.creaOrdineConfermatoWeb(
                    currentUser.getIdUtente(),
                    cart.toOrderRequests(),
                    requestedDeliveryTime
            );
            CartSessionUtils.clearCart(request);
            response.sendRedirect(request.getContextPath() + "/cliente/ordine-confermato?idOrdine=" + ordine.getId());
        } catch (IllegalArgumentException | IllegalStateException | NoSuchElementException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            renderCheckout(request, response, cart, exception.getMessage(), requestedDeliveryTimeValue);
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante la conferma checkout web", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            renderCheckout(
                    request,
                    response,
                    cart,
                    "Non e stato possibile confermare l'ordine in questo momento.",
                    requestedDeliveryTimeValue
            );
        }
    }

    /**
     * Ricostruisce una riga carrello dai dati persistiti del prodotto, evitando
     * di fidarsi di prezzo, nome o caratteristiche provenienti dal form.
     */
    private WebCartItem buildCartItem(long idProdotto,
                                      int quantita,
                                      List<Long> selectedCharacteristicIds) {
        int prodottoId = toProductIntId(idProdotto);
        ProdottoPubblicoResponse prodottoPubblico = prodottoService.getProdottoPubblicoById(prodottoId)
                .orElseThrow(() -> new NoSuchElementException("Prodotto non trovato nel men\u00f9 pubblico."));
        Prodotto prodotto = prodottoService.getProdottoById(prodottoId)
                .orElseThrow(() -> new NoSuchElementException("Prodotto non trovato nel men\u00f9 pubblico."));
        ordineService.validaConfigurazioneProdotto(prodotto.getId(), selectedCharacteristicIds);

        List<Caratteristica> caratteristicheDisponibili =
                caratteristicaService.getCaratteristicheByProdottoId(prodottoId);
        List<WebCartItemCharacteristic> caratteristicheSelezionate =
                validateSelectedCharacteristics(selectedCharacteristicIds, caratteristicheDisponibili);

        return new WebCartItem(
                prodottoPubblico.getId(),
                prodottoPubblico.getNome(),
                prodottoPubblico.getPrezzoBase(),
                quantita,
                prodotto.getMinutiPreparazione(),
                caratteristicheSelezionate
        );
    }

    /**
     * Verifica che le caratteristiche selezionate appartengano al prodotto e
     * che non ci siano duplicati o piu scelte nello stesso gruppo.
     */
    private List<WebCartItemCharacteristic> validateSelectedCharacteristics(List<Long> selectedIds,
                                                                            List<Caratteristica> disponibili) {
        if (selectedIds == null || selectedIds.isEmpty()) {
            return List.of();
        }

        Map<Long, Caratteristica> caratteristicheById = new LinkedHashMap<>();
        for (Caratteristica caratteristica : disponibili) {
            caratteristicheById.put(caratteristica.getId(), caratteristica);
        }

        Set<Long> uniqueIds = new LinkedHashSet<>();
        Set<Long> selectedGroups = new HashSet<>();
        List<WebCartItemCharacteristic> selected = new ArrayList<>();

        for (Long idCaratteristica : selectedIds) {
            if (idCaratteristica == null || idCaratteristica <= 0) {
                throw new IllegalArgumentException("Caratteristica non valida.");
            }
            if (!uniqueIds.add(idCaratteristica)) {
                throw new IllegalArgumentException("Caratteristiche duplicate nella richiesta.");
            }

            Caratteristica caratteristica = caratteristicheById.get(idCaratteristica);
            if (caratteristica == null) {
                throw new NoSuchElementException("Caratteristica non disponibile per il prodotto selezionato.");
            }

            Long idGruppo = caratteristica.getIdGruppoCaratteristiche();
            if (idGruppo != null && !selectedGroups.add(idGruppo)) {
                throw new IllegalArgumentException("Puoi scegliere una sola caratteristica per ciascun gruppo.");
            }

            selected.add(new WebCartItemCharacteristic(
                    caratteristica.getId(),
                    idGruppo,
                    caratteristica.getNome(),
                    caratteristica.getDifferenzaPrezzo()
            ));
        }

        return selected;
    }

    private List<Long> readSelectedCharacteristicIds(HttpServletRequest request) {
        List<Long> ids = new ArrayList<>();
        addParameterValues(ids, request.getParameterValues("caratteristiche"));

        for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
            if (entry.getKey() != null && entry.getKey().startsWith("caratteristicheGruppo_")) {
                addParameterValues(ids, entry.getValue());
            }
        }

        return ids;
    }

    private void addParameterValues(List<Long> ids, String[] values) {
        if (values == null) {
            return;
        }

        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }
            ids.add(parseRequiredLong(value, "Caratteristica non valida."));
        }
    }

    private WebCart getDisplayCart(HttpServletRequest request) {
        WebCart cart = CartSessionUtils.getExistingCart(request);
        return cart != null ? cart : new WebCart();
    }

    private Optional<Utente> loadCurrentCliente(HttpServletRequest request) {
        WebUserSession currentUser = SessionUtils.getCurrentUser(request);
        if (currentUser == null) {
            return Optional.empty();
        }

        return utenteService.getUtenteById(currentUser.getIdUtente());
    }

    private void addCartFlashMessages(HttpServletRequest request, Map<String, Object> model) {
        String successMessage = CartSessionUtils.consumeSuccessMessage(request);
        if (successMessage != null) {
            model.put("successMessage", successMessage);
        }

        String errorMessage = CartSessionUtils.consumeErrorMessage(request);
        if (errorMessage != null) {
            model.put("errorMessage", errorMessage);
        }
    }

    private void redirectToProductOrMenu(HttpServletRequest request,
                                         HttpServletResponse response,
                                         String idProdottoParam) throws IOException {
        Long idProdotto = parseOptionalLong(idProdottoParam);
        if (idProdotto != null && idProdotto > 0) {
            response.sendRedirect(request.getContextPath() + "/prodotti/" + idProdotto);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/menu");
    }

    private int toProductIntId(long idProdotto) {
        try {
            return Math.toIntExact(idProdotto);
        } catch (ArithmeticException exception) {
            throw new NoSuchElementException("Prodotto non trovato nel men\u00f9 pubblico.");
        }
    }

    private LocalDateTime parseRequestedDeliveryTime(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Formato orario di consegna non valido.");
        }
    }

    /**
     * Adatta il valore minimo al formato HTML datetime-local, arrotondando al
     * minuto successivo quando secondi o millisecondi renderebbero il limite non
     * selezionabile dal browser.
     */
    private String toDateTimeLocalValue(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        LocalDateTime value = dateTime.withSecond(0).withNano(0);
        if (dateTime.getSecond() > 0 || dateTime.getNano() > 0) {
            value = value.plusMinutes(1);
        }
        return value.format(DATETIME_LOCAL_FORMATTER);
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

    private long parseRequiredLong(String value, String errorMessage) {
        Long parsed = parseOptionalLong(value);
        if (parsed == null) {
            throw new IllegalArgumentException(errorMessage);
        }
        return parsed;
    }

    private int parseRequiredInt(String value, String errorMessage) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(errorMessage);
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private Long parseOptionalLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
