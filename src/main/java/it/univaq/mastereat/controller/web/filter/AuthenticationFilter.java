package it.univaq.mastereat.controller.web.filter;

import it.univaq.mastereat.dto.web.auth.WebUserSession;
import it.univaq.mastereat.util.SessionUtils;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Filtro di sicurezza della parte WE.
 *
 * Protegge le aree MVC riservate usando la sessione HTTP e verifica che il
 * ruolo dell'utente corrisponda al prefisso richiesto: cliente, staff o
 * proprietario.
 */
@WebFilter(filterName = "AuthenticationFilter", urlPatterns = {"/cliente/*", "/staff/*", "/proprietario/*"})
public class AuthenticationFilter implements Filter {

    private static final String ROLE_CLIENTE = "CLIENTE";
    private static final String ROLE_PERSONALE = "PERSONALE";
    private static final String ROLE_PROPRIETARIO = "PROPRIETARIO";

    @Override
    public void doFilter(ServletRequest servletRequest,
                         ServletResponse servletResponse,
                         FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        WebUserSession currentUser = SessionUtils.getCurrentUser(request);
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if (!isAuthorized(request, currentUser)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Accesso non autorizzato");
            return;
        }

        filterChain.doFilter(servletRequest, servletResponse);
    }

    private boolean isAuthorized(HttpServletRequest request, WebUserSession currentUser) {
        String servletPath = request.getServletPath();
        String role = currentUser.getRuolo();

        // La regola di autorizzazione WE e basata sul prefisso dell'area server-side.
        if (servletPath.startsWith("/cliente/")) {
            return ROLE_CLIENTE.equals(role);
        }

        if (servletPath.startsWith("/staff/")) {
            return ROLE_PERSONALE.equals(role);
        }

        if (servletPath.startsWith("/proprietario/")) {
            return ROLE_PROPRIETARIO.equals(role);
        }

        return true;
    }
}
