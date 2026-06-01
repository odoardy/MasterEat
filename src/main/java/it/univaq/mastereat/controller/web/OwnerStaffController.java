package it.univaq.mastereat.controller.web;

import it.univaq.mastereat.dto.web.owner.OwnerPersonaleCreateRequest;
import it.univaq.mastereat.dto.web.auth.WebUserSession;
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

@WebServlet(name = "OwnerStaffController", urlPatterns = {
        "/proprietario/personale",
        "/proprietario/personale/nuovo"
})
public class OwnerStaffController extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(OwnerStaffController.class.getName());

    private static final String ROLE_PROPRIETARIO = "PROPRIETARIO";
    private static final String FLASH_OWNER_STAFF_SUCCESS_ATTRIBUTE = "ownerStaffSuccessMessage";
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9._-]+$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^[+0-9 .()\\-]{6,30}$");

    private final UtenteService utenteService = new UtenteService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        WebUserSession currentUser = requireCurrentOwner(request, response);
        if (currentUser == null) {
            return;
        }

        switch (request.getServletPath()) {
            case "/proprietario/personale" -> renderStaffList(request, response, currentUser);
            case "/proprietario/personale/nuovo" -> renderStaffForm(request, response, emptyForm(), List.of());
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

        if (!"/proprietario/personale".equals(request.getServletPath())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        processCreateStaff(request, response, currentUser);
    }

    private void renderStaffList(HttpServletRequest request,
                                 HttpServletResponse response,
                                 WebUserSession currentUser) throws ServletException, IOException {
        Map<String, Object> model = new HashMap<>();
        addStaffFlashMessages(request, model);

        try {
            model.put("personale", utenteService.getPersonaleProprietario(currentUser.getIdUtente()));
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
            return;
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante il rendering personale proprietario", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            model.put("personale", List.of());
            model.put("errorMessage", "Non è stato possibile caricare il personale in questo momento.");
        }

        TemplateRenderer.render(request, response, "owner/staff-list.ftl", model);
    }

    private void renderStaffForm(HttpServletRequest request,
                                 HttpServletResponse response,
                                 Map<String, String> form,
                                 List<String> errors) throws ServletException, IOException {
        Map<String, Object> model = new HashMap<>();
        model.put("form", form);
        model.put("errors", errors);

        TemplateRenderer.render(request, response, "owner/staff-form.ftl", model);
    }

    private void processCreateStaff(HttpServletRequest request,
                                    HttpServletResponse response,
                                    WebUserSession currentUser) throws ServletException, IOException {
        Map<String, String> form = readForm(request);
        String password = request.getParameter("password");
        String confermaPassword = request.getParameter("confermaPassword");

        List<String> errors = validate(form, password, confermaPassword);
        if (!errors.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            renderStaffForm(request, response, form, errors);
            return;
        }

        OwnerPersonaleCreateRequest createRequest = new OwnerPersonaleCreateRequest(
                form.get("username"),
                password,
                confermaPassword,
                form.get("nome"),
                form.get("cognome"),
                form.get("email"),
                form.get("telefono")
        );

        try {
            utenteService.registraPersonale(currentUser.getIdUtente(), createRequest);
            SessionUtils.setFlashMessage(
                    request,
                    FLASH_OWNER_STAFF_SUCCESS_ATTRIBUTE,
                    "Membro personale creato correttamente."
            );
            response.sendRedirect(request.getContextPath() + "/proprietario/personale");
        } catch (IllegalArgumentException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            renderStaffForm(request, response, form, List.of(exception.getMessage()));
        } catch (SecurityException exception) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
        } catch (RuntimeException exception) {
            LOGGER.log(Level.SEVERE, "Errore durante creazione personale proprietario", exception);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            renderStaffForm(
                    request,
                    response,
                    form,
                    List.of("Non è stato possibile creare il membro personale in questo momento.")
            );
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

    private Map<String, String> readForm(HttpServletRequest request) {
        Map<String, String> form = emptyForm();
        form.put("username", normalize(request.getParameter("username")));
        form.put("nome", normalize(request.getParameter("nome")));
        form.put("cognome", normalize(request.getParameter("cognome")));
        form.put("email", normalize(request.getParameter("email")));
        form.put("telefono", normalize(request.getParameter("telefono")));
        return form;
    }

    private Map<String, String> emptyForm() {
        Map<String, String> form = new HashMap<>();
        form.put("username", "");
        form.put("nome", "");
        form.put("cognome", "");
        form.put("email", "");
        form.put("telefono", "");
        return form;
    }

    private List<String> validate(Map<String, String> form, String password, String confermaPassword) {
        List<String> errors = new ArrayList<>();

        validateRequiredText(errors, form.get("username"), "Username", 3, 50);
        if (!isBlank(form.get("username")) && !USERNAME_PATTERN.matcher(form.get("username")).matches()) {
            errors.add("Username non valido: usa solo lettere, numeri, punto, trattino o underscore.");
        }

        validatePassword(errors, password, confermaPassword);
        validateRequiredText(errors, form.get("nome"), "Nome", 1, 80);
        validateRequiredText(errors, form.get("cognome"), "Cognome", 1, 80);
        validateEmail(errors, form.get("email"));
        validateOptionalPhone(errors, form.get("telefono"));

        return errors;
    }

    private void validatePassword(List<String> errors, String password, String confermaPassword) {
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
        if (isBlank(confermaPassword)) {
            errors.add("Conferma password obbligatoria.");
        } else if (!Objects.equals(password, confermaPassword)) {
            errors.add("Password e conferma password non coincidono.");
        }
    }

    private void validateEmail(List<String> errors, String email) {
        validateRequiredText(errors, email, "Email", 1, 255);
        if (!isBlank(email) && !EMAIL_PATTERN.matcher(email).matches()) {
            errors.add("Email non valida.");
        }
    }

    private void validateOptionalPhone(List<String> errors, String telefono) {
        if (isBlank(telefono)) {
            return;
        }

        if (telefono.length() > 30) {
            errors.add("Telefono deve contenere al massimo 30 caratteri.");
        }
        if (!PHONE_PATTERN.matcher(telefono).matches()) {
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

    private void addStaffFlashMessages(HttpServletRequest request, Map<String, Object> model) {
        String successMessage = SessionUtils.consumeFlashMessage(request, FLASH_OWNER_STAFF_SUCCESS_ATTRIBUTE);
        if (successMessage != null) {
            model.put("successMessage", successMessage);
        }
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
