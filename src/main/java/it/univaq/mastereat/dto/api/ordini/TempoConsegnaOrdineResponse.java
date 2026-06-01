package it.univaq.mastereat.dto.api.ordini;

public class TempoConsegnaOrdineResponse {

    private long idOrdine;
    private int minutiConsegnaStimati;

    public TempoConsegnaOrdineResponse() {
    }

    public TempoConsegnaOrdineResponse(long idOrdine, int minutiConsegnaStimati) {
        this.idOrdine = idOrdine;
        this.minutiConsegnaStimati = minutiConsegnaStimati;
    }

    public long getIdOrdine() {
        return idOrdine;
    }

    public void setIdOrdine(long idOrdine) {
        this.idOrdine = idOrdine;
    }

    public int getMinutiConsegnaStimati() {
        return minutiConsegnaStimati;
    }

    public void setMinutiConsegnaStimati(int minutiConsegnaStimati) {
        this.minutiConsegnaStimati = minutiConsegnaStimati;
    }
}
