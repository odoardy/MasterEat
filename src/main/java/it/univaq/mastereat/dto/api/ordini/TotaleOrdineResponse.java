package it.univaq.mastereat.dto.api.ordini;

import java.math.BigDecimal;

public class TotaleOrdineResponse {

    private long idOrdine;
    private BigDecimal totale;

    public TotaleOrdineResponse() {
    }

    public TotaleOrdineResponse(long idOrdine, BigDecimal totale) {
        this.idOrdine = idOrdine;
        this.totale = totale;
    }

    public long getIdOrdine() {
        return idOrdine;
    }

    public void setIdOrdine(long idOrdine) {
        this.idOrdine = idOrdine;
    }

    public BigDecimal getTotale() {
        return totale;
    }

    public void setTotale(BigDecimal totale) {
        this.totale = totale;
    }
}
