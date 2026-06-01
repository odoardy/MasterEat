package it.univaq.mastereat.api.resources;

import it.univaq.mastereat.dao.DatabaseConnectionFactory;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@Path("/db-health")
public class DatabaseHealthResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDatabaseHealth() {
        try (Connection connection = DatabaseConnectionFactory.getConnection()) {
            return Response.ok(Map.of(
                    "applicazione", "MasterEat",
                    "database", "mastereat",
                    "stato", "CONNESSO"
            )).build();
        } catch (SQLException exception) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of(
                            "applicazione", "MasterEat",
                            "database", "mastereat",
                            "stato", "ERRORE",
                            "messaggio", getErrorMessage(exception)
                    ))
                    .build();
        }
    }

    private String getErrorMessage(SQLException exception) {
        return exception.getMessage() != null ? exception.getMessage() : "";
    }
}
