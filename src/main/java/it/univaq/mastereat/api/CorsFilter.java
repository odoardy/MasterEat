package it.univaq.mastereat.api;

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
 * Filtro CORS limitato alle API REST.
 *
 * La parte web resta session-based e non viene esposta cross-origin.
 */
@WebFilter(filterName = "CorsFilter", urlPatterns = "/api/*")
public class CorsFilter implements Filter {

    private static final String ALLOW_ORIGIN = "*";
    private static final String ALLOW_METHODS = "GET, POST, PUT, DELETE, OPTIONS";
    private static final String ALLOW_HEADERS = "Authorization, Content-Type, Accept";
    private static final String MAX_AGE_SECONDS = "3600";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        addCorsHeaders(response);

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        chain.doFilter(servletRequest, servletResponse);
    }

    private void addCorsHeaders(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", ALLOW_ORIGIN);
        response.setHeader("Access-Control-Allow-Methods", ALLOW_METHODS);
        response.setHeader("Access-Control-Allow-Headers", ALLOW_HEADERS);
        response.setHeader("Access-Control-Max-Age", MAX_AGE_SECONDS);
    }
}
