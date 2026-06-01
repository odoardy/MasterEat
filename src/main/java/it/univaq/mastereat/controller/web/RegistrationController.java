package it.univaq.mastereat.controller.web;

import it.univaq.mastereat.dto.web.auth.ClienteRegistrationRequest;
import it.univaq.mastereat.service.UtenteService;
import it.univaq.mastereat.util.PasswordPolicy;
import it.univaq.mastereat.util.SessionUtils;
import it.univaq.mastereat.util.TemplateRenderer;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

@WebServlet(name = "RegistrationController", urlPatterns = {"/registrazione", "/register"})
public class RegistrationController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(RegistrationController.class.getName());

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9._-]+$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+0-9 .()\\-]{6,30}$");

    private final UtenteService utenteService = new UtenteService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        if (SessionUtils.isAuthenticated(request)) {
            redirectToHome(request, response);
            return;
        }

        if (isRegisterAlias(request)) {
            response.sendRedirect(request.getContextPath() + "/registrazione");
            return;
        }

        renderRegistration(request, response, emptyForm(), List.of());
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        if (SessionUtils.isAuthenticated(request)) {
            redirectToHome(request, response);
            return;
        }

        Map<String, String> form = readForm(request);
        String password = request.getParameter("password");
        String passwordConfirm = request.getParameter("passwordConfirm");

        List<String> errors = validate(form, password, passwordConfirm);
        if (!errors.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            renderRegistration(request, response, form, errors);
            return;
        }

        ClienteRegistrationRequest registrationRequest = new ClienteRegistrationRequest(
                form.get("username"),
                password,
                form.get("nome"),
                form.get("cognome"),
                form.get("email"),
                form.get("telefono"),
                form.get("indirizzo"),
                form.get("citta"),
                form.get("cap")
        );

        try {
            utenteService.registraCliente(registrationRequest);
            SessionUtils.setFlashMessage(
                    request,
                    SessionUtils.FLASH_SUCCESS_ATTRIBUTE,
                    "Registrazione completata. Ora puoi accedere."
            );
            response.sendRedirect(request.getContextPath() + "/login");
        } catch (IllegalArgumentException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            renderRegistration(request, response, form, List.of(exception.getMessage()));
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante la registrazione cliente web", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            renderRegistration(
                    request,
                    response,
                    form,
                    List.of("Non e stato possibile completare la registrazione in questo momento.")
            );
        }
    }

    private void renderRegistration(HttpServletRequest request,
                                    HttpServletResponse response,
                                    Map<String, String> form,
                                    List<String> errors) throws ServletException, IOException {
        Map<String, Object> model = new HashMap<>();
        model.put("form", form);
        model.put("errors", errors);

        TemplateRenderer.render(request, response, "public/register.ftl", model);
    }

    private Map<String, String> readForm(HttpServletRequest request) {
        Map<String, String> form = emptyForm();
        form.put("username", normalize(request.getParameter("username")));
        form.put("nome", normalize(request.getParameter("nome")));
        form.put("cognome", normalize(request.getParameter("cognome")));
        form.put("email", normalize(request.getParameter("email")));
        form.put("telefono", normalize(request.getParameter("telefono")));
        form.put("indirizzo", normalize(request.getParameter("indirizzo")));
        form.put("citta", normalize(request.getParameter("citta")));
        form.put("cap", normalize(request.getParameter("cap")));
        return form;
    }

    private Map<String, String> emptyForm() {
        Map<String, String> form = new HashMap<>();
        form.put("username", "");
        form.put("nome", "");
        form.put("cognome", "");
        form.put("email", "");
        form.put("telefono", "");
        form.put("indirizzo", "");
        form.put("citta", "");
        form.put("cap", "");
        return form;
    }

    private List<String> validate(Map<String, String> form, String password, String passwordConfirm) {
        List<String> errors = new ArrayList<>();

        validateRequiredText(errors, form.get("username"), "Username", 3, 50);
        if (!isBlank(form.get("username")) && !USERNAME_PATTERN.matcher(form.get("username")).matches()) {
            errors.add("Username non valido: usa solo lettere, numeri, punto, trattino o underscore.");
        }

        validatePassword(errors, password, passwordConfirm);
        validateRequiredText(errors, form.get("nome"), "Nome", 1, 80);
        validateRequiredText(errors, form.get("cognome"), "Cognome", 1, 80);
        validateEmail(errors, form.get("email"));
        validatePhone(errors, form.get("telefono"));
        validateRequiredText(errors, form.get("indirizzo"), "Indirizzo", 1, 255);
        validateRequiredText(errors, form.get("citta"), "Citta", 1, 100);
        validateOptionalText(errors, form.get("cap"), "CAP", 20);

        return errors;
    }

    private void validatePassword(List<String> errors, String password, String passwordConfirm) {
        if (isBlank(password)) {
            errors.add("Password obbligatoria.");
            return;
        }

        if (password.length() > 128) {
            errors.add("Password deve contenere al massimo 128 caratteri.");
        }
        if (!PasswordPolicy.isValidNewPassword(password)) {
            errors.add(PasswordPolicy.ERROR_MESSAGE);
        }
        if (isBlank(passwordConfirm)) {
            errors.add("Conferma password obbligatoria.");
        } else if (!Objects.equals(password, passwordConfirm)) {
            errors.add("Password e conferma password non coincidono.");
        }
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

    private void redirectToHome(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.sendRedirect(request.getContextPath() + "/home");
    }

    private boolean isRegisterAlias(HttpServletRequest request) {
        return "/register".equals(request.getServletPath());
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
