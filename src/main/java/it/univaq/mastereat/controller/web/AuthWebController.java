package it.univaq.mastereat.controller.web;

import it.univaq.mastereat.dto.web.auth.WebUserSession;
import it.univaq.mastereat.model.Utente;
import it.univaq.mastereat.service.AuthService;
import it.univaq.mastereat.util.SessionUtils;
import it.univaq.mastereat.util.TemplateRenderer;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@WebServlet(name = "AuthWebController", urlPatterns = {"/login", "/logout"})
public class AuthWebController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(AuthWebController.class.getName());

    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (isLogoutRequest(request)) {
            logout(request, response);
            return;
        }

        if (SessionUtils.isAuthenticated(request)) {
            redirectToHome(request, response);
            return;
        }

        renderLogin(request, response, null, null);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (isLogoutRequest(request)) {
            logout(request, response);
            return;
        }

        processLogin(request, response);
    }

    private void processLogin(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String username = normalize(request.getParameter("username"));
        String password = request.getParameter("password");

        if (isBlank(username) || isBlank(password)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            renderLogin(request, response, username, "Credenziali non valide.");
            return;
        }

        try {
            Optional<Utente> utente = authService.autenticaUtente(username, password);
            if (utente.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                renderLogin(request, response, username, "Credenziali non valide.");
                return;
            }

            WebUserSession webUserSession = WebUserSession.fromUtente(utente.get());
            SessionUtils.login(request, webUserSession);
            redirectToHome(request, response);
        } catch (IllegalArgumentException exception) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            renderLogin(request, response, username, "Credenziali non valide.");
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante il login web", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            renderLogin(request, response, username, "Non e stato possibile completare il login in questo momento.");
        }
    }

    private void logout(HttpServletRequest request, HttpServletResponse response) throws IOException {
        SessionUtils.logout(request);
        redirectToHome(request, response);
    }

    private void renderLogin(HttpServletRequest request,
                             HttpServletResponse response,
                             String username,
                             String errorMessage) throws ServletException, IOException {
        Map<String, Object> model = new HashMap<>();
        model.put("username", username != null ? username : "");
        String successMessage = SessionUtils.consumeFlashMessage(request, SessionUtils.FLASH_SUCCESS_ATTRIBUTE);
        if (successMessage != null) {
            model.put("successMessage", successMessage);
        }
        if (errorMessage != null) {
            model.put("errorMessage", errorMessage);
        }

        TemplateRenderer.render(request, response, "public/login.ftl", model);
    }

    private void redirectToHome(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/home");
    }

    private boolean isLogoutRequest(HttpServletRequest request) {
        return "/logout".equals(request.getServletPath());
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
