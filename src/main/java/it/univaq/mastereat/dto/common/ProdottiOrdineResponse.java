package it.univaq.mastereat.dto.common;

import java.util.ArrayList;
import java.util.List;

public class ProdottiOrdineResponse {

    private long idOrdine;
    private List<RigaOrdineResponse> prodotti = new ArrayList<>();

    public ProdottiOrdineResponse() {
    }

    public ProdottiOrdineResponse(long idOrdine, List<RigaOrdineResponse> prodotti) {
        this.idOrdine = idOrdine;
        setProdotti(prodotti);
    }

    public long getIdOrdine() {
        return idOrdine;
    }

    public void setIdOrdine(long idOrdine) {
        this.idOrdine = idOrdine;
    }

    public List<RigaOrdineResponse> getProdotti() {
        return prodotti;
    }

    public void setProdotti(List<RigaOrdineResponse> prodotti) {
        this.prodotti = prodotti != null ? new ArrayList<>(prodotti) : new ArrayList<>();
    }
}
