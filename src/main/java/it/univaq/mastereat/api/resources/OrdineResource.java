package it.univaq.mastereat.api.resources;

import it.univaq.mastereat.dto.api.ordini.AggiornaStatoOrdineRequest;
import it.univaq.mastereat.dto.common.AggiungiProdottoOrdineRequest;
import it.univaq.mastereat.model.SessioneApi;
import it.univaq.mastereat.service.AuthService;
import it.univaq.mastereat.service.OrdineService;
import it.univaq.mastereat.util.TokenUtils;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

@Path("/ordini")
@Produces(MediaType.APPLICATION_JSON)
public class OrdineResource {

    private final AuthService authService = new AuthService();
    private final OrdineService ordineService = new OrdineService();

    @GET
    public Response getOrdini(@HeaderParam("Authorization") String authorizationHeader,
                              @HeaderParam("Authentication") String authenticationHeader,
                              @QueryParam("stato") String stato,
                              @QueryParam("dataDa") String dataDa,
                              @QueryParam("dataA") String dataA) {
        try {
            Optional<Long> idUtente = getIdUtenteAutenticato(authorizationHeader, authenticationHeader);
            if (idUtente.isEmpty()) {
                return unauthorized();
            }

            return Response.ok(ordineService.getOrdiniOperativi(idUtente.get(), stato, dataDa, dataA)).build();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return badRequest(exception.getMessage());
        } catch (SecurityException exception) {
            return forbidden(exception.getMessage());
        } catch (NoSuchElementException exception) {
            return notFound(exception.getMessage());
        } catch (RuntimeException exception) {
            return internalServerError("Errore interno durante il recupero degli ordini");
        }
    }

    @POST
    public Response creaOrdine(@HeaderParam("Authorization") String authorizationHeader,
                               @HeaderParam("Authentication") String authenticationHeader) {
        try {
            Optional<Long> idUtente = getIdUtenteAutenticato(authorizationHeader, authenticationHeader);
            if (idUtente.isEmpty()) {
                return unauthorized();
            }

            return Response.status(Response.Status.CREATED)
                    .entity(ordineService.creaOrdine(idUtente.get()))
                    .build();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return badRequest(exception.getMessage());
        } catch (SecurityException exception) {
            return forbidden(exception.getMessage());
        } catch (NoSuchElementException exception) {
            return notFound(exception.getMessage());
        } catch (RuntimeException exception) {
            return internalServerError("Errore interno durante la creazione dell'ordine");
        }
    }

    @POST
    @Path("/{idOrdine}/prodotti")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response aggiungiProdotto(@HeaderParam("Authorization") String authorizationHeader,
                                     @HeaderParam("Authentication") String authenticationHeader,
                                     @PathParam("idOrdine") long idOrdine,
                                     AggiungiProdottoOrdineRequest request) {
        try {
            Optional<Long> idUtente = getIdUtenteAutenticato(authorizationHeader, authenticationHeader);
            if (idUtente.isEmpty()) {
                return unauthorized();
            }

            return Response.status(Response.Status.CREATED)
                    .entity(ordineService.aggiungiProdotto(idUtente.get(), idOrdine, request))
                    .build();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return badRequest(exception.getMessage());
        } catch (SecurityException exception) {
            return forbidden(exception.getMessage());
        } catch (NoSuchElementException exception) {
            return notFound(exception.getMessage());
        } catch (RuntimeException exception) {
            return internalServerError("Errore interno durante l'inserimento del prodotto nell'ordine");
        }
    }

    @POST
    @Path("/{idOrdine}/conferma")
    public Response confermaOrdine(@HeaderParam("Authorization") String authorizationHeader,
                                   @HeaderParam("Authentication") String authenticationHeader,
                                   @PathParam("idOrdine") long idOrdine) {
        try {
            Optional<Long> idUtente = getIdUtenteAutenticato(authorizationHeader, authenticationHeader);
            if (idUtente.isEmpty()) {
                return unauthorized();
            }

            return Response.ok(ordineService.confermaOrdine(idUtente.get(), idOrdine)).build();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return badRequest(exception.getMessage());
        } catch (SecurityException exception) {
            return forbidden(exception.getMessage());
        } catch (NoSuchElementException exception) {
            return notFound(exception.getMessage());
        } catch (RuntimeException exception) {
            return internalServerError("Errore interno durante la conferma dell'ordine");
        }
    }

    @PUT
    @Path("/{idOrdine}/stato")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response aggiornaStatoOrdine(@HeaderParam("Authorization") String authorizationHeader,
                                        @HeaderParam("Authentication") String authenticationHeader,
                                        @PathParam("idOrdine") long idOrdine,
                                        AggiornaStatoOrdineRequest request) {
        try {
            Optional<Long> idUtente = getIdUtenteAutenticato(authorizationHeader, authenticationHeader);
            if (idUtente.isEmpty()) {
                return unauthorized();
            }

            return Response.ok(ordineService.aggiornaStatoOrdine(idUtente.get(), idOrdine, request)).build();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return badRequest(exception.getMessage());
        } catch (SecurityException exception) {
            return forbidden(exception.getMessage());
        } catch (NoSuchElementException exception) {
            return notFound(exception.getMessage());
        } catch (RuntimeException exception) {
            return internalServerError("Errore interno durante l'aggiornamento dello stato ordine");
        }
    }

    @GET
    @Path("/{idOrdine}/prodotti")
    public Response getProdottiOrdine(@HeaderParam("Authorization") String authorizationHeader,
                                      @HeaderParam("Authentication") String authenticationHeader,
                                      @PathParam("idOrdine") long idOrdine) {
        try {
            Optional<Long> idUtente = getIdUtenteAutenticato(authorizationHeader, authenticationHeader);
            if (idUtente.isEmpty()) {
                return unauthorized();
            }

            return Response.ok(ordineService.getProdottiOrdine(idUtente.get(), idOrdine)).build();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return badRequest(exception.getMessage());
        } catch (SecurityException exception) {
            return forbidden(exception.getMessage());
        } catch (NoSuchElementException exception) {
            return notFound(exception.getMessage());
        } catch (RuntimeException exception) {
            return internalServerError("Errore interno durante il recupero dei prodotti dell'ordine");
        }
    }

    @GET
    @Path("/{idOrdine}/totale")
    public Response getTotaleOrdine(@HeaderParam("Authorization") String authorizationHeader,
                                    @HeaderParam("Authentication") String authenticationHeader,
                                    @PathParam("idOrdine") long idOrdine) {
        try {
            Optional<Long> idUtente = getIdUtenteAutenticato(authorizationHeader, authenticationHeader);
            if (idUtente.isEmpty()) {
                return unauthorized();
            }

            return Response.ok(ordineService.getTotaleOrdine(idUtente.get(), idOrdine)).build();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return badRequest(exception.getMessage());
        } catch (SecurityException exception) {
            return forbidden(exception.getMessage());
        } catch (NoSuchElementException exception) {
            return notFound(exception.getMessage());
        } catch (RuntimeException exception) {
            return internalServerError("Errore interno durante il calcolo del totale dell'ordine");
        }
    }

    @GET
    @Path("/{idOrdine}/tempo-consegna")
    public Response getTempoConsegnaOrdine(@HeaderParam("Authorization") String authorizationHeader,
                                           @HeaderParam("Authentication") String authenticationHeader,
                                           @PathParam("idOrdine") long idOrdine) {
        try {
            Optional<Long> idUtente = getIdUtenteAutenticato(authorizationHeader, authenticationHeader);
            if (idUtente.isEmpty()) {
                return unauthorized();
            }

            return Response.ok(ordineService.getTempoConsegnaOrdine(idUtente.get(), idOrdine)).build();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return badRequest(exception.getMessage());
        } catch (SecurityException exception) {
            return forbidden(exception.getMessage());
        } catch (NoSuchElementException exception) {
            return notFound(exception.getMessage());
        } catch (RuntimeException exception) {
            return internalServerError("Errore interno durante il calcolo del tempo stimato dell'ordine");
        }
    }

    @GET
    @Path("/{idOrdine}/operatori")
    public Response getOperatoriOrdine(@HeaderParam("Authorization") String authorizationHeader,
                                       @HeaderParam("Authentication") String authenticationHeader,
                                       @PathParam("idOrdine") long idOrdine) {
        try {
            Optional<Long> idUtente = getIdUtenteAutenticato(authorizationHeader, authenticationHeader);
            if (idUtente.isEmpty()) {
                return unauthorized();
            }

            return Response.ok(ordineService.getOperatoriOrdine(idUtente.get(), idOrdine)).build();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return badRequest(exception.getMessage());
        } catch (SecurityException exception) {
            return forbidden(exception.getMessage());
        } catch (NoSuchElementException exception) {
            return notFound(exception.getMessage());
        } catch (RuntimeException exception) {
            return internalServerError("Errore interno durante il recupero degli operatori dell'ordine");
        }
    }

    @DELETE
    @Path("/{idOrdine}")
    public Response annullaOrdine(@HeaderParam("Authorization") String authorizationHeader,
                                  @HeaderParam("Authentication") String authenticationHeader,
                                  @PathParam("idOrdine") long idOrdine) {
        try {
            Optional<Long> idUtente = getIdUtenteAutenticato(authorizationHeader, authenticationHeader);
            if (idUtente.isEmpty()) {
                return unauthorized();
            }

            return Response.ok(ordineService.annullaOrdine(idUtente.get(), idOrdine)).build();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            return badRequest(exception.getMessage());
        } catch (SecurityException exception) {
            return forbidden(exception.getMessage());
        } catch (NoSuchElementException exception) {
            return notFound(exception.getMessage());
        } catch (RuntimeException exception) {
            return internalServerError("Errore interno durante l'annullamento dell'ordine");
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
