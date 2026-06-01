package it.univaq.mastereat.model;

import java.math.BigDecimal;

public class CaratteristicaRigaOrdine {

    private long id;
    private long idRigaOrdine;
    private long idCaratteristica;
    private Long idGruppoCaratteristicheSnapshot;
    private String nomeCaratteristicaSnapshot;
    private BigDecimal differenzaPrezzoSnapshot;

    public CaratteristicaRigaOrdine() {
    }

    public CaratteristicaRigaOrdine(long id,
                                    long idRigaOrdine,
                                    long idCaratteristica,
                                    Long idGruppoCaratteristicheSnapshot,
                                    String nomeCaratteristicaSnapshot,
                                    BigDecimal differenzaPrezzoSnapshot) {
        this.id = id;
        this.idRigaOrdine = idRigaOrdine;
        this.idCaratteristica = idCaratteristica;
        this.idGruppoCaratteristicheSnapshot = idGruppoCaratteristicheSnapshot;
        this.nomeCaratteristicaSnapshot = nomeCaratteristicaSnapshot;
        this.differenzaPrezzoSnapshot = differenzaPrezzoSnapshot;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getIdRigaOrdine() {
        return idRigaOrdine;
    }

    public void setIdRigaOrdine(long idRigaOrdine) {
        this.idRigaOrdine = idRigaOrdine;
    }

    public long getIdCaratteristica() {
        return idCaratteristica;
    }

    public void setIdCaratteristica(long idCaratteristica) {
        this.idCaratteristica = idCaratteristica;
    }

    public Long getIdGruppoCaratteristicheSnapshot() {
        return idGruppoCaratteristicheSnapshot;
    }

    public void setIdGruppoCaratteristicheSnapshot(Long idGruppoCaratteristicheSnapshot) {
        this.idGruppoCaratteristicheSnapshot = idGruppoCaratteristicheSnapshot;
    }

    public String getNomeCaratteristicaSnapshot() {
        return nomeCaratteristicaSnapshot;
    }

    public void setNomeCaratteristicaSnapshot(String nomeCaratteristicaSnapshot) {
        this.nomeCaratteristicaSnapshot = nomeCaratteristicaSnapshot;
    }

    public BigDecimal getDifferenzaPrezzoSnapshot() {
        return differenzaPrezzoSnapshot;
    }

    public void setDifferenzaPrezzoSnapshot(BigDecimal differenzaPrezzoSnapshot) {
        this.differenzaPrezzoSnapshot = differenzaPrezzoSnapshot;
    }
}
