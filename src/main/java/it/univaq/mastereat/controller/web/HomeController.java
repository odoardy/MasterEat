package it.univaq.mastereat.controller.web;

import it.univaq.mastereat.dto.common.ProdottoPubblicoResponse;
import it.univaq.mastereat.service.ProdottoService;
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

@WebServlet(name = "HomeController", urlPatterns = "/home")
public class HomeController extends HttpServlet {

    private static final int FEATURED_PRODUCTS_LIMIT = 6;

    private final ProdottoService prodottoService = new ProdottoService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        List<ProdottoPubblicoResponse> prodottiInEvidenza;
        String loadError = null;

        try {
            List<ProdottoPubblicoResponse> prodotti = prodottoService.getProdottiPubblici();
            prodottiInEvidenza = prodotti.size() > FEATURED_PRODUCTS_LIMIT
                    ? prodotti.subList(0, FEATURED_PRODUCTS_LIMIT)
                    : prodotti;
        } catch (RuntimeException exception) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            prodottiInEvidenza = List.of();
            loadError = "Non e stato possibile caricare i prodotti in questo momento.";
        }

        Map<String, Object> model = new HashMap<>();
        model.put("prodottiInEvidenza", prodottiInEvidenza);
        if (loadError != null) {
            model.put("loadError", loadError);
        }

        TemplateRenderer.render(request, response, "public/home.ftl", model);
    }
}
