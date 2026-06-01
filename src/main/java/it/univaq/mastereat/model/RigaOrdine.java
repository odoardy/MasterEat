package it.univaq.mastereat.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class RigaOrdine {

    private long id;
    private long idOrdine;
    private long idProdotto;
    private String nomeProdottoSnapshot;
    private BigDecimal prezzoBaseSnapshot;
    private int minutiPreparazioneSnapshot;
    private int quantita;
    private BigDecimal totaleRiga;
    private String creataIl;
    private List<CaratteristicaRigaOrdine> caratteristiche = new ArrayList<>();

    public RigaOrdine() {
    }

    public RigaOrdine(long id,
                      long idOrdine,
                      long idProdotto,
                      String nomeProdottoSnapshot,
                      BigDecimal prezzoBaseSnapshot,
                      int minutiPreparazioneSnapshot,
                      int quantita,
                      BigDecimal totaleRiga,
                      String creataIl,
                      List<CaratteristicaRigaOrdine> caratteristiche) {
        this.id = id;
        this.idOrdine = idOrdine;
        this.idProdotto = idProdotto;
        this.nomeProdottoSnapshot = nomeProdottoSnapshot;
        this.prezzoBaseSnapshot = prezzoBaseSnapshot;
        this.minutiPreparazioneSnapshot = minutiPreparazioneSnapshot;
        this.quantita = quantita;
        this.totaleRiga = totaleRiga;
        this.creataIl = creataIl;
        setCaratteristiche(caratteristiche);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getIdOrdine() {
        return idOrdine;
    }

    public void setIdOrdine(long idOrdine) {
        this.idOrdine = idOrdine;
    }

    public long getIdProdotto() {
        return idProdotto;
    }

    public void setIdProdotto(long idProdotto) {
        this.idProdotto = idProdotto;
    }

    public String getNomeProdottoSnapshot() {
        return nomeProdottoSnapshot;
    }

    public void setNomeProdottoSnapshot(String nomeProdottoSnapshot) {
        this.nomeProdottoSnapshot = nomeProdottoSnapshot;
    }

    public BigDecimal getPrezzoBaseSnapshot() {
        return prezzoBaseSnapshot;
    }

    public void setPrezzoBaseSnapshot(BigDecimal prezzoBaseSnapshot) {
        this.prezzoBaseSnapshot = prezzoBaseSnapshot;
    }

    public int getMinutiPreparazioneSnapshot() {
        return minutiPreparazioneSnapshot;
    }

    public void setMinutiPreparazioneSnapshot(int minutiPreparazioneSnapshot) {
        this.minutiPreparazioneSnapshot = minutiPreparazioneSnapshot;
    }

    public int getQuantita() {
        return quantita;
    }

    public void setQuantita(int quantita) {
        this.quantita = quantita;
    }

    public BigDecimal getTotaleRiga() {
        return totaleRiga;
    }

    public void setTotaleRiga(BigDecimal totaleRiga) {
        this.totaleRiga = totaleRiga;
    }

    public String getCreataIl() {
        return creataIl;
    }

    public void setCreataIl(String creataIl) {
        this.creataIl = creataIl;
    }

    public List<CaratteristicaRigaOrdine> getCaratteristiche() {
        return caratteristiche;
    }

    public void setCaratteristiche(List<CaratteristicaRigaOrdine> caratteristiche) {
        this.caratteristiche = caratteristiche != null ? new ArrayList<>(caratteristiche) : new ArrayList<>();
    }
}
