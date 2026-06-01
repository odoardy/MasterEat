package it.univaq.mastereat.api.resources;

import it.univaq.mastereat.service.ProdottoService;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;

@Path("/menu")
@Produces(MediaType.APPLICATION_JSON)
public class MenuResource {

    private final ProdottoService prodottoService = new ProdottoService();

    @GET
    public Response getMenu() {
        try {
            return Response.ok(prodottoService.getMenuPubblico()).build();
        } catch (RuntimeException exception) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(error("Errore interno durante il recupero del menu"))
                    .build();
        }
    }

    private Map<String, String> error(String message) {
        return Map.of("errore", message != null ? message : "Errore");
    }
}
