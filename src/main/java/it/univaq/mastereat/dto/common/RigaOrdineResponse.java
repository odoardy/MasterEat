package it.univaq.mastereat.dto.common;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class RigaOrdineResponse {

    private long idRigaOrdine;
    private long idProdotto;
    private String nomeProdotto;
    private int quantita;
    private BigDecimal prezzoBase;
    private int minutiPreparazione;
    private List<CaratteristicaOrdineResponse> caratteristiche = new ArrayList<>();
    private BigDecimal subtotaleRiga;

    public RigaOrdineResponse() {
    }

    public RigaOrdineResponse(long idRigaOrdine,
                              long idProdotto,
                              String nomeProdotto,
                              int quantita,
                              BigDecimal prezzoBase,
                              int minutiPreparazione,
                              List<CaratteristicaOrdineResponse> caratteristiche,
                              BigDecimal subtotaleRiga) {
        this.idRigaOrdine = idRigaOrdine;
        this.idProdotto = idProdotto;
        this.nomeProdotto = nomeProdotto;
        this.quantita = quantita;
        this.prezzoBase = prezzoBase;
        this.minutiPreparazione = minutiPreparazione;
        setCaratteristiche(caratteristiche);
        this.subtotaleRiga = subtotaleRiga;
    }

    public long getIdRigaOrdine() {
        return idRigaOrdine;
    }

    public void setIdRigaOrdine(long idRigaOrdine) {
        this.idRigaOrdine = idRigaOrdine;
    }

    public long getIdProdotto() {
        return idProdotto;
    }

    public void setIdProdotto(long idProdotto) {
        this.idProdotto = idProdotto;
    }

    public String getNomeProdotto() {
        return nomeProdotto;
    }

    public void setNomeProdotto(String nomeProdotto) {
        this.nomeProdotto = nomeProdotto;
    }

    public int getQuantita() {
        return quantita;
    }

    public void setQuantita(int quantita) {
        this.quantita = quantita;
    }

    public BigDecimal getPrezzoBase() {
        return prezzoBase;
    }

    public void setPrezzoBase(BigDecimal prezzoBase) {
        this.prezzoBase = prezzoBase;
    }

    public int getMinutiPreparazione() {
        return minutiPreparazione;
    }

    public void setMinutiPreparazione(int minutiPreparazione) {
        this.minutiPreparazione = minutiPreparazione;
    }

    public List<CaratteristicaOrdineResponse> getCaratteristiche() {
        return caratteristiche;
    }

    public void setCaratteristiche(List<CaratteristicaOrdineResponse> caratteristiche) {
        this.caratteristiche = caratteristiche != null ? new ArrayList<>(caratteristiche) : new ArrayList<>();
    }

    public BigDecimal getSubtotaleRiga() {
        return subtotaleRiga;
    }

    public void setSubtotaleRiga(BigDecimal subtotaleRiga) {
        this.subtotaleRiga = subtotaleRiga;
    }
}
