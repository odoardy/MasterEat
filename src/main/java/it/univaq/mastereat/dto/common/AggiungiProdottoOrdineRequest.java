package it.univaq.mastereat.dto.common;

import java.util.ArrayList;
import java.util.List;

public class AggiungiProdottoOrdineRequest {

    private long idProdotto;
    private int quantita;
    private List<Long> caratteristiche = new ArrayList<>();

    public AggiungiProdottoOrdineRequest() {
    }

    public AggiungiProdottoOrdineRequest(long idProdotto, int quantita, List<Long> caratteristiche) {
        this.idProdotto = idProdotto;
        this.quantita = quantita;
        setCaratteristiche(caratteristiche);
    }

    public long getIdProdotto() {
        return idProdotto;
    }

    public void setIdProdotto(long idProdotto) {
        this.idProdotto = idProdotto;
    }

    public int getQuantita() {
        return quantita;
    }

    public void setQuantita(int quantita) {
        this.quantita = quantita;
    }

    public List<Long> getCaratteristiche() {
        return caratteristiche;
    }

    public void setCaratteristiche(List<Long> caratteristiche) {
        this.caratteristiche = caratteristiche != null ? new ArrayList<>(caratteristiche) : new ArrayList<>();
    }
}
