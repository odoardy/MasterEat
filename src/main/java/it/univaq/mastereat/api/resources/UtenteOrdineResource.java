package it.univaq.mastereat.api.resources;

import it.univaq.mastereat.model.SessioneApi;
import it.univaq.mastereat.service.AuthService;
import it.univaq.mastereat.service.OrdineService;
import it.univaq.mastereat.util.TokenUtils;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Path("/utenti/{idUtente}/ordini")
@Produces(MediaType.APPLICATION_JSON)
public class UtenteOrdineResource {

    private final AuthService authService = new AuthService();
    private final OrdineService ordineService = new OrdineService();

    @GET
    public Response getOrdiniUtente(@HeaderParam("Authorization") String authorizationHeader,
                                    @HeaderParam("Authentication") String authenticationHeader,
                                    @PathParam("idUtente") long idUtente) {
        try {
            Optional<Long> idUtenteAutenticato = getIdUtenteAutenticato(authorizationHeader, authenticationHeader);
            if (idUtenteAutenticato.isEmpty()) {
                return unauthorized();
            }

            return Response.ok(ordineService.getOrdiniUtente(idUtenteAutenticato.get(), idUtente)).build();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return badRequest(exception.getMessage());
        } catch (SecurityException exception) {
            return forbidden(exception.getMessage());
        } catch (NoSuchElementException exception) {
            return notFound(exception.getMessage());
        } catch (RuntimeException exception) {
            return internalServerError("Errore interno durante il recupero degli ordini dell'utente");
        }
    }

    private Optional<Long> getIdUtenteAutenticato(String authorizationHeader, String authenticationHeader) {
        String token = TokenUtils.extractToken(authorizationHeader, authenticationHeader);
        if (token == null) {
            return Optional.empty();
        }

        return authService.verificaToken(token)
                .map(SessioneApi::getIdUtente);
    }

    private Response badRequest(String message) {
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(error(message))
                .build();
    }

    private Response unauthorized() {
        return Response.status(Response.Status.UNAUTHORIZED)
                .entity(error("Token mancante o non valido"))
                .build();
    }

    private Response forbidden(String message) {
        return Response.status(Response.Status.FORBIDDEN)
                .entity(error(message))
                .build();
    }

    private Response notFound(String message) {
        return Response.status(Response.Status.NOT_FOUND)
                .entity(error(message))
                .build();
    }

    private Response internalServerError(String message) {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error(message))
                .build();
    }

    private Map<String, String> error(String message) {
        return Map.of("errore", message != null ? message : "Errore");
    }
}
