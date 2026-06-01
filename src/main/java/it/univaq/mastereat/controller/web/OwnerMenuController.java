package it.univaq.mastereat.controller.web;

import it.univaq.mastereat.dto.web.owner.OwnerCaratteristicaResponse;
import it.univaq.mastereat.dto.web.owner.OwnerCaratteristicaSaveRequest;
import it.univaq.mastereat.dto.web.owner.OwnerGruppoCaratteristicheResponse;
import it.univaq.mastereat.dto.web.owner.OwnerGruppoCaratteristicheSaveRequest;
import it.univaq.mastereat.dto.web.owner.OwnerImmagineProdottoResponse;
import it.univaq.mastereat.dto.web.owner.OwnerIngredienteResponse;
import it.univaq.mastereat.dto.web.owner.OwnerIngredienteSaveRequest;
import it.univaq.mastereat.dto.web.owner.OwnerProdottoResponse;
import it.univaq.mastereat.dto.web.owner.OwnerProdottoSaveRequest;
import it.univaq.mastereat.dto.web.auth.WebUserSession;
import it.univaq.mastereat.service.CaratteristicaService;
import it.univaq.mastereat.service.GruppoCaratteristicheService;
import it.univaq.mastereat.service.ImmagineProdottoService;
import it.univaq.mastereat.service.IngredienteService;
import it.univaq.mastereat.service.ProdottoService;
import it.univaq.mastereat.util.SessionUtils;
import it.univaq.mastereat.util.TemplateRenderer;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller MVC dell'area proprietario dedicata alla gestione del menu.
 *
 * Mantiene nello stesso endpoint owner le operazioni su prodotti, immagini,
 * caratteristiche, gruppi di caratteristiche e ingredienti, delegando le
 * regole di dominio ai rispettivi service.
 */
@WebServlet(name = "OwnerMenuController", urlPatterns = {
        "/proprietario/menu",
        "/proprietario/menu/prodotti",
        "/proprietario/menu/prodotti/*"
})
// Limiti upload immagini: 3 MB per file, 4 MB per richiesta multipart.
@MultipartConfig(maxFileSize = 3145728L, maxRequestSize = 4194304L)
public class OwnerMenuController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(OwnerMenuController.class.getName());

    private static final String ROLE_PROPRIETARIO = "PROPRIETARIO";
    private static final String FLASH_OWNER_MENU_SUCCESS_ATTRIBUTE = "ownerMenuSuccessMessage";
    private static final String FLASH_OWNER_MENU_ERROR_ATTRIBUTE = "ownerMenuErrorMessage";

    private final ProdottoService prodottoService = new ProdottoService();
    private final CaratteristicaService caratteristicaService = new CaratteristicaService();
    private final GruppoCaratteristicheService gruppoCaratteristicheService = new GruppoCaratteristicheService();
    private final IngredienteService ingredienteService = new IngredienteService();
    private final ImmagineProdottoService immagineProdottoService = new ImmagineProdottoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        WebUserSession currentUser = requireCurrentOwner(request, response);
        if (currentUser == null) {
            return;
        }

        switch (request.getServletPath()) {
            case "/proprietario/menu" -> renderMenu(request, response, currentUser);
            case "/proprietario/menu/prodotti" -> dispatchProductGet(request, response, currentUser);
            default -> response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        WebUserSession currentUser = requireCurrentOwner(request, response);
        if (currentUser == null) {
            return;
        }

        if (!"/proprietario/menu/prodotti".equals(request.getServletPath())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        List<String> segments = getPathSegments(request.getPathInfo());
        if (segments.isEmpty()) {
            processCreateProduct(request, response, currentUser);
            return;
        }

        if (segments.size() == 2 && "modifica".equals(segments.get(1))) {
            processUpdateProduct(request, response, currentUser, segments.get(0));
            return;
        }

        if (segments.size() == 2 && "caratteristiche".equals(segments.get(1))) {
            processCreateCharacteristic(request, response, currentUser, segments.get(0));
            return;
        }

        if (segments.size() == 2 && "gruppi-caratteristiche".equals(segments.get(1))) {
            processCreateCharacteristicGroup(request, response, currentUser, segments.get(0));
            return;
        }

        if (segments.size() == 2 && "ingredienti".equals(segments.get(1))) {
            processCreateIngredient(request, response, currentUser, segments.get(0));
            return;
        }

        if (segments.size() == 2 && "immagini".equals(segments.get(1))) {
            processUploadImage(request, response, currentUser, segments.get(0));
            return;
        }

        if (segments.size() == 4 && "caratteristiche".equals(segments.get(1))
                && "modifica".equals(segments.get(3))) {
            processUpdateCharacteristic(request, response, currentUser, segments.get(0), segments.get(2));
            return;
        }

        if (segments.size() == 4 && "gruppi-caratteristiche".equals(segments.get(1))
                && "modifica".equals(segments.get(3))) {
            processUpdateCharacteristicGroup(request, response, currentUser, segments.get(0), segments.get(2));
            return;
        }

        if (segments.size() == 4 && "ingredienti".equals(segments.get(1))
                && "modifica".equals(segments.get(3))) {
            processUpdateIngredient(request, response, currentUser, segments.get(0), segments.get(2));
            return;
        }

        if (segments.size() == 4 && "caratteristiche".equals(segments.get(1))
                && "rimuovi".equals(segments.get(3))) {
            processRemoveCharacteristic(request, response, currentUser, segments.get(0), segments.get(2));
            return;
        }

        if (segments.size() == 4 && "gruppi-caratteristiche".equals(segments.get(1))
                && "rimuovi".equals(segments.get(3))) {
            processRemoveCharacteristicGroup(request, response, currentUser, segments.get(0), segments.get(2));
            return;
        }

        if (segments.size() == 4 && "ingredienti".equals(segments.get(1))
                && "rimuovi".equals(segments.get(3))) {
            processRemoveIngredient(request, response, currentUser, segments.get(0), segments.get(2));
            return;
        }

        if (segments.size() == 4 && "immagini".equals(segments.get(1))
                && "principale".equals(segments.get(3))) {
            processSetPrimaryImage(request, response, currentUser, segments.get(0), segments.get(2));
            return;
        }

        if (segments.size() == 4 && "immagini".equals(segments.get(1))
                && "rimuovi".equals(segments.get(3))) {
            processRemoveImage(request, response, currentUser, segments.get(0), segments.get(2));
            return;
        }

        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    // Vista principale e routing interno dell'area menu proprietario.
    private void renderMenu(HttpServletRequest request,
                            HttpServletResponse response,
                            WebUserSession currentUser) throws ServletException, IOException {
        Map<String, Object> model = new HashMap<>();
        addOwnerMenuFlashMessages(request, model);

        try {
            model.put("prodotti", prodottoService.getProdottiProprietario(currentUser.getIdUtente()));
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
            return;
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante il rendering menu proprietario", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            model.put("prodotti", List.of());
            model.put("errorMessage", "Non e stato possibile caricare il men\u00f9 in questo momento.");
        }

        TemplateRenderer.render(request, response, "owner/menu.ftl", model);
    }

    private void dispatchProductGet(HttpServletRequest request,
                                    HttpServletResponse response,
                                    WebUserSession currentUser) throws ServletException, IOException {
        List<String> segments = getPathSegments(request.getPathInfo());

        if (segments.size() == 1 && "nuovo".equals(segments.get(0))) {
            renderNewProductForm(request, response, currentUser);
            return;
        }

        if (segments.size() == 1) {
            renderProductDetail(request, response, currentUser, segments.get(0));
            return;
        }

        if (segments.size() == 2 && "modifica".equals(segments.get(1))) {
            renderEditProductForm(request, response, currentUser, segments.get(0));
            return;
        }

        if (segments.size() == 2 && "caratteristiche".equals(segments.get(1))) {
            renderProductCharacteristics(request, response, currentUser, segments.get(0));
            return;
        }

        if (segments.size() == 2 && "gruppi-caratteristiche".equals(segments.get(1))) {
            renderProductCharacteristicGroups(request, response, currentUser, segments.get(0));
            return;
        }

        if (segments.size() == 2 && "ingredienti".equals(segments.get(1))) {
            renderProductIngredients(request, response, currentUser, segments.get(0));
            return;
        }

        if (segments.size() == 2 && "immagini".equals(segments.get(1))) {
            renderProductImages(request, response, currentUser, segments.get(0));
            return;
        }

        if (segments.size() == 3 && "caratteristiche".equals(segments.get(1))
                && "nuova".equals(segments.get(2))) {
            renderNewCharacteristicForm(request, response, currentUser, segments.get(0));
            return;
        }

        if (segments.size() == 3 && "gruppi-caratteristiche".equals(segments.get(1))
                && "nuovo".equals(segments.get(2))) {
            renderNewCharacteristicGroupForm(request, response, currentUser, segments.get(0));
            return;
        }

        if (segments.size() == 3 && "ingredienti".equals(segments.get(1))
                && "nuovo".equals(segments.get(2))) {
            renderNewIngredientForm(request, response, currentUser, segments.get(0));
            return;
        }

        if (segments.size() == 4 && "caratteristiche".equals(segments.get(1))
                && "modifica".equals(segments.get(3))) {
            renderEditCharacteristicForm(request, response, currentUser, segments.get(0), segments.get(2));
            return;
        }

        if (segments.size() == 4 && "gruppi-caratteristiche".equals(segments.get(1))
                && "modifica".equals(segments.get(3))) {
            renderEditCharacteristicGroupForm(request, response, currentUser, segments.get(0), segments.get(2));
            return;
        }

        if (segments.size() == 4 && "ingredienti".equals(segments.get(1))
                && "modifica".equals(segments.get(3))) {
            renderEditIngredientForm(request, response, currentUser, segments.get(0), segments.get(2));
            return;
        }

        response.sendError(HttpServletResponse.SC_NOT_FOUND);
    }

    // Gestione prodotto e immagini.
    private void renderProductDetail(HttpServletRequest request,
                                     HttpServletResponse response,
                                     WebUserSession currentUser,
                                     String rawProductId) throws ServletException, IOException {
        Integer productId = parsePositiveInt(rawProductId);
        if (productId == null) {
            renderProductNotFound(request, response);
            return;
        }

        try {
            Optional<OwnerProdottoResponse> prodotto =
                    prodottoService.getProdottoProprietarioById(currentUser.getIdUtente(), productId);
            if (prodotto.isEmpty()) {
                renderProductNotFound(request, response);
                return;
            }

            Map<String, Object> model = new HashMap<>();
            model.put("prodotto", prodotto.get());
            addOwnerMenuFlashMessages(request, model);

            TemplateRenderer.render(request, response, "owner/product-detail.ftl", model);
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante il rendering dettaglio prodotto proprietario", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            TemplateRenderer.render(request, response, "owner/product-detail.ftl", Map.of(
                    "errorMessage", "Non e stato possibile caricare il prodotto in questo momento."
            ));
        }
    }

    private void renderProductImages(HttpServletRequest request,
                                     HttpServletResponse response,
                                     WebUserSession currentUser,
                                     String rawProductId) throws ServletException, IOException {
        Integer productId = parsePositiveInt(rawProductId);
        if (productId == null) {
            renderProductNotFound(request, response);
            return;
        }

        try {
            Optional<OwnerProdottoResponse> prodotto =
                    prodottoService.getProdottoProprietarioById(currentUser.getIdUtente(), productId);
            if (prodotto.isEmpty()) {
                renderProductNotFound(request, response);
                return;
            }

            List<OwnerImmagineProdottoResponse> immagini =
                    immagineProdottoService.getImmaginiProdottoProprietario(currentUser.getIdUtente(), productId);

            Map<String, Object> model = new HashMap<>();
            model.put("prodotto", prodotto.get());
            model.put("immagini", immagini);
            OwnerImmagineProdottoResponse immaginePrincipale = findPrimaryImage(immagini);
            if (immaginePrincipale != null) {
                model.put("immaginePrincipale", immaginePrincipale);
            }
            addOwnerMenuFlashMessages(request, model);

            TemplateRenderer.render(request, response, "owner/product-images.ftl", model);
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante il rendering immagini prodotto proprietario", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            TemplateRenderer.render(request, response, "owner/product-images.ftl", Map.of(
                    "errorMessage", "Non e stato possibile caricare le immagini in questo momento.",
                    "productId", productId
            ));
        }
    }

    private void renderNewProductForm(HttpServletRequest request,
                                      HttpServletResponse response,
                                      WebUserSession currentUser) throws ServletException, IOException {
        renderProductForm(
                request,
                response,
                currentUser,
                emptyForm(),
                List.of(),
                "Nuovo prodotto",
                "Crea prodotto",
                request.getContextPath() + "/proprietario/menu/prodotti",
                request.getContextPath() + "/proprietario/menu"
        );
    }

    private void renderEditProductForm(HttpServletRequest request,
                                       HttpServletResponse response,
                                       WebUserSession currentUser,
                                       String rawProductId) throws ServletException, IOException {
        Integer productId = parsePositiveInt(rawProductId);
        if (productId == null) {
            renderProductNotFound(request, response);
            return;
        }

        try {
            Optional<OwnerProdottoResponse> prodotto =
                    prodottoService.getProdottoProprietarioById(currentUser.getIdUtente(), productId);
            if (prodotto.isEmpty()) {
                renderProductNotFound(request, response);
                return;
            }

            renderProductForm(
                    request,
                    response,
                    currentUser,
                    toForm(prodotto.get()),
                    List.of(),
                    "Modifica prodotto",
                    "Salva modifiche",
                    request.getContextPath() + "/proprietario/menu/prodotti/" + productId + "/modifica",
                    request.getContextPath() + "/proprietario/menu/prodotti/" + productId
            );
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante il rendering form prodotto proprietario", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            renderProductForm(
                    request,
                    response,
                    currentUser,
                    emptyForm(),
                    List.of("Non e stato possibile caricare il prodotto in questo momento."),
                    "Modifica prodotto",
                    "Salva modifiche",
                    request.getContextPath() + "/proprietario/menu/prodotti/" + productId + "/modifica",
                    request.getContextPath() + "/proprietario/menu"
            );
        }
    }

    private void processCreateProduct(HttpServletRequest request,
                                      HttpServletResponse response,
                                      WebUserSession currentUser) throws ServletException, IOException {
        OwnerProdottoSaveRequest form = readForm(request);

        try {
            OwnerProdottoResponse prodotto =
                    prodottoService.creaProdottoProprietario(currentUser.getIdUtente(), form);
            SessionUtils.setFlashMessage(
                    request,
                    FLASH_OWNER_MENU_SUCCESS_ATTRIBUTE,
                    "Prodotto creato correttamente."
            );
            response.sendRedirect(request.getContextPath() + "/proprietario/menu/prodotti/" + prodotto.getId());
        } catch (IllegalArgumentException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            renderProductForm(
                    request,
                    response,
                    currentUser,
                    form,
                    List.of(exception.getMessage()),
                    "Nuovo prodotto",
                    "Crea prodotto",
                    request.getContextPath() + "/proprietario/menu/prodotti",
                    request.getContextPath() + "/proprietario/menu"
            );
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante creazione prodotto proprietario", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            renderProductForm(
                    request,
                    response,
                    currentUser,
                    form,
                    List.of("Non e stato possibile creare il prodotto in questo momento."),
                    "Nuovo prodotto",
                    "Crea prodotto",
                    request.getContextPath() + "/proprietario/menu/prodotti",
                    request.getContextPath() + "/proprietario/menu"
            );
        }
    }

    private void processUpdateProduct(HttpServletRequest request,
                                      HttpServletResponse response,
                                      WebUserSession currentUser,
                                      String rawProductId) throws ServletException, IOException {
        Integer productId = parsePositiveInt(rawProductId);
        if (productId == null) {
            renderProductNotFound(request, response);
            return;
        }

        OwnerProdottoSaveRequest form = readForm(request);
        String formAction = request.getContextPath() + "/proprietario/menu/prodotti/" + productId + "/modifica";
        String cancelUrl = request.getContextPath() + "/proprietario/menu/prodotti/" + productId;

        try {
            prodottoService.aggiornaProdottoProprietario(currentUser.getIdUtente(), productId, form);
            SessionUtils.setFlashMessage(
                    request,
                    FLASH_OWNER_MENU_SUCCESS_ATTRIBUTE,
                    "Prodotto aggiornato correttamente."
            );
            response.sendRedirect(cancelUrl);
        } catch (IllegalArgumentException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            renderProductForm(
                    request,
                    response,
                    currentUser,
                    form,
                    List.of(exception.getMessage()),
                    "Modifica prodotto",
                    "Salva modifiche",
                    formAction,
                    cancelUrl
            );
        } catch (NoSuchElementException exception) {
            renderProductNotFound(request, response);
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante aggiornamento prodotto proprietario", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            renderProductForm(
                    request,
                    response,
                    currentUser,
                    form,
                    List.of("Non e stato possibile aggiornare il prodotto in questo momento."),
                    "Modifica prodotto",
                    "Salva modifiche",
                    formAction,
                    cancelUrl
            );
        }
    }

    private void processUploadImage(HttpServletRequest request,
                                    HttpServletResponse response,
                                    WebUserSession currentUser,
                                    String rawProductId) throws ServletException, IOException {
        Integer productId = parsePositiveInt(rawProductId);
        if (productId == null) {
            renderProductNotFound(request, response);
            return;
        }

        Part imagePart;
        try {
            imagePart = request.getPart("immagine");
        } catch (IllegalStateException exception) {
            SessionUtils.setFlashMessage(
                    request,
                    FLASH_OWNER_MENU_ERROR_ATTRIBUTE,
                    "Il file immagine deve pesare al massimo 3 MB."
            );
            redirectToProductImages(request, response, productId);
            return;
        } catch (ServletException | IOException exception) {
            LOGGER.log(Level.WARNING, "Upload immagine prodotto non valido", exception);
            SessionUtils.setFlashMessage(
                    request,
                    FLASH_OWNER_MENU_ERROR_ATTRIBUTE,
                    "Upload immagine non valido."
            );
            redirectToProductImages(request, response, productId);
            return;
        }

        try {
            immagineProdottoService.caricaImmagineProdotto(
                    currentUser.getIdUtente(),
                    productId,
                    imagePart,
                    normalize(request.getParameter("testoAlternativo")),
                    request.getParameter("principale") != null
            );
            SessionUtils.setFlashMessage(
                    request,
                    FLASH_OWNER_MENU_SUCCESS_ATTRIBUTE,
                    "Immagine caricata correttamente."
            );
        } catch (IllegalArgumentException exception) {
            SessionUtils.setFlashMessage(request, FLASH_OWNER_MENU_ERROR_ATTRIBUTE, exception.getMessage());
        } catch (NoSuchElementException exception) {
            renderProductNotFound(request, response);
            return;
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
            return;
        } catch (IOException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante scrittura file immagine prodotto", exception);
            SessionUtils.setFlashMessage(
                    request,
                    FLASH_OWNER_MENU_ERROR_ATTRIBUTE,
                    "Non e stato possibile salvare il file immagine."
            );
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante upload immagine prodotto", exception);
            SessionUtils.setFlashMessage(
                    request,
                    FLASH_OWNER_MENU_ERROR_ATTRIBUTE,
                    "Non e stato possibile caricare l'immagine in questo momento."
            );
        }

        redirectToProductImages(request, response, productId);
    }

    private void processSetPrimaryImage(HttpServletRequest request,
                                        HttpServletResponse response,
                                        WebUserSession currentUser,
                                        String rawProductId,
                                        String rawImageId) throws ServletException, IOException {
        Integer productId = parsePositiveInt(rawProductId);
        Long imageId = parsePositiveLong(rawImageId);
        if (productId == null) {
            renderProductNotFound(request, response);
            return;
        }
        if (imageId == null) {
            SessionUtils.setFlashMessage(
                    request,
                    FLASH_OWNER_MENU_ERROR_ATTRIBUTE,
                    "Immagine prodotto non valida."
            );
            redirectToProductImages(request, response, productId);
            return;
        }

        try {
            immagineProdottoService.impostaImmaginePrincipale(currentUser.getIdUtente(), productId, imageId);
            SessionUtils.setFlashMessage(
                    request,
                    FLASH_OWNER_MENU_SUCCESS_ATTRIBUTE,
                    "Immagine principale aggiornata correttamente."
            );
        } catch (NoSuchElementException exception) {
            SessionUtils.setFlashMessage(
                    request,
                    FLASH_OWNER_MENU_ERROR_ATTRIBUTE,
                    "Immagine non trovata per il prodotto."
            );
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
            return;
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante aggiornamento immagine principale", exception);
            SessionUtils.setFlashMessage(
                    request,
                    FLASH_OWNER_MENU_ERROR_ATTRIBUTE,
                    "Non e stato possibile aggiornare l'immagine principale in questo momento."
            );
        }

        redirectToProductImages(request, response, productId);
    }

    private void processRemoveImage(HttpServletRequest request,
                                    HttpServletResponse response,
                                    WebUserSession currentUser,
                                    String rawProductId,
                                    String rawImageId) throws ServletException, IOException {
        Integer productId = parsePositiveInt(rawProductId);
        Long imageId = parsePositiveLong(rawImageId);
        if (productId == null) {
            renderProductNotFound(request, response);
            return;
        }
        if (imageId == null) {
            SessionUtils.setFlashMessage(
                    request,
                    FLASH_OWNER_MENU_ERROR_ATTRIBUTE,
                    "Immagine prodotto non valida."
            );
            redirectToProductImages(request, response, productId);
            return;
        }

        try {
            immagineProdottoService.rimuoviImmagineProdotto(currentUser.getIdUtente(), productId, imageId);
            SessionUtils.setFlashMessage(
                    request,
                    FLASH_OWNER_MENU_SUCCESS_ATTRIBUTE,
                    "Immagine rimossa correttamente."
            );
        } catch (NoSuchElementException exception) {
            SessionUtils.setFlashMessage(
                    request,
                    FLASH_OWNER_MENU_ERROR_ATTRIBUTE,
                    "Immagine non trovata per il prodotto."
            );
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
            return;
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante rimozione immagine prodotto", exception);
            SessionUtils.setFlashMessage(
                    request,
                    FLASH_OWNER_MENU_ERROR_ATTRIBUTE,
                    "Non e stato possibile rimuovere l'immagine in questo momento."
            );
        }

        redirectToProductImages(request, response, productId);
    }

    private void renderProductForm(HttpServletRequest request,
                                   HttpServletResponse response,
                                   WebUserSession currentUser,
                                   OwnerProdottoSaveRequest form,
                                   List<String> errors,
                                   String pageTitle,
                                   String submitLabel,
                                   String action,
                                   String cancelUrl) throws ServletException, IOException {
        Map<String, Object> model = new HashMap<>();
        model.put("form", form);
        model.put("errors", errors);
        model.put("pageTitle", pageTitle);
        model.put("submitLabel", submitLabel);
        model.put("action", action);
        model.put("cancelUrl", cancelUrl);

        try {
            model.put("categorie", prodottoService.getCategorieProdottoProprietario(currentUser.getIdUtente()));
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
            return;
        } catch (RuntimeException exception) {
            LOGGER.log(Level.WARNING, "Errore durante il caricamento categorie prodotto proprietario", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            model.put("categorie", List.of());
            model.put("loadError", "Non e stato possibile caricare le categorie prodotto.");
        }

        TemplateRenderer.render(request, response, "owner/product-form.ftl", model);
    }

    // Gestione caratteristiche configurabili del prodotto.
    private void renderProductCharacteristics(HttpServletRequest request,
                                              HttpServletResponse response,
                                              WebUserSession currentUser,
                                              String rawProductId) throws ServletException, IOException {
        Integer productId = parsePositiveInt(rawProductId);
        if (productId == null) {
            renderProductNotFound(request, response);
            return;
        }

        try {
            Optional<OwnerProdottoResponse> prodotto =
                    prodottoService.getProdottoProprietarioById(currentUser.getIdUtente(), productId);
            if (prodotto.isEmpty()) {
                renderProductNotFound(request, response);
                return;
            }

            Map<String, Object> model = new HashMap<>();
            model.put("prodotto", prodotto.get());
            model.put(
                    "caratteristiche",
                    caratteristicaService.getCaratteristicheProprietario(currentUser.getIdUtente(), productId)
            );
            addOwnerMenuFlashMessages(request, model);

            TemplateRenderer.render(request, response, "owner/product-characteristics.ftl", model);
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
        } catch (NoSuchElementException exception) {
            renderProductNotFound(request, response);
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante il rendering caratteristiche proprietario", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            TemplateRenderer.render(request, response, "owner/product-characteristics.ftl", Map.of(
                    "errorMessage", "Non e stato possibile caricare le caratteristiche in questo momento."
            ));
        }
    }

    private void renderNewCharacteristicForm(HttpServletRequest request,
                                             HttpServletResponse response,
                                             WebUserSession currentUser,
                                             String rawProductId) throws ServletException, IOException {
        Integer productId = parsePositiveInt(rawProductId);
        if (productId == null) {
            renderProductNotFound(request, response);
            return;
        }

        try {
            Optional<OwnerProdottoResponse> prodotto =
                    prodottoService.getProdottoProprietarioById(currentUser.getIdUtente(), productId);
            if (prodotto.isEmpty()) {
                renderProductNotFound(request, response);
                return;
            }

            renderCharacteristicForm(
                    request,
                    response,
                    currentUser,
                    prodotto.get(),
                    emptyCharacteristicForm(),
                    List.of(),
                    "Nuova caratteristica",
                    "Salva caratteristica",
                    request.getContextPath() + "/proprietario/menu/prodotti/" + productId + "/caratteristiche",
                    request.getContextPath() + "/proprietario/menu/prodotti/" + productId + "/caratteristiche"
            );
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante il rendering form caratteristica proprietario", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            TemplateRenderer.render(request, response, "owner/product-characteristic-form.ftl", Map.of(
                    "errorMessage", "Non e stato possibile caricare il form caratteristica in questo momento."
            ));
        }
    }

    private void renderEditCharacteristicForm(HttpServletRequest request,
                                              HttpServletResponse response,
                                              WebUserSession currentUser,
                                              String rawProductId,
                                              String rawCharacteristicId) throws ServletException, IOException {
        Integer productId = parsePositiveInt(rawProductId);
        Integer characteristicId = parsePositiveInt(rawCharacteristicId);
        if (productId == null) {
            renderProductNotFound(request, response);
            return;
        }
        if (characteristicId == null) {
            renderCharacteristicNotFound(request, response, productId);
            return;
        }

        try {
            Optional<OwnerProdottoResponse> prodotto =
                    prodottoService.getProdottoProprietarioById(currentUser.getIdUtente(), productId);
            if (prodotto.isEmpty()) {
                renderProductNotFound(request, response);
                return;
            }

            Optional<OwnerCaratteristicaResponse> caratteristica =
                    caratteristicaService.getCaratteristicaProprietarioById(
                            currentUser.getIdUtente(),
                            productId,
                            characteristicId
                    );
            if (caratteristica.isEmpty()) {
                renderCharacteristicNotFound(request, response, productId);
                return;
            }

            renderCharacteristicForm(
                    request,
                    response,
                    currentUser,
                    prodotto.get(),
                    toCharacteristicForm(caratteristica.get()),
                    List.of(),
                    "Modifica caratteristica",
                    "Salva modifiche",
                    request.getContextPath() + "/proprietario/menu/prodotti/" + productId
                            + "/caratteristiche/" + characteristicId + "/modifica",
                    request.getContextPath() + "/proprietario/menu/prodotti/" + productId + "/caratteristiche"
            );
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
        } catch (NoSuchElementException exception) {
            renderCharacteristicNotFound(request, response, productId);
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante il rendering modifica caratteristica proprietario", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            TemplateRenderer.render(request, response, "owner/product-characteristic-form.ftl", Map.of(
                    "errorMessage", "Non e stato possibile caricare la caratteristica in questo momento."
            ));
        }
    }

    private void processCreateCharacteristic(HttpServletRequest request,
                                             HttpServletResponse response,
                                             WebUserSession currentUser,
                                             String rawProductId) throws ServletException, IOException {
        Integer productId = parsePositiveInt(rawProductId);
        if (productId == null) {
            renderProductNotFound(request, response);
            return;
        }

        OwnerCaratteristicaSaveRequest form = readCharacteristicForm(request);
        String formAction = request.getContextPath() + "/proprietario/menu/prodotti/" + productId + "/caratteristiche";
        String cancelUrl = request.getContextPath() + "/proprietario/menu/prodotti/" + productId + "/caratteristiche";

        try {
            caratteristicaService.creaCaratteristicaProprietario(currentUser.getIdUtente(), productId, form);
            SessionUtils.setFlashMessage(
                    request,
                    FLASH_OWNER_MENU_SUCCESS_ATTRIBUTE,
                    "Caratteristica salvata correttamente."
            );
            response.sendRedirect(cancelUrl);
        } catch (IllegalArgumentException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            renderCharacteristicFormForProduct(
                    request,
                    response,
                    currentUser,
                    productId,
                    form,
                    List.of(exception.getMessage()),
                    "Nuova caratteristica",
                    "Salva caratteristica",
                    formAction,
                    cancelUrl
            );
        } catch (NoSuchElementException exception) {
            renderProductNotFound(request, response);
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante creazione caratteristica proprietario", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            renderCharacteristicFormForProduct(
                    request,
                    response,
                    currentUser,
                    productId,
                    form,
                    List.of("Non e stato possibile salvare la caratteristica in questo momento."),
                    "Nuova caratteristica",
                    "Salva caratteristica",
                    formAction,
                    cancelUrl
            );
        }
    }

    private void processUpdateCharacteristic(HttpServletRequest request,
                                             HttpServletResponse response,
                                             WebUserSession currentUser,
                                             String rawProductId,
                                             String rawCharacteristicId) throws ServletException, IOException {
        Integer productId = parsePositiveInt(rawProductId);
        Integer characteristicId = parsePositiveInt(rawCharacteristicId);
        if (productId == null) {
            renderProductNotFound(request, response);
            return;
        }
        if (characteristicId == null) {
            renderCharacteristicNotFound(request, response, productId);
            return;
        }

        OwnerCaratteristicaSaveRequest form = readCharacteristicForm(request);
        String formAction = request.getContextPath() + "/proprietario/menu/prodotti/" + productId
                + "/caratteristiche/" + characteristicId + "/modifica";
        String cancelUrl = request.getContextPath() + "/proprietario/menu/prodotti/" + productId + "/caratteristiche";

        try {
            caratteristicaService.aggiornaCaratteristicaProprietario(
                    currentUser.getIdUtente(),
                    productId,
                    characteristicId,
                    form
            );
            SessionUtils.setFlashMessage(
                    request,
                    FLASH_OWNER_MENU_SUCCESS_ATTRIBUTE,
                    "Caratteristica aggiornata correttamente."
            );
            response.sendRedirect(cancelUrl);
        } catch (IllegalArgumentException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            renderCharacteristicFormForProduct(
                    request,
                    response,
                    currentUser,
                    productId,
                    form,
                    List.of(exception.getMessage()),
                    "Modifica caratteristica",
                    "Salva modifiche",
                    formAction,
                    cancelUrl
            );
        } catch (NoSuchElementException exception) {
            renderCharacteristicNotFound(request, response, productId);
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante aggiornamento caratteristica proprietario", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            renderCharacteristicFormForProduct(
                    request,
                    response,
                    currentUser,
                    productId,
                    form,
                    List.of("Non e stato possibile aggiornare la caratteristica in questo momento."),
                    "Modifica caratteristica",
                    "Salva modifiche",
                    formAction,
                    cancelUrl
            );
        }
    }

    private void processRemoveCharacteristic(HttpServletRequest request,
                                             HttpServletResponse response,
                                             WebUserSession currentUser,
                                             String rawProductId,
                                             String rawCharacteristicId) throws ServletException, IOException {
        Integer productId = parsePositiveInt(rawProductId);
        Integer characteristicId = parsePositiveInt(rawCharacteristicId);
        if (productId == null) {
            renderProductNotFound(request, response);
            return;
        }
        if (characteristicId == null) {
            renderCharacteristicNotFound(request, response, productId);
            return;
        }

        try {
            caratteristicaService.rimuoviCaratteristicaProprietario(
                    currentUser.getIdUtente(),
                    productId,
                    characteristicId
            );
            SessionUtils.setFlashMessage(
                    request,
                    FLASH_OWNER_MENU_SUCCESS_ATTRIBUTE,
                    "Caratteristica disattivata correttamente."
            );
            response.sendRedirect(request.getContextPath()
                    + "/proprietario/menu/prodotti/" + productId + "/caratteristiche");
        } catch (NoSuchElementException exception) {
            renderCharacteristicNotFound(request, response, productId);
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante rimozione caratteristica proprietario", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            TemplateRenderer.render(request, response, "owner/product-characteristics.ftl", Map.of(
                    "errorMessage", "Non e stato possibile disattivare la caratteristica in questo momento."
            ));
        }
    }

    private void renderCharacteristicFormForProduct(HttpServletRequest request,
                                                    HttpServletResponse response,
                                                    WebUserSession currentUser,
                                                    int productId,
                                                    OwnerCaratteristicaSaveRequest form,
                                                    List<String> errors,
                                                    String pageTitle,
                                                    String submitLabel,
                                                    String action,
                                                    String cancelUrl) throws ServletException, IOException {
        Optional<OwnerProdottoResponse> prodotto =
                prodottoService.getProdottoProprietarioById(currentUser.getIdUtente(), productId);
        if (prodotto.isEmpty()) {
            renderProductNotFound(request, response);
            return;
        }

        renderCharacteristicForm(
                request,
                response,
                currentUser,
                prodotto.get(),
                form,
                errors,
                pageTitle,
                submitLabel,
                action,
                cancelUrl
        );
    }

    private void renderCharacteristicForm(HttpServletRequest request,
                                          HttpServletResponse response,
                                          WebUserSession currentUser,
                                          OwnerProdottoResponse prodotto,
                                          OwnerCaratteristicaSaveRequest form,
                                          List<String> errors,
                                          String pageTitle,
                                          String submitLabel,
                                          String action,
                                          String cancelUrl) throws ServletException, IOException {
        Map<String, Object> model = new HashMap<>();
        model.put("prodotto", prodotto);
        model.put("form", form);
        model.put("errors", errors);
        model.put("pageTitle", pageTitle);
        model.put("submitLabel", submitLabel);
        model.put("action", action);
        model.put("cancelUrl", cancelUrl);

        try {
            model.put(
                    "gruppi",
                    caratteristicaService.getGruppiCaratteristicheProprietario(
                            currentUser.getIdUtente(),
                            Math.toIntExact(prodotto.getId())
                    )
            );
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
            return;
        } catch (RuntimeException exception) {
            LOGGER.log(Level.WARNING, "Errore durante il caricamento gruppi caratteristiche", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            model.put("gruppi", List.of());
            model.put("loadError", "Non e stato possibile caricare i gruppi caratteristiche.");
        }

        TemplateRenderer.render(request, response, "owner/product-characteristic-form.ftl", model);
    }

    // Gestione dei gruppi che rendono obbligatorie o alternative le caratteristiche.
    private void renderProductCharacteristicGroups(HttpServletRequest request,
                                                   HttpServletResponse response,
                                                   WebUserSession currentUser,
                                                   String rawProductId) throws ServletException, IOException {
        Integer productId = parsePositiveInt(rawProductId);
        if (productId == null) {
            renderProductNotFound(request, response);
            return;
        }

        try {
            Optional<OwnerProdottoResponse> prodotto =
                    prodottoService.getProdottoProprietarioById(currentUser.getIdUtente(), productId);
            if (prodotto.isEmpty()) {
                renderProductNotFound(request, response);
                return;
            }

            Map<String, Object> model = new HashMap<>();
            model.put("prodotto", prodotto.get());
            model.put(
                    "gruppi",
                    gruppoCaratteristicheService.getGruppiCaratteristicheProprietario(
                            currentUser.getIdUtente(),
                            productId
                    )
            );
            addOwnerMenuFlashMessages(request, model);

            TemplateRenderer.render(request, response, "owner/product-characteristic-groups.ftl", model);
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
        } catch (NoSuchElementException exception) {
            renderProductNotFound(request, response);
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante il rendering gruppi caratteristiche proprietario", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            TemplateRenderer.render(request, response, "owner/product-characteristic-groups.ftl", Map.of(
                    "errorMessage", "Non e stato possibile caricare i gruppi caratteristiche in questo momento."
            ));
        }
    }

    private void renderNewCharacteristicGroupForm(HttpServletRequest request,
                                                  HttpServletResponse response,
                                                  WebUserSession currentUser,
                                                  String rawProductId) throws ServletException, IOException {
        Integer productId = parsePositiveInt(rawProductId);
        if (productId == null) {
            renderProductNotFound(request, response);
            return;
        }

        try {
            Optional<OwnerProdottoResponse> prodotto =
                    prodottoService.getProdottoProprietarioById(currentUser.getIdUtente(), productId);
            if (prodotto.isEmpty()) {
                renderProductNotFound(request, response);
                return;
            }

            renderCharacteristicGroupForm(
                    request,
                    response,
                    prodotto.get(),
                    emptyCharacteristicGroupForm(),
                    List.of(),
                    "Nuovo gruppo caratteristiche",
                    "Salva gruppo",
                    request.getContextPath() + "/proprietario/menu/prodotti/" + productId
                            + "/gruppi-caratteristiche",
                    request.getContextPath() + "/proprietario/menu/prodotti/" + productId
                            + "/gruppi-caratteristiche"
            );
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante il rendering form gruppo caratteristiche proprietario", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            TemplateRenderer.render(request, response, "owner/product-characteristic-group-form.ftl", Map.of(
                    "errorMessage", "Non e stato possibile caricare il form gruppo caratteristiche in questo momento."
            ));
        }
    }

    private void renderEditCharacteristicGroupForm(HttpServletRequest request,
                                                   HttpServletResponse response,
                                                   WebUserSession currentUser,
                                                   String rawProductId,
                                                   String rawGroupId) throws ServletException, IOException {
        Integer productId = parsePositiveInt(rawProductId);
        Long groupId = parsePositiveLong(rawGroupId);
        if (productId == null) {
            renderProductNotFound(request, response);
            return;
        }
        if (groupId == null) {
            renderCharacteristicGroupNotFound(request, response, productId);
            return;
        }

        try {
            Optional<OwnerProdottoResponse> prodotto =
                    prodottoService.getProdottoProprietarioById(currentUser.getIdUtente(), productId);
            if (prodotto.isEmpty()) {
                renderProductNotFound(request, response);
                return;
            }

            Optional<OwnerGruppoCaratteristicheResponse> gruppo =
                    gruppoCaratteristicheService.getGruppoCaratteristicheProprietarioById(
                            currentUser.getIdUtente(),
                            productId,
                            groupId
                    );
            if (gruppo.isEmpty()) {
                renderCharacteristicGroupNotFound(request, response, productId);
                return;
            }

            renderCharacteristicGroupForm(
                    request,
                    response,
                    prodotto.get(),
                    toCharacteristicGroupForm(gruppo.get()),
                    List.of(),
                    "Modifica gruppo caratteristiche",
                    "Salva modifiche",
                    request.getContextPath() + "/proprietario/menu/prodotti/" + productId
                            + "/gruppi-caratteristiche/" + groupId + "/modifica",
                    request.getContextPath() + "/proprietario/menu/prodotti/" + productId
                            + "/gruppi-caratteristiche"
            );
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
        } catch (NoSuchElementException exception) {
            renderCharacteristicGroupNotFound(request, response, productId);
        } catch (RuntimeException exception) {
            LOGGER.log(
                    Level.SEVERE,
                    "Errore durante il rendering modifica gruppo caratteristiche proprietario",
                    exception
            );
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            TemplateRenderer.render(request, response, "owner/product-characteristic-group-form.ftl", Map.of(
                    "errorMessage", "Non e stato possibile caricare il gruppo caratteristiche in questo momento."
            ));
        }
    }

    private void processCreateCharacteristicGroup(HttpServletRequest request,
                                                  HttpServletResponse response,
                                                  WebUserSession currentUser,
                                                  String rawProductId) throws ServletException, IOException {
        Integer productId = parsePositiveInt(rawProductId);
        if (productId == null) {
            renderProductNotFound(request, response);
            return;
        }

        OwnerGruppoCaratteristicheSaveRequest form = readCharacteristicGroupForm(request);
        String formAction = request.getContextPath() + "/proprietario/menu/prodotti/" + productId
                + "/gruppi-caratteristiche";
        String cancelUrl = request.getContextPath() + "/proprietario/menu/prodotti/" + productId
                + "/gruppi-caratteristiche";

        try {
            gruppoCaratteristicheService.creaGruppoCaratteristicheProprietario(
                    currentUser.getIdUtente(),
                    productId,
                    form
            );
            SessionUtils.setFlashMessage(
                    request,
                    FLASH_OWNER_MENU_SUCCESS_ATTRIBUTE,
                    "Gruppo caratteristiche salvato correttamente."
            );
            response.sendRedirect(cancelUrl);
        } catch (IllegalArgumentException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            renderCharacteristicGroupFormForProduct(
                    request,
                    response,
                    currentUser,
                    productId,
                    form,
                    List.of(exception.getMessage()),
                    "Nuovo gruppo caratteristiche",
                    "Salva gruppo",
                    formAction,
                    cancelUrl
            );
        } catch (NoSuchElementException exception) {
            renderProductNotFound(request, response);
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante creazione gruppo caratteristiche proprietario", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            renderCharacteristicGroupFormForProduct(
                    request,
                    response,
                    currentUser,
                    productId,
                    form,
                    List.of("Non e stato possibile salvare il gruppo caratteristiche in questo momento."),
                    "Nuovo gruppo caratteristiche",
                    "Salva gruppo",
                    formAction,
                    cancelUrl
            );
        }
    }

    private void processUpdateCharacteristicGroup(HttpServletRequest request,
                                                  HttpServletResponse response,
                                                  WebUserSession currentUser,
                                                  String rawProductId,
                                                  String rawGroupId) throws ServletException, IOException {
        Integer productId = parsePositiveInt(rawProductId);
        Long groupId = parsePositiveLong(rawGroupId);
        if (productId == null) {
            renderProductNotFound(request, response);
            return;
        }
        if (groupId == null) {
            renderCharacteristicGroupNotFound(request, response, productId);
            return;
        }

        OwnerGruppoCaratteristicheSaveRequest form = readCharacteristicGroupForm(request);
        String formAction = request.getContextPath() + "/proprietario/menu/prodotti/" + productId
                + "/gruppi-caratteristiche/" + groupId + "/modifica";
        String cancelUrl = request.getContextPath() + "/proprietario/menu/prodotti/" + productId
                + "/gruppi-caratteristiche";

        try {
            gruppoCaratteristicheService.aggiornaGruppoCaratteristicheProprietario(
                    currentUser.getIdUtente(),
                    productId,
                    groupId,
                    form
            );
            SessionUtils.setFlashMessage(
                    request,
                    FLASH_OWNER_MENU_SUCCESS_ATTRIBUTE,
                    "Gruppo caratteristiche aggiornato correttamente."
            );
            response.sendRedirect(cancelUrl);
        } catch (IllegalArgumentException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            renderCharacteristicGroupFormForProduct(
                    request,
                    response,
                    currentUser,
                    productId,
                    form,
                    List.of(exception.getMessage()),
                    "Modifica gruppo caratteristiche",
                    "Salva modifiche",
                    formAction,
                    cancelUrl
            );
        } catch (NoSuchElementException exception) {
            renderCharacteristicGroupNotFound(request, response, productId);
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante aggiornamento gruppo caratteristiche proprietario", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            renderCharacteristicGroupFormForProduct(
                    request,
                    response,
                    currentUser,
                    productId,
                    form,
                    List.of("Non e stato possibile aggiornare il gruppo caratteristiche in questo momento."),
                    "Modifica gruppo caratteristiche",
                    "Salva modifiche",
                    formAction,
                    cancelUrl
            );
        }
    }

    private void processRemoveCharacteristicGroup(HttpServletRequest request,
                                                  HttpServletResponse response,
                                                  WebUserSession currentUser,
                                                  String rawProductId,
                                                  String rawGroupId) throws ServletException, IOException {
        Integer productId = parsePositiveInt(rawProductId);
        Long groupId = parsePositiveLong(rawGroupId);
        if (productId == null) {
            renderProductNotFound(request, response);
            return;
        }
        if (groupId == null) {
            renderCharacteristicGroupNotFound(request, response, productId);
            return;
        }

        String redirectUrl = request.getContextPath() + "/proprietario/menu/prodotti/" + productId
                + "/gruppi-caratteristiche";

        try {
            gruppoCaratteristicheService.rimuoviGruppoCaratteristicheProprietario(
                    currentUser.getIdUtente(),
                    productId,
                    groupId
            );
            SessionUtils.setFlashMessage(
                    request,
                    FLASH_OWNER_MENU_SUCCESS_ATTRIBUTE,
                    "Gruppo caratteristiche disattivato correttamente."
            );
            response.sendRedirect(redirectUrl);
        } catch (IllegalArgumentException exception) {
            SessionUtils.setFlashMessage(
                    request,
                    FLASH_OWNER_MENU_ERROR_ATTRIBUTE,
                    exception.getMessage()
            );
            response.sendRedirect(redirectUrl);
        } catch (NoSuchElementException exception) {
            renderCharacteristicGroupNotFound(request, response, productId);
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante rimozione gruppo caratteristiche proprietario", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            TemplateRenderer.render(request, response, "owner/product-characteristic-groups.ftl", Map.of(
                    "errorMessage", "Non e stato possibile disattivare il gruppo caratteristiche in questo momento."
            ));
        }
    }

    private void renderCharacteristicGroupFormForProduct(HttpServletRequest request,
                                                         HttpServletResponse response,
                                                         WebUserSession currentUser,
                                                         int productId,
                                                         OwnerGruppoCaratteristicheSaveRequest form,
                                                         List<String> errors,
                                                         String pageTitle,
                                                         String submitLabel,
                                                         String action,
                                                         String cancelUrl) throws ServletException, IOException {
        Optional<OwnerProdottoResponse> prodotto =
                prodottoService.getProdottoProprietarioById(currentUser.getIdUtente(), productId);
        if (prodotto.isEmpty()) {
            renderProductNotFound(request, response);
            return;
        }

        renderCharacteristicGroupForm(
                request,
                response,
                prodotto.get(),
                form,
                errors,
                pageTitle,
                submitLabel,
                action,
                cancelUrl
        );
    }

    private void renderCharacteristicGroupForm(HttpServletRequest request,
                                               HttpServletResponse response,
                                               OwnerProdottoResponse prodotto,
                                               OwnerGruppoCaratteristicheSaveRequest form,
                                               List<String> errors,
                                               String pageTitle,
                                               String submitLabel,
                                               String action,
                                               String cancelUrl) throws ServletException, IOException {
        Map<String, Object> model = new HashMap<>();
        model.put("prodotto", prodotto);
        model.put("form", form);
        model.put("errors", errors);
        model.put("pageTitle", pageTitle);
        model.put("submitLabel", submitLabel);
        model.put("action", action);
        model.put("cancelUrl", cancelUrl);

        TemplateRenderer.render(request, response, "owner/product-characteristic-group-form.ftl", model);
    }

    // Gestione ingredienti: associazione al prodotto e catalogo ingredienti owner.
    private void renderProductIngredients(HttpServletRequest request,
                                          HttpServletResponse response,
                                          WebUserSession currentUser,
                                          String rawProductId) throws ServletException, IOException {
        Integer productId = parsePositiveInt(rawProductId);
        if (productId == null) {
            renderProductNotFound(request, response);
            return;
        }

        try {
            Optional<OwnerProdottoResponse> prodotto =
                    prodottoService.getProdottoProprietarioById(currentUser.getIdUtente(), productId);
            if (prodotto.isEmpty()) {
                renderProductNotFound(request, response);
                return;
            }

            Map<String, Object> model = new HashMap<>();
            model.put("prodotto", prodotto.get());
            model.put(
                    "ingredienti",
                    ingredienteService.getIngredientiProprietario(currentUser.getIdUtente(), productId)
            );
            addOwnerMenuFlashMessages(request, model);

            TemplateRenderer.render(request, response, "owner/product-ingredients.ftl", model);
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
        } catch (NoSuchElementException exception) {
            renderProductNotFound(request, response);
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante il rendering ingredienti proprietario", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            TemplateRenderer.render(request, response, "owner/product-ingredients.ftl", Map.of(
                    "errorMessage", "Non e stato possibile caricare gli ingredienti in questo momento."
            ));
        }
    }

    private void renderNewIngredientForm(HttpServletRequest request,
                                         HttpServletResponse response,
                                         WebUserSession currentUser,
                                         String rawProductId) throws ServletException, IOException {
        Integer productId = parsePositiveInt(rawProductId);
        if (productId == null) {
            renderProductNotFound(request, response);
            return;
        }

        try {
            Optional<OwnerProdottoResponse> prodotto =
                    prodottoService.getProdottoProprietarioById(currentUser.getIdUtente(), productId);
            if (prodotto.isEmpty()) {
                renderProductNotFound(request, response);
                return;
            }

            renderIngredientForm(
                    request,
                    response,
                    currentUser,
                    prodotto.get(),
                    emptyIngredientForm(),
                    List.of(),
                    "Nuovo ingrediente",
                    "Salva ingrediente",
                    request.getContextPath() + "/proprietario/menu/prodotti/" + productId + "/ingredienti",
                    request.getContextPath() + "/proprietario/menu/prodotti/" + productId + "/ingredienti",
                    true
            );
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante il rendering form ingrediente proprietario", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            TemplateRenderer.render(request, response, "owner/product-ingredient-form.ftl", Map.of(
                    "errorMessage", "Non e stato possibile caricare il form ingrediente in questo momento."
            ));
        }
    }

    private void renderEditIngredientForm(HttpServletRequest request,
                                          HttpServletResponse response,
                                          WebUserSession currentUser,
                                          String rawProductId,
                                          String rawIngredientId) throws ServletException, IOException {
        Integer productId = parsePositiveInt(rawProductId);
        Long ingredientId = parsePositiveLong(rawIngredientId);
        if (productId == null) {
            renderProductNotFound(request, response);
            return;
        }
        if (ingredientId == null) {
            renderIngredientNotFound(request, response, productId);
            return;
        }

        try {
            Optional<OwnerProdottoResponse> prodotto =
                    prodottoService.getProdottoProprietarioById(currentUser.getIdUtente(), productId);
            if (prodotto.isEmpty()) {
                renderProductNotFound(request, response);
                return;
            }

            Optional<OwnerIngredienteResponse> ingrediente =
                    ingredienteService.getIngredienteProprietarioById(
                            currentUser.getIdUtente(),
                            productId,
                            ingredientId
                    );
            if (ingrediente.isEmpty()) {
                renderIngredientNotFound(request, response, productId);
                return;
            }

            renderIngredientForm(
                    request,
                    response,
                    currentUser,
                    prodotto.get(),
                    toIngredientForm(ingrediente.get()),
                    List.of(),
                    "Modifica ingrediente",
                    "Salva modifiche",
                    request.getContextPath() + "/proprietario/menu/prodotti/" + productId
                            + "/ingredienti/" + ingredientId + "/modifica",
                    request.getContextPath() + "/proprietario/menu/prodotti/" + productId + "/ingredienti",
                    false
            );
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
        } catch (NoSuchElementException exception) {
            renderIngredientNotFound(request, response, productId);
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante il rendering modifica ingrediente proprietario", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            TemplateRenderer.render(request, response, "owner/product-ingredient-form.ftl", Map.of(
                    "errorMessage", "Non e stato possibile caricare l'ingrediente in questo momento."
            ));
        }
    }

    private void processCreateIngredient(HttpServletRequest request,
                                         HttpServletResponse response,
                                         WebUserSession currentUser,
                                         String rawProductId) throws ServletException, IOException {
        Integer productId = parsePositiveInt(rawProductId);
        if (productId == null) {
            renderProductNotFound(request, response);
            return;
        }

        OwnerIngredienteSaveRequest form = readIngredientForm(request);
        String formAction = request.getContextPath() + "/proprietario/menu/prodotti/" + productId + "/ingredienti";
        String cancelUrl = request.getContextPath() + "/proprietario/menu/prodotti/" + productId + "/ingredienti";

        try {
            ingredienteService.creaIngredienteProprietario(currentUser.getIdUtente(), productId, form);
            SessionUtils.setFlashMessage(
                    request,
                    FLASH_OWNER_MENU_SUCCESS_ATTRIBUTE,
                    "Ingrediente salvato correttamente."
            );
            response.sendRedirect(cancelUrl);
        } catch (IllegalArgumentException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            renderIngredientFormForProduct(
                    request,
                    response,
                    currentUser,
                    productId,
                    form,
                    List.of(exception.getMessage()),
                    "Nuovo ingrediente",
                    "Salva ingrediente",
                    formAction,
                    cancelUrl,
                    true
            );
        } catch (NoSuchElementException exception) {
            renderProductNotFound(request, response);
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante salvataggio ingrediente proprietario", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            renderIngredientFormForProduct(
                    request,
                    response,
                    currentUser,
                    productId,
                    form,
                    List.of("Non e stato possibile salvare l'ingrediente in questo momento."),
                    "Nuovo ingrediente",
                    "Salva ingrediente",
                    formAction,
                    cancelUrl,
                    true
            );
        }
    }

    private void processUpdateIngredient(HttpServletRequest request,
                                         HttpServletResponse response,
                                         WebUserSession currentUser,
                                         String rawProductId,
                                         String rawIngredientId) throws ServletException, IOException {
        Integer productId = parsePositiveInt(rawProductId);
        Long ingredientId = parsePositiveLong(rawIngredientId);
        if (productId == null) {
            renderProductNotFound(request, response);
            return;
        }
        if (ingredientId == null) {
            renderIngredientNotFound(request, response, productId);
            return;
        }

        OwnerIngredienteSaveRequest form = readIngredientForm(request);
        String formAction = request.getContextPath() + "/proprietario/menu/prodotti/" + productId
                + "/ingredienti/" + ingredientId + "/modifica";
        String cancelUrl = request.getContextPath() + "/proprietario/menu/prodotti/" + productId + "/ingredienti";

        try {
            ingredienteService.aggiornaIngredienteProprietario(
                    currentUser.getIdUtente(),
                    productId,
                    ingredientId,
                    form
            );
            SessionUtils.setFlashMessage(
                    request,
                    FLASH_OWNER_MENU_SUCCESS_ATTRIBUTE,
                    "Ingrediente aggiornato correttamente."
            );
            response.sendRedirect(cancelUrl);
        } catch (IllegalArgumentException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            renderIngredientFormForProduct(
                    request,
                    response,
                    currentUser,
                    productId,
                    form,
                    List.of(exception.getMessage()),
                    "Modifica ingrediente",
                    "Salva modifiche",
                    formAction,
                    cancelUrl,
                    false
            );
        } catch (NoSuchElementException exception) {
            renderIngredientNotFound(request, response, productId);
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante aggiornamento ingrediente proprietario", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            renderIngredientFormForProduct(
                    request,
                    response,
                    currentUser,
                    productId,
                    form,
                    List.of("Non e stato possibile aggiornare l'ingrediente in questo momento."),
                    "Modifica ingrediente",
                    "Salva modifiche",
                    formAction,
                    cancelUrl,
                    false
            );
        }
    }

    private void processRemoveIngredient(HttpServletRequest request,
                                         HttpServletResponse response,
                                         WebUserSession currentUser,
                                         String rawProductId,
                                         String rawIngredientId) throws ServletException, IOException {
        Integer productId = parsePositiveInt(rawProductId);
        Long ingredientId = parsePositiveLong(rawIngredientId);
        if (productId == null) {
            renderProductNotFound(request, response);
            return;
        }
        if (ingredientId == null) {
            renderIngredientNotFound(request, response, productId);
            return;
        }

        try {
            ingredienteService.rimuoviIngredienteProprietario(
                    currentUser.getIdUtente(),
                    productId,
                    ingredientId
            );
            SessionUtils.setFlashMessage(
                    request,
                    FLASH_OWNER_MENU_SUCCESS_ATTRIBUTE,
                    "Ingrediente rimosso dal prodotto correttamente."
            );
            response.sendRedirect(request.getContextPath()
                    + "/proprietario/menu/prodotti/" + productId + "/ingredienti");
        } catch (NoSuchElementException exception) {
            renderIngredientNotFound(request, response, productId);
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante rimozione ingrediente proprietario", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            TemplateRenderer.render(request, response, "owner/product-ingredients.ftl", Map.of(
                    "errorMessage", "Non e stato possibile rimuovere l'ingrediente in questo momento."
            ));
        }
    }

    private void renderIngredientFormForProduct(HttpServletRequest request,
                                                HttpServletResponse response,
                                                WebUserSession currentUser,
                                                int productId,
                                                OwnerIngredienteSaveRequest form,
                                                List<String> errors,
                                                String pageTitle,
                                                String submitLabel,
                                                String action,
                                                String cancelUrl,
                                                boolean showCatalogSelect) throws ServletException, IOException {
        Optional<OwnerProdottoResponse> prodotto =
                prodottoService.getProdottoProprietarioById(currentUser.getIdUtente(), productId);
        if (prodotto.isEmpty()) {
            renderProductNotFound(request, response);
            return;
        }

        renderIngredientForm(
                request,
                response,
                currentUser,
                prodotto.get(),
                form,
                errors,
                pageTitle,
                submitLabel,
                action,
                cancelUrl,
                showCatalogSelect
        );
    }

    private void renderIngredientForm(HttpServletRequest request,
                                      HttpServletResponse response,
                                      WebUserSession currentUser,
                                      OwnerProdottoResponse prodotto,
                                      OwnerIngredienteSaveRequest form,
                                      List<String> errors,
                                      String pageTitle,
                                      String submitLabel,
                                      String action,
                                      String cancelUrl,
                                      boolean showCatalogSelect) throws ServletException, IOException {
        Map<String, Object> model = new HashMap<>();
        model.put("prodotto", prodotto);
        model.put("form", form);
        model.put("errors", errors);
        model.put("pageTitle", pageTitle);
        model.put("submitLabel", submitLabel);
        model.put("action", action);
        model.put("cancelUrl", cancelUrl);
        model.put("showCatalogSelect", showCatalogSelect);

        try {
            if (showCatalogSelect) {
                model.put(
                        "ingredientiCatalogo",
                        ingredienteService.getIngredientiCatalogoProprietario(currentUser.getIdUtente())
                );
            } else {
                model.put("ingredientiCatalogo", List.of());
            }
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
            return;
        } catch (RuntimeException exception) {
            LOGGER.log(Level.WARNING, "Errore durante il caricamento catalogo ingredienti", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            model.put("ingredientiCatalogo", List.of());
            model.put("loadError", "Non e stato possibile caricare il catalogo ingredienti.");
        }

        TemplateRenderer.render(request, response, "owner/product-ingredient-form.ftl", model);
    }

    // Utility comuni per flash message, form binding e parsing dei path owner.
    private OwnerImmagineProdottoResponse findPrimaryImage(List<OwnerImmagineProdottoResponse> immagini) {
        if (immagini == null || immagini.isEmpty()) {
            return null;
        }

        for (OwnerImmagineProdottoResponse immagine : immagini) {
            if (immagine.isPrincipale()) {
                return immagine;
            }
        }
        return immagini.get(0);
    }

    private void redirectToProductImages(HttpServletRequest request,
                                         HttpServletResponse response,
                                         int productId) throws IOException {
        response.sendRedirect(request.getContextPath()
                + "/proprietario/menu/prodotti/" + productId + "/immagini");
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

    private void renderProductNotFound(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        TemplateRenderer.render(request, response, "owner/product-detail.ftl", Map.of(
                "errorMessage", "Prodotto non trovato."
        ));
    }

    private void renderCharacteristicNotFound(HttpServletRequest request,
                                              HttpServletResponse response,
                                              int productId) throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        TemplateRenderer.render(request, response, "owner/product-characteristics.ftl", Map.of(
                "errorMessage", "Caratteristica non trovata.",
                "productId", productId
        ));
    }

    private void renderCharacteristicGroupNotFound(HttpServletRequest request,
                                                   HttpServletResponse response,
                                                   int productId) throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        TemplateRenderer.render(request, response, "owner/product-characteristic-groups.ftl", Map.of(
                "errorMessage", "Gruppo caratteristiche non trovato.",
                "productId", productId
        ));
    }

    private void renderIngredientNotFound(HttpServletRequest request,
                                          HttpServletResponse response,
                                          int productId) throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        TemplateRenderer.render(request, response, "owner/product-ingredients.ftl", Map.of(
                "errorMessage", "Ingrediente non trovato.",
                "productId", productId
        ));
    }

    private void addOwnerMenuFlashMessages(HttpServletRequest request, Map<String, Object> model) {
        String successMessage = SessionUtils.consumeFlashMessage(request, FLASH_OWNER_MENU_SUCCESS_ATTRIBUTE);
        if (successMessage != null) {
            model.put("successMessage", successMessage);
        }
        String errorMessage = SessionUtils.consumeFlashMessage(request, FLASH_OWNER_MENU_ERROR_ATTRIBUTE);
        if (errorMessage != null) {
            model.put("errorMessage", errorMessage);
        }
    }

    private OwnerProdottoSaveRequest readForm(HttpServletRequest request) {
        return new OwnerProdottoSaveRequest(
                normalize(request.getParameter("nome")),
                normalize(request.getParameter("descrizione")),
                normalize(request.getParameter("prezzoBase")),
                normalize(request.getParameter("minutiPreparazione")),
                normalize(request.getParameter("idCategoria")),
                normalize(request.getParameter("descrizionePreparazione")),
                request.getParameter("attivo") != null ? "true" : "false"
        );
    }

    private OwnerProdottoSaveRequest emptyForm() {
        return new OwnerProdottoSaveRequest("", "", "", "", "", "", "true");
    }

    private OwnerCaratteristicaSaveRequest readCharacteristicForm(HttpServletRequest request) {
        return new OwnerCaratteristicaSaveRequest(
                normalize(request.getParameter("nome")),
                normalize(request.getParameter("descrizione")),
                normalize(request.getParameter("differenzaPrezzo")),
                normalize(request.getParameter("idGruppoCaratteristiche")),
                request.getParameter("selezionataDefault") != null ? "true" : "false",
                request.getParameter("attiva") != null ? "true" : "false"
        );
    }

    private OwnerCaratteristicaSaveRequest emptyCharacteristicForm() {
        return new OwnerCaratteristicaSaveRequest("", "", "0.00", "", "false", "true");
    }

    private OwnerGruppoCaratteristicheSaveRequest readCharacteristicGroupForm(HttpServletRequest request) {
        return new OwnerGruppoCaratteristicheSaveRequest(
                normalize(request.getParameter("nome")),
                normalize(request.getParameter("descrizione")),
                request.getParameter("obbligatorio") != null ? "true" : "false",
                request.getParameter("attivo") != null ? "true" : "false"
        );
    }

    private OwnerGruppoCaratteristicheSaveRequest emptyCharacteristicGroupForm() {
        return new OwnerGruppoCaratteristicheSaveRequest("", "", "false", "true");
    }

    private OwnerIngredienteSaveRequest readIngredientForm(HttpServletRequest request) {
        return new OwnerIngredienteSaveRequest(
                normalize(request.getParameter("idIngrediente")),
                normalize(request.getParameter("nome")),
                normalize(request.getParameter("quantita")),
                normalize(request.getParameter("unitaMisura")),
                request.getParameter("allergene") != null ? "true" : "false",
                request.getParameter("attivo") != null ? "true" : "false"
        );
    }

    private OwnerIngredienteSaveRequest emptyIngredientForm() {
        return new OwnerIngredienteSaveRequest("", "", "", "", "false", "true");
    }

    private OwnerProdottoSaveRequest toForm(OwnerProdottoResponse prodotto) {
        return new OwnerProdottoSaveRequest(
                prodotto.getNome(),
                prodotto.getDescrizione(),
                formatDecimal(prodotto.getPrezzoBase()),
                Integer.toString(prodotto.getMinutiPreparazione()),
                prodotto.getIdCategoria() != null ? prodotto.getIdCategoria().toString() : "",
                prodotto.getDescrizionePreparazione(),
                prodotto.isAttivo() ? "true" : "false"
        );
    }

    private OwnerCaratteristicaSaveRequest toCharacteristicForm(OwnerCaratteristicaResponse caratteristica) {
        return new OwnerCaratteristicaSaveRequest(
                caratteristica.getNome(),
                caratteristica.getDescrizione(),
                formatDecimal(caratteristica.getDifferenzaPrezzo()),
                caratteristica.getIdGruppoCaratteristiche() != null
                        ? caratteristica.getIdGruppoCaratteristiche().toString()
                        : "",
                caratteristica.isSelezionataDefault() ? "true" : "false",
                caratteristica.isAttiva() ? "true" : "false"
        );
    }

    private OwnerGruppoCaratteristicheSaveRequest toCharacteristicGroupForm(
            OwnerGruppoCaratteristicheResponse gruppo) {
        return new OwnerGruppoCaratteristicheSaveRequest(
                gruppo.getNome(),
                gruppo.getDescrizione(),
                gruppo.isObbligatorio() ? "true" : "false",
                gruppo.isAttivo() ? "true" : "false"
        );
    }

    private OwnerIngredienteSaveRequest toIngredientForm(OwnerIngredienteResponse ingrediente) {
        return new OwnerIngredienteSaveRequest(
                Long.toString(ingrediente.getId()),
                ingrediente.getNome(),
                formatDecimal(ingrediente.getQuantita()),
                ingrediente.getUnitaMisura(),
                ingrediente.isAllergene() ? "true" : "false",
                ingrediente.isAttivo() ? "true" : "false"
        );
    }

    private List<String> getPathSegments(String pathInfo) {
        if (pathInfo == null || pathInfo.isBlank() || "/".equals(pathInfo)) {
            return List.of();
        }

        String normalizedPath = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        List<String> segments = new ArrayList<>();
        for (String segment : normalizedPath.split("/")) {
            if (!segment.isBlank()) {
                segments.add(segment);
            }
        }
        return segments;
    }

    private Integer parsePositiveInt(String rawId) {
        try {
            int id = Integer.parseInt(rawId);
            return id > 0 ? id : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private Long parsePositiveLong(String rawId) {
        try {
            long id = Long.parseLong(rawId);
            return id > 0 ? id : null;
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

    private String formatDecimal(BigDecimal value) {
        return value != null ? value.toPlainString() : "";
    }
}
