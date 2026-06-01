package it.univaq.mastereat.dto.api.ordini;

import java.util.ArrayList;
import java.util.List;

public class OperatoriOrdineResponse {

    private long idOrdine;
    private List<OperatoreOrdineResponse> operatori = new ArrayList<>();

    public OperatoriOrdineResponse() {
    }

    public OperatoriOrdineResponse(long idOrdine, List<OperatoreOrdineResponse> operatori) {
        this.idOrdine = idOrdine;
        setOperatori(operatori);
    }

    public long getIdOrdine() {
        return idOrdine;
    }

    public void setIdOrdine(long idOrdine) {
        this.idOrdine = idOrdine;
    }

    public List<OperatoreOrdineResponse> getOperatori() {
        return operatori;
    }

    public void setOperatori(List<OperatoreOrdineResponse> operatori) {
        this.operatori = operatori != null ? new ArrayList<>(operatori) : new ArrayList<>();
    }
}
