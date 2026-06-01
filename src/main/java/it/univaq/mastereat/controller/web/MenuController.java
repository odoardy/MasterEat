package it.univaq.mastereat.controller.web;

import it.univaq.mastereat.dto.common.MenuPubblicoResponse;
import it.univaq.mastereat.dto.common.ProdottoPubblicoResponse;
import it.univaq.mastereat.dto.web.customer.WebProductCharacteristicGroup;
import it.univaq.mastereat.model.Caratteristica;
import it.univaq.mastereat.service.CaratteristicaService;
import it.univaq.mastereat.service.ProdottoService;
import it.univaq.mastereat.util.CartSessionUtils;
import it.univaq.mastereat.util.TemplateRenderer;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@WebServlet(name = "MenuController", urlPatterns = {"/menu", "/prodotti", "/prodotti/*"})
public class MenuController extends HttpServlet {

    private final ProdottoService prodottoService = new ProdottoService();
    private final CaratteristicaService caratteristicaService = new CaratteristicaService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (isProductDetailRequest(request)) {
            renderProductDetail(request, response);
            return;
        }

        renderMenu(request, response);
    }

    private boolean isProductDetailRequest(HttpServletRequest request) {
        return "/prodotti".equals(request.getServletPath())
                && request.getPathInfo() != null
                && !"/".equals(request.getPathInfo());
    }

    private void renderMenu(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String query = normalize(request.getParameter("q"));
        String prezzoMinParam = normalize(request.getParameter("prezzoMin"));
        String prezzoMaxParam = normalize(request.getParameter("prezzoMax"));
        boolean hasFilters = query != null || prezzoMinParam != null || prezzoMaxParam != null;

        MenuPubblicoResponse menu = new MenuPubblicoResponse();
        List<ProdottoPubblicoResponse> prodottiFiltrati = List.of();
        String filterError = null;
        String loadError = null;

        try {
            if (hasFilters) {
                BigDecimal prezzoMin = parseBigDecimal(prezzoMinParam, "prezzo minimo");
                BigDecimal prezzoMax = parseBigDecimal(prezzoMaxParam, "prezzo massimo");
                prodottiFiltrati = prodottoService.cercaProdottiPubblici(query, prezzoMin, prezzoMax);
            } else {
                menu = prodottoService.getMenuPubblico();
            }
        } catch (IllegalArgumentException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            hasFilters = false;
            filterError = exception.getMessage();
            menu = loadMenuFallback(response);
        } catch (RuntimeException exception) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            loadError = "Non e stato possibile caricare il men\u00f9 in questo momento.";
        }

        Map<String, Object> model = new HashMap<>();
        model.put("menu", menu);
        model.put("prodottiFiltrati", prodottiFiltrati);
        model.put("hasFilters", hasFilters);
        model.put("q", valueOrEmpty(query));
        model.put("prezzoMin", valueOrEmpty(prezzoMinParam));
        model.put("prezzoMax", valueOrEmpty(prezzoMaxParam));
        if (filterError != null) {
            model.put("filterError", filterError);
        }
        if (loadError != null) {
            model.put("loadError", loadError);
        }

        TemplateRenderer.render(request, response, "public/menu.ftl", model);
    }

    private MenuPubblicoResponse loadMenuFallback(HttpServletResponse response) {
        try {
            return prodottoService.getMenuPubblico();
        } catch (RuntimeException exception) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return new MenuPubblicoResponse();
        }
    }

    private void renderProductDetail(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer productId = parseProductId(request.getPathInfo());
        if (productId == null) {
            renderProductNotFound(request, response, null);
            return;
        }

        try {
            Optional<ProdottoPubblicoResponse> prodotto = prodottoService.getProdottoPubblicoById(productId);
            if (prodotto.isEmpty()) {
                renderProductNotFound(request, response, productId);
                return;
            }

            Map<String, Object> model = new HashMap<>();
            model.put("prodotto", prodotto.get());
            addProductCharacteristics(model, productId);
            addCartFlashMessages(request, model);

            TemplateRenderer.render(request, response, "public/product-detail.ftl", model);
        } catch (RuntimeException exception) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            TemplateRenderer.render(request, response, "public/product-detail.ftl", Map.of(
                    "loadError", "Non e stato possibile caricare il prodotto in questo momento."
            ));
        }
    }

    private void addProductCharacteristics(Map<String, Object> model, int productId) {
        List<Caratteristica> caratteristiche = caratteristicaService.getCaratteristicheByProdottoId(productId);
        List<Caratteristica> caratteristicheLibere = new ArrayList<>();
        Map<Long, List<Caratteristica>> caratteristicheByGruppo = new LinkedHashMap<>();

        for (Caratteristica caratteristica : caratteristiche) {
            Long idGruppo = caratteristica.getIdGruppoCaratteristiche();
            if (idGruppo == null) {
                caratteristicheLibere.add(caratteristica);
            } else {
                caratteristicheByGruppo
                        .computeIfAbsent(idGruppo, ignored -> new ArrayList<>())
                        .add(caratteristica);
            }
        }

        List<WebProductCharacteristicGroup> gruppi = new ArrayList<>();
        for (Map.Entry<Long, List<Caratteristica>> entry : caratteristicheByGruppo.entrySet()) {
            Caratteristica first = entry.getValue().isEmpty() ? null : entry.getValue().get(0);
            gruppi.add(new WebProductCharacteristicGroup(
                    entry.getKey(),
                    firstNonBlank(first != null ? first.getNomeGruppoCaratteristiche() : null, "Opzioni"),
                    first != null ? first.getDescrizioneGruppoCaratteristiche() : null,
                    entry.getValue()
            ));
        }

        model.put("caratteristicheLibere", caratteristicheLibere);
        model.put("caratteristicheGruppi", gruppi);
    }

    private void addCartFlashMessages(HttpServletRequest request, Map<String, Object> model) {
        String successMessage = CartSessionUtils.consumeSuccessMessage(request);
        if (successMessage != null) {
            model.put("cartSuccessMessage", successMessage);
        }

        String errorMessage = CartSessionUtils.consumeErrorMessage(request);
        if (errorMessage != null) {
            model.put("cartErrorMessage", errorMessage);
        }
    }

    private void renderProductNotFound(HttpServletRequest request,
                                       HttpServletResponse response,
                                       Integer productId) throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        TemplateRenderer.render(request, response, "public/product-detail.ftl", Map.of(
                "notFound", true,
                "productId", productId != null ? productId : ""
        ));
    }

    private Integer parseProductId(String pathInfo) {
        String rawId = pathInfo != null && pathInfo.startsWith("/")
                ? pathInfo.substring(1)
                : pathInfo;
        if (rawId == null || rawId.isBlank() || rawId.contains("/")) {
            return null;
        }

        try {
            int id = Integer.parseInt(rawId);
            return id > 0 ? id : null;
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private BigDecimal parseBigDecimal(String value, String label) {
        if (value == null) {
            return null;
        }

        try {
            return new BigDecimal(value.replace(',', '.'));
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Il campo " + label + " non e valido.");
        }
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String valueOrEmpty(String value) {
        return value != null ? value : "";
    }

    private String firstNonBlank(String value, String fallback) {
        return value != null && !value.isBlank() ? value : fallback;
    }
}
