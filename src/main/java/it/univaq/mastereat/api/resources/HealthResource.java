package it.univaq.mastereat.api.resources;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.time.Instant;
import java.util.Map;

@Path("/health")
public class HealthResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, String> getHealth() {
        return Map.of(
                "applicazione", "MasterEat",
                "tipo", "Jersey REST API",
                "stato", "OK",
                "timestamp", Instant.now().toString()
        );
    }
}
