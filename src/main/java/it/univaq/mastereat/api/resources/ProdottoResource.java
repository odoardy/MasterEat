package it.univaq.mastereat.api.resources;

import it.univaq.mastereat.dto.common.ProdottoPubblicoResponse;
import it.univaq.mastereat.service.ProdottoService;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Path("/prodotti")
@Produces(MediaType.APPLICATION_JSON)
public class ProdottoResource {

    private final ProdottoService prodottoService = new ProdottoService();

    @GET
    public Response getProdotti(@QueryParam("nome") String nome,
                                @QueryParam("prezzoMin") String prezzoMinParam,
                                @QueryParam("prezzoMax") String prezzoMaxParam) {
        try {
            String nomeNormalizzato = normalize(nome);
            BigDecimal prezzoMin = parseBigDecimal(prezzoMinParam, "prezzoMin");
            BigDecimal prezzoMax = parseBigDecimal(prezzoMaxParam, "prezzoMax");

            List<ProdottoPubblicoResponse> prodotti;
            if (nomeNormalizzato != null || prezzoMin != null || prezzoMax != null) {
                prodotti = prodottoService.cercaProdottiPubblici(nomeNormalizzato, prezzoMin, prezzoMax);
            } else {
                prodotti = prodottoService.getProdottiPubblici();
            }

            return Response.ok(prodotti).build();
        } catch (IllegalArgumentException exception) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(error(exception.getMessage()))
                    .build();
        } catch (RuntimeException exception) {
            return internalServerError();
        }
    }

    @GET
    @Path("/{id}")
    public Response getProdottoById(@PathParam("id") int id) {
        try {
            Optional<ProdottoPubblicoResponse> prodotto = prodottoService.getProdottoPubblicoById(id);
            if (prodotto.isPresent()) {
                return Response.ok(prodotto.get()).build();
            }

            return Response.status(Response.Status.NOT_FOUND)
                    .entity(error("Prodotto non trovato"))
                    .build();
        } catch (RuntimeException exception) {
            return internalServerError();
        }
    }

    private Response internalServerError() {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error("Errore interno durante il recupero dei prodotti"))
                .build();
    }

    private Map<String, String> error(String message) {
        return Map.of("errore", message != null ? message : "Errore");
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private BigDecimal parseBigDecimal(String value, String parameterName) {
        String normalizedValue = normalize(value);
        if (normalizedValue == null) {
            return null;
        }

        try {
            return new BigDecimal(normalizedValue);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Parametro " + parameterName + " non valido");
        }
    }
}
