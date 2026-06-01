package it.univaq.mastereat.api.resources;

import it.univaq.mastereat.dto.api.auth.LoginRequest;
import it.univaq.mastereat.dto.api.auth.LoginResponse;
import it.univaq.mastereat.service.AuthService;
import it.univaq.mastereat.util.TokenUtils;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    private final AuthService authService = new AuthService();

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(LoginRequest request) {
        if (request == null || isBlank(request.getUsername()) || isBlank(request.getPassword())) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(error("Username e password sono obbligatori"))
                    .build();
        }

        try {
            LoginResponse response = authService.login(request);
            if (response == null) {
                return Response.status(Response.Status.UNAUTHORIZED)
                        .entity(error("Credenziali non valide"))
                        .build();
            }

            return Response.ok(response).build();
        } catch (IllegalArgumentException exception) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(error(exception.getMessage()))
                    .build();
        } catch (RuntimeException exception) {
            return internalServerError("Errore interno durante il login");
        }
    }

    @POST
    @Path("/logout")
    public Response logout(@HeaderParam("Authorization") String authorizationHeader,
                           @HeaderParam("Authentication") String authenticationHeader) {
        String token = TokenUtils.extractToken(authorizationHeader, authenticationHeader);
        if (token == null) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(error("Token mancante o non valido"))
                    .build();
        }

        try {
            if (authService.logout(token)) {
                return Response.noContent().build();
            }

            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(error("Token mancante o non valido"))
                    .build();
        } catch (RuntimeException exception) {
            return internalServerError("Errore interno durante il logout");
        }
    }

    private Response internalServerError(String message) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error(message))
                .build();
    }

    private Map<String, String> error(String message) {
        return Map.of("errore", message != null ? message : "Errore");
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
