package it.univaq.mastereat.api;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;

public class MasterEatApplication extends ResourceConfig {

    public MasterEatApplication() {
        packages("it.univaq.mastereat.api.resources"); // dice a Jersey di cercare le risorse REST nel package specificato
        register(JacksonFeature.class); // registra JacksonFeature per abilitare la serializzazione/deserializzazione JSON
    }
}
