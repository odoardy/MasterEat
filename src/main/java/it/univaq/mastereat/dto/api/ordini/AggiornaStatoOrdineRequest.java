package it.univaq.mastereat.dto.api.ordini;

public class AggiornaStatoOrdineRequest {

    private String nuovoStato;

    public AggiornaStatoOrdineRequest() {
    }

    public AggiornaStatoOrdineRequest(String nuovoStato) {
        this.nuovoStato = nuovoStato;
    }

    public String getNuovoStato() {
        return nuovoStato;
    }

    public void setNuovoStato(String nuovoStato) {
        this.nuovoStato = nuovoStato;
    }
}
