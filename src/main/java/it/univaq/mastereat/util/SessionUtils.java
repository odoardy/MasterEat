package it.univaq.mastereat.util;

import it.univaq.mastereat.dto.web.auth.WebUserSession;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

public final class SessionUtils {

    public static final String CURRENT_USER_ATTRIBUTE = "currentUser";
    public static final String FLASH_SUCCESS_ATTRIBUTE = "flashSuccessMessage";

    private SessionUtils() {
    }

    public static void login(HttpServletRequest request, WebUserSession utenteSessione) {
        HttpSession existingSession = request.getSession(false);
        if (existingSession != null) {
            existingSession.invalidate();
        }

        HttpSession session = request.getSession(true);
        session.setAttribute(CURRENT_USER_ATTRIBUTE, utenteSessione);
    }

    public static void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    public static WebUserSession getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        Object currentUser = session.getAttribute(CURRENT_USER_ATTRIBUTE);
        if (currentUser instanceof WebUserSession webUserSession) {
            return webUserSession;
        }

        return null;
    }

    public static void updateCurrentUser(HttpServletRequest request, WebUserSession utenteSessione) {
        if (utenteSessione == null) {
            return;
        }

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.setAttribute(CURRENT_USER_ATTRIBUTE, utenteSessione);
        }
    }

    public static boolean isAuthenticated(HttpServletRequest request) {
        return getCurrentUser(request) != null;
    }

    public static boolean hasRole(HttpServletRequest request, String ruolo) {
        WebUserSession currentUser = getCurrentUser(request);
        return currentUser != null
                && ruolo != null
                && ruolo.equals(currentUser.getRuolo());
    }

    public static void setFlashMessage(HttpServletRequest request, String attributeName, String message) {
        if (attributeName == null || attributeName.isBlank() || message == null || message.isBlank()) {
            return;
        }

        HttpSession session = request.getSession(true);
        session.setAttribute(attributeName, message);
    }

    public static String consumeFlashMessage(HttpServletRequest request, String attributeName) {
        if (attributeName == null || attributeName.isBlank()) {
            return null;
        }

        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }

        Object message = session.getAttribute(attributeName);
        session.removeAttribute(attributeName);

        if (message instanceof String value) {
            return value;
        }

        return null;
    }
}
